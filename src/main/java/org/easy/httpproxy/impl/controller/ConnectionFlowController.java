/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.impl.controller;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.TimeUnit;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.LastHttpContent;
import org.easy.httpproxy.core.ConnectionFlow;
import org.easy.httpproxy.core.HttpFilters;
import org.easy.httpproxy.core.HttpFiltersSource;
import org.easy.httpproxy.impl.adapter.ProxyToServerConnectionAdaper;
import org.easy.httpproxy.impl.listener.ClientConnectionClosingFutureListener;
import org.easy.httpproxy.impl.listener.ServerConnectionClosingFutureListener;
import org.easy.httpproxy.impl.listener.ServerConnectionOpeningFutureListener;
import org.easy.httpproxy.impl.listener.WriteToServerFutureListener;
import org.easy.httpproxy.impl.server.ProxyBootstrap.Config;
import org.easy.httpproxy.impl.socket.ExtendedNioSocketChannel;
import org.easy.httpproxy.impl.util.ProxyUtil;

/**
 * This class is going to be the mediator between client2proxy and proxy2server
 * channels
 *
 * @author dnikiforov
 */
public class ConnectionFlowController implements ConnectionFlow {

	private final static Logger LOG = Logger.getLogger(ConnectionFlowController.class.getName());
	private final static WriteToServerFutureListener WRITE_TO_SERVER_FUTURE_LISTENER = new WriteToServerFutureListener();

	private final Channel clientChannel;
	private volatile ExtendedNioSocketChannel serverChannel;
	private final Map<SocketAddress, ExtendedNioSocketChannel> proxyToServerChannels = new ConcurrentHashMap<>();
	private volatile boolean isKeepAlive = false;
	private volatile HttpFilters httpFilters;
	private final HttpFiltersSource httpFiltersSource;
	private final NioEventLoopGroup serverGroup;
	private final Config config;

	public ConnectionFlowController(Channel clientChannel, HttpFiltersSource httpFiltersSource, Config config, NioEventLoopGroup serverGroup) {
		this.clientChannel = clientChannel;
		this.httpFiltersSource = httpFiltersSource;
		this.config = config;
		this.serverGroup = serverGroup;
	}

	@Override
	public void setUpCloseClientHandler() {
		listenClientChannelOnClose(clientChannel);
	}

	private void listenClientChannelOnClose(Channel channel) {
		ChannelFuture clientCloseFuture = channel.closeFuture();
		clientCloseFuture.addListener(new ClientConnectionClosingFutureListener(proxyToServerChannels));
	}

	private void listenServerChannelOnClose(Channel channel) {
		ChannelFuture clientCloseFuture = channel.closeFuture();
		clientCloseFuture.addListener(new ServerConnectionClosingFutureListener(proxyToServerChannels));
	}

	/**
	 * Method setups the connection to server
	 *
	 * @param request
	 * @throws InterruptedException
	 */
	private void setUpServerConnection(HttpRequest request) throws InterruptedException {
		SocketAddress resolveTargetServer = httpFilters.proxyToServerResolutionStarted(request);
		serverChannel = proxyToServerChannels.get(resolveTargetServer);
		//The connection should not be closed till the first write/read operation
		//due to timeout. For this selected() should be called
		if (serverChannel == null || !serverChannel.lock()) {
			LOG.fine("Create new server connection");
			initiateNewConnection(resolveTargetServer);
		} else {
			LOG.fine("Use existing server connection");
		}
		ChannelPipeline pipeline = serverChannel.pipeline();
		setUpAggregator(pipeline);
		httpFilters.proxyToServerConnectionSucceeded(pipeline);
	}

	private void initiateNewConnection(SocketAddress resolveTargetServer) throws InterruptedException {
		Bootstrap bootstrap = new Bootstrap().group(serverGroup)
				.channel(ExtendedNioSocketChannel.class)
				.remoteAddress(resolveTargetServer)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
						config.getConnectTimeout())
				.handler(new ProxyToSererChannelInitializer());
		ChannelFuture connectFuture = bootstrap.connect();
		ServerConnectionOpeningFutureListener futureListener = new ServerConnectionOpeningFutureListener(httpFilters);
		connectFuture.addListener(futureListener);
		//This statement is blocking and throws exception in case of failure
		connectFuture.sync();
		serverChannel = (ExtendedNioSocketChannel) connectFuture.channel();
		listenServerChannelOnClose(serverChannel);
		proxyToServerChannels.put(resolveTargetServer, serverChannel);
	}

	@Override
	public HttpResponse init(HttpRequest request, ChannelHandlerContext ctx) throws InterruptedException {
		if (HttpUtil.isKeepAlive(request)) {
			isKeepAlive = true;
		}
		httpFilters = httpFiltersSource.filterRequest(request, ctx);
		HttpResponse response = fireClientToProxyRequest(request);
		if (response == null) {
			setUpServerConnection(request);
		}
		return response;
	}

	@Override
	public ChannelFuture writeToClient(Object obj, boolean flush) {
		if (obj instanceof FullHttpResponse) {
			ProxyUtil.setLengthHeader((FullHttpResponse) obj);
		} else if (obj instanceof HttpResponse) {
			ProxyUtil.setChunkHeader(obj);
		}
		ProxyUtil.setConnectionHeader(obj, isKeepAlive);
		ChannelFuture writeFuture;
		if (flush) {
			writeFuture = clientChannel.writeAndFlush(obj);
		} else {
			writeFuture = clientChannel.write(obj);
		}
		return writeFuture;
	}

	@Override
	public ChannelFuture writeToServer(Object obj, boolean flush) {
		if (serverChannel != null) {
			obj = ProxyUtil.transformRequestToServer(obj, isKeepAlive);
			ChannelFuture writeFuture;
			if (flush) {
				writeFuture = serverChannel.writeAndFlush(obj);
			} else {
				writeFuture = serverChannel.write(obj);
			}
			writeFuture.addListener(WRITE_TO_SERVER_FUTURE_LISTENER);
			serverChannel.setFlowCompleted(false);
			LOG.log(Level.FINE, "Write to server {0}", obj.getClass().getName());
			return writeFuture;
		} else {
			LOG.log(Level.SEVERE, "Server connection is not established");
			return null;
		}
	}

	//public HttpFilters getHttpFilters() {
	//	return httpFilters;
	//}
	protected void setUpAggregator(ChannelPipeline pipeline) {
		int maxAggregatedContentLength = httpFiltersSource.getMaximumResponseBufferSizeInBytes();
		if (maxAggregatedContentLength > 0) {
			if (pipeline.get(INFLATOR) == null) {
				HttpContentDecompressor httpContentDecompressor = new HttpContentDecompressor();				
				if (pipeline.get(AGGREGATOR) != null) {
					pipeline.addBefore(AGGREGATOR, INFLATOR, httpContentDecompressor);
				} else {
					pipeline.addBefore(HANDLER, INFLATOR, httpContentDecompressor);
				}
			}
			if (pipeline.get(AGGREGATOR) == null) {
				pipeline.addBefore(HANDLER, AGGREGATOR, new HttpObjectAggregator(maxAggregatedContentLength));
			}
		}
	}

	protected Channel getServerChannel() {
		return serverChannel;
	} 
	
	@Override
	public HttpResponse fireClientToProxyRequest(HttpObject msg) {
		HttpResponse response = httpFilters.clientToProxyRequest(msg);
		return response;
	}

	@Override
	public void fireServerToProxyResponse(HttpObject msg) {
		httpFilters.serverToProxyResponse(msg);
	}

	@Override
	public void flushToClient() {
		clientChannel.flush();
	}

	@Override
	public void flushToServer() {
		serverChannel.flush();
	}

	@Override
	public void readFromServer(Object msg) {
		if (msg instanceof HttpObject) {
			if (msg instanceof LastHttpContent) {
				serverChannel.setFlowCompleted(true);
			}
			fireServerToProxyResponse((HttpObject)msg);
			writeToClient(msg, true);
		}
	}

	@Override
	public void fireServerToProxyResponseTimedOut() {
		httpFilters.serverToProxyResponseTimedOut();
	}

	private class ProxyToSererChannelInitializer extends ChannelInitializer<SocketChannel> {

		@Override
		protected void initChannel(SocketChannel channel) throws Exception {
			ChannelPipeline pipeline = channel.pipeline();
			pipeline.addLast(CODEC, new HttpClientCodec(
					config.getMaxInitialLineLength(),
					config.getMaxHeaderSize(),
					config.getMaxChunkSize()
			));
			int idleServerTimeOut = config.getIdleConnectionTimeout();
			pipeline.addLast(IDLE_STATE_HANDLER, new IdleStateHandler(idleServerTimeOut, 0, 0, TimeUnit.SECONDS));
			pipeline.addLast(HANDLER, new ProxyToServerConnectionAdaper(ConnectionFlowController.this));
		}

	}

}

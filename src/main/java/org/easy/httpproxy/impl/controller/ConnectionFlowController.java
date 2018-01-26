/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.impl.controller;

import org.easy.httpproxy.impl.listener.ClientConnectionClosingFutureListener;
import org.easy.httpproxy.impl.listener.ServerConnectionClosingFutureListener;
import org.easy.httpproxy.impl.listener.ServerConnectionOpeningFutureListener;
import org.easy.httpproxy.impl.socket.ExtendedNioSocketChannel;
import org.easy.httpproxy.impl.util.ProxyUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
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
import org.easy.httpproxy.core.ConnectionFlow;
import org.easy.httpproxy.impl.adapter.ProxyToServerConnectionAdaper;
import org.easy.httpproxy.impl.server.ProxyBootstrap.Config;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.TimeUnit;
import org.easy.httpproxy.impl.listener.WriteFutureListener;
import org.easy.httpproxy.core.HttpFilters;
import org.easy.httpproxy.core.HttpFiltersSource;
import io.netty.handler.codec.http.HttpContentDecompressor;

/**
 * This class is going to be the mediator between client2proxy and proxy2server
 * channels
 *
 * @author dnikiforov
 */
public class ConnectionFlowController implements ConnectionFlow {

	private final static Logger LOG = Logger.getLogger(ConnectionFlowController.class.getName());
	
	public static final String AGGREGATOR = "aggregator";
	public static final String INFLATER = "inflater";
	
	private final Channel clientChannel;
	private volatile ExtendedNioSocketChannel serverChannel;
	private final Map<SocketAddress, ExtendedNioSocketChannel> proxyToServerChannels = new ConcurrentHashMap<>();
	private volatile boolean isKeepAlive = false;
	private final HttpRequest request;
	private final HttpFilters httpFilters;
	private final HttpFiltersSource httpFiltersSource;
	private final NioEventLoopGroup serverGroup;
	private final Config config;

	public ConnectionFlowController(Channel clientChannel, HttpRequest request, HttpFiltersSource httpFiltersSource, HttpFilters httpFilters, Config config, NioEventLoopGroup serverGroup) {
		this.clientChannel = clientChannel;
		this.request = request;
		this.httpFilters = httpFilters;
		this.httpFiltersSource = httpFiltersSource;
		this.config = config;
		this.serverGroup = serverGroup;
	}

	@Override
	public void handleClientClose() {
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
			initiateNewConnection(resolveTargetServer, request);
		} else {
			LOG.info("Use existing server connection");
		}
	}

	private void initiateNewConnection(SocketAddress resolveTargetServer, HttpRequest request) throws InterruptedException {
		LOG.info("Create new server connection");
		Bootstrap bootstrap = new Bootstrap().group(serverGroup)
				.channel(ExtendedNioSocketChannel.class)
				.remoteAddress(resolveTargetServer)
				.handler(new ProxyToSererChannelInitializer(request));
		ChannelFuture connectFuture = bootstrap.connect();
		ServerConnectionOpeningFutureListener futureListener = new ServerConnectionOpeningFutureListener(httpFilters);
		connectFuture.addListener(futureListener);
		connectFuture.sync();
		serverChannel = (ExtendedNioSocketChannel) connectFuture.channel();
		listenServerChannelOnClose(serverChannel);
		proxyToServerChannels.put(resolveTargetServer, serverChannel);
	}

	@Override
	public void init(HttpRequest request) throws InterruptedException {
		if (HttpUtil.isKeepAlive(request)) {
			isKeepAlive = true;
		}
		setUpServerConnection(request);
	}

	@Override
	public ChannelFuture writeToClient(Object obj) {
		obj = ProxyUtil.transformAnswerToClient((HttpObject) obj);
		if (obj instanceof FullHttpResponse) {
			ProxyUtil.setLengthHeader((FullHttpResponse) obj);
		} else if (obj instanceof HttpResponse) {
			ProxyUtil.setChunkHeader(obj);
		}
		ProxyUtil.setConnectionHeader(obj, isKeepAlive);
		ChannelFuture writeAndFlush = clientChannel.writeAndFlush(obj);
		return writeAndFlush;
	}

	@Override
	public ChannelFuture writeToServer(Object obj) {
		if (serverChannel!=null) {
			obj = ProxyUtil.transformRequestToServer(obj, isKeepAlive);
			ChannelFuture writeAndFlush = serverChannel.writeAndFlush(obj);
			writeAndFlush.addListener(new WriteFutureListener());
			LOG.log(Level.FINE, "Write to server {0}", obj.getClass().getName());
			return writeAndFlush;
		} else {
			LOG.log(Level.SEVERE, "Server connection is not established");
			return null;
		}	
	}

	public HttpFilters getHttpFilters() {
		return httpFilters;
	}

	private class ProxyToSererChannelInitializer extends ChannelInitializer<SocketChannel> {

		private final HttpRequest request;

		public ProxyToSererChannelInitializer(HttpRequest request) {
			this.request = request;
		}

		@Override
		protected void initChannel(SocketChannel channel) throws Exception {
			ChannelPipeline pipeline = channel.pipeline();
			pipeline.addLast(new HttpClientCodec(
				config.getMaxInitialLineLength(),
                config.getMaxHeaderSize(),
                config.getMaxChunkSize()			
			));
			int idleServerTimeOut = config.getIdleServerTimeOut();
			channel.pipeline().addLast("idleStateHandler", new IdleStateHandler(0, 0, idleServerTimeOut, TimeUnit.SECONDS));
			int maxAggregatedContentLength = httpFiltersSource.getMaximumResponseBufferSizeInBytes();
			if (maxAggregatedContentLength > 0) {
				pipeline.addLast(INFLATER, new HttpContentDecompressor());
				pipeline.addLast(AGGREGATOR, new HttpObjectAggregator(maxAggregatedContentLength));
			}
			pipeline.addLast("handler", new ProxyToServerConnectionAdaper(ConnectionFlowController.this));
		}

	}

}

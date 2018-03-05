/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.impl.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.easy.httpproxy.core.ConnectionFlow.AGGREGATOR;
import static org.easy.httpproxy.core.ConnectionFlow.DECODER;
import static org.easy.httpproxy.core.ConnectionFlow.ENCODER;
import static org.easy.httpproxy.core.ConnectionFlow.IDLE_STATE_HANDLER;
import static org.easy.httpproxy.core.ConnectionFlow.INFLATOR;
import org.easy.httpproxy.core.HttpFiltersSource;
import org.easy.httpproxy.impl.adapter.ClientToProxyConnectionAdapter;

/**
 *
 * @author dnikiforov
 */
public class ProxyBootstrap {

	private static final Logger LOG = Logger.getLogger(ServerBootstrap.class.getName());

	public static class Config {

		private int listenPort = 80;
		private int idleConnectionTimeout = 60;
		private int connectTimeout = 40000;
		private final int maxInitialLineLength = 8192;
		private final int maxHeaderSize = 8192 * 2;
		private final int maxChunkSize = 8192 * 2;

		public int getConnectTimeout() {
			return connectTimeout;
		}

		public int getListenPort() {
			return listenPort;
		}

		public int getIdleConnectionTimeout() {
			return idleConnectionTimeout;
		}

		public int getMaxInitialLineLength() {
			return maxInitialLineLength;
		}

		public int getMaxHeaderSize() {
			return maxHeaderSize;
		}

		public int getMaxChunkSize() {
			return maxChunkSize;
		}

	}

	private NioEventLoopGroup bossGroup;
	private NioEventLoopGroup clientGroup;
	private NioEventLoopGroup serverGroup;
	private ChannelFuture sync;
	private final Config config;
	private ThreadPoolConfiguration threadPoolConfiguration;
	private HttpFiltersSource httpFiltersSource;
	private String name;

	public ProxyBootstrap() {
		config = new Config();
		threadPoolConfiguration = new ThreadPoolConfiguration();
	}

	public void setThreadPoolConfiguration(ThreadPoolConfiguration threadPoolConfiguration) {
		this.threadPoolConfiguration = threadPoolConfiguration;
	}

	public final ProxyBootstrap port(final int listenPort) {
		config.listenPort = listenPort;
		return this;
	}

	public final ProxyBootstrap setIdleConnectionTimeout(final int idleConnectionTimeout) {
		config.idleConnectionTimeout = idleConnectionTimeout;
		return this;
	}

	public final ProxyBootstrap setConnectTimeout(final int connectTimeout) {
		config.connectTimeout = connectTimeout;
		return this;
	}

	public void setHttpFiltersSource(HttpFiltersSource httpFiltersSource) {
		this.httpFiltersSource = httpFiltersSource;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void bootstrap() {

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				shutdown();
			}
		});

		bossGroup = new NioEventLoopGroup(threadPoolConfiguration.getAcceptors());
		clientGroup = new NioEventLoopGroup(threadPoolConfiguration.getClientWorkers());
		serverGroup = new NioEventLoopGroup(threadPoolConfiguration.getServerWorkers());
		ServerBootstrap bootStrap = new ServerBootstrap();
		bootStrap
				.group(bossGroup, clientGroup)
				.channel(NioServerSocketChannel.class)
				.childHandler(new ClientToProxyChannelInitializer())
				.option(ChannelOption.SO_BACKLOG, 128)
				.childOption(ChannelOption.SO_KEEPALIVE, true);
		try {
			sync = bootStrap.bind(config.listenPort).sync();
			ChannelFuture closeFuture = sync.channel().closeFuture();
			closeFuture.addListener(new BootstrapChannelCloseListener());
		} catch (InterruptedException ex) {
			LOG.log(Level.SEVERE, null, ex);
		}

	}

	private void groupsShutdown() {
		serverGroup.shutdownGracefully();
		clientGroup.shutdownGracefully();
		bossGroup.shutdownGracefully();
	}

	public void shutdown() {
		groupsShutdown();
		try {
			sync.channel().closeFuture().sync();
		} catch (InterruptedException e) {
		}
	}

	private class ClientToProxyChannelInitializer extends ChannelInitializer<SocketChannel> {

		@Override
		public void initChannel(SocketChannel ch) throws Exception {
			ChannelPipeline pipeline = ch.pipeline();
			pipeline.addLast(DECODER, new HttpRequestDecoder(
					config.getMaxInitialLineLength(),
					config.getMaxHeaderSize(),
					config.getMaxChunkSize()
			));
			pipeline.addLast(ENCODER, new HttpResponseEncoder());
			int maxAggregatedContentLength = httpFiltersSource.getMaximumRequestBufferSizeInBytes();
			if (maxAggregatedContentLength > 0) {
				pipeline.addLast(INFLATOR, new HttpContentDecompressor());
				pipeline.addLast(AGGREGATOR, new HttpObjectAggregator(maxAggregatedContentLength));
			}
			pipeline.addLast(IDLE_STATE_HANDLER, new IdleStateHandler(0, 0, config.idleConnectionTimeout, TimeUnit.SECONDS));
			pipeline.addLast(new ClientToProxyConnectionAdapter(httpFiltersSource, config, serverGroup));
		}
	}

	private class BootstrapChannelCloseListener implements ChannelFutureListener {

		@Override
		public void operationComplete(ChannelFuture f) throws Exception {
			groupsShutdown();
		}

	}

}

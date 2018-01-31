/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.impl.server;

import static org.easy.httpproxy.core.ConnectionFlow.INFLATOR;
import org.easy.httpproxy.core.HttpFiltersSource;
import org.easy.httpproxy.impl.adapter.ClientToProxyConnectionAdapter;
import static org.easy.httpproxy.core.ConnectionFlow.AGGREGATOR;
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

/**
 *
 * @author dnikiforov
 */
public class ProxyBootstrap {
	
	private static final Logger LOG = Logger.getLogger(ServerBootstrap.class.getName());
	
	public static class Config {
		
		private int listenPort = 80;
		private int idleClientTimeOut = 60;
		private int idleServerTimeOut = 10;
		private final int maxInitialLineLength = 8192;
		private final int maxHeaderSize = 8192 * 2;
		private final int maxChunkSize = 8192 * 2;
		
		public int getListenPort() {
			return listenPort;
		}
		
		public int getIdleClientTimeOut() {
			return idleClientTimeOut;
		}
		
		public int getIdleServerTimeOut() {
			return idleServerTimeOut;
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
	
	public final ProxyBootstrap idleClientTimeOut(final int idleClientTimeOut) {
		config.idleClientTimeOut = idleClientTimeOut;
		return this;
	}
	
	public final ProxyBootstrap idleServerTimeOut(final int idleServerTimeOut) {
		config.idleServerTimeOut = idleServerTimeOut;
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
			pipeline.addLast("decoder", new HttpRequestDecoder(
					config.getMaxInitialLineLength(),
					config.getMaxHeaderSize(),
					config.getMaxChunkSize()
			));
			pipeline.addLast("encoder", new HttpResponseEncoder());
			int maxAggregatedContentLength = httpFiltersSource.getMaximumRequestBufferSizeInBytes();
			if (maxAggregatedContentLength > 0) {
				pipeline.addLast(INFLATOR, new HttpContentDecompressor());
				pipeline.addLast(AGGREGATOR, new HttpObjectAggregator(maxAggregatedContentLength));
			}			
			pipeline.addLast("idleStateHandler", new IdleStateHandler(0, 0, config.idleClientTimeOut, TimeUnit.SECONDS));
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

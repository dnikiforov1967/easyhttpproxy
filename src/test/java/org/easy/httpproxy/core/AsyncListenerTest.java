/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.core;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author dnikiforov
 */
public class AsyncListenerTest {

	static class TestFutureListener implements ChannelFutureListener {
		
		private volatile int status = 0;
		
		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			status = 1;
		}

		public int getStatus() {
			return status;
		}

	}	
	
	static NioEventLoopGroup group;

	public AsyncListenerTest() {
	}

	// TODO add test methods here.
	// The methods must be annotated with annotation @Test. For example:
	//
	@Test
	public void testAsyncListener() throws InterruptedException {
		
		Bootstrap bootstrap = new Bootstrap().group(group)
				.channel(NioSocketChannel.class)
				.remoteAddress(new InetSocketAddress("localhost", 9090))
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
						100)
				.handler(new SimpleChannelInboundHandler<Object>() {
					@Override
					protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
					}
				});
		ChannelFuture connect = bootstrap.connect();
		TestFutureListener testFutureListener = new TestFutureListener();
		int status = testFutureListener.getStatus();
		assertEquals(0, status);
		try {
			connect.sync();
		} catch(Exception e) {
			
		}
		connect.addListener(testFutureListener);
		TimeUnit.SECONDS.sleep(3);
		status = testFutureListener.getStatus();
		assertEquals(status,1);
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		group = new NioEventLoopGroup();
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		group.shutdownGracefully();
	}

	@BeforeMethod
	public void setUpMethod() throws Exception {
	}

	@AfterMethod
	public void tearDownMethod() throws Exception {
	}
}

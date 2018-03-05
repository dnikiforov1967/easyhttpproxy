/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.core;

import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import org.easy.httpproxy.impl.listener.ServerConnectionClosingFutureListener;
import org.easy.httpproxy.impl.socket.ExtendedNioSocketChannel;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 *
 * @author dnikiforov
 */
public class ServerConnectionClosingFutureListenerTest {
	
	static NioEventLoopGroup group;
	
	public ServerConnectionClosingFutureListenerTest() {
	}
	
	@DataProvider
	private Object[][] getData() throws InterruptedException {
		ChannelFuture future = mock(ChannelFuture.class);
		ExtendedNioSocketChannel extendedNioSocketChannel1 = new ExtendedNioSocketChannel();
		ExtendedNioSocketChannel extendedNioSocketChannel2 = new ExtendedNioSocketChannel();
		group.register(extendedNioSocketChannel1);
		group.register(extendedNioSocketChannel2);		
		
		ExtendedNioSocketChannel channel1 = spy(extendedNioSocketChannel1);
		ExtendedNioSocketChannel channel2 = spy(extendedNioSocketChannel2);
		
		doReturn(new InetSocketAddress("localhost",90)).when(channel1).remoteAddress();
		doReturn(new InetSocketAddress("localhost",90)).when(channel2).remoteAddress();
		
		when(future.channel()).thenReturn(channel1);
		Map<SocketAddress, SocketChannelExtentionInterface> map = new HashMap();
		map.put(channel1.remoteAddress(), channel1);
		
		return new Object[][] {{channel1, channel2, future, map}};
	}

	// TODO add test methods here.
	// The methods must be annotated with annotation @Test. For example:
	//
	@Test(dataProvider = "getData")
	public void hello(ExtendedNioSocketChannel channel1, ExtendedNioSocketChannel channel2, ChannelFuture future, Map<SocketAddress, SocketChannelExtentionInterface> map) throws Exception {
		ServerConnectionClosingFutureListener listener = new ServerConnectionClosingFutureListener(map);
		listener.operationComplete(future);
		assertTrue(map.isEmpty());
		map.put(channel1.remoteAddress(), channel1);
		map.put(channel2.remoteAddress(), channel2);
		listener.operationComplete(future);
		assertFalse(map.isEmpty());
		assertTrue(map.containsKey(channel2.remoteAddress()));
		assertTrue(map.containsValue(channel2));
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.core;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import org.easy.httpproxy.impl.adapter.ProxyToServerConnectionAdaper;
import org.easy.httpproxy.impl.socket.ExtendedNioSocketChannel;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
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
public class ProxyToServerConnectionAdapterTest {

	public ProxyToServerConnectionAdapterTest() {
	}

	@DataProvider
	private Object[][] getContextAndChannel() {
		final ExtendedNioSocketChannel channel = new ExtendedNioSocketChannel() {
			boolean closed = false;

			@Override
			public ChannelFuture close() {
				closed = true;
				return null;
			}

			@Override
			public boolean isOpen() {
				return !closed;
			}
		};
		ChannelHandlerContext context = mock(ChannelHandlerContext.class);
		when(context.channel()).thenReturn(channel);
		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				return channel.close();
			}
		}).when(context).close();
		return new Object[][]{{context, channel}};
	}

	// TODO add test methods here.
	// The methods must be annotated with annotation @Test. For example:
	//
	@Test(dataProvider = "getContextAndChannel")
	public void testClose(ChannelHandlerContext context, ExtendedNioSocketChannel channel) throws Exception {
		assertTrue(channel.isOpen());
		ProxyToServerConnectionAdaper adapter = new ProxyToServerConnectionAdaper(null);
		adapter.userEventTriggered(context, IdleStateEvent.ALL_IDLE_STATE_EVENT);
		assertFalse(channel.isOpen());
	}

	@Test(dataProvider = "getContextAndChannel")
	public void testNoClose(ChannelHandlerContext context, ExtendedNioSocketChannel channel) throws Exception {
		channel.lock();
		assertTrue(channel.isOpen());
		ProxyToServerConnectionAdaper adapter = new ProxyToServerConnectionAdaper(null);
		adapter.userEventTriggered(context, IdleStateEvent.ALL_IDLE_STATE_EVENT);
		assertTrue(channel.isOpen());
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@BeforeMethod
	public void setUpMethod() throws Exception {
	}

	@AfterMethod
	public void tearDownMethod() throws Exception {
	}
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.core;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import java.net.InetSocketAddress;
import java.util.List;
import static org.easy.httpproxy.core.ConnectionFlow.AGGREGATOR;
import static org.easy.httpproxy.core.ConnectionFlow.CODEC;
import static org.easy.httpproxy.core.ConnectionFlow.HANDLER;
import static org.easy.httpproxy.core.ConnectionFlow.IDLE_STATE_HANDLER;
import static org.easy.httpproxy.core.ConnectionFlow.INFLATOR;
import org.easy.httpproxy.impl.controller.ConnectionFlowController;
import org.easy.httpproxy.impl.server.ProxyBootstrap.Config;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

/**
 *
 * @author dnikiforov
 */
public class AggregatorTest {

	static NioEventLoopGroup group;
	static final List<String> handlerNames = Lists.newArrayList(
			CODEC,
			IDLE_STATE_HANDLER,
			INFLATOR,
			AGGREGATOR,
			HANDLER);

	private static class TestFiltersAdapter extends HttpFiltersAdapter {

		public TestFiltersAdapter(HttpRequest originalRequest, ChannelHandlerContext ctx) {
			super(originalRequest, ctx);
		}

		@Override
		public InetSocketAddress proxyToServerResolutionStarted(HttpObject httpObject) {
			return new InetSocketAddress("www.google.com", 80);
		}

	}

	private static class TestFiltersSourceAdapter extends HttpFiltersSourceAdapter {

		@Override
		public int getMaximumResponseBufferSizeInBytes() {
			return Integer.MAX_VALUE;
		}

		@Override
		public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
			return new TestFiltersAdapter(originalRequest, ctx);
		}

	}

	private static class TestConnectionFlowController extends ConnectionFlowController {

		public TestConnectionFlowController(Channel clientChannel, HttpFiltersSource httpFiltersSource, Config config, NioEventLoopGroup serverGroup) {
			super(clientChannel, httpFiltersSource, config, serverGroup);
		}

		public Channel returnServerChannel() {
			return this.getServerChannel();
		}

		public void restoreAggregator(ChannelPipeline pipeline) {
			this.setUpAggregator(pipeline);
		}

	}

	// TODO add test methods here.
	// The methods must be annotated with annotation @Test. For example:
	//
	@Test
	public void testAggregator() throws InterruptedException {
		TestConnectionFlowController cfc = new TestConnectionFlowController(null, new TestFiltersSourceAdapter(), new Config(), group);
		HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
		cfc.init(request, null);
		Channel serverChannel = cfc.returnServerChannel();
		ChannelPipeline pipeline = serverChannel.pipeline();
		List<String> names = pipeline.names().subList(0, handlerNames.size());
		assertEquals(names, handlerNames);
		pipeline.remove(INFLATOR);
		cfc.restoreAggregator(pipeline);
		names = pipeline.names().subList(0, handlerNames.size());
		assertEquals(names, handlerNames);
		pipeline.remove(AGGREGATOR);
		cfc.restoreAggregator(pipeline);
		names = pipeline.names().subList(0, handlerNames.size());
		assertEquals(names, handlerNames);
		pipeline.remove(AGGREGATOR);
		pipeline.remove(INFLATOR);
		cfc.restoreAggregator(pipeline);
		names = pipeline.names().subList(0, handlerNames.size());
		assertEquals(names, handlerNames);
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

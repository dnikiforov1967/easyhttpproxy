/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.impl.adapter;

import org.easy.httpproxy.impl.controller.ConnectionFlowController;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.timeout.IdleStateEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.easy.httpproxy.core.ConnectionFlow;
import org.easy.httpproxy.impl.server.ProxyBootstrap.Config;
import io.netty.channel.Channel;
import org.easy.httpproxy.core.HttpFilters;
import org.easy.httpproxy.core.HttpFiltersSource;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 *
 * @author dnikiforov
 */
public class ClientToProxyConnectionAdapter extends ChannelInboundHandlerAdapter {

	private static final Logger LOG = Logger.getLogger(ClientToProxyConnectionAdapter.class.getName());

	private ConnectionFlow flowController;
	private final HttpFiltersSource httpFiltersSource;
	private final NioEventLoopGroup serverGroup;
	private final Config config;
	private HttpFilters httpFilters;

	public ClientToProxyConnectionAdapter(HttpFiltersSource httpFiltersSource, Config config, NioEventLoopGroup serverGroup) {
		this.httpFiltersSource = httpFiltersSource;
		this.config = config;
		this.serverGroup = serverGroup;

	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		proceedInboundMessage(msg, ctx);
	}

	private void proceedInboundMessage(Object msg, ChannelHandlerContext ctx) throws InterruptedException {
		if (msg instanceof HttpObject) {
			if (msg instanceof HttpRequest) {
				HttpRequest request = (HttpRequest) msg;
				httpFilters = httpFiltersSource.filterRequest(request, ctx);
			}
			HttpResponse response = httpFilters.clientToProxyRequest((HttpObject) msg);
			if (response != null) {
				ctx.writeAndFlush(response);
			} else {
				prodceedHttpMessage(msg, ctx);
			}
		}
	}

	private void prodceedHttpMessage(Object msg, ChannelHandlerContext ctx) throws InterruptedException {
		if (msg instanceof HttpRequest) {
			HttpRequest request = (HttpRequest) msg;
			if (flowController == null) {
				Channel clientChannel = ctx.channel();
				flowController = new ConnectionFlowController(clientChannel, httpFiltersSource, config, serverGroup);
				//close event should be handled
				flowController.handleClientClose();
			}
			flowController.setHttpFilters(httpFilters);
			flowController.init(request);
		}
		flowController.writeToServer(msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// Close the connection when an exception is raised.
		cause.printStackTrace();
		ctx.close();
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		try {
			if (evt instanceof IdleStateEvent) {
				LOG.log(Level.FINE, "Client connection {0} to proxy timed out", ctx.channel().remoteAddress());
				ctx.close();
			}
		} finally {
			super.userEventTriggered(ctx, evt);
		}
	}

}

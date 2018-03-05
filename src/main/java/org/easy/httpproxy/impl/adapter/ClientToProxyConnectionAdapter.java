/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.impl.adapter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.timeout.IdleStateEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCounted;
import org.easy.httpproxy.core.ConnectionFlow;
import org.easy.httpproxy.core.HttpFiltersSource;
import org.easy.httpproxy.impl.controller.ConnectionFlowController;
import org.easy.httpproxy.impl.server.ProxyBootstrap.Config;

/**
 *
 * @author dnikiforov
 */
public class ClientToProxyConnectionAdapter extends ChannelInboundHandlerAdapter {

	private static final Logger LOG = Logger.getLogger(ClientToProxyConnectionAdapter.class.getName());

	private volatile ConnectionFlow flowController;
	private final HttpFiltersSource httpFiltersSource;
	private final NioEventLoopGroup serverGroup;
	private final Config config;
	private volatile boolean shortCircle = false;

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
		HttpResponse response = null;
		if (msg instanceof HttpObject) {
			if (msg instanceof HttpRequest) {
				//The request header means new flow cycle 
				shortCircle = false;
				HttpRequest request = (HttpRequest) msg;
				if (flowController == null) {
					Channel clientChannel = ctx.channel();
					flowController = new ConnectionFlowController(clientChannel, httpFiltersSource, config, serverGroup);
					//close event should be handled
					flowController.setUpCloseClientHandler();
				}
				response = flowController.init(request, ctx);
			} else {
				response = flowController.fireClientToProxyRequest((HttpObject) msg);
			}
			//Non-null response means the short circle
			if (response != null) {
				shortCircle = true;
			}
			//Short circle means no communication to server 
			if (shortCircle) {
				//Unused message release
				if (msg instanceof ReferenceCounted) {
					((ReferenceCounted) msg).release();
				}
				//response can be null if short circle includes several steps
				//response should not be flushed by default
				if (response!=null) {
					flowController.writeToClient(response, false);
				}
				//If the last part of request got the response should be completely flushed
				if (msg instanceof LastHttpContent) {
					flowController.flushToClient();
				}
			} else {
				flowController.writeToServer(msg, true);
			}
		}
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

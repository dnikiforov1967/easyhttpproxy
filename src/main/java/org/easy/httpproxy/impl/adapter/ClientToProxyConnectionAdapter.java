/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.impl.adapter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import java.util.logging.Logger;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCounted;
import org.easy.httpproxy.core.HttpFiltersSource;
import org.easy.httpproxy.core.ProxyServerConfig;
import org.easy.httpproxy.core.SocketChannelExtentionInterface;
import org.easy.httpproxy.impl.controller.ConnectionFlowController;

/**
 *
 * @author dnikiforov
 */
public class ClientToProxyConnectionAdapter extends AbstractConnectionAdapter {

	private static final Logger LOG = Logger.getLogger(ClientToProxyConnectionAdapter.class.getName());

	private final HttpFiltersSource httpFiltersSource;
	private final EventLoopGroup serverGroup;
	private final ProxyServerConfig config;
	private final Class<? extends SocketChannelExtentionInterface> serverSocketClass;

	public ClientToProxyConnectionAdapter(HttpFiltersSource httpFiltersSource, ProxyServerConfig config, EventLoopGroup serverGroup, Class<? extends SocketChannelExtentionInterface> serverSocketClass) {
		this.httpFiltersSource = httpFiltersSource;
		this.config = config;
		this.serverGroup = serverGroup;
		this.serverSocketClass = serverSocketClass;
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
				HttpRequest request = (HttpRequest) msg;
				if (flowController == null) {
					Channel clientChannel = ctx.channel();
					flowController = new ConnectionFlowController(clientChannel, httpFiltersSource, config, serverGroup, serverSocketClass);
					//close event should be handled
					flowController.setUpCloseClientHandler();
				}
				flowController.setShortCircuit(false);
				response = flowController.init(request, ctx);
			} else {
				response = flowController.fireClientToProxyRequest((HttpObject) msg);
			}
			//Non-null response means the short circle
			if (response != null) {
				flowController.setShortCircuit(true);
			}
			//Short circle means no communication to server 
			if (flowController.getShortCircuit()) {
				//Unused message release
				if (msg instanceof ReferenceCounted) {
					((ReferenceCounted) msg).release();
				}
				//response can be null if short circle includes several steps
				//response should not be flushed by default
				if (response != null) {
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

}

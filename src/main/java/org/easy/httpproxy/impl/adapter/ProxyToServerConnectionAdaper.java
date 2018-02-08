/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.impl.adapter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import java.util.logging.Logger;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import org.easy.httpproxy.core.ConnectionFlow;
import org.easy.httpproxy.impl.socket.ExtendedNioSocketChannel;

/**
 * Simplest handler what accepts client channel as a parameter and read server
 * responses there
 *
 * @author dnikiforov
 */
public class ProxyToServerConnectionAdaper extends ChannelInboundHandlerAdapter {

	private static final Logger LOG = Logger.getLogger(ProxyToServerConnectionAdaper.class.getName());

	private final ConnectionFlow flowController;

	public ProxyToServerConnectionAdaper(final ConnectionFlow controller) {
		this.flowController = controller;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof HttpObject) {
			flowController.fireServerToProxyResponse((HttpObject)msg);
			flowController.writeToClient(msg, true);
		}
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			ExtendedNioSocketChannel channel = (ExtendedNioSocketChannel) ctx.channel();
			boolean lock = channel.lock();
			if (lock) {
				ctx.close();
			}
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// Close the connection when an exception is raised.
		cause.printStackTrace();
		ctx.close();
	}	

}

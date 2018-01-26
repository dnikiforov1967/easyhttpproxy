/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.impl.adapter;

import org.easy.httpproxy.impl.socket.ExtendedNioSocketChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;
import java.util.logging.Logger;
import org.easy.httpproxy.core.ConnectionFlow;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Simplest handler what accepts client channel as a parameter and read server
 * responses there
 *
 * @author dnikiforov
 */
public class ProxyToServerConnectionAdaper extends SimpleChannelInboundHandler {

	private static final Logger LOG = Logger.getLogger(ProxyToServerConnectionAdaper.class.getName());

	private final ConnectionFlow flowController;

	public ProxyToServerConnectionAdaper(final ConnectionFlow controller) {
		this.flowController = controller;
	}

	public ProxyToServerConnectionAdaper(final ConnectionFlow controller, boolean autoRelease) {
		super(autoRelease);
		this.flowController = controller;
	}

	public ProxyToServerConnectionAdaper(final ConnectionFlow controller, Class inboundMessageType) {
		super(inboundMessageType);
		this.flowController = controller;
	}

	public ProxyToServerConnectionAdaper(final ConnectionFlow controller, Class inboundMessageType, boolean autoRelease) {
		super(inboundMessageType, autoRelease);
		this.flowController = controller;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext chc, Object obj) throws Exception {
		if (obj instanceof HttpObject) {
			flowController.getHttpFilters().serverToProxyResponse((HttpObject)obj);
			flowController.writeToClient(obj);
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

}

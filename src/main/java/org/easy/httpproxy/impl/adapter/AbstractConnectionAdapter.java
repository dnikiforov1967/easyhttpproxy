/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.impl.adapter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.ReadTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.easy.httpproxy.core.ConnectionFlow;

/**
 *
 * @author dnikiforov
 */
abstract class AbstractConnectionAdapter extends ChannelInboundHandlerAdapter {

	private static final Logger LOG = Logger.getLogger(AbstractConnectionAdapter.class.getName());

	protected volatile ConnectionFlow flowController;

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		try {
			if (evt instanceof IdleStateEvent) {
				try {
					if (flowController.readTimedOut()) {
						LOG.log(Level.INFO, "{0} of address {1} read timed out", new Object[]{this.getClass().getSimpleName(), ctx.channel().remoteAddress()});
						throw ReadTimeoutException.INSTANCE;
					} else {
						LOG.log(Level.FINE, "{0} of address {1} timed out", new Object[]{this.getClass().getSimpleName(), ctx.channel().remoteAddress()});
					}
				} finally {
					ctx.close();
				}
			}
		} finally {
			super.userEventTriggered(ctx, evt);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// Close the connection when an exception is raised.
		if (!(cause instanceof ReadTimeoutException)) {
			cause.printStackTrace();
			ctx.close();
		}
	}

}

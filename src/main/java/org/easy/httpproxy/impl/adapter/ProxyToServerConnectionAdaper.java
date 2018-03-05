/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.impl.adapter;

import io.netty.channel.ChannelHandlerContext;
import java.util.logging.Logger;
import org.easy.httpproxy.core.ConnectionFlow;

/**
 * Simplest handler what accepts client channel as a parameter and read server
 * responses there
 *
 * @author dnikiforov
 */
public class ProxyToServerConnectionAdaper extends AbstractConnectionAdapter {

	private static final Logger LOG = Logger.getLogger(ProxyToServerConnectionAdaper.class.getName());

	public ProxyToServerConnectionAdaper(final ConnectionFlow controller) {
		this.flowController = controller;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		flowController.readFromServer(msg);
	}

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.impl.listener;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import java.net.SocketAddress;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.easy.httpproxy.impl.socket.ExtendedNioSocketChannel;

/**
 * This listener executes base operation - it shutdowns event loop thread group related to particular channel
 * 
 * @author dnikiforov
 */
public abstract class ConnectionClosingFutureListener implements ChannelFutureListener {

	protected static final Logger LOG = Logger.getLogger(ConnectionClosingFutureListener.class.getName());

	private final Map<SocketAddress, ExtendedNioSocketChannel> map;

	public ConnectionClosingFutureListener(Map<SocketAddress, ExtendedNioSocketChannel> map) {
		this.map = map;
	}
	
	protected final Map<SocketAddress, ExtendedNioSocketChannel> getMap() {
		return map;
	}
	
	@Override
	public void operationComplete(ChannelFuture f) throws Exception {
		LOG.log(Level.FINE, "Future " + f.toString() + " was completed");
	}

}

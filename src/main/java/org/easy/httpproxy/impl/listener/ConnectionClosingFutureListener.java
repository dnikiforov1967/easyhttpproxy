/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.impl.listener;

import io.netty.channel.ChannelFutureListener;
import java.net.SocketAddress;
import java.util.Map;
import org.easy.httpproxy.core.SocketChannelExtentionInterface;

/**
 * This listener executes base operation - it shutdowns event loop thread group related to particular channel
 * 
 * @author dnikiforov
 */
public abstract class ConnectionClosingFutureListener implements ChannelFutureListener {

	private final Map<SocketAddress, SocketChannelExtentionInterface> map;

	public ConnectionClosingFutureListener(Map<SocketAddress, SocketChannelExtentionInterface> map) {
		this.map = map;
	}
	
	protected final Map<SocketAddress, SocketChannelExtentionInterface> getMap() {
		return map;
	}

}

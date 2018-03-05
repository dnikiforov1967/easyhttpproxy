/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.impl.listener;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import java.net.SocketAddress;
import java.util.Map;
import java.util.logging.Logger;
import org.easy.httpproxy.core.SocketChannelExtentionInterface;
import org.easy.httpproxy.impl.util.StatisticsUtil;


/**
 * This listener handles the event of server connection close
 * @author dnikiforov
 */
public class ServerConnectionClosingFutureListener extends ConnectionClosingFutureListener {

	private static final Logger LOG = Logger.getLogger(ServerConnectionClosingFutureListener.class.getName());	
	
	public ServerConnectionClosingFutureListener(final Map<SocketAddress, SocketChannelExtentionInterface> map) {
		super(map);
	}

	@Override
	public void operationComplete(ChannelFuture f) throws Exception {
		Channel channel = f.channel();
		SocketAddress remoteAddress = channel.remoteAddress();
		boolean remove = getMap().remove(remoteAddress, channel);
		if (remove) {
			LOG.fine("Server connection was removed");
		} else {
			LOG.fine("Connection was not found in map");
		}
		if (f.isSuccess()) {
			StatisticsUtil.serverConnectionClose();
		}
	}

}

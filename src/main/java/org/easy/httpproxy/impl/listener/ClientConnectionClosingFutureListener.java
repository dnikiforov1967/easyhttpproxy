/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.impl.listener;

import io.netty.channel.ChannelFuture;
import java.net.SocketAddress;
import java.util.Map;
import java.util.logging.Logger;
import org.easy.httpproxy.core.SocketChannelExtentionInterface;
import org.easy.httpproxy.impl.util.StatisticsUtil;


/**
 *
 * @author dnikiforov
 */
public class ClientConnectionClosingFutureListener extends ConnectionClosingFutureListener {

	private static final Logger LOG = Logger.getLogger(ClientConnectionClosingFutureListener.class.getName());		
	
	public ClientConnectionClosingFutureListener(Map<SocketAddress, SocketChannelExtentionInterface> map) {
		super(map);
	}

	@Override
	public void operationComplete(ChannelFuture f) throws Exception {
		Map<SocketAddress, SocketChannelExtentionInterface> map = getMap();
		map.values().forEach((ch) -> {
			if (ch.isOpen()) {
				ch.close();
			}
		});
		map.clear();
		LOG.fine("Connection map is cleared");
		if (f.isSuccess()) {
			StatisticsUtil.clientConnectionClose();
		}
	}

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.impl.listener;

import org.easy.httpproxy.impl.socket.ExtendedNioSocketChannel;
import io.netty.channel.ChannelFuture;
import java.net.SocketAddress;
import java.util.Map;


/**
 *
 * @author dnikiforov
 */
public class ClientConnectionClosingFutureListener extends ConnectionClosingFutureListener {

	public ClientConnectionClosingFutureListener(Map<SocketAddress, ExtendedNioSocketChannel> map) {
		super(map);
	}

	@Override
	public void operationComplete(ChannelFuture f) throws Exception {
		Map<SocketAddress, ExtendedNioSocketChannel> map = getMap();
		map.values().forEach((ch) -> {
			if (ch.isOpen()) {
				ch.close();
			}
		});
		map.clear();
		LOG.info("Connection map is cleared");
		super.operationComplete(f);
	}

}

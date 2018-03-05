/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.impl.listener;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.easy.httpproxy.impl.socket.ExtendedNioSocketChannel;

/**
 *
 * @author dnikiforov
 */
public class WriteToServerFutureListener implements ChannelFutureListener {

	@Override
	public void operationComplete(ChannelFuture future) throws Exception {
		ExtendedNioSocketChannel channel = (ExtendedNioSocketChannel) future.channel();
		channel.unlock();
	}

}

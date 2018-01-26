/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.impl.listener;

import org.easy.httpproxy.impl.socket.ExtendedNioSocketChannel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * If the asynchronous server write operation completed we should set idle
 * status on channel no matter what the result was - this allows timeout handler
 * to close connection.
 *
 * @author dnikiforov
 */
public class WriteFutureListener implements ChannelFutureListener {

	@Override
	public void operationComplete(ChannelFuture future) throws Exception {
		ExtendedNioSocketChannel channel = (ExtendedNioSocketChannel) future.channel();
		channel.unlock();
	}

}

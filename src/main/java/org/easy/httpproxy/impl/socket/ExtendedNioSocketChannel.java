/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.impl.socket;

import io.netty.channel.socket.nio.NioSocketChannel;
import org.easy.httpproxy.core.SocketChannelExtentionInterface;

/**
 * Class extends the existing NioSocketChannel Three states are appended -
 * idle(0), timed out (-1) and used (1)
 *
 * @author dnikiforov
 */
public class ExtendedNioSocketChannel extends NioSocketChannel implements SocketChannelExtentionInterface {

	private volatile boolean isFlowCompleted = true;

	@Override
	public void setFlowCompleted(boolean isCompleted) {
		isFlowCompleted = isCompleted;
	}

	@Override
	public boolean isFlowCompleted() {
		return isFlowCompleted;
	}

}

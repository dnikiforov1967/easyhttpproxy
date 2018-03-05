/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.core;

import io.netty.channel.socket.SocketChannel;

/**
 *
 * @author dnikiforov
 */
public interface SocketChannelExtentionInterface extends SocketChannel {
	void setFlowCompleted(boolean isCompleted);
	boolean isFlowCompleted();
}

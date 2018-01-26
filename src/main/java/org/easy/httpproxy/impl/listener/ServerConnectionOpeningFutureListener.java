/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.impl.listener;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;
import org.easy.httpproxy.core.HttpFilters;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;


/**
 * This listener handles the server connection open
 * It should call serverConnectionFailed() callback method call in case of failure
 * 
 * @author dnikiforov
 */
public class ServerConnectionOpeningFutureListener implements ChannelFutureListener {

	private final HttpFilters httpFilters;

	public ServerConnectionOpeningFutureListener(HttpFilters httpFilters) {
		this.httpFilters = httpFilters;
	}

	@Override
	public void operationComplete(ChannelFuture future) throws Exception {
		if (!future.isSuccess()) {
			httpFilters.proxyToServerConnectionFailed();
		} else {
			ChannelPipeline pipeline = future.channel().pipeline();
			httpFilters.proxyToServerConnectionSucceeded(pipeline);
		}
		
	}

}

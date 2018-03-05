/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.core;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

/**
 *
 * @author dnikiforov
 */
public interface ConnectionFlow {

	String AGGREGATOR = "aggregator";
	String INFLATOR = "inflator";
	String HANDLER = "handler";
	String CODEC = "codec";
	String DECODER = "decoder";
	String ENCODER = "encoder";
	String IDLE_STATE_HANDLER = "idleStateHandler";
	
	ChannelFuture writeToClient(Object obj, boolean flush);

	ChannelFuture writeToServer(Object obj, boolean flush);
	
	void flushToClient();
	
	void flushToServer();

	void setUpCloseClientHandler();

	HttpResponse init(HttpRequest request, ChannelHandlerContext ctx) throws InterruptedException;

	HttpResponse fireClientToProxyRequest(HttpObject msg);
	
	void fireServerToProxyResponseTimedOut();

	void fireServerToProxyResponse(HttpObject msg);
	
	void readFromServer(Object msg);
	
	void setShortCircuit(boolean shortCircuit);
	
	boolean getShortCircuit();
	
	boolean readTimedOut();
	
}

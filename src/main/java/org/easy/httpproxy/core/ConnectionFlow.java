/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.core;

import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.HttpRequest;

/**
 *
 * @author dnikiforov
 */
public interface ConnectionFlow {

	String AGGREGATOR = "aggregator";
	String INFLATOR = "inflator";
	String HANDLER = "handler";	
	
	ChannelFuture writeToClient(Object obj);

	ChannelFuture writeToServer(Object obj);

	void handleClientClose();
	
	void init(HttpRequest request) throws InterruptedException;
	
	HttpFilters getHttpFilters();
	
	void setHttpFilters(HttpFilters httpFilters);
}

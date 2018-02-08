/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.core;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import java.net.InetSocketAddress;

/**
 *
 * @author dnikiforov
 */
public interface HttpFilters {

	HttpResponse clientToProxyRequest(HttpObject httpObject);

    HttpResponse proxyToServerRequest(HttpObject httpObject);

    HttpObject serverToProxyResponse(HttpObject httpObject);

    void serverToProxyResponseReceived();

    InetSocketAddress proxyToServerResolutionStarted(
            HttpObject httpObject);

    void proxyToServerConnectionFailed();

    void proxyToServerConnectionSucceeded(ChannelPipeline pipeline);

}
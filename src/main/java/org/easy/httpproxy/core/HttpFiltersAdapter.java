/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.core;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import java.net.InetSocketAddress;

/**
 *
 * @author dnikiforov
 */
public class HttpFiltersAdapter implements HttpFilters {

	/**
	 * A default, stateless, no-op {@link HttpFilters} instance.
	 */
	public static final HttpFiltersAdapter NOOP_FILTER = new HttpFiltersAdapter(null);

	protected final HttpRequest originalRequest;
	protected final ChannelHandlerContext ctx;

	public HttpFiltersAdapter(HttpRequest originalRequest,
			ChannelHandlerContext ctx) {
		this.originalRequest = originalRequest;
		this.ctx = ctx;
	}

	public HttpFiltersAdapter(HttpRequest originalRequest) {
		this(originalRequest, null);
	}

	@Override
	public HttpResponse clientToProxyRequest(HttpObject httpObject) {
		return null;
	}

	@Override
	public HttpResponse proxyToServerRequest(HttpObject httpObject) {
		return null;
	}

	@Override
	public HttpObject serverToProxyResponse(HttpObject httpObject) {
		return httpObject;
	}

	@Override
	public void serverToProxyResponseReceived() {
	}

	@Override
	public InetSocketAddress proxyToServerResolutionStarted(
			HttpObject httpObject) {
		return null;
	}

	@Override
	public void proxyToServerConnectionFailed() {
	}

	@Override
	public void proxyToServerConnectionSucceeded(ChannelPipeline pipeline) {
	}

	@Override
	public void serverToProxyResponseTimedOut() {
	}
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.core;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import java.net.InetSocketAddress;

/**
 *
 * @author dnikiforov
 */
public interface HttpFilters {
    /**
     * Filters requests on their way from the client to the proxy. To interrupt processing of this request and return a
     * response to the client immediately, return an HttpResponse here. Otherwise, return null to continue processing as
     * usual.
     * <p>
     * <b>Important:</b> When returning a response, you must include a mechanism to allow the client to determine the length
     * of the message (see RFC 7230, section 3.3.3: https://tools.ietf.org/html/rfc7230#section-3.3.3 ). For messages that
     * may contain a body, you may do this by setting the Transfer-Encoding to chunked, setting an appropriate
     * Content-Length, or by adding a "Connection: close" header to the response (which will instruct LittleProxy to close
     * the connection). If the short-circuit response contains body content, it is recommended that you return a
     * FullHttpResponse.
     * 
     * @param httpObject Client to Proxy HttpRequest (and HttpContent, if chunked)
     * @return a short-circuit response, or null to continue processing as usual
     */
    HttpResponse clientToProxyRequest(HttpObject httpObject);

    /**
     * Filters requests on their way from the proxy to the server. To interrupt processing of this request and return a
     * response to the client immediately, return an HttpResponse here. Otherwise, return null to continue processing as
     * usual.
     * <p>
     * <b>Important:</b> When returning a response, you must include a mechanism to allow the client to determine the length
     * of the message (see RFC 7230, section 3.3.3: https://tools.ietf.org/html/rfc7230#section-3.3.3 ). For messages that
     * may contain a body, you may do this by setting the Transfer-Encoding to chunked, setting an appropriate
     * Content-Length, or by adding a "Connection: close" header to the response. (which will instruct LittleProxy to close
     * the connection). If the short-circuit response contains body content, it is recommended that you return a
     * FullHttpResponse.
     * 
     * @param httpObject Proxy to Server HttpRequest (and HttpContent, if chunked)
     * @return a short-circuit response, or null to continue processing as usual
     */
    HttpResponse proxyToServerRequest(HttpObject httpObject);

    /**
     * Informs filter that proxy to server request is being sent.
     */
    void proxyToServerRequestSending();

    /**
     * Informs filter that the HTTP request, including any content, has been sent.
     */
    void proxyToServerRequestSent();

    /**
     * Filters responses on their way from the server to the proxy.
     * 
     * @param httpObject
     *            Server to Proxy HttpResponse (and HttpContent, if chunked)
     * @return the modified (or unmodified) HttpObject. Returning null will
     *         force a disconnect.
     */
    HttpObject serverToProxyResponse(HttpObject httpObject);

    /**
     * Informs filter that a timeout occurred before the server response was received by the client. The timeout may have
     * occurred while the client was sending the request, waiting for a response, or after the client started receiving
     * a response (i.e. if the response from the server "stalls").
     *
     * See {@link HttpProxyServerBootstrap#withIdleConnectionTimeout(int)} for information on setting the timeout.
     */
    void serverToProxyResponseTimedOut();

    /**
     * Informs filter that server to proxy response is being received.
     */
    void serverToProxyResponseReceiving();

    /**
     * Informs filter that server to proxy response has been received.
     */
    void serverToProxyResponseReceived();

    /**
     * Filters responses on their way from the proxy to the client.
     * 
     * @param httpObject
     *            Proxy to Client HttpResponse (and HttpContent, if chunked)
     * @return the modified (or unmodified) HttpObject. Returning null will
     *         force a disconnect.
     */
    HttpObject proxyToClientResponse(HttpObject httpObject);

    /**
     * Informs filter that proxy to server connection is in queue.
     */
    void proxyToServerConnectionQueued();

    /**
     * Filter DNS resolution from proxy to server.
     * 
     * @param resolvingServerHostAndPort
     *            Server "HOST:PORT"
     * @return alternative address resolution. Returning null will let normal
     *         DNS resolution continue.
     */
    InetSocketAddress proxyToServerResolutionStarted(
            HttpObject httpObject);

    /**
     * Informs filter that proxy to server DNS resolution failed for the specified host and port.
     *
     * @param hostAndPort hostname and port the proxy failed to resolve
     */
    void proxyToServerResolutionFailed(String hostAndPort);

    /**
     * Informs filter that proxy to server DNS resolution has happened.
     * 
     * @param serverHostAndPort
     *            Server "HOST:PORT"
     * @param resolvedRemoteAddress
     *            Address it was proxyToServerResolutionSucceeded to
     */
    void proxyToServerResolutionSucceeded(String serverHostAndPort,
            InetSocketAddress resolvedRemoteAddress);

    /**
     * Informs filter that proxy to server connection is initiating.
     */
    void proxyToServerConnectionStarted();

    /**
     * Informs filter that proxy to server ssl handshake is initiating.
     */
    void proxyToServerConnectionSSLHandshakeStarted();

    /**
     * Informs filter that proxy to server connection has failed.
     */
    void proxyToServerConnectionFailed();

    /**
     * Informs filter that proxy to server connection has succeeded.
     *
     * @param serverCtx the {@link io.netty.channel.ChannelHandlerContext} used to connect to the server
     */
    void proxyToServerConnectionSucceeded(ChannelPipeline pipeline);

}
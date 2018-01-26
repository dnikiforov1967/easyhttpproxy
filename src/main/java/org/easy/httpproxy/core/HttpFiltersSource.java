/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.core;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;

/**
 *
 * @author dnikiforov
 */
public interface HttpFiltersSource {
    /**
     * Return an {@link HttpFilters} object for this request if and only if we
     * want to filter the request and/or its responses.
     * 
     * @param originalRequest
     * @return
     */
    HttpFilters filterRequest(HttpRequest originalRequest,
            ChannelHandlerContext ctx);

    /**
     * Indicate how many (if any) bytes to buffer for incoming
     * {@link HttpRequest}s. A value of 0 or less indicates that no buffering
     * should happen and that messages will be passed to the {@link HttpFilters}
     * request filtering methods chunk by chunk. A positive value will cause
     * LittleProxy to try an create a {@link FullHttpRequest} using the data
     * received from the client, with its content already decompressed (in case
     * the client was compressing it). If the request size exceeds the maximum
     * buffer size, the request will fail.
     * 
     * @return
     */
    int getMaximumRequestBufferSizeInBytes();

    /**
     * Indicate how many (if any) bytes to buffer for incoming
     * {@link HttpResponse}s. A value of 0 or less indicates that no buffering
     * should happen and that messages will be passed to the {@link HttpFilters}
     * response filtering methods chunk by chunk. A positive value will cause
     * LittleProxy to try an create a {@link FullHttpResponse} using the data
     * received from the server, with its content already decompressed (in case
     * the server was compressing it). If the response size exceeds the maximum
     * buffer size, the response will fail.
     * 
     * @return
     */
    int getMaximumResponseBufferSizeInBytes();
}

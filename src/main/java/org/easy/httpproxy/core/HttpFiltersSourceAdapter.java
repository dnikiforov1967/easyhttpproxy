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
public class HttpFiltersSourceAdapter implements HttpFiltersSource {

    public HttpFilters filterRequest(HttpRequest originalRequest) {
        return new HttpFiltersAdapter(originalRequest, null);
    }
    
    @Override
    public HttpFilters filterRequest(HttpRequest originalRequest,
            ChannelHandlerContext ctx) {
        return filterRequest(originalRequest);
    }

    @Override
    public int getMaximumRequestBufferSizeInBytes() {
        return 0;
    }

    @Override
    public int getMaximumResponseBufferSizeInBytes() {
        return 0;
    }

}
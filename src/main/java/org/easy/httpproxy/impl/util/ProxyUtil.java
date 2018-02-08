/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.impl.util;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.AsciiString;
import java.text.MessageFormat;
import java.util.logging.Logger;

/**
 *
 * @author dnikiforov
 */
public final class ProxyUtil {

	private static final Logger LOG = Logger.getLogger(ProxyUtil.class.getName());

	private ProxyUtil() {

	}

	public static Object transformRequestToServer(Object obj, boolean keepAlive) {
		if (obj instanceof HttpRequest) {
			HttpRequest request = (HttpRequest) obj;
			AsciiString value = keepAlive ? HttpHeaderValues.KEEP_ALIVE : HttpHeaderValues.CLOSE; 
			request.headers().set(HttpHeaderNames.CONNECTION, value);
			request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
		}
		return obj;
	}

	public static void setChunkHeader(Object obj) {
		if (obj instanceof HttpResponse) {
			HttpResponse response = (HttpResponse) obj;
			if (!response.headers().contains(HttpHeaderNames.CONTENT_LENGTH)) {
				response.headers().add(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
				LOG.fine(MessageFormat.format("Append chunked header to {0}", obj.getClass().getName()));
			}
		}
	}

	static int getContentLength(ByteBuf byteBuf) {
		return byteBuf.readableBytes();
	}

	public static void setLengthHeader(FullHttpResponse response) {
		if (!response.headers().contains(HttpHeaderNames.CONTENT_LENGTH)) {
			response.headers().remove(HttpHeaderNames.TRANSFER_ENCODING);
			ByteBuf content = response.content();
			int length = getContentLength(content);
			response.headers().add(HttpHeaderNames.CONTENT_LENGTH, length);
		}
		LOG.fine(MessageFormat.format("Append length header to {0}", response.getClass().getName()));
	}

	public static void setConnectionHeader(Object obj, boolean isKeepAlive) {
		if (obj instanceof HttpResponse) {
			HttpResponse response = (HttpResponse) obj;
			if (isKeepAlive) {
				response.headers().set(
						HttpHeaderNames.CONNECTION,
						HttpHeaderValues.KEEP_ALIVE
				);
			} else {
				response.headers().set(
						HttpHeaderNames.CONNECTION,
						HttpHeaderValues.CLOSE
				);
			}
			LOG.fine(MessageFormat.format("Append connection header to {0}", obj.getClass().getName()));
		}
	}

}

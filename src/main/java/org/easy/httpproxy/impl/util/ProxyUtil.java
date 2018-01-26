/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.impl.util;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
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

	private static ByteBuf transformHttpContent(HttpContent httpContent) {
		return httpContent.content().copy();
	}

	private static HttpResponse transformFullHttpResponse(FullHttpResponse originalResponse) {
		ByteBuf copy = originalResponse.content().copy();
		DefaultFullHttpResponse response = new DefaultFullHttpResponse(originalResponse.protocolVersion(), originalResponse.status(), copy);
		response.headers().add(originalResponse.headers());
		return response;
	}

	private static HttpResponse transformHttpResponse(HttpResponse originalResponse) {
		DefaultHttpResponse response = new DefaultHttpResponse(originalResponse.protocolVersion(), originalResponse.status());
		response.headers().add(originalResponse.headers());
		return response;
	}

	private static HttpContent transformLastHttpContent(LastHttpContent lastHttpContent) {
		return lastHttpContent.copy();
	}

	public static Object transformAnswerToClient(Object httpObject) {
		if (httpObject instanceof FullHttpResponse) {
			httpObject = transformFullHttpResponse((FullHttpResponse) httpObject);
			return httpObject;
		}
		if (httpObject instanceof HttpResponse) {
			httpObject = transformHttpResponse((HttpResponse) httpObject);
		}
		if (httpObject instanceof HttpContent) {
			if (httpObject instanceof LastHttpContent) {
				httpObject = transformLastHttpContent((LastHttpContent) httpObject);
			} else {
				httpObject = transformHttpContent((HttpContent) httpObject);
			}
		}
		LOG.info(MessageFormat.format("Write to client {0}", httpObject.getClass().getName()));
		return httpObject;
	}

	public static Object transformRequestToServer(Object obj, boolean keepAlive) {
		if (obj instanceof HttpRequest) {
			HttpRequest request = (HttpRequest) obj;
			AsciiString value = keepAlive ? HttpHeaderValues.KEEP_ALIVE : HttpHeaderValues.CLOSE; 
			request.headers().set(HttpHeaderNames.CONNECTION, value);
			request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
		}
		if (obj instanceof HttpContent) {
			if (obj instanceof LastHttpContent) {
				LastHttpContent lastHttpContent = (LastHttpContent) obj;
				LastHttpContent copy = lastHttpContent.copy();
				obj = copy;
			} else {
				HttpContent httpContent = (HttpContent) obj;
				ByteBuf content = httpContent.content();
				ByteBuf copy = content.copy();
				obj = copy;
			}
		}
		return obj;
	}

	public static void setChunkHeader(Object obj) {
		if (obj instanceof HttpResponse) {
			HttpResponse response = (HttpResponse) obj;
			if (!response.headers().contains(HttpHeaderNames.CONTENT_LENGTH)) {
				response.headers().add(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
				LOG.info(MessageFormat.format("I append chunked header to {0}", obj.getClass().getName()));
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
		LOG.info(MessageFormat.format("I append length header to {0}", response.getClass().getName()));
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
			LOG.info(MessageFormat.format("I append connection header to {0}", obj.getClass().getName()));
		}
	}

}

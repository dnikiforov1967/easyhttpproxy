/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.core;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import java.net.InetSocketAddress;
import org.easy.httpproxy.impl.controller.ConnectionFlowController;
import org.easy.httpproxy.impl.server.ProxyBootstrap.Config;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author dnikiforov
 */
public class ControllerTest {
	
	public ControllerTest() {
	}

	// TODO add test methods here.
	// The methods must be annotated with annotation @Test. For example:
	//
	@Test(expectedExceptions = {java.net.ConnectException.class})
	public void testInit() throws InterruptedException {
		HttpFiltersSourceAdapter adapter = new HttpFiltersSourceAdapter() {
			@Override
			public HttpFilters filterRequest(HttpRequest originalRequest) {
				return new HttpFiltersAdapter(originalRequest, null) {
					@Override
					public InetSocketAddress proxyToServerResolutionStarted(HttpObject httpObject) {
						return new InetSocketAddress("localhost",9001);
					}
				};
			}
			
		};
		HttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/resource/all");
		ConnectionFlow controller = new ConnectionFlowController(null, adapter, new Config(), new NioEventLoopGroup());
		controller.init(httpRequest, null);
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@BeforeMethod
	public void setUpMethod() throws Exception {
	}

	@AfterMethod
	public void tearDownMethod() throws Exception {
	}
}

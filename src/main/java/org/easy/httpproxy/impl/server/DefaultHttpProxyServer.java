/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.impl.server;

import org.easy.httpproxy.core.HttpFiltersSource;
import org.easy.httpproxy.core.HttpProxyServer;
import java.net.InetSocketAddress;

/**
 *
 * @author dnikiforov
 */
public final class DefaultHttpProxyServer implements HttpProxyServer {
	
	private final ProxyBootstrap proxyBootstrap;
	
	private DefaultHttpProxyServer() {
		proxyBootstrap = new ProxyBootstrap();
	}
	
	public static DefaultHttpProxyServer bootstrap() {
		return new DefaultHttpProxyServer();
	}
	
	public DefaultHttpProxyServer withPort(int port) {
		proxyBootstrap.port(port);
		return this;
	}
	
	public DefaultHttpProxyServer withThreadPoolConfiguration(ThreadPoolConfiguration threadPoolConfiguration) {
		proxyBootstrap.setThreadPoolConfiguration(threadPoolConfiguration);
		return this;
	}
	
	public DefaultHttpProxyServer withFiltersSource(HttpFiltersSource httpFiltersSource) {
		proxyBootstrap.setHttpFiltersSource(httpFiltersSource);
		return this;
	}
	
	public DefaultHttpProxyServer withName(String proxyName) {
		proxyBootstrap.setName(proxyName);
		return this;
	}
	
	public DefaultHttpProxyServer start() {
		proxyBootstrap.bootstrap();
		return this;
	}

	@Override
	public int getIdleConnectionTimeout() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void setIdleConnectionTimeout(int idleConnectionTimeout) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public int getConnectTimeout() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void setConnectTimeout(int connectTimeoutMs) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void stop() {
		proxyBootstrap.shutdown();
	}

	@Override
	public void abort() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public InetSocketAddress getListenAddress() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void setThrottle(long readThrottleBytesPerSecond, long writeThrottleBytesPerSecond) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
}

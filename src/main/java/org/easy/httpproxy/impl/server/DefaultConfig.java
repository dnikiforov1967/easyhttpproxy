/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.impl.server;

import org.easy.httpproxy.core.ProxyServerConfig;


/**
 *
 * @author dnikiforov
 */
public class DefaultConfig implements ProxyServerConfig {

	private int listenPort = 80;
	private int idleConnectionTimeout = 60;
	private int connectTimeout = 40000;
	private int maxInitialLineLength = 8192;
	private int maxHeaderSize = 8192 * 2;
	private int maxChunkSize = 8192 * 2;

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public int getListenPort() {
		return listenPort;
	}

	public int getIdleConnectionTimeout() {
		return idleConnectionTimeout;
	}

	public int getMaxInitialLineLength() {
		return maxInitialLineLength;
	}

	public int getMaxHeaderSize() {
		return maxHeaderSize;
	}

	public int getMaxChunkSize() {
		return maxChunkSize;
	}

	@Override
	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout=connectTimeout;
	}

	@Override
	public void setListenPort(int listenPort) {
		this.listenPort=listenPort;
	}

	@Override
	public void setIdleConnectionTimeout(int idleConnectionTimeout) {
		this.idleConnectionTimeout=idleConnectionTimeout;
	}

	@Override
	public void setMaxInitialLineLength(int maxInitialLineLength) {
		this.maxInitialLineLength=maxInitialLineLength;
	}

	@Override
	public void setMaxHeaderSize(int maxHeaderSize) {
		this.maxHeaderSize=maxHeaderSize;
	}

	@Override
	public void setMaxChunkSize(int maxChunkSize) {
		this.maxChunkSize=maxChunkSize;
	}

}

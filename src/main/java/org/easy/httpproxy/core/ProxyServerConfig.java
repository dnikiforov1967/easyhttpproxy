/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.core;

/**
 *
 * @author dnikiforov
 */
public interface ProxyServerConfig {

	int getConnectTimeout();

	int getListenPort();

	int getIdleConnectionTimeout();

	int getMaxInitialLineLength();

	int getMaxHeaderSize();

	int getMaxChunkSize();

	void setConnectTimeout(int connectTimeout);

	void setListenPort(int listenPort);

	void setIdleConnectionTimeout(int idleConnectionTimeout);

	void setMaxInitialLineLength(int maxInitialLineLength);

	void setMaxHeaderSize(int maxHeaderSize);

	void setMaxChunkSize(int maxChunkSize);
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.core;

import java.net.InetSocketAddress;

/**
 *
 * @author dnikiforov
 */
public interface HttpProxyServer {

    int getIdleConnectionTimeout();

    void setIdleConnectionTimeout(int idleConnectionTimeout);

    /**
     * Returns the maximum time to wait, in milliseconds, to connect to a server.
     */
    int getConnectTimeout();

    /**
     * Sets the maximum time to wait, in milliseconds, to connect to a server.
     */
    void setConnectTimeout(int connectTimeoutMs);
    /**
     * Stops the server and all related clones. Waits for traffic to stop before shutting down.
     */
    void stop();

	HttpProxyServer start();
	
    /**
     * Stops the server and all related clones immediately, without waiting for traffic to stop.
     */
    void abort();

    /**
     * Return the address on which this proxy is listening.
     * 
     * @return
     */
    InetSocketAddress getListenAddress();

    /**
     * <p>
     * Set the read/write throttle bandwidths (in bytes/second) for this proxy.
     * </p>
     * @param readThrottleBytesPerSecond
     * @param writeThrottleBytesPerSecond
     */
    void setThrottle(long readThrottleBytesPerSecond, long writeThrottleBytesPerSecond);
}


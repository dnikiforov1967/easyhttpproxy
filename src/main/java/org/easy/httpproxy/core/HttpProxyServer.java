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
public interface HttpProxyServer {

    /**
     * Stops the server and all related clones. Waits for traffic to stop before shutting down.
     */
    void stop();

	HttpProxyServer start();

}


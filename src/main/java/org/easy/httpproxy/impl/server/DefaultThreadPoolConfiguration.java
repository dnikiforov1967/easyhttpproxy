/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.impl.server;

import org.easy.httpproxy.core.ThreadPoolConfiguration;


/**
 *
 * @author dnikiforov
 */
public class DefaultThreadPoolConfiguration implements ThreadPoolConfiguration {
	
	private int acceptors = 1;
	private int clientWorkers = 16;
	private int serverWorkers = 16;
	
	@Override
	public void withAcceptorThreads(int acceptors) {
		this.acceptors = acceptors;
	}
	
	@Override
	public void withClientToProxyWorkerThreads(int clientWorkers) {
		this.clientWorkers = clientWorkers;
	}
	
	@Override
	public void withProxyToServerWorkerThreads(int serverWorkers) {
		this.serverWorkers = serverWorkers;
	}

	@Override
	public int getAcceptors() {
		return acceptors;
	}

	@Override
	public int getClientWorkers() {
		return clientWorkers;
	}

	@Override
	public int getServerWorkers() {
		return serverWorkers;
	}

}

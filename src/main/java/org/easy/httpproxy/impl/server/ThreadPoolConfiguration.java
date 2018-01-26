/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.impl.server;

/**
 *
 * @author dnikiforov
 */
public class ThreadPoolConfiguration {
	
	private int acceptors = 1;
	private int clientWorkers = 16;
	private int serverWorkers = 16;
	
	public void withAcceptorThreads(int acceptors) {
		this.acceptors = acceptors;
	}
	
	public void withClientToProxyWorkerThreads(int clientWorkers) {
		this.clientWorkers = clientWorkers;
	}
	
	public void withProxyToServerWorkerThreads(int serverWorkers) {
		this.serverWorkers = serverWorkers;
	}

	public int getAcceptors() {
		return acceptors;
	}

	public int getClientWorkers() {
		return clientWorkers;
	}

	public int getServerWorkers() {
		return serverWorkers;
	}

}

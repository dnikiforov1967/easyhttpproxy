/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.impl.util;

/**
 *
 * @author dnikiforov
 */
public class ConnectionsStat {

	private final int openClientConnections;
	private final int openServerConnections;

	public ConnectionsStat() {
		this.openClientConnections = StatisticsUtil.openClientConnections();
		this.openServerConnections = StatisticsUtil.openServerConnections();
	}

	public int getOpenClientConnections() {
		return openClientConnections;
	}

	public int getOpenServerConnections() {
		return openServerConnections;
	}

}

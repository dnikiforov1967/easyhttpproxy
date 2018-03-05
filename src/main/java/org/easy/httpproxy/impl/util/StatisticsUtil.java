/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.impl.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author dnikiforov
 */
public final class StatisticsUtil {
	
	private StatisticsUtil() {
		
	}
	
	private static final AtomicInteger openClientConnections = new AtomicInteger();
	private static final AtomicInteger openServerConnections = new AtomicInteger();
	
	public static int clientConnectionOpen() {
		return openClientConnections.incrementAndGet();
	}

	public static int serverConnectionOpen() {
		return openServerConnections.incrementAndGet();
	}

	public static int serverConnectionClose() {
		return openServerConnections.decrementAndGet();
	}

	public static int clientConnectionClose() {
		return openClientConnections.decrementAndGet();
	}
	
	public static int openClientConnections() {
		return openClientConnections.get();
	}
	
	public static int openServerConnections() {
		return openServerConnections.get();
	}
	
	public static void reset() {
		openClientConnections.set(0);
		openServerConnections.set(0);
	}
	
}

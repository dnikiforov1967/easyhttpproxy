/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easy.httpproxy.core;

import org.easy.httpproxy.impl.socket.ExtendedNioSocketChannel;
import static org.testng.Assert.*;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 *
 * @author dnikiforov
 */
public class ExtendedNioSocketChannelTest {
	
	public ExtendedNioSocketChannelTest() {
	}

	// TODO add test methods here.
	// The methods must be annotated with annotation @Test. For example:
	//
	// @Test
	// public void hello() {}

	@org.testng.annotations.BeforeClass
	public static void setUpClass() throws Exception {
	}

	@org.testng.annotations.AfterClass
	public static void tearDownClass() throws Exception {
	}

	@org.testng.annotations.BeforeMethod
	public void setUpMethod() throws Exception {
	}

	@org.testng.annotations.AfterMethod
	public void tearDownMethod() throws Exception {
	}
	
	@DataProvider
	private Object[][] getFreeChannel() {
		return new Object[][] {{new ExtendedNioSocketChannel()}};
	}
	
	@Test(dataProvider = "getFreeChannel")
	public void lockTest(ExtendedNioSocketChannel channel) {
		boolean lock = channel.lock();
		assertTrue(lock);
	}

	@Test(dataProvider = "getFreeChannel")
	public void unlockTest(ExtendedNioSocketChannel channel) {
		channel.lock();
		boolean unlock = channel.unlock();
		assertTrue(unlock);
	}

	@Test(dataProvider = "getFreeChannel")
	public void doublelockTest(ExtendedNioSocketChannel channel) {
		channel.lock();
		boolean lock = channel.lock();
		assertFalse(lock);
	}
	
	
}

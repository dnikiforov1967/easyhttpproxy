package org.easy.httpproxy.impl.socket;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.concurrent.atomic.AtomicInteger;
import org.easy.httpproxy.core.SocketChannelExtentionInterface;

/**
 * Class extends the existing NioSocketChannel Three states are appended -
 * idle(0), timed out (-1) and used (1)
 *
 * @author dnikiforov
 */
public class ExtendedNioSocketChannel extends NioSocketChannel implements SocketChannelExtentionInterface {

	private final AtomicInteger state = new AtomicInteger(0);
	private volatile boolean isFlowCompleted = true;

	@Override
	public boolean lock() {
		//Set as "used" just if its current state is "idle"
		boolean compareAndSet = state.compareAndSet(0, 1);
		return compareAndSet;
	}

	@Override
	public boolean unlock() {
		//Set as "idle" just if its current state is "used"
		boolean compareAndSet = state.compareAndSet(1, 0);
		return compareAndSet;
	}

	@Override
	public void setFlowCompleted(boolean isCompleted) {
		isFlowCompleted = isCompleted;
	}

	@Override
	public boolean isFlowCompleted() {
		return isFlowCompleted;
	}

}

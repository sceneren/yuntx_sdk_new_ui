package com.yuntongxun.mcm.genesys.util;

import java.util.concurrent.atomic.AtomicInteger;

public class ReferenceIdUtils {

	private static AtomicInteger sequenceNumber = new AtomicInteger(1);

	public static int getReferenceId() {
		if (sequenceNumber.get() == Integer.MAX_VALUE) {
			sequenceNumber.lazySet(1);
		}
		return sequenceNumber.getAndIncrement();
	}

}

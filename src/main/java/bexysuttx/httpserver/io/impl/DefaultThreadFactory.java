package bexysuttx.httpserver.io.impl;

import java.util.concurrent.ThreadFactory;

class DefaultThreadFactory implements ThreadFactory {

	private String name;
	private int count;

	DefaultThreadFactory() {
		super();
		name = "executor-thread-";
		count = 1;
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread th = new Thread(r, name + (count++));
		th.setDaemon(false);
		th.setPriority(8);
		return th;
	}

}

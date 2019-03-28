package net.anfoya.java.util.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShutdownHook {
	private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownHook.class);

	public ShutdownHook(Runnable runnable) {
		this(runnable, true, Thread.currentThread().getStackTrace()[2].getClassName());
	}

	public ShutdownHook(Runnable runnable, boolean daemon) {
		this(runnable, daemon, Thread.currentThread().getStackTrace()[2].getClassName());
	}

	public ShutdownHook(Runnable runnable, boolean daemon, String log) {
		Thread hook = new Thread(() -> {
			runnable.run();
			LOGGER.info("[shutdown complete] {}", log);
		});
		hook.setDaemon(daemon);

		Runtime.getRuntime().addShutdownHook(hook);
	}
}

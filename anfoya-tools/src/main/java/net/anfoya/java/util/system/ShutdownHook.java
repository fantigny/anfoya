package net.anfoya.java.util.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.anfoya.java.util.VoidCallable;

public class ShutdownHook {
	private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownHook.class);

	public ShutdownHook(final VoidCallable callable) {
		this(callable, true, Thread.currentThread().getStackTrace()[2].getClassName());
	}

	public ShutdownHook(final VoidCallable callable, final boolean daemon) {
		this(callable, daemon, Thread.currentThread().getStackTrace()[2].getClassName());
	}

	protected ShutdownHook(final VoidCallable callable, final boolean daemon, final String className) {
		final Thread hook = new Thread(() -> {
			try {
				callable.call();
			} catch (Exception e) {
				LOGGER.error("shutdown hook execution failed for {}", className, e);
				return;
			}

			String simpleName;
			try {
				simpleName = className.substring(className.lastIndexOf(".") + 1);
			} catch (Exception e) {
				simpleName = className;
			}
			
			LOGGER.info("shutdown complete for {}", simpleName);
		});
		hook.setDaemon(daemon);

		Runtime.getRuntime().addShutdownHook(hook);
	}
}

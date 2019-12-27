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
		final String name = cleanName(className);
		final Runnable hook = createHook(callable, name);
		Runtime.getRuntime().addShutdownHook(new Thread(hook) {{ setDaemon(daemon); }});
	}

	private String cleanName(String className) {
		try {
			return className.substring(className.lastIndexOf(".") + 1);
		} catch (Exception e) {
			return className;
		}
	}

	private Runnable createHook(VoidCallable callable, String name) {
		return () -> {
			try {
				callable.call();
				LOGGER.info("shutdown complete for {}", name);
			} catch (Exception e) {
				LOGGER.error("shutdown hook execution failed for {}", name, e);
			}
		};
	}
}

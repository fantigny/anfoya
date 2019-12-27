package net.anfoya.java.util.concurrent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.anfoya.java.util.VoidCallback;
import net.anfoya.java.util.system.ShutdownHook;

public class ThreadPool {
	public enum PoolPriority { MIN, REG, MAX, MUST_RUN }

	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPool.class);
	private static final Map<PoolPriority, ObservableExecutorService> priorityPools = new HashMap<>();

	////////////////////
	////////////////////
	
	private static ThreadPool instance = null;

	public static void setDefault(ObservableExecutorService min, ObservableExecutorService reg, ObservableExecutorService max) {
		if (instance != null) {
			throw new IllegalStateException("already initialized");
		}

		instance = new ThreadPool(min, reg, max);
	}

	public static ThreadPool getDefault() {
		if (instance == null) {
			instance = new ThreadPool(ObservableExecutors.newCachedThreadPool("min", Thread.MIN_PRIORITY)
					, ObservableExecutors.newCachedThreadPool("reg", Thread.NORM_PRIORITY)
					, ObservableExecutors.newCachedThreadPool("max", Thread.MAX_PRIORITY));
		}

		return instance;
	}

	////////////////////
	////////////////////
	
	private ThreadPool(ObservableExecutorService min, ObservableExecutorService reg, ObservableExecutorService max) {
		priorityPools.put(PoolPriority.MIN, min);
		priorityPools.put(PoolPriority.REG, reg);
		priorityPools.put(PoolPriority.MAX, max);
		priorityPools.put(PoolPriority.MUST_RUN, ObservableExecutors.newCachedThreadPool("must-run", Thread.NORM_PRIORITY));

		new ShutdownHook(() -> waitForMustRun(), false);
	}

	private void waitForMustRun() {
		final ObservableExecutorService mustRun = priorityPools.get(PoolPriority.MUST_RUN);
		while(mustRun.isRunning()) {
			try {
				Thread.sleep(250);
			} catch (final Exception e) {
				LOGGER.error("interrupted while running mandatory process", e);
			}
		}
	}

	public void submit(final PoolPriority priority, final String description, final Runnable runnable) {
		priorityPools.get(priority).submit(runnable, description);
	}

	public <T> Future<T> submit(final PoolPriority priority, final String description, final Callable<T> callable) {
		return priorityPools.get(priority).submit(callable, description);
	}

	public void addOnChange(final PoolPriority priority, final VoidCallback<Map<Future<?>, String>> callback) {
		final ObservableExecutorService pool = priorityPools.get(priority);
		if (pool != null) {
			pool.addOnChange(callback);
		}
	}

	public void mustRun(final String description, final Runnable runnable) {
		priorityPools.get(PoolPriority.MUST_RUN).submit(runnable, description);
	}
}

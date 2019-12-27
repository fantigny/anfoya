package net.anfoya.java.io;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.anfoya.java.util.system.ShutdownHook;

public class TmpFileHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(TmpFileHandler.class);
	private static final String TEMP_FOLDER = System.getProperty("java.io.tmpdir") + File.separatorChar;

	////////////////////
	////////////////////

	private static TmpFileHandler instance;

	public static void setDefault(TmpFileHandler cleaner) {
		if (instance != null) {
			throw new IllegalStateException("already initialized");
		}

		instance = cleaner;
	}

	public static TmpFileHandler getDefault() {
		if (instance == null) {
			instance = new TmpFileHandler();
		}
		return instance;
	}

	////////////////////
	////////////////////

	private final Set<String> paths;

	private TmpFileHandler() {
		paths = new HashSet<>();

		new ShutdownHook(() -> clean(), true);
	}

	private File add(File file) {
		paths.add(file.getAbsolutePath());

		return file;
	}

	public void clean() {
		LOGGER.info("cleaning...");
		paths.parallelStream().forEach(p -> {
			try {
				new File(p).delete();
			} catch (Exception e) {
				LOGGER.warn("failed to delete tmp file: {} ({})", p, e.getMessage());
			}
		});
		LOGGER.info("deleted {} files", paths.size());
	}

	public File createTempFile(String prefix, String suffix) throws IOException {
		return add(File.createTempFile(prefix, suffix, new File(TEMP_FOLDER)));
	}

	public File createTempFile(String name) {
		return add(new File(TEMP_FOLDER + name));
	}
}

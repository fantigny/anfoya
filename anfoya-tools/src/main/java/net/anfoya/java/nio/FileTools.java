package net.anfoya.java.nio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileTools {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileTools.class);

	private volatile boolean dry;

	public void setDry(boolean dry) {
		LOGGER.info("<< dry session {} here >>", dry? "starts": "ends");
		this.dry = dry;
	}

	public void createFolder(Path folder) throws IOException {
		LOGGER.info("create {}", folder);

		if (dry) {
			LOGGER.warn("<< dry >>");
			return;
		}

		Files.createDirectory(folder);
	}

	public void move(Path source, Path target) throws IOException 		{
		LOGGER.info("move {} to {}", source.getFileName(), target.getParent());

		if (dry) {
			LOGGER.warn("<< dry >>");
			return;
		}

		Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
	}

	public boolean isEmpty(Path folder) {
		try {
			return Files.isDirectory(folder) && !Files.list(folder).findAny().isPresent();
		} catch (final IOException e) {
			return false;
		}
	}

	public void delete(Path file) throws IOException {
		LOGGER.info("delete {}", file);

		if (dry) {
			LOGGER.warn("<< dry >>");
			return;
		}

		Files.delete(file);
	}

}

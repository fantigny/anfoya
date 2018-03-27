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

	public void createFolder(Path folder) {
		LOGGER.info("create {}", folder);
		if (!dry) {
			try {
				Files.createDirectory(folder);
			} catch (final IOException ex) {
				LOGGER.error("create folder {}", folder, ex);
			}
		}
	}

	public void moveFile(Path source, Path target) 		{
		LOGGER.info("move {} to {}", source.getFileName(), target.getParent());
		if (!dry) {
			try {
				Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
			} catch (final IOException ex) {
				LOGGER.error("move file from {} to {}", source, target, ex);
			}
		}
	}

	public boolean isEmpty(Path folder) {
		try {
			return Files.isDirectory(folder) && !Files.list(folder).findAny().isPresent();
		} catch (final IOException e) {
			return false;
		}
	}

	public void delete(Path file) {
		LOGGER.info("delete {}", file);
		if (!dry) {
			try {
				Files.delete(file);
			} catch (final IOException e) {
				LOGGER.error("delete empty folder {}", file, e);
			}
		}
	}


}

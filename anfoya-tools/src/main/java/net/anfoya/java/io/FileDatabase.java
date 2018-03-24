package net.anfoya.java.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileDatabase {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileDatabase.class);

	private final Path path;
	private final int maxFilesPerFolder;
	private final int folderNameLength;

	private final Set<Path> files;

	public FileDatabase(Path path, int maxFilesPerFolder, int folderNameLength) {
		this.path = path;
		this.maxFilesPerFolder = maxFilesPerFolder;
		this.folderNameLength = folderNameLength;

		files = new TreeSet<>();
	}

	public void init() throws FileDatabaseException {
		try {
			refreshFileSet(Files.walk(path));
			buildFolders();
			cleanUp(Files.walk(path));
		} catch (final IOException e) {
			throw new FileDatabaseException("", e);
		}
	}

	private void refreshFileSet(Stream<Path> fileStream) {
		// add new files
		fileStream
			.filter(p -> !Files.isDirectory(p))
			.forEach(p -> files.add(p));

		// remove non existing files
		for(final Iterator<Path> i=files.iterator(); i.hasNext();) {
			final Path file = i.next();
			if (Files.notExists(file)) {
				i.remove();
			}
		}
	}

	private void buildFolders() {
		final Map<Path, Set<Path>> folderFiles = new TreeMap<>();

		// build new folder structure
		Set<Path> files = null;
		for(final Path file: this.files) {
			final Path destination = getDestinationPath(file);
			if (files == null || files.size() == maxFilesPerFolder && !folderFiles.containsKey(destination)) {
				files = new TreeSet<>();
				folderFiles.put(destination, files);
			}
			files.add(file);
		}

		// implement it
		folderFiles.entrySet().forEach(e -> {
			final Path folder = e.getKey();
			for(final Path file: e.getValue()) {
				final Path newFile = Paths.get(folder.toString(), file.getFileName().toString());
				LOGGER.debug("implement {}", newFile.toString());

				if (!folder.equals(file.getParent())) {
					if (Files.notExists(folder)) {
						LOGGER.info("create folder {}", folder.toString());
						try {
							Files.createDirectory(folder);
						} catch (final IOException e1) {
							LOGGER.error("create folder {}", folder.toString(), e);
							break;
						}
					}
					LOGGER.info("move file {} to {}", file.toString(), newFile.toString());
					try {
						Files.move(file, newFile, StandardCopyOption.REPLACE_EXISTING);
					} catch (final IOException e1) {
						LOGGER.error("move file {} to {}", file.toString(), newFile.toString(), e);
					}
				}
			}
		});
	}

	private Path getDestinationPath(Path file) {
		final String filename  = file.getFileName().toString();
		return Paths.get(path.toString(), filename
				.substring(0, Math.min(filename.length(), folderNameLength + 1)));
	}

	private void cleanUp(Stream<Path> fileStream) {
		fileStream
			.filter(p -> {
				try {
					return Files.isDirectory(p) && !Files.list(p).findAny().isPresent();
				} catch (final IOException e) {
					return false;
				}
			})
			.forEach(p -> {
				try {
					LOGGER.info("delete empty folder {}", p);
					Files.delete(p);
				} catch (final IOException e) {
					LOGGER.error("delete empty folder {}", p, e);
				}
			});
	}
}

package net.anfoya.java.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
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

	private boolean needRebuild() {
		final Map<Path, Long> folderFileCount = new HashMap<>();
		files.stream().forEach(f -> {
			final Path folder = f.getParent();
			Long count = folderFileCount.get(folder);
			folderFileCount.put(folder, count == null? 1: ++count);
		});

		return folderFileCount
				.values()
				.stream()
				.filter(c -> c > maxFilesPerFolder)
				.findFirst()
				.isPresent();
	}

	private void buildFolders() {
		final Map<Path, Set<Path>> folderFiles = new TreeMap<>();

		// build new folder structure
		for(final Path file: files) {
			final Path destination = getDestinationFolder(file);
			Set<Path> files = null;
			for(final Entry<Path, Set<Path>> e: folderFiles.entrySet()) {
				if (e.getKey().compareTo(destination) <= 0) {
					if (e.getValue().size() < maxFilesPerFolder) {
						files = e.getValue();
						break;
					}
				}
			}
			if (files == null) {
				folderFiles.put(destination, new TreeSet<>(Arrays.asList(file)));
			} else {
				files.add(file);
			}
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

	private Path getDestinationFolder(Path file) {
		String folderName  = file.getFileName().toString();
		while(folderName.length() < folderNameLength) {
			folderName += "_";
		}
		folderName = folderName.substring(0, folderNameLength + 1).toUpperCase();
		return Paths.get(path.toString(), folderName);
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

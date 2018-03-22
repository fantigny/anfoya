package net.anfoya.java.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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

	public void init() throws IOException {
		refreshFileSet();
		if (needRebuild()) {
			buildFolders();
			cleanUp();
			refreshFileSet();
		}
	}

	private void refreshFileSet() throws IOException {
		// add new files
		Files.walk(path)
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

		return true || folderFileCount
				.values()
				.stream()
				.filter(c -> c > maxFilesPerFolder)
				.findFirst()
				.isPresent();
	}

	private void buildFolders() throws IOException {
		final Map<Path, Set<Path>> folderFiles = new TreeMap<>();

		// build new folder structure
		for(final Path file: files) {
			final Path destination = getDestinationFolder(file);
			final Optional<Entry<Path, Set<Path>>> folderEntry = folderFiles.entrySet()
					.stream()
					.filter(e -> {
						final String existing = e.getKey().getFileName().toString();
						final String current = destination.getFileName().toString();
						return existing.compareTo(current) <= 0 && e.getValue().size() < maxFilesPerFolder;
					})
					.findFirst();
			if (folderEntry.isPresent()) {
				folderEntry.get().getValue().add(file);
			} else {
				folderFiles.put(destination, new HashSet<>(Arrays.asList(file)));
			}
		}

		// implement it
		for(final Entry<Path, Set<Path>> e: folderFiles.entrySet()) {
			final Path folder = e.getKey();
			for(final Path file: e.getValue()) {
				if (!folder.equals(file.getParent())) {
					if (Files.notExists(folder)) {
						LOGGER.info("create folder {}", folder.toString());
						Files.createDirectory(folder);
					}
					final Path newFile = Paths.get(folder.toString(), file.getFileName().toString());
					LOGGER.info("move file {} to {}", file.toString(), newFile.toString());
					Files.move(file, newFile, StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}
	}

	private Path getDestinationFolder(Path file) {
		String folderName  = file.getFileName().toString();
		while(folderName.length() < folderNameLength) {
			folderName += "_";
		}
		folderName = folderName.replaceAll(" ", "_");
		folderName = folderName.substring(0, folderNameLength + 1).toUpperCase();
		return Paths.get(path.toString(), folderName);
	}

	private void cleanUp() throws IOException {
		Files
			.walk(path)
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

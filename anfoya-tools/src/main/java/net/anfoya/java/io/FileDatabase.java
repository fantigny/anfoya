package net.anfoya.java.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileDatabase {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileDatabase.class);

	private final Path path;
	private final int folderFileCount;
	private final int folderNameLength;

	private final Comparator<Path> pathComparator;
	private final Set<Path> files;

	public FileDatabase(Path path, int folderFileCount, int folderNameLength) {
		this.path = path;
		this.folderFileCount = folderFileCount;
		this.folderNameLength = folderNameLength;

		pathComparator = new Comparator<Path>() {
			@Override public int compare(Path p1, Path p2) {
				final char c1 = p1.getFileName().toString().charAt(0);
				final char c2 = p2.getFileName().toString().charAt(0);

				if (Character.isLetterOrDigit(c1) && !Character.isLetterOrDigit(c2)) {
					return 1;
				}
				if (!Character.isLetterOrDigit(c1) && Character.isLetterOrDigit(c2)) {
					return -1;
				}

				return p1.getFileName().compareTo(p2.getFileName());
			}
		};
		files = new TreeSet<>(pathComparator);
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
		LOGGER.debug("listing files from {}", path.toString());

		// add new files
		files.addAll(fileStream
				.filter(p -> !Files.isDirectory(p))
				.collect(Collectors.toSet()));

		// remove non existing files
		files.removeIf(f -> Files.notExists(f));

		LOGGER.debug("found {} files", files.size());
	}

	private void buildFolders() {
		final NavigableMap<Path, Set<Path>> folderFiles = new TreeMap<>();

		// build new folder structure
		files
			.stream()
			.forEach(f -> {
				final Path destination = getDestination(f);
				Set<Path> files = folderFiles.isEmpty()? null: folderFiles.lastEntry().getValue();
				if (files == null || files.size() == folderFileCount) {
					files = new TreeSet<>(pathComparator);
					folderFiles.put(destination, files);
				}
				files.add(f);
			});

		// implement it
		folderFiles
			.entrySet()
			.forEach(e -> {
				final Path folder = e.getKey();
				for(final Path file: e.getValue()) {
					final Path newFile = Paths.get(folder.toString(), file.getFileName().toString());
					LOGGER.debug("implement {}", newFile.toString());
					if (!newFile.equals(file)) {
						if (!folder.equals(file.getParent()) && Files.notExists(folder)) {
							LOGGER.info("create folder {}", folder.toString());
							try {
								Files.createDirectory(folder);
							} catch (final IOException e1) {
								LOGGER.error("create folder {}", folder.toString(), e);
								break;
							}
						}
						LOGGER.info("move file to {} (from {})", newFile.toString(), file.toString());
						try {
							Files.move(file, newFile, StandardCopyOption.REPLACE_EXISTING);
						} catch (final IOException e1) {
							LOGGER.error("move file to {} (from {})", newFile.toString(), file.toString(), e);
						}
					}
				}
			});
	}

	private Path getDestination(Path file) {
		// build destination path from filename
		final String filename = file.getFileName().toString();
		final String foldername = filename.substring(0, Math.min(filename.length(), folderNameLength + 1));
		return Paths.get(path.toString(), foldername);
	}

	private void cleanUp(Stream<Path> fileStream) {
		// remove empty folders
		fileStream
			.filter(p -> {
				try {
					// check folder is empty
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

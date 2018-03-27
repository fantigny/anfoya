package net.anfoya.java.nio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FolderOrganiser {
	private static final Logger LOGGER = LoggerFactory.getLogger(FolderOrganiser.class);
	private static final Comparator<String> FILENAME_COMPARATOR = new Comparator<String>() { // allows special characters to be sorted first
		@Override public int compare(String filename1, String filename2) {
			final char c1 = filename1.charAt(0), c2 = filename2.charAt(0);
			return Character.isLetterOrDigit(c1) && !Character.isLetterOrDigit(c2)? 1
					: !Character.isLetterOrDigit(c1) && Character.isLetterOrDigit(c2)? -1
							: filename1.toLowerCase().compareTo(filename2.toLowerCase());
		}
	};

	private final Path path;

	private final FileTools fileTools;
	private final Map<String, Path> files;

	public FolderOrganiser(Path path) {
		this.path = path;

		fileTools = new FileTools();
		files = new ConcurrentSkipListMap<>(FILENAME_COMPARATOR);
	}

	public FolderOrganiser dry(boolean dry) {
		fileTools.setDry(dry);

		return this;
	}

	public Set<String> getFilenames() {
		return Collections.unmodifiableSet(files.keySet());
	}

	public Path getPath(String filename) {
		return files.get(filename);
	}

	public FolderOrganiser reload() throws IOException {
		LOGGER.debug("discovering {}", path);

		// remove non existing files
		files.entrySet().removeIf(e -> Files.notExists(e.getValue()));

		// add new files (and count folders)
		final AtomicInteger folderCount = new AtomicInteger(-1);
		try (final Stream<Path> stream = Files.walk(path)) {
			files.putAll(stream
					.peek(p -> folderCount.addAndGet(Files.isDirectory(p)? 1: 0))
					.filter(p -> !Files.isDirectory(p))
					.collect(Collectors.toMap(p -> p.getFileName().toString(), p -> p)));

		}

		LOGGER.info("found {} files in {} folders", files.size(), folderCount.get());

		return this;
	}

	public FolderOrganiser cleanUp() throws IOException {
		try (final Stream<Path> stream = Files.walk(path)) {
		// remove empty folders
			stream
				.filter(p -> fileTools.isEmpty(p))
				.parallel()
				.forEach(p -> fileTools.delete(p));
		}

		return this;
	}

	public FolderOrganiser organise(int fileCount, int maxNameLength) {
		final Map<Path, Set<Path>> folderFiles = new HashMap<>();

		// build new folder/files hierarchy
		final AtomicReference<Path> currentDestination = new AtomicReference<>();
		files
			.values()
			.stream()
			.forEach(f -> {
				final Path destination = getDestination(f, maxNameLength);
				if (folderFiles.isEmpty()
						|| folderFiles.get(currentDestination.get()).size() >= fileCount
						&& !folderFiles.containsKey(destination)) {
					folderFiles.put(destination, new HashSet<>());
					currentDestination.set(destination);
				}
				folderFiles.get(currentDestination.get()).add(f);
			});

		// create folders
		folderFiles
			.keySet()
			.parallelStream()
			.filter(f -> !Files.exists(f))
			.forEach(f -> fileTools.createFolder(f));

		// move files
		folderFiles
			.entrySet()
			.stream()
			.flatMap(e -> e.getValue()
					.stream()
					.map(f -> new AbstractMap.SimpleEntry<>(
							/* source */ f,
							/* target */ Paths.get(e.getKey().toString(), f.getFileName().toString()))))
			.filter(e -> !Files.exists(e.getValue()))
			.parallel()
			.forEach(e -> {
				fileTools.moveFile(e.getKey(), e.getValue());
				files.put(e.getValue().getFileName().toString(), e.getValue());
			});

		return this;
	}

	private Path getDestination(Path file, int maxNameLength) {
		// build destination path from filename
		final String fileName = file.getFileName().toString();
		final String folderName = fileName.substring(0, Math.min(fileName.length(), maxNameLength));
		return Paths.get(path.toString(), folderName);
	}
}

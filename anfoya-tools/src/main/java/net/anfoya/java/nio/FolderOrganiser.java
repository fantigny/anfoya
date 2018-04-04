package net.anfoya.java.nio;

import java.io.FileNotFoundException;
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
	protected static final int FILE_COUNT = 20;
	protected static final int MAX_FILE_NAME_LENGTH = 4;

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
	private final Map<String, Path> filenamesPath;

	public FolderOrganiser(Path path) {
		this.path = path;

		fileTools = new FileTools();
		filenamesPath = new ConcurrentSkipListMap<>(FILENAME_COMPARATOR);
	}

	public FolderOrganiser dry(boolean dry) {
		fileTools.setDry(dry);

		return this;
	}

	public Set<String> getFilenames() {
		return Collections.unmodifiableSet(filenamesPath.keySet());
	}

	public Path getPath(String filename) {
		return filenamesPath.get(filename);
	}

	public boolean exists(String filename) {
		return filenamesPath.keySet().contains(filename);
	}

	public FolderOrganiser reload() throws IOException {
		LOGGER.debug("discovering {}", path);

		// remove non existing files
		filenamesPath.entrySet().removeIf(e -> Files.notExists(e.getValue()));

		// add new files (and count folders)
		final AtomicInteger folderCount = new AtomicInteger(-1);
		try (final Stream<Path> stream = Files.walk(path)) {
			final Set<Path> duplicates = new HashSet<>();
			filenamesPath.putAll(stream
					.peek(p -> folderCount.addAndGet(Files.isDirectory(p)? 1: 0))
					.filter(p -> !Files.isDirectory(p))
					.collect(Collectors.toMap(
							p -> p.getFileName().toString()
							, p -> p
							, (p1, p2) -> {
								duplicates.add(p2);
								return p1;
							})));
			filenamesPath.putAll(duplicates
					.stream()
					.collect(Collectors.toMap(p -> getDuplicateFilename(p), p -> p)));
		}

		LOGGER.info("found {} files in {} folders", filenamesPath.size(), folderCount.get());

		return this;
	}

	public FolderOrganiser cleanUp() throws IOException {
		try (final Stream<Path> stream = Files.walk(path)) {
			stream
				.parallel()
				.forEach(p -> {
					if (fileTools.isEmpty(p)
							|| p.getFileName().toString().startsWith("._")
							|| p.getFileName().toString().equals(".DS_Store")) {
						fileTools.delete(p);
						// update path in filenamesPath to keep in sync
						final String filename = p.getFileName().toString();
						filenamesPath.remove(filename);
					}
				});
		}

		return this;
	}

	public void rename(String filename, String newFilename) throws FileNotFoundException {
		if (!filenamesPath.containsKey(filename)) {
			throw new FileNotFoundException(filename);
		}

		final Path source = filenamesPath.get(filename);
		final Path target = Paths.get(source.getParent().toString(), newFilename);

		fileTools.moveFile(source, target);
	}

	public FolderOrganiser organise() {
		return organise(FILE_COUNT, MAX_FILE_NAME_LENGTH);
	}

	public FolderOrganiser organise(int fileCount, int maxNameLength) {
		final Map<Path, Set<Path>> folderFiles = new HashMap<>();

		// build new folder/files hierarchy
		final AtomicReference<Path> currentDestination = new AtomicReference<>();
		filenamesPath
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
		final Set<String> filenames = new HashSet<>();
		folderFiles
			.entrySet()
			.stream()
			.flatMap(e -> e.getValue()
					.stream()
					.map(f -> {
						// handle duplicate filenames
						String filename = f.getFileName().toString();
						if (filenames.contains(filename)) {
							filename = getDuplicateFilename(f);
						}
						filenames.add(filename);

						return new AbstractMap.SimpleEntry<>(f, Paths.get(e.getKey().toString(), filename));
					}))
			.filter(e -> !e.getKey().equals(e.getValue()))
			.parallel()
			.forEach(e -> {
				final Path source = e.getKey(), target = e.getValue();
				fileTools.moveFile(source, target);

				// update path in filenamesPath to keep in sync
				final String filename = target.getFileName().toString();
				filenamesPath.remove(filename);
				filenamesPath.put(filename, target);
			});

		return this;
	}

	private String getDuplicateFilename(Path duplicate) {
		// build filename for a duplicated path
		final String filename = duplicate.getFileName().toString();
		final String extension = filename.substring(filename.lastIndexOf(".")+1);
		return new StringBuilder()
				.append(filename)
				.append(" (from ")
				.append(duplicate.getParent().getFileName().toString())
				.append(")")
				.append(extension.isEmpty()? "": ".")
				.append(extension)
				.toString();
	}

	private Path getDestination(Path file, int maxNameLength) { //TODO create class PathBuilder
		// build destination path from filename
		 String fileName = file.getFileName().toString();
		 if (fileName.contains(" from ")) {
			 fileName = fileName.substring(0, fileName.indexOf(" from "));
		 }
		final String folderName = fileName.substring(0, Math.min(fileName.length(), maxNameLength));
		return Paths.get(path.toString(), folderName);
	}
}

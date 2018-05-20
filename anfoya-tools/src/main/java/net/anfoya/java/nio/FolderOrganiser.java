package net.anfoya.java.nio;

import java.awt.Desktop;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FolderOrganiser {
	private static final Logger LOGGER = LoggerFactory.getLogger(FolderOrganiser.class);

	protected static final int DEFAULT_MAX_FILE_NAME_LENGTH = 4;

	// allows special characters to be sorted first
	private static final Comparator<String> FILENAME_COMPARATOR = new Comparator<String>() {
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

	public FolderOrganiser setDry(boolean dry) {
		fileTools.setDry(dry);

		return this;
	}

	public Set<String> getFilenames() {
		return Collections.unmodifiableSet(filenamesPath.keySet());
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

	public Set<Exception> cleanUpFolders() throws IOException {
		final Set<Exception> exceptions = new CopyOnWriteArraySet<>();

		try (final Stream<Path> stream = Files.walk(path)) {
			stream
				.parallel()
				.forEach(p -> {
					if (fileTools.isEmpty(p)) {
						try { fileTools.delete(p); }
						catch (final IOException e) { exceptions.add(e); }
					}
				});
		}

		return exceptions;
	}

	public Set<Exception> cleanUpFiles() throws IOException {
		final Set<Exception> exceptions = new CopyOnWriteArraySet<>();

		try (final Stream<Path> stream = Files.walk(path)) {
			stream
				.parallel()
				.forEach(p -> {
					if (cleanUpFile(p.getFileName().toString())) {
						try { fileTools.delete(p); }
						catch (final IOException e) { exceptions.add(e); }

						// update path in filenamesPath to keep in sync
						final String filename = p.getFileName().toString();
						filenamesPath.remove(filename);
					}
				});
		}

		return exceptions;
	}

	private boolean cleanUpFile(String fileName) {
		return Arrays
				.stream(new String[] { ">._", "=.DS_Store", "<.nfo", "<.txt", "<.jpg", "<.png" })
				.map(s -> {
					final String lowerFileName = fileName.toLowerCase(), lowerSearch = s.substring(1).toLowerCase();
					switch (s.charAt(0)) {
					case '=':	return lowerFileName.equals(lowerSearch);
					case '>':	return lowerFileName.startsWith(lowerSearch);
					case '<':	return lowerFileName.endsWith(lowerSearch);
					case '*':	return lowerFileName.contains(lowerSearch);
					default:	return false;
					}
				})
				.reduce(false, (a, b) -> a || b);
	}

	public void rename(String filename, String newFilename) throws IOException {
		if (!filenamesPath.containsKey(filename)) {
			throw new FileNotFoundException(filename);
		}

		final Path source = filenamesPath.get(filename);
		final Path target = Paths.get(source.getParent().toString(), newFilename);

		fileTools.move(source, target);
	}

	public Set<Exception> organise(int filePerFolder) {
		return organise(filePerFolder, DEFAULT_MAX_FILE_NAME_LENGTH);
	}

	public Set<Exception> organise(int fileCount, int maxNameLength) {
		final Set<Exception> exceptions = new CopyOnWriteArraySet<>();
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
			.filter(p -> !Files.exists(p))
			.forEach(p -> {
				try { fileTools.createFolder(p); }
				catch (final IOException e) { exceptions.add(e); }
			});

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

				try { fileTools.move(source, target); }
				catch (final IOException ex) { exceptions.add(ex); }

				// update path in filenamesPath to keep in sync
				final String filename = target.getFileName().toString();
				filenamesPath.remove(filename);
				filenamesPath.put(filename, target);
			});

		return exceptions;
	}

	protected Path getPath(String filename) {
		return filenamesPath.get(filename);
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

	public Set<Exception> open(Set<String> filenames) {
		final Set<Exception> exceptions = new CopyOnWriteArraySet<>();

		filenames
				.parallelStream()
				.forEach(f -> {
					try { Desktop.getDesktop().open(getPath(f).toFile()); }
					catch (final IOException ex) { exceptions .add(ex); }
				});

		return exceptions;
	}

	public Set<Exception> delete(Set<String> filenames) {
		final Set<Exception> exceptions = new CopyOnWriteArraySet<>();

		filenames
				.parallelStream()
				.forEach(f -> {
					try { fileTools.delete(getPath(f)); }
					catch (final IOException ex) { exceptions .add(ex); }
				});

		return exceptions;
	}

	public void open(FileViewer viewer, String filename) throws Exception {
		viewer.open(getPath(filename));
	}
}

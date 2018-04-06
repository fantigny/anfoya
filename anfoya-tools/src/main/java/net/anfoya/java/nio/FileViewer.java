package net.anfoya.java.nio;

import java.nio.file.Path;

public interface FileViewer {

	public void init();
	public void open(Path path);
	public void close();

}

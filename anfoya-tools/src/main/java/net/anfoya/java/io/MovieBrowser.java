package net.anfoya.java.io;

import java.io.IOException;
import java.nio.file.Paths;

import net.anfoya.java.nio.FolderOrganiser;

public class MovieBrowser {

	public static void main(String[] args) throws IOException {
		new FolderOrganiser(Paths.get("/Volumes/movies/dl/"))
			.dry(false)
			.reload()
			.organise(20, 4)
			.cleanUp();
	}
}

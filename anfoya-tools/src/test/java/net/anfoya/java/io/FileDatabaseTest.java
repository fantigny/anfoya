package net.anfoya.java.io;

import java.io.IOException;
import java.nio.file.Paths;

import org.junit.Test;

public class FileDatabaseTest {

	@Test
	public void test() throws IOException {
		new FileDatabase(Paths.get("/Volumes/movies/dl"), 50, 5).init();
	}
}

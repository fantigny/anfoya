package net.anfoya.java.io;

import java.nio.file.Paths;

import org.junit.Test;

public class FileDatabaseTest {

	@Test
	public void test() throws FileDatabaseException {
		new FileDatabase(Paths.get("/Volumes/movies/dl"), 50, 5).init();
	}
}

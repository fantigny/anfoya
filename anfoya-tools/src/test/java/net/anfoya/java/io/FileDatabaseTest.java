package net.anfoya.java.io;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

public class FileDatabaseTest {

	@Test
	public void test() throws FileDatabaseException {
		new FileDatabase(Paths.get("src/test/resources/testFiles"), 3, 4).init();

		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/[test/[test1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/[test/[test2.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/[test/[test3.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test./test.1A.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test./test.A1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test./test1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test2/test2.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test2/test3.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test2/test4.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test5/test5.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test5/testA1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test5/testA2.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/testA/testA3.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/testA/testAA1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/testA/testAB1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/testZ/testZA1.txt")));

		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/[test1/[test1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/[test1/[test2.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/[test1/[test3.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/[test1/test.1A.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test.A/test.A1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test.A/test1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test.A/test2.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test.A/test3.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test4./test4.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test4./test5.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test4./testA1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test4./testA2.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/testA3/testA3.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/testA3/testAA1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/testA3/testAB1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/testA3/testZA1.txt")));

		new FileDatabase(Paths.get("src/test/resources/testFiles"), 4, 5).init();

		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/[test1/[test1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/[test1/[test2.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/[test1/[test3.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/[test1/test.1A.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test.A/test.A1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test.A/test1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test.A/test2.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test.A/test3.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test4./test4.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test4./test5.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test4./testA1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test4./testA2.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/testA3/testA3.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/testA3/testAA1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/testA3/testAB1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/testA3/testZA1.txt")));

		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/[test/[test1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/[test/[test2.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/[test/[test3.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test./test.1A.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test./test.A1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test./test1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test2/test2.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test2/test3.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test2/test4.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test5/test5.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test5/testA1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test5/testA2.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/testA/testA3.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/testA/testAA1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/testA/testAB1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/testZ/testZA1.txt")));
	}
}

package net.anfoya.java.nio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;

public class FolderOrganiserTest {

	@Ignore @Test public void organiseMovies() throws IOException {
		new FolderOrganiser(Paths.get("/Volumes/movies/dl")).setDry(true).reload().organise(10000, -1);
	}

	@Test public void test_3_4() throws IOException {

		new FolderOrganiser(Paths.get("src/test/resources/testFiles"))
			.reload()
			.organise(3, 4);

		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/[tes/[test1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/[tes/[test2.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/[tes/[test3.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test/test.1A.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test/test.A1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test/test1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test/test2.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test/test3.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test/test4.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test/test5.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test/testA1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test/testA2.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test/testA3.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test/testAA1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test/testAB1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test/testZA1.txt")));

		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/[test/[test1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/[test/[test2.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/[test/[test3.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/[tes1/test.1A.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test./test.A1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test./test1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test./test2.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test./test3.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test4/test4.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test4/test5.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test4/testA1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test4/testA2.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/testA/testA3.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/testA/testAA1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/testA/testAB1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/testA/testZA1.txt")));
	}

	@Test public void test_4_5() throws IOException {
		new FolderOrganiser(Paths.get("src/test/resources/testFiles"))
			.reload()
			.organise(4, 5);

		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/[test/[test1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/[test/[test2.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/[test/[test3.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/[test/test.1A.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test./test.A1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test./test1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test./test2.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test./test3.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test4/test4.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test4/test5.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test4/testA1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test4/testA2.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/testA/testA3.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/testA/testAA1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/testA/testAB1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/testA/testZA1.txt")));

		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/[tes/[test1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/[tes/[test2.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/[tes/[test3.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/test.1A.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/test.A1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/test1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/test2.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/test3.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/test4.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/test5.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/testA1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/testA2.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/testA3.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/testAA1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/testAB1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/testZA1.txt")));
	}

	@Test public void testConsistency() throws IOException {
		final FolderOrganiser organiser = new FolderOrganiser(Paths.get("src/test/resources/testFiles"))
			.reload();

		organiser.organise(4, 5);

		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/[test/[test1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/[test/[test2.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/[test/[test3.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/[test/test.1A.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test./test.A1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test./test1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test./test2.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test./test3.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test4/test4.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test4/test5.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test4/testA1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test4/testA2.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/testA/testA3.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/testA/testAA1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/testA/testAB1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/testA/testZA1.txt")));

		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/[tes/[test1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/[tes/[test2.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/[tes/[test3.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/test.1A.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/test.A1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/test1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/test2.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/test3.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/test4.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/test5.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/testA1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/testA2.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/testA3.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/testAA1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/testAB1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/testZA1.txt")));

		organiser.organise(4, 5);

		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/[test/[test1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/[test/[test2.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/[test/[test3.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/[test/test.1A.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test./test.A1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test./test1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test./test2.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test./test3.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test4/test4.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test4/test5.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test4/testA1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test4/testA2.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/testA/testA3.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/testA/testAA1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/testA/testAB1.txt")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/testA/testZA1.txt")));

		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/[tes/[test1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/[tes/[test2.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/[tes/[test3.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/test.1A.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/test.A1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/test1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/test2.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/test3.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/test4.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/test5.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/testA1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/testA2.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/testA3.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/testAA1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/testAB1.txt")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test/testZA1.txt")));
	}

	@Test public void testFilenames() throws IOException {
		final FolderOrganiser organiser = new FolderOrganiser(Paths.get("src/test/resources/testFiles"))
				.reload();

		assertEquals(16, organiser.getFilenames().size());

		organiser.organise(5, 5);
		assertEquals("testAB1.txt", organiser.getFilenames().stream().skip(14).findFirst().get());

		organiser.organise(4, 5);
		assertEquals("testAB1.txt", organiser.getFilenames().stream().skip(14).findFirst().get());
	}

	@Test public void testReload() throws IOException {
		final FolderOrganiser organiser = new FolderOrganiser(Paths.get("src/test/resources/testFiles"));

		new FolderOrganiser(Paths.get("src/test/resources/testFiles"))
				.reload()
				.organise(5, 5);

		organiser.reload();
		assertEquals(Paths.get("src/test/resources/testFiles/testA/testAB1.txt"), organiser.getPath("testAB1.txt"));

		new FolderOrganiser(Paths.get("src/test/resources/testFiles"))
			.reload()
			.organise(4, 4);

		organiser.reload();
		assertEquals(Paths.get("src/test/resources/testFiles/test/testAB1.txt"), organiser.getPath("testAB1.txt"));
	}

	@Test public void testCleanUp() throws IOException {
		final FolderOrganiser organiser = new FolderOrganiser(Paths.get("src/test/resources/testFiles"))
			.reload();

		organiser.organise(3, 4);
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/[tes")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test")));

		organiser.organise(5, 5);
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/[tes")));
		assertTrue(Files.exists(Paths.get("src/test/resources/testFiles/test")));

		organiser.cleanUp();
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/[tes")));
		assertFalse(Files.exists(Paths.get("src/test/resources/testFiles/test")));
	}
}

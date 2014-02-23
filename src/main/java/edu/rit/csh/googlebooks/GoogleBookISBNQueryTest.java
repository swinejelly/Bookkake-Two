package edu.rit.csh.googlebooks;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.rit.csh.Resources;

public class GoogleBookISBNQueryTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test(expected = IllegalStateException.class)
	public void testInvalidConstructor() {
		//dash is invalid by Google Books API. Should immediately fail.
		new GoogleBookISBNQuery("978-0486295060", Resources.googleBooksApiKey);
	}
	
	@Test(expected = IllegalStateException.class)
	public void testInvalidSetDigitsOff() {
		//12 digits
		new GoogleBookISBNQuery("978486295060", Resources.googleBooksApiKey);
	}
	
	@Test(expected = IllegalStateException.class)
	public void testInvalidSetEmpty() {
		//12 digits
		new GoogleBookISBNQuery("", "");
	}
	
	@Test
	public void test() {
		GoogleBookISBNQuery qry = new GoogleBookISBNQuery("9780486295060", Resources.googleBooksApiKey);
		assertTrue(qry.getRequest().startsWith("https://www.googleapis.com/books/v1/volumes?q=isbn:9780486295060"));
	}
}

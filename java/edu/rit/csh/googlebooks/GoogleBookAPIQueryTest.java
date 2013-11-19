package edu.rit.csh.googlebooks;

import static org.junit.Assert.*;

import org.junit.Test;

public class GoogleBookAPIQueryTest {
	
	@Test
	public void testConstructorEquivalence(){
		GoogleBookAPIQuery qry1 = new GoogleBookAPIQuery();
		qry1.setTitle("Pride and Prejudice");
		qry1.setAuthor("Jane Austen");
		qry1.setAPIKey("QWERTY_B2ANANA");
		GoogleBookAPIQuery qry2 = new GoogleBookAPIQuery("Pride and Prejudice", "QWERTY_B2ANANA");
		qry2.setAuthor("Jane Austen");
		GoogleBookAPIQuery qry3 = new GoogleBookAPIQuery("Pride and Prejudice", "Jane Austen", "QWERTY_B2ANANA");
		
		String str1 = qry1.getRequest();
		String str2 = qry2.getRequest();
		String str3 = qry3.getRequest();
		
		//Assert that all Queries return identical strings.
		assertEquals(str1, str2);
		assertEquals(str2, str3);
		assertEquals(str1, str3);
	}
	
	@Test(expected = IllegalStateException.class)
	public void testNoTitleFail(){
		new GoogleBookAPIQuery().getRequest();
	}
	

	@Test
	public void testGetRequestValid() {
		GoogleBookAPIQuery qry1 = new GoogleBookAPIQuery();
		qry1.setTitle("flowers");
		assertEquals("https://www.googleapis.com/books/v1/volumes?q=flowers", qry1.getRequest());
		
		GoogleBookAPIQuery qry2 = new GoogleBookAPIQuery("flowers", "KEY_DFDA678");
		assertEquals("https://www.googleapis.com/books/v1/volumes?q=flowers&key=KEY_DFDA678",
				qry2.getRequest());
		
		GoogleBookAPIQuery qry3 = new GoogleBookAPIQuery("flowers", "keyes", "KEY_DFDA678");
		assertEquals("https://www.googleapis.com/books/v1/volumes?q=flowers+inauthor:keyes&key=KEY_DFDA678", 
				qry3.getRequest());
	}

}

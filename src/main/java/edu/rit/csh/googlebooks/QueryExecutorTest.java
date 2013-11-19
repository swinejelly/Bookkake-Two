package edu.rit.csh.googlebooks;

import static org.junit.Assert.*;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class QueryExecutorTest {

	@Test
	public void testRetrieveJSON() throws JSONException, IOException {
		APIQuery mockQry = mock(APIQuery.class);
		when(mockQry.getRequest()).thenReturn("https://www.googleapis.com/books/v1/volumes?q=flowers+inauthor:keyes");
		JSONObject jsonObj = QueryExecutor.retrieveJSON(mockQry);
		
		assertNotNull(jsonObj);
	}
	
	@Test(expected = JSONException.class)
	public void testRetrieveJSONInvalidContent() throws JSONException, IOException{
		APIQuery mockQry = mock(APIQuery.class);
		when(mockQry.getRequest()).thenReturn("https://www.google.com");
		QueryExecutor.retrieveJSON(mockQry);
	}

}

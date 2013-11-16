package edu.rit.csh.googlebooks;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Convenience class to take APIQueries and return a JSON object from 
 * the designated URL.
 * @author scott
 *
 */
public class QueryExecutor {
	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	private static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONObject json = new JSONObject(jsonText);
			return json;
		} finally {
			is.close();
		}
	}
	
	/**
	 * Queries the target of the APIQuery and attempts to parse the return value
	 * as a JSON document
	 * @param qry APIQuery
	 * @return JSONObject if Query can be executed and content can be parsed as JSON.
	 * Will never return null.
	 * @throws JSONException If content cannot be parsed as JSON.
	 * @throws IOException If a stream cannot be opened to qry's target.
	 */
	public static JSONObject retrieveJSON(APIQuery qry) throws JSONException, IOException{
		return readJsonFromUrl(qry.getRequest());
	}
}

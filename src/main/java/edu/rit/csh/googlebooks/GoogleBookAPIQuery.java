package edu.rit.csh.googlebooks;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * A partial APIQuery implementation for the Google Books API.
 * @author Scott Jordan
 */
public class GoogleBookAPIQuery implements APIQuery {
	private static String baseURL = 
			"https://www.googleapis.com/books/v1/volumes?q=";
	/**Title to search for. Required. */
	private String title;
	/**Author to filter by. Optional. */
	private String author;
	/**API Key for Google Books. Not strictly necessary, but highly recommended. */
	private String APIKey;
	
	public GoogleBookAPIQuery(){
		this.title = "";
		this.author = "";
		this.APIKey = "";
	}
	
	public GoogleBookAPIQuery(String title, String APIkey){
		this.title = title;
		this.author = "";
		this.APIKey = APIkey;
	}
	
	public GoogleBookAPIQuery(String title, String author, String APIkey){
		this.title = title;
		this.author = author;
		this.APIKey = APIkey;
	}

	@Override
	public String getRequest(){
		if (title.isEmpty()){
			throw new IllegalStateException("Title is empty");
		}else if (APIKey.isEmpty()){
			throw new IllegalStateException("No API key provided.");
		}else{
			StringBuilder urlBuilder = new StringBuilder(baseURL);
			try{
				urlBuilder.append(URLEncoder.encode(title, "UTF-8"));
				if (!author.isEmpty()){
					urlBuilder.append("+inauthor:");
					urlBuilder.append(URLEncoder.encode(author, "UTF-8"));
				}
				urlBuilder.append("&key=");
				urlBuilder.append(URLEncoder.encode(APIKey, "UTF-8"));
				return urlBuilder.toString();
			}catch (UnsupportedEncodingException e){
				throw new AssertionError("UTF-8 is unknown");
			}
		}
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getAPIKey() {
		return APIKey;
	}

	public void setAPIKey(String aPIKey) {
		APIKey = aPIKey;
	}
}

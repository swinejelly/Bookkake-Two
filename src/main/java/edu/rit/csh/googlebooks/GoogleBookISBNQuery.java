package edu.rit.csh.googlebooks;

public class GoogleBookISBNQuery implements APIQuery {
	private static String baseURL = 
			"https://www.googleapis.com/books/v1/volumes?q=isbn:";
	
	private String isbn;
	/**API key for google books*/
	private String apiKey;
	
	public GoogleBookISBNQuery(String isbn, String apiKey){
		setIsbn(isbn);
		this.apiKey = apiKey;
	}

	@Override
	public String getRequest() {
		if (apiKey.isEmpty()){
			throw new IllegalStateException("No API key provided!");
		}
		return baseURL + isbn + "&key=" + apiKey;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		if (!isbn.matches("^(97(8|9))?\\d{9}(\\d|X)$")){
			throw new IllegalStateException("Invalid ISBN!: " + isbn);
		}else{
			this.isbn = isbn;
		}
	}

}

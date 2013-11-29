package edu.rit.csh.googlebooks;

public class GoogleBookISBNQuery implements APIQuery {
	private static String baseURL = 
			"https://www.googleapis.com/books/v1/volumes?q=isbn:";
	
	private String isbn;
	
	public GoogleBookISBNQuery(String isbn){
		setIsbn(isbn);
	}

	@Override
	public String getRequest() {
		return baseURL + isbn;
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

package edu.rit.csh.models;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.rit.csh.Resources;
import edu.rit.csh.WicketApplication;
import edu.rit.csh.googlebooks.GoogleBookAPIQuery;
import edu.rit.csh.googlebooks.GoogleBookISBNQuery;
import edu.rit.csh.googlebooks.QueryExecutor;

@Entity
@Table(name="BOOKINFOS")
public class BookInfo implements Serializable{
	private static final long serialVersionUID = 1L;
	private String isbn = "";
	private String title = "";
	private String publisher = "";
	private String description = "";
	private String thumbnailURL = "";
	private String authors = "";
	private List<Book> books;
	private BookRequest bookRequest;
	
	private BookInfo(){}
	
	public BookInfo(String retrievalIsbn, JSONObject obj){
		title = obj.optString("title");
		publisher = obj.optString("publisher");
		description = obj.optString("description");
		//find a thumbnail
		JSONObject thumbnails = obj.optJSONObject("imageLinks");
		thumbnailURL = thumbnails == null ? "" : thumbnails.optString("thumbnail");
		//Construct Authors
		StringBuilder sb = new StringBuilder(64);
		JSONArray authorsJSON = obj.optJSONArray("authors");
		if (authorsJSON != null){
			for (int i = 0; i < authorsJSON.length(); i++){
				String s = authorsJSON.getString(i);
				sb.append(s);
				if (i < authorsJSON.length()-1){
					sb.append(", ");
				}
			}
		}
		authors = sb.toString();
		
		if (retrievalIsbn == null){
			//get isbn
			JSONArray isbns = obj.optJSONArray("industryIdentifiers");
			if (isbns != null){
				for (int i = 0; i < isbns.length(); i++){
					JSONObject isbnObj = isbns.getJSONObject(i);
					if ("ISBN_13".equals(isbnObj.getString("type"))){
						isbn = isbnObj.getString("identifier");
						break;
					}
				}
			}
		}else{
			isbn = retrievalIsbn;
		}
	}
	
	/**
	 * Return the BookInfo for a given ISBN transparently,
	 * first checking the database for a cached entry and otherwise
	 * querying Google Books.
	 * @param isbn isbn10 or 13 of a book.
	 * @return the BookInfo if retrievable, else null.
	 */
	public static BookInfo getBookInfo(String isbn){
		Session sess = Resources.sessionFactory.openSession();
		sess.beginTransaction();
		//Get the BookInfo from the database, if that fails try to get it
		//from google books API and persist it.
		BookInfo info = (BookInfo) sess.get(BookInfo.class, isbn);
		if (info == null){
			GoogleBookISBNQuery qry = new GoogleBookISBNQuery(isbn, Resources.googleBooksApiKey);
			try {
				JSONObject obj = QueryExecutor.retrieveJSON(qry);
				//the relevant information is in items[0].volumeInfo in JSON
				JSONArray items = obj.optJSONArray("items");
				if (items != null){
					JSONObject bookJSON = items.getJSONObject(0);
					if (bookJSON != null){
						JSONObject volumeInfo = bookJSON.getJSONObject("volumeInfo");
						if (volumeInfo != null){
							info = new BookInfo(isbn, volumeInfo);
						}
					}
				}
			} catch (JSONException | IOException e) {
				e.printStackTrace();
			}
		}
		
		if (info != null){
			sess.save(info);
		}
		
		sess.getTransaction().commit();
		sess.close();
		return info;
	}
	
	public static List<BookInfo> searchBooks(String title, String author, int cap){
		List<BookInfo> books = new ArrayList<BookInfo>();
		
		GoogleBookAPIQuery qry = new GoogleBookAPIQuery();
		qry.setAPIKey(Resources.googleBooksApiKey);
		qry.setTitle(title);
		if (author != null && !author.isEmpty()){
			qry.setAuthor(author);
		}
		JSONObject json = null;
		try{
			json = QueryExecutor.retrieveJSON(qry);
		}catch (IOException | JSONException e){
			e.printStackTrace();
		}
		if (json != null){
			JSONArray bookObjects = json.optJSONArray("items");
			if (bookObjects != null){
				cap = Math.min(cap, bookObjects.length());
				for (int i = 0; i < cap; i++){
					JSONObject bookJSON = bookObjects.getJSONObject(i).getJSONObject("volumeInfo");
					BookInfo info = new BookInfo(null, bookJSON);
					if (!info.isbn.isEmpty()){
						books.add(info);
					}else{
						cap = Math.min(cap + 1, bookObjects.length());
					}
				}
			}
		}
		return books;
	}
	
	public static List<BookInfo> getAllBookInfos(){
		Session sess = Resources.sessionFactory.openSession();
		sess.beginTransaction();
		@SuppressWarnings("unchecked")
		List<BookInfo> books = sess.createCriteria(BookInfo.class).list();
		sess.close();
		return books;
	}
	
	@Id
	public String getIsbn() {
		return isbn;
	}
	
	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPublisher() {
		return publisher;
	}
	
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	
	@Column(columnDefinition="TEXT")
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getThumbnailURL() {
		return thumbnailURL;
	}
	
	public void setThumbnailURL(String thumbnailURL) {
		this.thumbnailURL = thumbnailURL;
	}
	
	public String getAuthors() {
		return authors;
	}
	
	public void setAuthors(String authors) {
		this.authors = authors;
	}

	@OneToMany(mappedBy = "bookInfo", fetch = FetchType.EAGER)
	public List<Book> getBooks() {
		return books;
	}

	public void setBooks(List<Book> books) {
		this.books = books;
	}
	
	@OneToOne(optional = true, mappedBy="bookInfo")
	public BookRequest getBookRequest() {
		return bookRequest;
	}

	public void setBookRequest(BookRequest bookRequest) {
		this.bookRequest = bookRequest;
	}

	@Override
	public boolean equals(Object o){
		if (o instanceof BookInfo && ((BookInfo)o).getIsbn().equals(isbn)){
			return true;
		}else{
			return false;
		}
	}
}

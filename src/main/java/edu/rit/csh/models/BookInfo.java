package edu.rit.csh.models;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.rit.csh.WicketApplication;
import edu.rit.csh.googlebooks.GoogleBookAPIQuery;
import edu.rit.csh.googlebooks.GoogleBookISBNQuery;
import edu.rit.csh.googlebooks.QueryExecutor;

@Entity
@Table(name="BOOKINFOS")
public class BookInfo implements Serializable{
	private static final long serialVersionUID = -271644167475844819L;
	private String isbn = "";
	private String title = "";
	private String publisher = "";
	private String description = "";
	private String thumbnailURL = "";
	private String authors = "";
	
	private BookInfo(){}
	
	public BookInfo(JSONObject obj){
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
	}
	
	/**
	 * Return the BookInfo for a given ISBN transparently,
	 * first checking the database for a cached entry and otherwise
	 * querying Google Books.
	 * @param isbn isbn10 or 13 of a book.
	 * @return the BookInfo if retrievable, else null.
	 */
	public static BookInfo getBookInfo(String isbn){
		Session sess = WicketApplication.getSessionFactory().openSession();
		sess.beginTransaction();
		BookInfo b = getBookInfo(sess, isbn);
		sess.getTransaction().commit();
		sess.close();
		return b;
	}
	
	/**
	 * Return the BookInfo for a given ISBN transparently,
	 * first checking the database for a cached entry and otherwise
	 * querying Google Books.
	 * @param sess a hibernate session, which will not be closed or
	 * committed.
	 * @param isbn isbn10 or 13 of a book.
	 * @return the BookInfo if retrievable, else null.
	 */
	public static BookInfo getBookInfo(Session sess, String isbn){
		BookInfo info = (BookInfo) sess.get(BookInfo.class, isbn);
		if (info != null){
			return info;
		}else{
			GoogleBookISBNQuery qry = new GoogleBookISBNQuery(isbn);
			try {
				JSONObject obj = QueryExecutor.retrieveJSON(qry);
				//the relevant information is in items[0].volumeInfo in JSON
				JSONArray items = obj.optJSONArray("items");
				if (items != null){
					JSONObject bookJSON = items.getJSONObject(0);
					if (bookJSON != null){
						JSONObject volumeInfo = bookJSON.getJSONObject("volumeInfo");
						if (volumeInfo != null){
							info = new BookInfo(volumeInfo);
							if (info != null){
								sess.save(info);
								return info;
							}
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	
	public static List<BookInfo> searchBooks(String title, String author, int cap){
		Session sess = WicketApplication.getSessionFactory().openSession();
		sess.beginTransaction();
		List<BookInfo> bookInfos = searchBooks(sess, title, author, cap);
		sess.getTransaction().commit();
		sess.close();
		return bookInfos;
	}
	
	/**
	 * Search google's public Book API and return cap number of results.
	 * @param sess hibernate session which will not be closed nor committed
	 * @param title title search query. Required.
	 * @param author author search query. Optional. (null or "")
	 * @param cap maximum number of books to return.
	 * @return
	 */
	public static List<BookInfo> searchBooks(Session sess, String title, String author, int cap){
		List<BookInfo> books = new ArrayList<BookInfo>();
		
		GoogleBookAPIQuery qry = new GoogleBookAPIQuery();
		qry.setTitle(title);
		if (author != null){
			qry.setAuthor(author);
		}
		JSONObject json;
		try{
			json = QueryExecutor.retrieveJSON(qry);
		}catch (IOException e){
			e.printStackTrace();
			return books;
		}catch (JSONException e){
			e.printStackTrace();
			return books;
		}
		
		JSONArray bookObjects = json.optJSONArray("items");
		if (bookObjects != null){
			cap = Math.min(cap, bookObjects.length());
			for (int i = 0; i < cap; i++){
				JSONObject bookJSON = bookObjects.getJSONObject(i).getJSONObject("volumeInfo");
				BookInfo info = new BookInfo(bookJSON);
				if (!info.isbn.isEmpty()){
					books.add(info);
				}
			}
		}
		
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
}

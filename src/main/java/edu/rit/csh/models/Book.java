package edu.rit.csh.models;

import java.util.HashMap;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.wicket.protocol.http.WebApplication;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.annotations.GenericGenerator;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.rit.csh.WicketApplication;

@Entity
@Table(name = "BOOKS")
public class Book {
	private Long id;
	
	private String isbn;
	
	private String ownerUID;
	
	private boolean active = true;
	
	public Book(){
		
	}
	
	public Book(String isbn, String uid){
		this.setIsbn(isbn);
		this.setOwnerUID(uid);
	}
	
	public static Book getBook(String isbn, String ownerUID){
		SessionFactory fact = WicketApplication.getSessionFactory();
		Session sess = fact.openSession();
		sess.beginTransaction();
		Book b = getBook(sess, isbn, ownerUID);
		sess.close();
		return b;
	}
	
	/**
	 * Get the book with the given isbn belonging to the user identified
	 * by ownerUID
	 * @param sess an open hibernate session with a transaction started. 
	 * Caller assumes responsibility for closing and committing.
	 * @param isbn isbn of the book. must be an exact match.
	 * @return A book if found, else null.
	 */
	public static Book getBook(Session sess, String isbn, String ownerUID){
		Query qry = sess.createQuery("from Book where isbn = :isbn and ownerUID = :uid");
		qry.setParameter("isbn", isbn);
		qry.setParameter("uid", ownerUID);
		Book b = (Book) qry.uniqueResult();
		return b;
	}

	/**
	 * Creates a book using a session from the WicketApplication's
	 * SessionFactory. Requires the app to be running.
	 * @param sess Hibernate Session
	 * @param isbn ISBN code
	 * @param ownerUID LDAP UID of the user.
	 */
	public static Book createBook(String isbn, String ownerUID){
		SessionFactory fact = WicketApplication.getSessionFactory();
		Session sess = fact.openSession();
		sess.beginTransaction();
		Book b = createBook(sess, isbn, ownerUID);
		sess.getTransaction().commit();
		sess.close();
		return b;
	}
	
	/**
	 * Creates a book using the given (open) session.
	 * Does not close or commit the transaction.
	 * @param sess Hibernate Session
	 * @param isbn ISBN code
	 * @param ownerUID LDAP UID of the user.
	 */
	public static Book createBook(Session sess, String isbn, String ownerUID){
		Book b = new Book(isbn, ownerUID);
		sess.save(b);
		return b;
	}
	
	/**
	 * Returns all books returned by the user with UIDnumber ownerUID.
	 * App must be running.
	 * @param ownerUID UIDnumber of the user.
	 * @return list of all books owned (regardless of possession) by the user.
	 */
	public static List<Book> getOwnedBooks(String ownerUID){
		SessionFactory fact = WicketApplication.getSessionFactory();
		Session sess = fact.openSession();
		sess.beginTransaction();
		List<Book> ownedBooks = getOwnedBooks(sess, ownerUID);
		sess.getTransaction().commit();
		sess.close();
		return ownedBooks;
	}
	
	/**
	 * Returns all books returned by the user with UIDnumber ownerUID.
	 * @param sess An opened session, which the caller assumes responsibility
	 * for closing.
	 * @param ownerUID UIDnumber of the user.
	 * @return list of all books owned (regardless of possession) by the user.
	 */
	public static List<Book> getOwnedBooks(Session sess, String ownerUID){
		Query qry = sess.createQuery("from Book where ownerUID = :uid and active = true");
		qry.setParameter("uid", ownerUID);
		List<Book> ownedBooks = qry.list();
		return ownedBooks;
	}
	
	public void delete(){
		Session sess = WicketApplication.getSessionFactory().openSession();
		sess.beginTransaction();
		sess.update(this);
		delete(sess);
		sess.getTransaction().commit();
		sess.close();
	}
	
	public void delete(Session sess){
		setActive(false);
	}
	
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy="increment")
	public Long getId(){
		return id;
	}
	
	@SuppressWarnings("unused")
	private void setId(Long id){
		this.id = id;
	}

	public String getIsbn() {
		return isbn;
	}

	private void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public String getOwnerUID() {
		return ownerUID;
	}

	private void setOwnerUID(String ownerUID) {
		this.ownerUID = ownerUID;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public static HashMap<String, String> buildBookModel(JSONObject obj){
		HashMap<String, String> model = new HashMap<String, String>();
		model.put("title", obj.optString("title"));
		model.put("publisher", obj.optString("publisher"));
		//description
		String description = obj.optString("description");
		StringBuilder sb;
		if (description.length() > 600){
			sb = new StringBuilder(600);
			sb.append(description.substring(0, 597));
			sb.append("...");
			description = sb.toString();
		}
		model.put("description", description);
		JSONObject thumbnails = obj.optJSONObject("imageLinks");
		String thumbnailUrl = thumbnails == null ? "" : thumbnails.optString("thumbnail", "");
		model.put("thumbnailUrl", thumbnailUrl);
		//Construct Authors
		sb = new StringBuilder(64);
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
		model.put("authors", sb.toString());
		
		//get isbn
		JSONArray isbns = obj.optJSONArray("industryIdentifiers");
		if (isbns != null){
			for (int i = 0; i < isbns.length(); i++){
				JSONObject isbn = isbns.getJSONObject(i);
				if ("ISBN_13".equals(isbn.getString("type"))){
					model.put("ISBN_13", isbn.getString("identifier"));
					break;
				}
			}
		}
		return model;
	}
}

package edu.rit.csh.models;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.annotations.GenericGenerator;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.rit.csh.WicketApplication;
import edu.rit.csh.auth.LDAPUser;

@Entity
@Table(name = "BOOKS")
public class Book implements Serializable{
	private static final long serialVersionUID = -8012947208250080965L;
	
	private static SessionFactory sessFact;
	public static void setSessFact(SessionFactory fact){
		sessFact = fact;
	}

	private Long id;
	
	private String isbn;
	
	private String ownerUID;
	
	private BorrowPeriod borrowPeriod;
	
	private boolean active = true;
	
	public Book(){
		
	}
	
	public Book(String isbn, String uid){
		this.setIsbn(isbn);
		this.setOwnerUID(uid);
	}
	
	/**
	 * Get the book with the given isbn belonging to the user identified
	 * by ownerUID. Returns inactive ("deleted") books too, but these
	 * can be differentiated by the active field.
	 * @param isbn isbn of the book. must be an exact match.
	 * @return A book if found, else null.
	 */
	public static Book getBook(String isbn, String ownerUID){
		Session sess = sessFact.openSession();
		sess.beginTransaction();
		Query qry = sess.createQuery("from Book where isbn = :isbn and ownerUID = :uid");
		qry.setParameter("isbn", isbn);
		qry.setParameter("uid", ownerUID);
		Book b = (Book) qry.uniqueResult();
		sess.getTransaction().commit();
		sess.close();
		return b;
	}

	/**
	 * Creates a book.
	 * @param isbn ISBN code
	 * @param ownerUID LDAP UID of the user.
	 */
	public static Book createBook(String isbn, String ownerUID){
		Session sess = sessFact.openSession();
		sess.beginTransaction();
		Book b = new Book(isbn, ownerUID);
		sess.save(b);
		sess.getTransaction().commit();
		sess.close();
		return b;
	}
	
	/**
	 * Returns all books returned by the user with UIDnumber ownerUID.
	 * App must be running.
	 * @param ownerUID UIDnumber of the user.
	 * @return list of all books owned (regardless of possession) by the user.
	 */
	public static List<Book> getOwnedBooks(String ownerUID){
		Session sess = sessFact.openSession();
		sess.beginTransaction();
		Query qry = sess.createQuery("from Book where ownerUID = :uid and active = true");
		qry.setParameter("uid", ownerUID);
		List<Book> ownedBooks = qry.list();
		sess.getTransaction().commit();
		sess.close();
		return ownedBooks;
	}
	
	/**
	 * Return all books belonging to the user possessorUID or that are 
	 * currently being borrowed by possessorUID.
	 * @return list of all books possessed by user.
	 */
	public static List<Book> getPossessedBooks(String possessorUID){
		return getPossessedBooks(possessorUID, Calendar.getInstance());
	}
	
	/**
	 * Return all books belonging to the user possessorUID or that are 
	 * being borrowed by possessorUID at time when.
	 * @return list of all books possessed by user.
	 */
	public static List<Book> getPossessedBooks(String possessorUID, Calendar when){
		Session sess = sessFact.openSession();
		sess.beginTransaction();
		//List<Book> possessedBooks = getPossessedBooks(sess, when, possessorUID);
		Query qry = sess.createQuery("from Book where (ownerUID = :uid or borrowPeriod != null) and active = true");
		qry.setParameter("uid", possessorUID);
		List<Book> possessedBooks = qry.list();
		Iterator<Book> iter = possessedBooks.iterator();
		while (iter.hasNext()){
			Book b = iter.next();
			if (b.getOwnerUID().equals(possessorUID) && b.borrowPeriod == null){
				//Book belongs to possessorUID and has no associated BorrowPeriod
				continue;
			}else if (b.getOwnerUID().equals(possessorUID) && b.borrowPeriod != null){
				//if the book has an associated BorrowPeriod we need to verify that
				//the book is not currently lent out.
				int beginComp = b.borrowPeriod.getBegin().compareTo(when);
				int endComp = b.borrowPeriod.getEnd().compareTo(when);
				if ((beginComp <= 0) && (endComp >= 0)){
					iter.remove();
				}
			}else if (!b.getOwnerUID().equals(possessorUID) && b.borrowPeriod.getBorrowerUID().equals(possessorUID)){
				//if the book does not belong to possessorUID (implying it has a BorrowPeriod)
				//then we should exclude it unless it coincides with b.borrowPeriod
				int beginComp = b.borrowPeriod.getBegin().compareTo(when);
				int endComp = b.borrowPeriod.getEnd().compareTo(when);
				if (!(beginComp <= 0) || !(endComp >= 0)){
					iter.remove();
				}
			}else{
				//book neither belongs to possessorUID nor has a BorrowPeriod by possessorUID
				iter.remove();
			}
		}
		sess.getTransaction().commit();
		sess.close();
		return possessedBooks;
	}
	
	/**
	 * @param isbn the isbn of the book (doesn't convert between isbn10/13)
	 * @return all active books that have isbn isbn
	 */
	public static List<Book> getBooksByIsbn(String isbn){
		Session sess = sessFact.openSession();
		sess.beginTransaction();
		Query qry = sess.createQuery("from Book where isbn = :isbn and active = true");
		qry.setParameter("isbn", isbn);
		List<Book> books = qry.list();
		sess.getTransaction().commit();
		sess.close();
		return books;
	}
	
	/**
	 * Makes this book inactive so that it won't show up anymore
	 * in the application.
	 */
	public void delete(){
		Session sess = sessFact.openSession();
		sess.beginTransaction();
		sess.update(this);
		setActive(false);
		sess.getTransaction().commit();
		sess.close();
	}
	
	/**
	 * borrowerUID borrows a book from begin to end.
	 */
	public void borrow(String borrowerUID, Calendar begin, Calendar end){
		Session sess = sessFact.openSession();
		sess.beginTransaction();
		sess.update(this);
		borrowPeriod = new BorrowPeriod();
		borrowPeriod.setBorrowerUID(borrowerUID);
		borrowPeriod.setBegin(begin);
		borrowPeriod.setEnd(end);
		borrowPeriod.setBook(this);
		sess.save(borrowPeriod);
		sess.getTransaction().commit();
		sess.close();
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
	
	@OneToOne(optional = true)
	public BorrowPeriod getBorrowPeriod() {
		return borrowPeriod;
	}

	private void setBorrowPeriod(BorrowPeriod borrowPeriod) {
		this.borrowPeriod = borrowPeriod;
	}

	/**
	 * Automatically gets the owner using a LDAP connection
	 * to CSH's LDAP server.
	 * @return the LDAPUser if successful, else null.
	 */
	@Transient
	public LDAPUser getOwner(){
		try {
			return WicketApplication.getWicketApplication().getLDAPProxy().getUser(ownerUID);
		} catch (LdapException | IOException | CursorException e) {
			return null;
		}
	}
	
	/**
	 * Set the new owner of this book to newOwner
	 */
	public void setOwner(LDAPUser newOwner){
		ownerUID = newOwner.getUidnumber();
	}
	
	/**
	 * Returns the possessor of the Book on date using a connection to
	 * CSH's LDAP server.
	 * @return the possessing LDAPUser if successful, else null
	 */
	@Transient
	public LDAPUser getPossessor(Calendar date){
		try{
			if (borrowPeriod == null){
				return getOwner();
			}else if (borrowPeriod.getBegin().compareTo(date) <= 0 &&
				borrowPeriod.getEnd().compareTo(date) >= 0){
				return WicketApplication.getWicketApplication().getLDAPProxy().getUser(ownerUID);
			}else{
				return getOwner();
			}
		} catch (LdapException | IOException | CursorException e) {
			return null;
		}
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

package edu.rit.csh.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.wicket.protocol.http.WebApplication;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.annotations.GenericGenerator;

import edu.rit.csh.WicketApplication;

@Entity
@Table(name = "BOOKS")
public class Book {
	private Long id;
	
	private String isbn;
	
	private String ownerUID;
	
	public Book(){
		
	}
	
	public Book(String isbn, String uid){
		this.setIsbn(isbn);
		this.setOwnerUID(uid);
	}
	
	/**
	 * Creates a book using a session from the WicketApplication's
	 * SessionFactory. Requires the app to be running.
	 * @param sess Hibernate Session
	 * @param isbn ISBN code
	 * @param ownerUID LDAP UID of the user.
	 */
	public static Book createBook(String isbn, String ownerUID){
		WicketApplication app = (WicketApplication)WebApplication.get();
		SessionFactory fact = app.getSessionFactory();
		Session sess = fact.openSession();
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
}

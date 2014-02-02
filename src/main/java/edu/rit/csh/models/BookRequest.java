package edu.rit.csh.models;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "BOOKREQUESTS")
/**
 * Entity representing a general request to borrow a book.
 * Anyone who has the book on the website can fulfill it automatically
 * by clicking on the request.
 * @author Scott Jordan
 *
 */
public class BookRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	private static SessionFactory sessFact;
	public static void setSessFact(SessionFactory fact){
		sessFact = fact;
	}
	private Long id;
	/**Uidnumber of the user*/
	private String requesterUID;
	/**Reference to what book is being requested.*/
	private BookInfo bookInfo;
	/**When the user will no longer need the book*/
	private Calendar end;
	
	public BookRequest(){
		
	}
	
	public BookRequest(String isbn, String requesterUID, Calendar end){
		setBookInfo(BookInfo.getBookInfo(isbn));
		this.setRequesterUID(requesterUID);
		setEnd(end);
	}
	
	public static BookRequest createBookRequest(String isbn, String requesterUID, Calendar end){
		Session sess = sessFact.openSession();
		sess.beginTransaction();
		BookRequest br = new BookRequest(isbn, requesterUID, end);
		sess.save(br);
		sess.getTransaction().commit();
		sess.close();
		return br;
	}
	
	@SuppressWarnings("unchecked")
	public static List<BookRequest> allBookRequests(){
		Session sess = sessFact.openSession();
		sess.beginTransaction();
		Query q = sess.createQuery("from BookRequest");
		List<BookRequest> requests = (List<BookRequest>)q.list(); 
		sess.close();
		return requests;
	}

	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy="increment")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRequesterUID() {
		return requesterUID;
	}

	public void setRequesterUID(String uidnumber) {
		this.requesterUID = uidnumber;
	}

	@OneToOne(optional = false, cascade = {CascadeType.PERSIST})
	public BookInfo getBookInfo() {
		return bookInfo;
	}

	public void setBookInfo(BookInfo bookInfo) {
		this.bookInfo = bookInfo;
	}

	public Calendar getEnd() {
		return end;
	}

	public void setEnd(Calendar end) {
		this.end = end;
	}
}

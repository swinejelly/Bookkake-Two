package edu.rit.csh.models;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.CallbackException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.classic.Lifecycle;

import edu.rit.csh.Resources;
import edu.rit.csh.auth.LDAPUser;

@Entity
@Table(name = "BOOKREQUESTS")
/**
 * Entity representing a general request to borrow a book.
 * Anyone who has the book on the website can fulfill it automatically
 * by clicking on the request.
 * @author Scott Jordan
 *
 */
public class BookRequest implements Serializable, Lifecycle {
	private static final long serialVersionUID = 1L;
	private Long id;
	/**entryUUID of the user*/
	private String requesterEntryUUID;
	/**Actual LDAPUser that made the request*/
	@Transient
	public LDAPUser requester;
	/**Reference to what book is being requested.*/
	private BookInfo bookInfo;
	/**When the user will no longer need the book*/
	private Calendar end;
	
	public BookRequest(){
		
	}
	
	public BookRequest(String isbn, String requesterEntryUUID, Calendar end){
		setBookInfo(BookInfo.getBookInfo(isbn));
		this.setRequesterEntryUUID(requesterEntryUUID);
		setEnd(end);
	}
	
	public static BookRequest createBookRequest(String isbn, String requesterEntryUUID, Calendar end){
		Session sess = Resources.sessionFactory.openSession();
		sess.beginTransaction();
		BookRequest br = new BookRequest(isbn, requesterEntryUUID, end);
		sess.save(br);
		sess.getTransaction().commit();
		sess.close();
		return br;
	}
	
	@SuppressWarnings("unchecked")
	public static List<BookRequest> allBookRequests(){
		Session sess = Resources.sessionFactory.openSession();
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

	public String getRequesterEntryUUID() {
		return requesterEntryUUID;
	}

	public void setRequesterEntryUUID(String entryUUID) {
		this.requesterEntryUUID = entryUUID;
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
	

	public boolean onSave(Session s) throws CallbackException {
		return false;
	}

	@Override
	public boolean onUpdate(Session s) throws CallbackException {
		return false;
	}

	@Override
	public boolean onDelete(Session s) throws CallbackException {
		return false;
	}

	@Override
	public void onLoad(Session s, Serializable id) {
		Callable<LDAPUser> future = new Callable<LDAPUser>(){
			public LDAPUser call() throws Exception {
				return Resources.ldapProxy.getUser(requesterEntryUUID);
			}
		};
		try {
			requester = Resources.threadExecutor.submit(future).get(2, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();
		}
	}
}

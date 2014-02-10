package edu.rit.csh.models;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.rit.csh.Resources;

public class BookRequestTest {	
	
	@After
	public void cleanTest(){
		Session sess = Resources.sessionFactory.openSession();
		sess.beginTransaction();
		Query qry = sess.createQuery("delete from Book");
		qry.executeUpdate();
		Query qry2 = sess.createQuery("delete from BorrowPeriod");
		qry2.executeUpdate();
		Query qry3 = sess.createQuery("delete from BookRequest");
		qry3.executeUpdate();
		Query qry4 = sess.createQuery("delete from BookInfo");
		qry4.executeUpdate();
		sess.getTransaction().commit();
		sess.close();
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testBookRequestString() {
		//Wealth of Nations
		BookRequest.createBookRequest("9780857081087", "10412", Calendar.getInstance());
		//War of the Worlds
		BookRequest.createBookRequest("9781604502442", "10413", Calendar.getInstance());
		
		Session session = Resources.sessionFactory.openSession();
		session.beginTransaction();
		@SuppressWarnings("unchecked")
		List<BookRequest> results = session.createQuery("from BookRequest").list();
		assertEquals(2, results.size());
		for (BookRequest b: results){
			System.out.printf("Request %d for %s\n", b.getId(), b.getBookInfo().getTitle());
		}
		session.getTransaction().commit();
		session.close();
	}

}

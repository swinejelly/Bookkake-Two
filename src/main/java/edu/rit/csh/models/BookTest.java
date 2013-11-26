package edu.rit.csh.models;

import static org.junit.Assert.*;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class BookTest {
	private static SessionFactory sessFact;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sessFact = new Configuration().configure().buildSessionFactory();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testPersistence() {
		//Wealth of Nations
		
		
		//War of the Worlds
		
		Session session = sessFact.openSession();
		session.beginTransaction();
		Book.createBook(session, "0486295060", "5678");
		Book.createBook(session, "1604598913", "1234");
		session.getTransaction().commit();
		session.close();
		
		session = sessFact.openSession();
		session.beginTransaction();
		List<Book> results = session.createQuery("from Book").list();
		assertEquals(2, results.size());
		for (Book b: results){
			System.out.printf("%d:%s belongs to %s\n", b.getId(), b.getIsbn(), b.getOwnerUID());
		}
	}

}

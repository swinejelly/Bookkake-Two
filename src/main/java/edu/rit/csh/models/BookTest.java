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
		Book b1 = new Book("1604598913", "1234");
		Book b2 = new Book("0486295060", "5678");
		
		Session session = sessFact.openSession();
		
		session.beginTransaction();
		session.save(b1);
		session.save(b2);
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

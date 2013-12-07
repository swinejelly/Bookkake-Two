package edu.rit.csh.models;

import static org.junit.Assert.*;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.After;
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
		sessFact.close();
	}
	
	@After
	public void cleanTest(){
		Session sess = sessFact.openSession();
		sess.beginTransaction();
		Query qry = sess.createQuery("delete from Book");
		qry.executeUpdate();
		sess.getTransaction().commit();
		sess.close();
	}

	@Test
	public void testPersistence() {
		Session session = sessFact.openSession();
		session.beginTransaction();
		//Wealth of Nations
		Book.createBook(session, "0486295060", "5678");
		//War of the Worlds
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

	@Test
	public void testGetOwnedBooks(){
		Session session = sessFact.openSession();
		session.beginTransaction();
		Book.createBook(session, "0486295060", "5678");
		Book.createBook(session, "1604598913", "1234");
		Book.createBook(session, "9780807208847", "5678");
		Book.createBook(session, "9780142409848", "4321");
		session.getTransaction().commit();
		session.close();
		
		session = sessFact.openSession();
		session.beginTransaction();
		List<Book> results1234 = Book.getOwnedBooks(session, "1234");
		List<Book> results5678 = Book.getOwnedBooks(session, "5678");
		List<Book> results4321 = Book.getOwnedBooks(session, "4321");
		//should return empty list.
		List<Book> results9999 = Book.getOwnedBooks(session, "9999");
		assertNotNull(results1234);
		assertNotNull(results5678);
		assertNotNull(results4321);
		assertNotNull(results9999);
		
		assertEquals(1, results1234.size());
		assertEquals(2, results5678.size());
		assertEquals(1, results4321.size());
		assertEquals(0, results9999.size());
	}
	
	/**
	 * Verify that getOwnedBooks doesn't return any deleted books.
	 */
	@Test
	public void testGetOwnedBooksDeleted(){
		Session createSess = sessFact.openSession();
		createSess.beginTransaction();
		Book.createBook(createSess, "9780807208847", "1234");
		Book.createBook(createSess, "9780142409848", "1234").delete(createSess);
		Book.createBook(createSess, "0486295060", "1234");
		createSess.getTransaction().commit();
		createSess.close();
		
		Session testSess = sessFact.openSession();
		testSess.beginTransaction();
		List<Book> books = Book.getOwnedBooks(testSess, "1234");
		assertEquals(2, books.size());
	}
	
	/**
	 * Verify that deleting books works.
	 */
	@Test
	public void testDelete(){
		//create book
		String isbn = "9780807208847";
		Session createSess = sessFact.openSession();
		createSess.beginTransaction();
		Book.createBook(createSess, isbn, "1234");
		createSess.getTransaction().commit();
		createSess.close();
		//get book and "delete" it
		Session deleteSess = sessFact.openSession();
		deleteSess.beginTransaction();
		Query deleteQuery = deleteSess.createQuery("from Book where isbn = :isbn");
		deleteQuery.setString("isbn", isbn);
		Book deleteBook = (Book)deleteQuery.uniqueResult();
		deleteBook.delete(deleteSess);
		deleteSess.getTransaction().commit();
		deleteSess.close();
		//get book again and check that it's "deleted"
		Session testSess = sessFact.openSession();
		testSess.beginTransaction();
		Query testQry = testSess.createQuery("from Book where isbn = :isbn");
		testQry.setString("isbn", isbn);
		Book testBook = (Book)testQry.uniqueResult();
		assertFalse(testBook.isActive());
	}
	
	/**
	 * Verify that deleting books works when done in the same session the
	 * book is created in.
	 */
	@Test
	public void testDelete2(){
		//create book
		String isbn = "9780807208847";
		Session createSess = sessFact.openSession();
		createSess.beginTransaction();
		Book b = Book.createBook(createSess, isbn, "1234");
		b.delete(createSess);
		createSess.getTransaction().commit();
		createSess.close();
		//get book again and check that it's "deleted"
		Session testSess = sessFact.openSession();
		testSess.beginTransaction();
		Query testQry = testSess.createQuery("from Book where isbn = :isbn");
		testQry.setString("isbn", isbn);
		Book testBook = (Book)testQry.uniqueResult();
		assertFalse(testBook.isActive());
	}
	
	/**
	 * Test that the get method returns books when properly identified.
	 */
	@Test
	public void testGet(){
		String isbn = "9780807208847";
		String ownerUID = "1234";
		Session createSess = sessFact.openSession();
		createSess.beginTransaction();
		Book b = Book.createBook(createSess, isbn, ownerUID);
		b.delete(createSess);
		createSess.getTransaction().commit();
		createSess.close();
		
		Session testSess = sessFact.openSession();
		testSess.beginTransaction();
		Book bTest = Book.getBook(testSess, isbn, ownerUID);
		assertNotNull(bTest);
		assertEquals(isbn, bTest.getIsbn());
		assertEquals(ownerUID, bTest.getOwnerUID());
	}
	
	/**
	 * Test getBooksByIsbn
	 */
	@Test
	public void testGetBooksByIsbn(){
		String isbns[] = {"9780807208847", "0486295060", "0486295060"};
		String uids[]  = {"1234",          "5678",          "1234"};
		Session createSess = sessFact.openSession();
		createSess.beginTransaction();
		for (int i = 0; i < isbns.length; i++){
			Book.createBook(createSess, isbns[i], uids[i]);
		}
		createSess.getTransaction().commit();
		createSess.close();
		
		Session testSess = sessFact.openSession();
		testSess.beginTransaction();
		List<Book> books0486 = Book.getBooksByIsbn(testSess, "0486295060");
		assertEquals(2, books0486.size());
		
		List<Book> books9780 = Book.getBooksByIsbn(testSess, "9780807208847");
		assertEquals(1, books9780.size());
		
		List<Book> noBooks = Book.getBooksByIsbn(testSess, "1604598913");
		assertNotNull(noBooks);
		assertEquals(0, noBooks.size());
	}
}

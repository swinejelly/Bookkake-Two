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
import org.junit.BeforeClass;
import org.junit.Test;

import edu.rit.csh.Resources;

public class BookTest {
	
	@After
	public void cleanTest(){
		Session sess = Resources.sessionFactory.openSession();
		sess.beginTransaction();
		Query qry = sess.createQuery("delete from Book");
		qry.executeUpdate();
		Query qry2 = sess.createQuery("delete from BorrowPeriod");
		qry2.executeUpdate();
		Query qry3 = sess.createQuery("delete from BookInfo");
		qry3.executeUpdate();
		sess.getTransaction().commit();
		sess.close();
	}

	@Test
	public void testPersistence() {
		//Wealth of Nations
		Book.createBook("9780857081087", "10413");
		//War of the Worlds
		Book.createBook("9781604502442", "10412");
		
		Session session = Resources.sessionFactory.openSession();
		session.beginTransaction();
		@SuppressWarnings("unchecked")
		List<Book> results = session.createQuery("from Book").list();
		assertEquals(2, results.size());
		for (Book b: results){
			System.out.printf("%d:%s belongs to %s\n", b.getId(), b.getIsbn(), b.getOwnerUID());
		}
		session.getTransaction().commit();
		session.close();
	}
	
	@Test 
	public void testGetPossessedBooks(){
		Book.createBook("9780857081087", "10413");
		Book.createBook("143951688X", "10412");
		Book.createBook("143951688X", "10413");
		Book.createBook("9780142409848", "10414");
		
		
		List<Book> results10412 = Book.getOwnedBooks("10412");
		List<Book> results10413 = Book.getOwnedBooks("10413");
		List<Book> results10414 = Book.getOwnedBooks("10414");
		//should return empty list.
		List<Book> results9999 = Book.getOwnedBooks("9999");
		assertNotNull(results10412);
		assertNotNull(results10413);
		assertNotNull(results10414);
		assertNotNull(results9999);
		
		assertEquals(1, results10412.size());
		assertEquals(2, results10413.size());
		assertEquals(1, results10414.size());
		assertEquals(0, results9999.size());
		Book borrowBook = results10412.get(0);
		Calendar start = Calendar.getInstance(), end = Calendar.getInstance(), mid = Calendar.getInstance();
		start.setTimeInMillis(1000000000);
		end.setTimeInMillis(2000000000);
		mid.setTimeInMillis(1500000000);
		borrowBook.borrow("10413", start, end);
		List<Book> afterBorrow10412 = Book.getPossessedBooks("10412", mid);
		List<Book> afterBorrow10413 = Book.getPossessedBooks("10413", mid);
		List<Book> afterBorrow10414 = Book.getPossessedBooks("10414", mid);
		assertEquals(0, afterBorrow10412.size());
		assertEquals(3, afterBorrow10413.size());
		assertEquals(1, afterBorrow10414.size());
		
	}

	@Test
	public void testGetOwnedBooks(){
		Book.createBook("9780857081087", "10413");
		Book.createBook("143951688X", "10412");
		Book.createBook("143951688X", "10413");
		Book.createBook("9780142409848", "10414");
		
		List<Book> results10412 = Book.getOwnedBooks("10412");
		List<Book> results10413 = Book.getOwnedBooks("10413");
		List<Book> results10414 = Book.getOwnedBooks("10414");
		//should return empty list.
		List<Book> results9999 = Book.getOwnedBooks("9999");
		assertNotNull(results10412);
		assertNotNull(results10413);
		assertNotNull(results10414);
		assertNotNull(results9999);
		
		assertEquals(1, results10412.size());
		assertEquals(2, results10413.size());
		assertEquals(1, results10414.size());
		assertEquals(0, results9999.size());
	}
	
	/**
	 * Verify that getOwnedBooks doesn't return any deleted books.
	 */
	@Test
	public void testGetOwnedBooksDeleted(){
		Book.createBook("143951688X", "10412");
		Book.createBook("9780142409848", "10412").delete();
		Book.createBook("9780857081087", "10412");
		
		List<Book> books = Book.getOwnedBooks("10412");
		assertEquals(2, books.size());
	}
	
	/**
	 * Verify that deleting books works.
	 */
	@Test
	public void testDelete(){
		//create book
		String isbn = "143951688X";
		Book.createBook(isbn, "10412");
		//get book and "delete" it
		Book deleteBook = Book.getBook(isbn, "10412");
		deleteBook.delete();
		//get book again and check that it's "deleted"
		Book testBook = Book.getBook(isbn, "10412");
		assertFalse(testBook.isActive());
	}
	
	/**
	 * Test that the get method returns books when properly identified.
	 */
	@Test
	public void testGet(){
		String isbn = "143951688X";
		String ownerUID = "10412";
		Book b = Book.createBook(isbn, ownerUID);
		
		Book bTest = Book.getBook(isbn, ownerUID);
		assertNotNull(bTest);
		assertEquals(isbn, bTest.getIsbn());
		assertEquals(ownerUID, bTest.getOwnerUID());
	}
	
	/**
	 * Test getBooksByIsbn
	 */
	@Test
	public void testGetBooksByIsbn(){
		String isbns[] = {"143951688X", "9780857081087", "9780857081087"};
		String uids[]  = {"10412",          "10413",          "10412"};
		for (int i = 0; i < isbns.length; i++){
			Book.createBook(isbns[i], uids[i]);
		}
		
		List<Book> books0486 = Book.getBooksByIsbn("9780857081087");
		assertEquals(2, books0486.size());
		
		List<Book> books9780 = Book.getBooksByIsbn("143951688X");
		assertEquals(1, books9780.size());
		
		List<Book> noBooks = Book.getBooksByIsbn("1604598913");
		assertNotNull(noBooks);
		assertEquals(0, noBooks.size());
	}
	
	@Test
	public void testBorrow(){
		String isbn = "143951688X";
		String ownerUID = "10412";
		Book b = Book.createBook(isbn, ownerUID);
		b.delete();
		
		Book b1 = Book.getBook(isbn, ownerUID);
		Calendar begin = Calendar.getInstance();
		begin.setTimeInMillis(1000000);
		//defaults to time of running + 4 days
		Calendar end = Calendar.getInstance();
		end.setTimeInMillis(2000000);
		end.add(Calendar.DAY_OF_YEAR, 4);
		b1.borrow("10413", begin, end);
		
		assertNotNull(b1.getBorrowPeriod());
		assertNotNull(b1.getBorrowPeriod().getBook());
		assertEquals("10413", b1.getBorrowPeriod().getBorrowerUID());
		
		b1.removeBorrow();
		assertNull(b1.getBorrowPeriod());
	}
	
	@Test
	public void testGive(){
		String isbn = "143951688X";
		String ownerUID = "10412";
		Book b = Book.createBook(isbn, ownerUID);
		
		b.give("10413");
		
		Book bRetrieved = Book.getBooksByIsbn(isbn).get(0);
		assertEquals("10413", bRetrieved.getOwnerUID());
		
		Calendar today = Calendar.getInstance();
		
		Calendar future = Calendar.getInstance();
		future.add(Calendar.DATE, 5);
		
		bRetrieved.borrow("10413", today, future);
		
		bRetrieved.give("10414");
		
		Book bRetrieved2 = Book.getBooksByIsbn(isbn).get(0);
		
		assertNull(bRetrieved2.getBorrowPeriod());
	}
	
	@Test
	public void testBookInfoAssoc(){
		String isbn = "143951688X";
		String owner1 = "10412";
		String owner2 = "10413";
		Book b1 = Book.createBook(isbn, owner1);
		Book b2 = Book.createBook(isbn, owner2);
		
		BookInfo info = BookInfo.getBookInfo(isbn);
		assertNotNull(info);
		assertNotNull(info.getBooks());
		assertEquals(2, info.getBooks().size());
		assertTrue(info.getBooks().contains(b1));
		assertTrue(info.getBooks().contains(b2));
	}
}

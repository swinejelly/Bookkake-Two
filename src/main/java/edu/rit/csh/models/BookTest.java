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
	
	private static final String swinejelly = "9df26e5c-9fb5-1031-8712-6505321e93f3";
	private static final String ross       = "174da6dc-9895-1030-8c66-190500625717";
	private static final String ben        = "1c624ae8-5a2e-102f-98fa-b57270bb12b3";

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
		Book.createBook("9780857081087", ben);
		//War of the Worlds
		Book.createBook("9781604502442", swinejelly);
		
		Session session = Resources.sessionFactory.openSession();
		session.beginTransaction();
		@SuppressWarnings("unchecked")
		List<Book> results = session.createQuery("from Book").list();
		assertEquals(2, results.size());
		for (Book b: results){
			System.out.printf("%d:%s belongs to %s\n", b.getId(), b.getIsbn(), b.getOwnerEntryUUID());
		}
		session.getTransaction().commit();
		session.close();
	}
	
	@Test 
	public void testGetPossessedBooks(){
		Book.createBook("9780857081087", ben);
		Book.createBook("143951688X", swinejelly);
		Book.createBook("143951688X", ben);
		Book.createBook("9780142409848", ross);
		
		
		List<Book> resultsMe = Book.getOwnedBooks(swinejelly);
		List<Book> resultsBen = Book.getOwnedBooks(ben);
		List<Book> resultsRoss = Book.getOwnedBooks(ross);
		//should return empty list.
		List<Book> results9999 = Book.getOwnedBooks("9999");
		assertNotNull(resultsMe);
		assertNotNull(resultsBen);
		assertNotNull(resultsRoss);
		assertNotNull(results9999);
		
		assertEquals(1, resultsMe.size());
		assertEquals(2, resultsBen.size());
		assertEquals(1, resultsRoss.size());
		assertEquals(0, results9999.size());
		Book borrowBook = resultsMe.get(0);
		Calendar start = Calendar.getInstance(), end = Calendar.getInstance(), mid = Calendar.getInstance();
		start.setTimeInMillis(1000000000);
		end.setTimeInMillis(2000000000);
		mid.setTimeInMillis(1500000000);
		borrowBook.borrow(ben, start, end);
		List<Book> afterBorrowMe = Book.getPossessedBooks(swinejelly, mid);
		List<Book> afterBorrowBen = Book.getPossessedBooks(ben, mid);
		List<Book> afterBorrowRoss = Book.getPossessedBooks(ross, mid);
		assertEquals(0, afterBorrowMe.size());
		assertEquals(3, afterBorrowBen.size());
		assertEquals(1, afterBorrowRoss.size());
		
	}

	@Test
	public void testGetOwnedBooks(){
		Book.createBook("9780857081087", ben);
		Book.createBook("143951688X", swinejelly);
		Book.createBook("143951688X", ben);
		Book.createBook("9780142409848", ross);
		
		List<Book> resultsMe = Book.getOwnedBooks(swinejelly);
		List<Book> resultsBen = Book.getOwnedBooks(ben);
		List<Book> resultsRoss = Book.getOwnedBooks(ross);
		//should return empty list.
		List<Book> results9999 = Book.getOwnedBooks("9999");
		assertNotNull(resultsMe);
		assertNotNull(resultsBen);
		assertNotNull(resultsRoss);
		assertNotNull(results9999);
		
		assertEquals(1, resultsMe.size());
		assertEquals(2, resultsBen.size());
		assertEquals(1, resultsRoss.size());
		assertEquals(0, results9999.size());
	}
	
	/**
	 * Verify that getOwnedBooks doesn't return any deleted books.
	 */
	@Test
	public void testGetOwnedBooksDeleted(){
		Book.createBook("143951688X", swinejelly);
		Book.createBook("9780142409848", swinejelly).delete();
		Book.createBook("9780857081087", swinejelly);
		
		List<Book> books = Book.getOwnedBooks(swinejelly);
		assertEquals(2, books.size());
	}
	
	/**
	 * Verify that deleting books works.
	 */
	@Test
	public void testDelete(){
		//create book
		String isbn = "143951688X";
		Book.createBook(isbn, swinejelly);
		//get book and "delete" it
		Book deleteBook = Book.getBook(isbn, swinejelly);
		deleteBook.delete();
		//get book again and check that it's "deleted"
		Book testBook = Book.getBook(isbn, swinejelly);
		assertFalse(testBook.isActive());
	}
	
	/**
	 * Test that the get method returns books when properly identified.
	 */
	@Test
	public void testGet(){
		String isbn = "143951688X";
		String ownerUID = swinejelly;
		Book b = Book.createBook(isbn, ownerUID);
		
		Book bTest = Book.getBook(isbn, ownerUID);
		assertNotNull(bTest);
		assertEquals(isbn, bTest.getIsbn());
		assertEquals(ownerUID, bTest.getOwnerEntryUUID());
	}
	
	/**
	 * Test getBooksByIsbn
	 */
	@Test
	public void testGetBooksByIsbn(){
		String isbns[] = {"143951688X", "9780857081087", "9780857081087"};
		String uids[]  = {swinejelly,          ben,          swinejelly};
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
		String ownerUID = swinejelly;
		Book b = Book.createBook(isbn, ownerUID);
		b.delete();
		
		Book b1 = Book.getBook(isbn, ownerUID);
		Calendar begin = Calendar.getInstance();
		begin.setTimeInMillis(1000000);
		//defaults to time of running + 4 days
		Calendar end = Calendar.getInstance();
		end.setTimeInMillis(2000000);
		end.add(Calendar.DAY_OF_YEAR, 4);
		b1.borrow(ben, begin, end);
		
		assertNotNull(b1.getBorrowPeriod());
		assertNotNull(b1.getBorrowPeriod().getBook());
		assertEquals(ben, b1.getBorrowPeriod().getBorrowerEntryUUID());
		
		b1.removeBorrow();
		assertNull(b1.getBorrowPeriod());
	}
	
	@Test
	public void testGive(){
		String isbn = "143951688X";
		String ownerUID = swinejelly;
		Book b = Book.createBook(isbn, ownerUID);
		
		b.give(ben);
		
		Book bRetrieved = Book.getBooksByIsbn(isbn).get(0);
		assertEquals(ben, bRetrieved.getOwnerEntryUUID());
		
		Calendar today = Calendar.getInstance();
		
		Calendar future = Calendar.getInstance();
		future.add(Calendar.DATE, 5);
		
		bRetrieved.borrow(ben, today, future);
		
		bRetrieved.give(ross);
		
		Book bRetrieved2 = Book.getBooksByIsbn(isbn).get(0);
		
		assertNull(bRetrieved2.getBorrowPeriod());
	}
	
	@Test
	public void testBookInfoAssoc(){
		String isbn = "143951688X";
		String owner1 = swinejelly;
		String owner2 = ben;
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

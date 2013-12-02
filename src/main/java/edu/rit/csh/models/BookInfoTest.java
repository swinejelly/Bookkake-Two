package edu.rit.csh.models;

import static org.junit.Assert.*;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class BookInfoTest {
	private static SessionFactory sessFact;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sessFact = new Configuration().configure().buildSessionFactory();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		sessFact.close();
	}

	@Test
	public void testCaching() {
		Session sess = sessFact.openSession();
		sess.beginTransaction();
		BookInfo preInfo1 = BookInfo.getBookInfo(sess, "9781604598919");
		BookInfo preInfo2 = BookInfo.getBookInfo(sess, "9780142409848");
		sess.getTransaction().commit();
		sess.close();
		
		Session testSess = sessFact.openSession();
		testSess.beginTransaction();
		BookInfo info1 = (BookInfo)testSess.get(BookInfo.class, "9781604598919");
		BookInfo info2 = (BookInfo)testSess.get(BookInfo.class, "9780142409848");
		List<BookInfo> infos = testSess.createCriteria(BookInfo.class).list();
		
		assertEquals(2, infos.size());
		assertNotNull(info1);
		assertNotNull(info2);
	}

}

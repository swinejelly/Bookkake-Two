package edu.rit.csh.models;

import static org.junit.Assert.*;

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

public class BookInfoTest {
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
	public void testCaching() {
		//should cache book infos into db
		BookInfo.getBookInfo("9781604598919");
		BookInfo.getBookInfo("9780142409848");
		
		Session testSess = Resources.sessionFactory.openSession();
		testSess.beginTransaction();
		BookInfo info1 = (BookInfo)testSess.get(BookInfo.class, "9781604598919");
		BookInfo info2 = (BookInfo)testSess.get(BookInfo.class, "9780142409848");
		@SuppressWarnings("unchecked")
		List<BookInfo> infos = testSess.createCriteria(BookInfo.class).list();
		
		assertEquals(2, infos.size());
		assertNotNull(info1);
		assertNotNull(info2);
	}

}

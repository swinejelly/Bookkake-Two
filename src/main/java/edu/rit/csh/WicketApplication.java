package edu.rit.csh;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import edu.rit.csh.auth.LDAPProxy;
import edu.rit.csh.auth.LDAPUser;
import edu.rit.csh.auth.UserWebSession;
import edu.rit.csh.googlebooks.GoogleBookAPIQuery;
import edu.rit.csh.models.Book;
import edu.rit.csh.models.BookInfo;
import edu.rit.csh.models.BorrowPeriod;
import edu.rit.csh.pages.HomePage;

/**
 * Application object for your web application. If you want to run this application without deploying, run the Start class.
 * 
 * @see edu.rit.csh.Start#main(String[])
 */
public class WicketApplication extends WebApplication
{
	
	private SessionFactory sessionFactory;
	private LDAPProxy ldapProxy;
	private String googleBooksApiKey;
	private ThreadPoolExecutor threadExecutor;
	
	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class<? extends WebPage> getHomePage()
	{
		return HomePage.class;
	}

	/**
	 * @see org.apache.wicket.Application#init()
	 */
	@Override
	public void init()
	{
		super.init();
		sessionFactory = new Configuration().configure().buildSessionFactory();
		Book.setSessFact(sessionFactory);
		BookInfo.setSessFact(sessionFactory);
		BorrowPeriod.setSessFact(sessionFactory);
		
		//Init ldapProxy
		try {
			ldapProxy = new LDAPProxy("ldap.properties");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		//Init googleBooksAPIkey
		try (BufferedInputStream stream = new BufferedInputStream(
				new FileInputStream("googlebooks.properties"))){ 
		Properties props = new Properties();
		props.load(stream);
		stream.close();
		googleBooksApiKey = props.getProperty("key");
		} catch (IOException e){
			e.printStackTrace();
			System.exit(1);
		}
		
		//init threadExecutor
		threadExecutor = new ThreadPoolExecutor(2, 8, 
				30, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(8));
		
		PackageResourceReference bgRef = 
				new PackageResourceReference(getClass(), "background.jpg");
		mountResource("/assets", bgRef);
	}
	
	@Override
	public final Session newSession(Request request, Response response){
		UserWebSession sess = new UserWebSession(request);
		LDAPUser dummyUser;
		try {
			dummyUser = getLDAPProxy().getUser("10412");
			sess.setUser(dummyUser);
		} catch (LdapException | CursorException e) {
			e.printStackTrace();
		}
		return sess;
	}
	
	public static WicketApplication getWicketApplication(){
		return (WicketApplication)WebApplication.get();
	}
	
	public SessionFactory getSessionFactory(){
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public LDAPProxy getLDAPProxy(){
		return ldapProxy;
	}
	
	public GoogleBookAPIQuery authGoogleBooksQuery(){
		GoogleBookAPIQuery qry = new GoogleBookAPIQuery();
		qry.setAPIKey(googleBooksApiKey);
		return qry;
	}
	
	public ThreadPoolExecutor getThreadExecutor(){
		return threadExecutor;
	}
}

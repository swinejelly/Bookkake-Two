package edu.rit.csh;

import java.io.IOException;
import java.io.InputStream;
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
import org.apache.wicket.request.http.WebRequest;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import edu.rit.csh.auth.LDAPProxy;
import edu.rit.csh.auth.LDAPUser;
import edu.rit.csh.auth.UserWebSession;
import edu.rit.csh.googlebooks.GoogleBookAPIQuery;
import edu.rit.csh.models.Book;
import edu.rit.csh.models.BookInfo;
import edu.rit.csh.models.BookRequest;
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
		BookRequest.setSessFact(sessionFactory);
		BorrowPeriod.setSessFact(sessionFactory);
		
		//Init ldapProxy
		try {
			ldapProxy = new LDAPProxy("/ldap.properties");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		//Init googleBooksAPIkey
		try (InputStream stream = 
				this.getClass().getResourceAsStream("/googlebooks.properties")){ 
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
	}
	
	@Override
	public final Session newSession(Request request, Response response){
		UserWebSession sess = new UserWebSession(request);
		LDAPUser user = null;;
		String uidnum;
		//Get the uidnum
		if (usesDevelopmentConfig()){
			uidnum = "10412";
		}else{
			WebRequest wRequest = (WebRequest)request;
			uidnum = wRequest.getHeader("X-WEBAUTH-LDAP-UIDN");
		}
		
		try {
			user = getLDAPProxy().getUser(uidnum);
			sess.setUser(user);
		} catch(LdapException | CursorException e){
			e.printStackTrace();
		}
		sess.setUser(user);
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

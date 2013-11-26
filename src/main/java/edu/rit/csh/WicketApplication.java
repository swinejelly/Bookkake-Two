package edu.rit.csh;

import java.io.IOException;

import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import edu.rit.csh.auth.LDAPProxy;
import edu.rit.csh.auth.LDAPUser;
import edu.rit.csh.auth.UserWebSession;

/**
 * Application object for your web application. If you want to run this application without deploying, run the Start class.
 * 
 * @see edu.rit.csh.Start#main(String[])
 */
public class WicketApplication extends WebApplication
{
	
	private SessionFactory sessionFactory;
	
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
	}
	
	@Override
	public final Session newSession(Request request, Response response){
		UserWebSession sess = new UserWebSession(request);
		LDAPUser dummyUser;
		try {
			dummyUser = new LDAPProxy("ldap.properties").getUser("10412");
			sess.setUser(dummyUser);
		} catch (LdapException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CursorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sess;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
}

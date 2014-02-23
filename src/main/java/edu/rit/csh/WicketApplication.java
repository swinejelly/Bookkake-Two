package edu.rit.csh;


import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.http.WebRequest;

import edu.rit.csh.auth.LDAPUser;
import edu.rit.csh.auth.UserWebSession;
import edu.rit.csh.pages.HomePage;

/**
 * Application object for your web application. If you want to run this application without deploying, run the Start class.
 * 
 * @see edu.rit.csh.Start#main(String[])
 */
public class WicketApplication extends WebApplication
{
	
	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class<? extends WebPage> getHomePage()
	{
		return HomePage.class;
	}

	@Override
	public final Session newSession(Request request, Response response){
		UserWebSession sess = new UserWebSession(request);
		LDAPUser user = null;;
		String entryUUID;
		//Get the entryUUID
		if (usesDevelopmentConfig()){
			entryUUID = "9df26e5c-9fb5-1031-8712-6505321e93f3";
		}else{
			WebRequest wRequest = (WebRequest)request;
			entryUUID = wRequest.getHeader("X-WEBAUTH-LDAP-ENTRYUUID");
		}
		
		try {
			user = Resources.ldapProxy.getUser(entryUUID);
			sess.setUser(user);
		} catch(LdapException | CursorException e){
			e.printStackTrace();
		}
		sess.setUser(user);
		return sess;
	}
	
//	public static WicketApplication getWicketApplication(){
//		return (WicketApplication)WebApplication.get();
//	}
}

package edu.rit.csh;


import edu.rit.csh.auth.LDAPUser;
import edu.rit.csh.auth.UserWebSession;
import edu.rit.csh.pages.HomePage;
import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.http.WebRequest;

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
		String uidnum;
		//Get the uidnum
		if (usesDevelopmentConfig()){
			uidnum = "10412";
		}else{
			WebRequest wRequest = (WebRequest)request;
			uidnum = wRequest.getHeader("X-WEBAUTH-LDAP-UIDN");
		}
        //LDAP library seems to throw an exception related to
        //its connection timing out after inactivity,
        //which is normal. If an exception occurs or the user isn't returned,
        //try again.
        int ldapTries = 0, ldapTriesMax = 3;
        while (ldapTries++ < ldapTriesMax){
            try {
                user = Resources.ldapProxy.getUser(uidnum);
            } catch(LdapException | CursorException e){
                e.printStackTrace();
            }
            if (user != null){
                sess.setUser(user);
                break;
            }
        }
		return sess;
	}
}

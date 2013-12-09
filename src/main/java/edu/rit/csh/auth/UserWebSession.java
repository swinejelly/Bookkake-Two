package edu.rit.csh.auth;

import java.io.IOException;

import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;

import edu.rit.csh.WicketApplication;
/**
 * A Websession that holds an LDAP user. 
 */
public class UserWebSession extends WebSession {
	private static final long serialVersionUID = -8542214390684177113L;
	
	private LDAPUser user;
	
	public UserWebSession(Request request) {
		super(request);
	}
	
	public LDAPUser getUser(){
		return user;
	}
	
	public void setUser(String uid){
		LDAPUser dummyUser;
		try {
			dummyUser = WicketApplication.getWicketApplication().getLDAPProxy().getUser(uid);
			if (dummyUser != null){
				setUser(dummyUser);
				System.out.printf("User set to %s: %s\n", uid, user.getGivenname());	
			}else{
				System.out.printf("User %s could not be found\n", uid);
			}
		} catch (LdapException | IOException | CursorException e) {
			e.printStackTrace();
		}
	}
	
	public void setUser(LDAPUser user){
		this.user = user;
	}
}

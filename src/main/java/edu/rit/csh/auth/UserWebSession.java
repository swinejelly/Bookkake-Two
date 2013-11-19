package edu.rit.csh.auth;

import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;
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
	
	public void setUser(LDAPUser user){
		this.user = user;
	}
}

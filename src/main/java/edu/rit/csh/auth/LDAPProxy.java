package edu.rit.csh.auth;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;

public class LDAPProxy {
	
	private LdapConnection connection;
	private String username, password;
	
	/**
	 * Constructs an LDAPProxy to the specified host using the 
	 * given parameters.
	 * @param host LDAP server address
	 */
	public LDAPProxy(String host, String username, String password){
		this.username = username;
		this.password = password;
		connection = new LdapNetworkConnection(host);
	}
	
	/**
	 * Constructs an LDAPProxy using the values stored under keys
	 * "host", "username", and "password" the file at "path"
	 * @throws IOException 
	 */
	public LDAPProxy(String path) throws IOException{
		InputStream stream = this.getClass().getResourceAsStream(path);
		Properties props = new Properties();
		props.load(stream);
		this.username = props.getProperty("username");
		this.password = props.getProperty("password");
		connection = new LdapNetworkConnection(props.getProperty("host"));
		stream.close();
	}

	/**
	 * Attempt to fetch the user from the specified data source
	 * (currently only CSH's schema is supported) and return
	 * an LDAP user
	 * @param entryUUID the user's entryUUID
	 * @return LDAPUser
	 * @throws IOException 
	 * @throws LdapException 
	 * @throws CursorException 
	 */
	public LDAPUser getUser(String entryUUID) throws LdapException, CursorException{
		if (connect()){
			EntryCursor cursor = connection.search("ou=Users,dc=csh,dc=rit,dc=edu", "(entryUUID="+entryUUID+")", SearchScope.SUBTREE);
			if (cursor.next()){
				Entry entry = cursor.get();
				return constructFromEntry(entry);
			}
			return null;
		}
		return null;
	}
	
	public LDAPUser getUserByUsername(String uid) throws LdapException, CursorException{
		if (connect()){
			EntryCursor cursor = connection.search("ou=Users,dc=csh,dc=rit,dc=edu", "(uid="+uid+")", SearchScope.SUBTREE);
			if (cursor.next()){
				Entry entry = cursor.get();
				return constructFromEntry(entry);
			}
			return null;
		}
		return null;
	}
	
	/**
	 * @return a list of all active users. If no connection can be established returns
	 * an empty list
	 */
	public List<LDAPUser> getActiveUsers() throws LdapException, CursorException{
		List<LDAPUser> activeUsers = new ArrayList<LDAPUser>(64);
		if (connect()){
			EntryCursor cursor = connection.search("ou=Users,dc=csh,dc=rit,dc=edu", "(active=1)", SearchScope.SUBTREE);
			while (cursor.next()){
				Entry entry = cursor.get();
				LDAPUser user = constructFromEntry(entry);
				if (user != null){
					activeUsers.add(user);
				}
			}
		}
		return activeUsers;
	}
	
	/**
	 * If not already connected and authenticated, attempt to do so.
	 * Return whether the connection is connected after attempt.
	 */
	private boolean connect(){
		boolean connected = connection.isConnected();
		boolean authenticated = connection.isAuthenticated();
		try{
			if (!connected){
				connection.connect();
			}
			if (!authenticated){
				connection.bind(username, password);
			}
		}catch (LdapException e){
			e.printStackTrace();
			return false;
		}
		assert connection.isConnected() && connection.isAuthenticated();
		return true;
	}
	
	/**
	 * @param e An entry for a user (ou=Users) from CSH LDAP
	 * @return an LDAPUser (null if an error occurs)
	 */
	private LDAPUser constructFromEntry(Entry e){
		try{
			Map<String, String> values = new HashMap<String, String>(36);
			for (Attribute attr: e.getAttributes()){
				values.put(attr.getId(), attr.getString());
			}
			boolean onfloor = values.get("onfloor").equals("1");
			boolean active = values.get("active").equals("1");
			LDAPUser user = new LDAPUser(values.get("uid"),
					values.get("givenname"),
					values.get("cn"),
					onfloor,
					active,
					values.get("entryUUID"),
					values.get("roomnumber"));
			return user;
		}catch (LdapInvalidAttributeValueException err){
			err.printStackTrace();
			return null;
		}
	}
}

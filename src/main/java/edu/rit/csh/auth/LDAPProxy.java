package edu.rit.csh.auth;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
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
		BufferedInputStream stream = new BufferedInputStream(
				new FileInputStream(path));
		Properties props = new Properties();
		props.load(stream);
		this.username = props.getProperty("username");
		this.password = props.getProperty("password");
		connection = new LdapNetworkConnection(props.getProperty("host"));
	}

	/**
	 * Attempt to fetch the user from the specified data source
	 * (currently only CSH's schema is supported) and return
	 * an LDAP user
	 * @param uid the user's UID
	 * @return LDAPUser
	 * @throws IOException 
	 * @throws LdapException 
	 * @throws CursorException 
	 */
	public LDAPUser getUser(String uid) throws LdapException, IOException, CursorException{
		connection.bind(username, password);
		EntryCursor cursor = connection.search("ou=Users,dc=csh,dc=rit,dc=edu", "(uidnumber=1)", SearchScope.SUBTREE);
		if (cursor.next()){
			Entry entry = cursor.get();
			Map<String, String> values = new HashMap<String, String>(36);
			for (Attribute attr: entry.getAttributes()){
				values.put(attr.getId(), attr.getString());
			}
			boolean onfloor = values.get("onfloor").equals("1");
			boolean active = values.get("active").equals("1");
			LDAPUser user = new LDAPUser(	values.get("uid"),
											values.get("givenname"),
											onfloor,
											active,
											values.get("uidnumber"),
											values.get("roomnumber"));
			return user;
		}
		return null;
	}
}

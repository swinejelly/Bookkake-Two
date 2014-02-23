package edu.rit.csh.auth;

import java.io.Serializable;

/**
 * A POJO model that contains information about a user pulled from LDAP or
 * custom constructed for testing and debugging. LDAPUser cannot modify the LDAP 
 * database, and only serves as an information container.
 * @author scott
 *
 */
public class LDAPUser implements Serializable{
	private static final long serialVersionUID = 1L;
	/**Username*/
	private String uid;
	/**First/nickname*/
	private String givenname;
	/**Full name*/
	private String commonname;
	/**Whether a member is on floor*/
	private boolean onfloor;
	/**Whether a member is active*/
	private boolean active;
	/**Unique identifier code*/
	private String entryUUID;
	/**Current room number*/
	private String roomnumber;
	
	public LDAPUser(String uid, String givenname, String commonname, boolean onfloor,
			boolean active, String entryUUID, String roomnumber) {
		this.uid = uid;
		this.givenname = givenname;
		this.commonname = commonname;
		this.onfloor = onfloor;
		this.active = active;
		this.entryUUID = entryUUID;
		this.roomnumber = roomnumber;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getGivenname() {
		return givenname;
	}

	public void setGivenname(String givenname) {
		this.givenname = givenname;
	}

	public String getCommonname() {
		return commonname;
	}

	public void setCommonname(String commonname) {
		this.commonname = commonname;
	}

	public boolean isOnfloor() {
		return onfloor;
	}

	public void setOnfloor(boolean onfloor) {
		this.onfloor = onfloor;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getEntryUUID() {
		return entryUUID;
	}

	public void setEntryUUID(String entryUUID) {
		this.entryUUID = entryUUID;
	}

	public String getRoomnumber() {
		return roomnumber;
	}

	public void setRoomnumber(String roomnumber) {
		this.roomnumber = roomnumber;
	}

}

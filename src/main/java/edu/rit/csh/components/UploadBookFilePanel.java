package edu.rit.csh.components;

import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.wicket.markup.html.panel.Panel;

import edu.rit.csh.models.Book;

public class UploadBookFilePanel extends Panel {
	private static final long serialVersionUID = 7310840385138025930L;

	public UploadBookFilePanel(String id, Book book){
		super(id);
		add(new UploadBookFileForm("uploadBookFileForm", book));
	}

}

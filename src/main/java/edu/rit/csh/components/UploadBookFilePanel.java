package edu.rit.csh.components;

import org.apache.wicket.markup.html.panel.Panel;

import edu.rit.csh.models.Book;

public class UploadBookFilePanel extends Panel {
	private static final long serialVersionUID = 1L;

	public UploadBookFilePanel(String id, Book book){
		super(id);
		add(new UploadBookFileForm("uploadBookFileForm", book));
	}

}

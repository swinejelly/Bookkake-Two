package edu.rit.csh.components;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import edu.rit.csh.models.Book;

public class UploadBookFilePanel extends Panel {
	private static final long serialVersionUID = 1L;

	public UploadBookFilePanel(String id, IModel<Book> model){
		super(id, model);
		add(new UploadBookFileForm("uploadBookFileForm", model));
	}

}

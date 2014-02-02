package edu.rit.csh.components;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import edu.rit.csh.models.BookInfo;

public class RequestBookPanel extends Panel {
	private static final long serialVersionUID = 1L;

	public RequestBookPanel(String id, IModel<BookInfo> model) {
		super(id, model);
		add(new RequestBookForm("requestBookForm", model));
	}

}

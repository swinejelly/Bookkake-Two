package edu.rit.csh;

import org.apache.wicket.markup.html.panel.Panel;

public class AddBookPanel extends Panel {
	private static final long serialVersionUID = 4461133275889450083L;
	
	private final AddBookForm form;

	public AddBookPanel(String id) {
		super(id);
		form = new AddBookForm("addBookForm");
		add(form);
	}

}

package edu.rit.csh.components;

import org.apache.wicket.markup.html.panel.Panel;

public class SearchBookPanel extends Panel {
	private static final long serialVersionUID = 1L;
	
	private final SearchBookForm form;

	public SearchBookPanel(String id) {
		super(id);
		form = new SearchBookForm("searchBookForm");
		add(form);
	}

}

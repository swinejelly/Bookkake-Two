package edu.rit.csh;

import org.apache.wicket.markup.html.panel.Panel;

public class SearchBookPanel extends Panel {
	private static final long serialVersionUID = 4461133275889450083L;
	
	private final SearchBookForm form;

	public SearchBookPanel(String id) {
		super(id);
		form = new SearchBookForm("searchBookForm");
		add(form);
	}

}

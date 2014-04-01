package edu.rit.csh.components;

import org.apache.wicket.markup.html.panel.Panel;

public class SearchBookPanel extends Panel {
	private static final long serialVersionUID = 1L;
	
	private final SearchBookForm form;
    private String variant;
	public SearchBookPanel(String id, String action) {
		super(id);
        variant = action;
		form = new SearchBookForm("searchBookForm", action);
		add(form);
	}

    @Override
    public String getVariation(){
        return variant;
    }
}

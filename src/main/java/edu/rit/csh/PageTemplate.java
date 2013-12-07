package edu.rit.csh;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebPage;

public class PageTemplate extends WebPage {
	private static final long serialVersionUID = -6071606580892913447L;
	
	private   final MarkupContainer header;
	private   final MarkupContainer footer;
	
	public PageTemplate(){
		header = new Header("header");
		footer = new Footer("footer");
		add(header);
		add(footer);
	}
}

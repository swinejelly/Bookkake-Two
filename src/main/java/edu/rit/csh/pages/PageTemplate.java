package edu.rit.csh.pages;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebPage;

import edu.rit.csh.components.Footer;
import edu.rit.csh.components.Header;

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

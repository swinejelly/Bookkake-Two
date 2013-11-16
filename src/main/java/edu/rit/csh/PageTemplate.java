package edu.rit.csh;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class PageTemplate extends WebPage {
	private static final long serialVersionUID = -6071606580892913447L;
	
	private   final MarkupContainer header;
	private   final MarkupContainer footer;
	
	@SuppressWarnings("serial")
	public PageTemplate(){
		header = new Header("header");
		footer = new Footer("footer");
		add(header);
		add(footer);
	}
}

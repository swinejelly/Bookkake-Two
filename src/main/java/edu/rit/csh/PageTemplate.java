package edu.rit.csh;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class PageTemplate extends WebPage {
	private static final long serialVersionUID = -6071606580892913447L;

	public PageTemplate(final PageParameters parameters){
		add(new Header("header"));
		add(new Label("body", "BODY"));
		add(new Footer("footer"));
	}
}

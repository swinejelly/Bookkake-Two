package edu.rit.csh;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

public class PublicSearchResultsPage extends PageTemplate {
	private static final long serialVersionUID = -3286899693398677487L;

	public PublicSearchResultsPage() {
		super();
	}
	
	public PublicSearchResultsPage(PageParameters params){
		super();
		StringValue title = params.get("title");
		add(new Label("title").setDefaultModel(Model.of(title)));
	}

}

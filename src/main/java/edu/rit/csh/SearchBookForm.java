package edu.rit.csh;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;


/**
 * Form for the creation of a new Book.
 * @author scott
 *
 */
public class SearchBookForm extends Form {
	private static final long serialVersionUID = 8123125720433269221L;
	
	private final TextField title;
	private final Button submit;

	public SearchBookForm(String id) {
		super(id);
		title = new TextField("title");
		title.setDefaultModel(Model.of(""));
		title.setLabel(Model.of("Title"));
		add(title);
		
		submit = new Button("searchBookSubmit");
		add(submit);
		setDefaultModel(new CompoundPropertyModel<SearchBookForm>(this));
	}
	
	@Override
	public void onSubmit(){
		String titleStr = (String)title.getDefaultModelObject();
		PageParameters params = new PageParameters();
		params.add("title", titleStr);
		setResponsePage(PublicSearchResultsPage.class, params);
	}

}

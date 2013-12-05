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
	
	private String title;
	private String author;

	public SearchBookForm(String id) {
		super(id);
		setDefaultModel(new CompoundPropertyModel<SearchBookForm>(this));
		
		TextField title = new TextField("title");
		title.setLabel(Model.of("Title"));
		add(title);
		
		TextField author = new TextField("author");
		author.setLabel(Model.of("Author"));
		author.setRequired(false);
		add(author);
		
		add(new Button("searchBookSubmit"));

	}
	
	@Override
	public void onSubmit(){
		PageParameters params = new PageParameters();
		params.add("title", title);
		if (author != null){
			params.add("author", author);
		}
		setResponsePage(PublicSearchResultsPage.class, params);
	}

}

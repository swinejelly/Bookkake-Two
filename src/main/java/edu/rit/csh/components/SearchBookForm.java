package edu.rit.csh.components;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import edu.rit.csh.pages.PublicSearchResultsPage;


/**
 * Form for the creation of a new Book.
 * @author scott
 *
 */
public class SearchBookForm extends Form<SearchBookForm> {
	private static final long serialVersionUID = 1L;
	
	private String title;
	private String author;

	public SearchBookForm(String id) {
		super(id);
		setDefaultModel(new CompoundPropertyModel<SearchBookForm>(this));
		
		TextField<String> title = new TextField<>("title");
		title.setLabel(Model.of("Title"));
		add(title);
		
		TextField<String> author = new TextField<>("author");
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

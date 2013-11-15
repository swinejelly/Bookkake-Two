package edu.rit.csh;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;


/**
 * Form for the creation of a new Book.
 * @author scott
 *
 */
public class SearchBookForm extends Form {
	private static final long serialVersionUID = 8123125720433269221L;
	
	private final TextField title;

	public SearchBookForm(String id) {
		super(id);
		title = new TextField("title");
		title.setDefaultModel(Model.of(""));
		add(title);
		setDefaultModel(new CompoundPropertyModel<SearchBookForm>(this));
	}

}

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
public class AddBookForm extends Form {
	private static final long serialVersionUID = 8123125720433269221L;
	
	private final TextField isbn;

	public AddBookForm(String id) {
		super(id);
		isbn = new TextField("isbn");
		isbn.setDefaultModel(Model.of(""));
		add(isbn);
		setDefaultModel(new CompoundPropertyModel<AddBookForm>(this));
	}

}

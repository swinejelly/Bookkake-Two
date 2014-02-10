package edu.rit.csh.components;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import edu.rit.csh.auth.UserWebSession;
import edu.rit.csh.models.Book;
import edu.rit.csh.models.BookInfo;
import edu.rit.csh.pages.HomePage;

public class AddBookPanel extends Panel {
	private static final long serialVersionUID = 1L;

	public AddBookPanel(String id, IModel<BookInfo> model) {
		super(id, model);
		add(new AddBookForm("addBookForm", model));
	}
	
	private class AddBookForm extends Form<BookInfo> {
		private static final long serialVersionUID = 1L;
		
		public AddBookForm(String id, IModel<BookInfo> model) {
			super(id, model);
		}
		
		@Override
		public void onSubmit(){
			super.onSubmit();
			setResponsePage(HomePage.class);
			//Create and persist book.
			UserWebSession session = (UserWebSession)Session.get();
			Book.createBook(getModelObject().getIsbn(), session.getUser().getUidnumber());
		}
	}
}

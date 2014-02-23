package edu.rit.csh.components;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.DefaultCssAutoCompleteTextField;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import edu.rit.csh.auth.UserWebSession;
import edu.rit.csh.models.Book;
import edu.rit.csh.models.BookInfo;
import edu.rit.csh.pages.OwnedBookSearchResultsPage;

public class SearchOwnedBookForm extends Form<SearchOwnedBookForm> {
	private static final long serialVersionUID = 1L;
	
	private String title;
	private final BookAutoCompleteField titleField;
	private final Button submitButton;
	private List<BookInfo> bookInfos;

	public SearchOwnedBookForm(String id) {
		super(id);
		setDefaultModel(new CompoundPropertyModel<SearchOwnedBookForm>(this));
		String entryUUID = ((UserWebSession)this.getWebSession()).getUser().getEntryUUID();
		List<Book> unownedBooks = Book.getUnpossessedBooks(entryUUID);
		bookInfos = new ArrayList<>(unownedBooks.size());
		for (Book b: unownedBooks){
			bookInfos.add(b.getBookInfo());
		}
		
		List<String> titles = new ArrayList<String>(bookInfos.size());
		for (BookInfo b: bookInfos){
			titles.add(b.getTitle());
		}

		titleField = new BookAutoCompleteField("title", titles);
		titleField.setLabel(Model.of("Title"));
		add(titleField);
		submitButton = new Button("searchOwnedBookSubmit");
		submitButton.add(AttributeModifier.replace("class", "ui blue disabled button"));
		add(submitButton);
	}

	
	@Override
	public void onSubmit(){
		if (titleField.choices.contains(title)){
			String isbn = "";
			for (BookInfo b: bookInfos){
				if (b.getTitle().equals(title)){
					isbn = b.getIsbn();
					break;
				}
			}
			PageParameters params = new PageParameters();
			params.add("isbn", isbn);
			setResponsePage(OwnedBookSearchResultsPage.class, params);
		}
	}
	
	private class BookAutoCompleteField extends DefaultCssAutoCompleteTextField<String>{
		private static final long serialVersionUID = 1L;
		private List<String> choices;

		public BookAutoCompleteField(String id, List<String> choices) {
			super(id);
			this.choices = choices;
			
			add(new OnChangeAjaxBehavior() {
				private static final long serialVersionUID = 1L;

				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					String input = BookAutoCompleteField.this.getValue();
					if (BookAutoCompleteField.this.choices.contains(input)){
						submitButton.add(AttributeModifier.replace("class", "ui blue button"));
						target.add(submitButton);
					}else{
						submitButton.add(AttributeModifier.replace("class", "ui blue disabled button"));
						target.add(submitButton);
					}
				}
			});
		}

		@Override
		protected Iterator<String> getChoices(String input) {
			List<String> suggestions = new ArrayList<String>();
			String inputCap = input.toUpperCase();
			for (String title: choices){
				if (title.toUpperCase().contains(inputCap)){
					suggestions.add(title);
				}
			}
			return suggestions.iterator();
		}
	}
}

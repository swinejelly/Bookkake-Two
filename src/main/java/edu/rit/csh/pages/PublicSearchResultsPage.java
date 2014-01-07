package edu.rit.csh.pages;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import edu.rit.csh.auth.UserWebSession;
import edu.rit.csh.models.Book;
import edu.rit.csh.models.BookInfo;

public class PublicSearchResultsPage extends PageTemplate {
	private static final long serialVersionUID = 1L;

	public PublicSearchResultsPage() {
		super();
	}
	
	public PublicSearchResultsPage(PageParameters params){
		super();
		String title  = params.get("title").toString();
		String author = params.get("author").toString();
		
		List<BookInfo> books = BookInfo.searchBooks(title, author, 10);
		if (books.isEmpty()){
			add(new WebMarkupContainer("books").setVisible(false));
		}else{
			add(new ListView<BookInfo>("books", books){
				private static final long serialVersionUID = 1L;
				@Override
				protected void populateItem(ListItem<BookInfo> item) {
					//add labels backed by property models on item.
					item.add(new Label("title",       new PropertyModel<BookInfo>(item.getModel(), "title")));
					item.add(new Label("publisher",   new PropertyModel<BookInfo>(item.getModel(), "publisher")));
					item.add(new Label("authors",     new PropertyModel<BookInfo>(item.getModel(), "authors")));
					item.add(new Label("description", new PropertyModel<BookInfo>(item.getModel(), "description")));

					//Add image
					WebMarkupContainer img = new WebMarkupContainer("img");
					img.add(AttributeModifier.replace("src", 
							new PropertyModel<BookInfo>(item.getModel(), "thumbnailURL")));
					item.add(img);
					//Add Book button
					final String isbn = item.getModelObject().getIsbn();

					Form<Void> addBook = new Form<Void>("actions"){
						private static final long serialVersionUID = 1L;
						@Override
						protected void onSubmit(){
							super.onSubmit();
							setResponsePage(HomePage.class);
							//Create and persist book.
							UserWebSession session = (UserWebSession)Session.get();
							Book.createBook(isbn, session.getUser().getUidnumber());
						}
					};
					item.add(addBook);
				}

				@Override
				public boolean isVisible(){
					return !getList().isEmpty();
				}
			});
		}
	}

}
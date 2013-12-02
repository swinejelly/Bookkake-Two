package edu.rit.csh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.rit.csh.auth.UserWebSession;
import edu.rit.csh.googlebooks.GoogleBookISBNQuery;
import edu.rit.csh.googlebooks.QueryExecutor;
import edu.rit.csh.models.Book;
import edu.rit.csh.models.BookInfo;

public class HomePage extends PageTemplate {
	private static final long serialVersionUID = -5915056470130165360L;
	
	/**
	 * The current action of the HomeContent.
	 * Defaults to a blank WebMarkupContainer, but can be replaced
	 * via AJAX to something more complicated.
	 */
	private WebMarkupContainer action;
	
	/**
	 * The link to add a book.
	 */
	private AjaxLink addBookLink;

	public HomePage(){
		super();
		action = new WebMarkupContainer("action");
		action.setOutputMarkupId(true);
		addBookLink = new AjaxLink("addBookLink"){
			private static final long serialVersionUID = 2908147205998969131L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				//construct the new AddBookPanel
				WebMarkupContainer actionPanel = new SearchBookPanel("action");
				actionPanel.setOutputMarkupId(true);
				//Replace it in the page hierarchy
				action.replaceWith(actionPanel);
				//Communicate change to client
				target.add(actionPanel);
				action = actionPanel;
			}
			
		};
		
		add(action);
		add(addBookLink);
		
		String uidNum = ((UserWebSession)getSession()).getUser().getUidnumber();
		List<Book> userBooks = Book.getOwnedBooks(uidNum);
		final List<BookInfo> bookInfos = new ArrayList<BookInfo>();
		for (Book b: userBooks){
			BookInfo info = BookInfo.getBookInfo(b.getIsbn());
			if (info != null){
				bookInfos.add(info);
			}
		}
		final ListView<BookInfo> myBooks = 
		new ListView<BookInfo>("books", bookInfos){
			private static final long serialVersionUID = -4592905416065301177L;

			@Override
			protected void populateItem(final ListItem<BookInfo> item) {
				//turn on outputmarkupid for AJAX features (below)
				item.setOutputMarkupId(true);
				
				//Add title
				item.add(new Label("title", new PropertyModel(item.getModel(), "title")));
				
				//Add link to delete book
				final String isbn13 = ((BookInfo)item.getModelObject()).getIsbn();
				final String ownerUID = ((UserWebSession)UserWebSession.get()).getUser().getUidnumber();
				item.add(new AjaxFallbackLink("delete"){
					private static final long serialVersionUID = 589193295987221975L;
						@Override
						public void onClick(AjaxRequestTarget target) {
							Book.getBook(isbn13, ownerUID).delete();
							bookInfos.remove(item.getModelObject());
							//parent is myBooks
							target.add(HomePage.this);
						}
				});
				
			}
		};
		myBooks.setOutputMarkupId(true);
		add(myBooks);
	}
}

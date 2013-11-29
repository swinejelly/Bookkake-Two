package edu.rit.csh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.rit.csh.auth.UserWebSession;
import edu.rit.csh.googlebooks.GoogleBookISBNQuery;
import edu.rit.csh.googlebooks.QueryExecutor;
import edu.rit.csh.models.Book;

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
		List<HashMap<String,String>> bookModels = new ArrayList<HashMap<String,String>>();
		for (Book b: userBooks){
			GoogleBookISBNQuery qry = new GoogleBookISBNQuery(b.getIsbn());
			try {
				JSONObject obj = QueryExecutor.retrieveJSON(qry);
				JSONArray items = obj.optJSONArray("items");
				if (items != null){
					JSONObject bookJSON = items.getJSONObject(0);
					if (bookJSON != null){
						JSONObject volumeInfo = bookJSON.getJSONObject("volumeInfo");
						if (volumeInfo != null){
							bookModels.add(Book.buildBookModel(volumeInfo));	
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace(); continue;
			} catch (IOException e) {
				e.printStackTrace(); continue;
			}
		}
		add(new ListView<HashMap<String,String>>("books", bookModels){
			private static final long serialVersionUID = -4592905416065301177L;

			@Override
			protected void populateItem(ListItem<HashMap<String, String>> item) {
				System.out.println(item.getModel().getObject().get("title"));
				item.add(new Label("title", new PropertyModel(item.getModel(), "[title]")));
			}
			
		});
	}
}

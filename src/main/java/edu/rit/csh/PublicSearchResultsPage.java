package edu.rit.csh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.rit.csh.auth.UserWebSession;
import edu.rit.csh.googlebooks.GoogleBookAPIQuery;
import edu.rit.csh.googlebooks.QueryExecutor;
import edu.rit.csh.models.Book;

public class PublicSearchResultsPage extends PageTemplate {
	private static final long serialVersionUID = -3286899693398677487L;

	public PublicSearchResultsPage() {
		super();
	}
	
	public PublicSearchResultsPage(PageParameters params){
		super();
		String title  = params.get("title").toString();
		String author = params.get("author").toString();
		
		GoogleBookAPIQuery qry = new GoogleBookAPIQuery();
		qry.setTitle(title);
		if (author != null){
			qry.setAuthor(author);
		}

		
		JSONObject json = null;
		String jsonResult;
		try{
			json = QueryExecutor.retrieveJSON(qry);
			jsonResult = "Query Successful";
		}catch (IOException e){
			jsonResult = "IOException:" + e.getMessage();
		}catch (JSONException e){
			jsonResult = "JSONException:" + e.getMessage();
		}
		
		add(new Label("title").setDefaultModel(Model.of(title)));
		add(new Label("author").setDefaultModel(Model.of(author)));
		add(new Label("jsonResult").setDefaultModel(Model.of(jsonResult)));
		if (json == null){
			add(new WebMarkupContainer("books").setVisible(false));
		}
		
		
		List<HashMap<String, String>> books = new ArrayList<HashMap<String, String>>();
		JSONArray bookObjects = json.optJSONArray("items");
		if (bookObjects != null){
			int cap = Math.min(10, bookObjects.length());
			for (int i = 0; i < cap; i++){
				JSONObject bookJSON = bookObjects.getJSONObject(i).getJSONObject("volumeInfo");
				HashMap<String, String> model = Book.buildBookModel(bookJSON);
				if (model.containsKey("ISBN_10") || model.containsKey("ISBN_13")){
					books.add(model);
				}
			}
		}
		add(new Label("numItems").setDefaultModel(Model.of(books.size())));
		
		add(new ListView<HashMap<String, String>>("books", books){
			private static final long serialVersionUID = -8791724204152958230L;
			@Override
			protected void populateItem(ListItem<HashMap<String, String>> item) {
				//add labels backed by property models on item.
				item.add(new Label("title", new PropertyModel(item.getModel(), "[title]")));
				item.add(new Label("publisher", new PropertyModel(item.getModel(), "[publisher]")));
				item.add(new Label("authors", new PropertyModel(item.getModel(), "[authors]")));
				item.add(new Label("description", new PropertyModel(item.getModel(), "[description]")));
				
				//Add image
				WebMarkupContainer img = new WebMarkupContainer("img");
				img.add(AttributeModifier.replace("src", 
						new PropertyModel(item.getModel(), "[thumbnailUrl]")));
				item.add(img);
				//Add Book button
				final String isbn13;
				if (item.getModelObject().containsKey("ISBN_13")){
					isbn13 = item.getModelObject().get("ISBN_13");
				}else{
					isbn13 = "";
				}
				
				Form addBook = new Form("actions"){
					private String isbn = isbn13;
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

package edu.rit.csh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.rit.csh.googlebooks.GoogleBookAPIQuery;
import edu.rit.csh.googlebooks.QueryExecutor;

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
		
		
		List<JSONBookModel> books = new ArrayList<JSONBookModel>();
		JSONArray bookObjects = json.optJSONArray("items");
		if (bookObjects != null){
			int cap = Math.min(10, bookObjects.length());
			for (int i = 0; i < cap; i++){
				JSONObject bookJSON = bookObjects.getJSONObject(i).getJSONObject("volumeInfo");
				books.add(new JSONBookModel(bookJSON));
			}
		}
		add(new Label("numItems").setDefaultModel(Model.of(books.size())));
		
		add(new ListView<JSONBookModel>("books", books){
			private static final long serialVersionUID = -8791724204152958230L;
			@Override
			protected void populateItem(ListItem<JSONBookModel> item) {
				//add labels backed by propertymodels on item.
				for (String s: new String[] {"title", "publisher", "authors", "description"}){
					item.add(new Label(s, new PropertyModel(item.getModel(), s)));
				}
				
				WebMarkupContainer img = new WebMarkupContainer("img");
				img.add(AttributeModifier.replace("src", new PropertyModel(item.getModel(), "thumbnailUrl")));
				item.add(img);
			}
			
			@Override
			public boolean isVisible(){
				return !getList().isEmpty();
			}
		});
	}

	
	private class JSONBookModel{
		public String getTitle() {
			return title;
		}

		public List<String> getAuthors() {
			return authors;
		}

		public String getPublisher() {
			return publisher;
		}

		public String getDescription() {
			return description;
		}

		public String getThumbnailUrl() {
			return thumbnailUrl;
		}

		private String title;
		private List<String> authors;
		private String publisher;
		private String description;
		private String thumbnailUrl;
		
		/**
		 * Constructs a JSONBookModel using a JSONObject source.
		 */
		public JSONBookModel(JSONObject json){
			title = json.getString("title");
			publisher = json.optString("publisher", "");
			description = json.optString("description", "");
			if (description.length() > 600){
				StringBuilder sb = new StringBuilder(600);
				sb.append(description.substring(0, 597));
				sb.append("...");
				description = sb.toString();
			}
			
			JSONObject thumbnails = json.optJSONObject("imageLinks");
			thumbnailUrl = thumbnails == null ? "" : thumbnails.optString("thumbnail", "");
			
			JSONArray authorsJSON = json.optJSONArray("authors");
			authors = new ArrayList<String>();
			if (authorsJSON != null){
				for (int i = 0; i < authorsJSON.length(); i++){
					authors.add(authorsJSON.getString(i));
				}
			}
		}
	}
}

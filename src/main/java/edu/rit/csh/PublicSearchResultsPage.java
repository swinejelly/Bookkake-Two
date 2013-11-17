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
import org.apache.wicket.util.string.StringValue;
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
		qry.setAuthor(author);
		
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
		
		
		List<JSONBookModel> books = new ArrayList<JSONBookModel>();
		JSONArray bookObjects = json.getJSONArray("items");
		int cap = Math.min(10, bookObjects.length());
		for (int i = 0; i < cap; i++){
			JSONObject bookJSON = bookObjects.getJSONObject(i).getJSONObject("volumeInfo");
			books.add(new JSONBookModel(bookJSON));
		}
		
		add(new ListView<JSONBookModel>("books", books){
			private static final long serialVersionUID = -8791724204152958230L;
			@Override
			protected void populateItem(ListItem<JSONBookModel> item) {
				item.add(new Label("title", new PropertyModel(item.getModel(), "title")));
				item.add(new Label("publisher", new PropertyModel(item.getModel(), "publisher")));
				item.add(new Label("authors", new PropertyModel(item.getModel(), "authors")));
				item.add(new Label("description", new PropertyModel(item.getModel(), "description")));
				
				WebMarkupContainer img = new WebMarkupContainer("img");
				img.add(AttributeModifier.replace("src", new PropertyModel(item.getModel(), "thumbnailUrl")));
				item.add(img);
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
			
			JSONObject thumbnails = json.getJSONObject("imageLinks");
			thumbnailUrl = thumbnails.optString("thumbnail", "");
			
			JSONArray authorsJSON = json.getJSONArray("authors");
			authors = new ArrayList<String>();
			for (int i = 0; i < authorsJSON.length(); i++){
				authors.add(authorsJSON.getString(i));
			}
		}
	}
}

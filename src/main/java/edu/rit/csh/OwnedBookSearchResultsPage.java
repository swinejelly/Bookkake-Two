package edu.rit.csh;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import edu.rit.csh.auth.LDAPUser;
import edu.rit.csh.models.Book;
import edu.rit.csh.models.BookInfo;

public class OwnedBookSearchResultsPage extends PageTemplate {
	private static final long serialVersionUID = 7610159315337229723L;

	public OwnedBookSearchResultsPage(){
		super();
	}
	
	public OwnedBookSearchResultsPage(PageParameters params) throws IOException, LdapException, CursorException{
		super();
		String isbn = params.get("isbn").toString();
		System.out.println(isbn);
		
		BookInfo info = BookInfo.getBookInfo(isbn);
		
		add(new Label("title", new PropertyModel(info, "title")));
		add(new Label("publisher", new PropertyModel(info, "publisher")));
		add(new Label("authors", new PropertyModel(info, "authors")));
		add(new Label("description", new PropertyModel(info, "description")));
		
		WebMarkupContainer img = new WebMarkupContainer("img");
		img.add(AttributeModifier.replace("src",
			new PropertyModel(info, "thumbnailURL")));
		add(img);
		
		List<Book> activeBooks = Book.getBooksByIsbn(isbn);
		final Map<String, LDAPUser> users = new TreeMap<>();
		for (Book b: activeBooks){
			if (!users.containsKey(b.getOwnerUID())){
				LDAPUser user = b.getOwner();
				users.put(b.getOwnerUID(), user);
				System.out.println(user.getGivenname());
			}
		}
		
		add(new ListView<Book>("book", activeBooks) {
			private static final long serialVersionUID = 1047917720579988798L;

			@Override
			protected void populateItem(ListItem<Book> item) {
				Book b = item.getModelObject();
				item.add(new Label("owner", new PropertyModel(users.get(b.getOwnerUID()), "givenname")));
				item.add(new Label("status", "STATUS"));
				item.add(new Label("actions", "ACTIONS"));
				
			}
			
		});
	}
}

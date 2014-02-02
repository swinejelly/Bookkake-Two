package edu.rit.csh.pages;

import java.text.SimpleDateFormat;

import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;

import edu.rit.csh.WicketApplication;
import edu.rit.csh.auth.LDAPUser;
import edu.rit.csh.components.SearchBookPanel;
import edu.rit.csh.models.BookInfo;
import edu.rit.csh.models.BookRequest;

public class RequestPage extends PageTemplate {
	private static final long serialVersionUID = 1L;
	
	public RequestPage(){
		super();
		add(new SearchBookPanel("requestBookPanel", "request"));
		
		add(new ListView<BookRequest>("bookRequest", BookRequest.allBookRequests()){
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<BookRequest> item) {
				WebMarkupContainer img = new WebMarkupContainer("thumbnail");
				img.add(AttributeModifier.replace("src", 
						new PropertyModel<BookRequest>(item.getModel(), "bookInfo.thumbnailURL")));
				item.add(img);
				item.add(new Label("title", new PropertyModel<>(item.getModel(), "bookInfo.title")));
				LDAPUser requester;
				try {
					requester = WicketApplication.getWicketApplication().getLDAPProxy()
							.getUser(item.getModelObject().getRequesterUID());
					item.add(new Label("requester", requester.getUid()));
				} catch (LdapException | CursorException e) {
					e.printStackTrace();
					item.add(new Label("requester", "Unable to load username."));
				}
				item.add(new Label("end", new SimpleDateFormat("yyyy-MM-dd").format(item.getModelObject().getEnd().getTime())));
			}
		});
	}
}

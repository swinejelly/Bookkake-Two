package edu.rit.csh.pages;

import java.util.LinkedList;
import java.util.List;

import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;

import edu.rit.csh.auth.UserWebSession;
import edu.rit.csh.components.GiveBookPanel;
import edu.rit.csh.components.SearchBookPanel;
import edu.rit.csh.components.SearchOwnedBookPanel;
import edu.rit.csh.components.UploadBookFilePanel;
import edu.rit.csh.models.Book;

public class HomePage extends PageTemplate {
	private static final long serialVersionUID = 1L;
	
	/**
	 * The current action of the HomeContent.
	 * Defaults to a blank WebMarkupContainer, but can be replaced
	 * via AJAX to something more complicated.
	 */
	private WebMarkupContainer action;
	
	/**
	 * The Title for action
	 */
	private Label actionTitle;
	
	/**
	 * The link to add a book.
	 */
	private AjaxLink<Void> addBookLink;
	
	/**
	 * The link to search for a book.
	 */
	private AjaxLink<Void> searchBookLink;

	public HomePage(){
		super();
		action = new WebMarkupContainer("action");
		action.setOutputMarkupId(true);
		
		actionTitle = new Label("actionTitle", "Action");
		actionTitle.setOutputMarkupId(true);
		add(actionTitle);
		addBookLink = new AjaxLink<Void>("addBookLink"){
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				//construct the new SearchBookPanel
				WebMarkupContainer actionPanel = new SearchBookPanel("action");
				actionPanel.setOutputMarkupId(true);
				//Replace it in the page hierarchy
				action.replaceWith(actionPanel);
				//Communicate change to client
				target.add(actionPanel);
				//replace actionTitle
				Label l = new Label("actionTitle", "Add Book");
				l.setOutputMarkupId(true);
				actionTitle.replaceWith(l);
				target.add(l);
				
				action = actionPanel;
				actionTitle = l;
			}
			
		};
		
		searchBookLink = new AjaxLink<Void>("searchBookLink"){
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				//Construct SearchOwnedBookPanel
				WebMarkupContainer actionPanel = new SearchOwnedBookPanel("action");
				actionPanel.setOutputMarkupId(true);
				//replace
				action.replaceWith(actionPanel);
				//return panel to client.
				target.add(actionPanel);
				Label l = new Label("actionTitle", "Borrow Book");
				l.setOutputMarkupId(true);
				actionTitle.replaceWith(l);
				target.add(l);
				
				action = actionPanel;
				actionTitle = l;
			}
		};
		
		add(action);
		add(addBookLink);
		add(searchBookLink);
		
		String uidNum = ((UserWebSession)getSession()).getUser().getUidnumber();
		//List of all books OWNED by the user.
		List<Book> userOwnedBooks = Book.getOwnedBooks(uidNum);
		//List of all books POSSESSED by the user.
		List<Book> userPossessedBooks = Book.getPossessedBooks(uidNum);
		//List of all books OWNED AND POSSESSED by the user. Displayed.
		//User can delete these books.
		final List<Book> userOwnedPossessedBooks = new LinkedList<Book>(userOwnedBooks);
		userOwnedPossessedBooks.retainAll(userPossessedBooks);
		//List of all books LENT by the user. Displayed.
		//User can mark these books as returned to themselves.
		final List<Book> userLentBooks = new LinkedList<Book>(userOwnedBooks);
		userLentBooks.removeAll(userPossessedBooks);
		//List of all books BORROWED by the user. Displayed.
		//User can mark these books as returned to the lender.
		final List<Book> userBorrowedBooks = new LinkedList<Book>(userPossessedBooks);
		userBorrowedBooks.removeAll(userOwnedBooks);
		
		final ListView<Book> ownedPossessedBooksView = 
		new ListView<Book>("ownedPossessedBooks", userOwnedPossessedBooks){
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<Book> item) {
				//Add title
				item.add(new Label("title", new PropertyModel<Book>(item.getModel(), "bookInfo.title")));
				
				//Add link to delete book
				AjaxFallbackLink<Book> deleteLink = new AjaxFallbackLink<Book>("delete"){
					private static final long serialVersionUID = 1L;
					@Override
					public void onClick(AjaxRequestTarget target) {
						getModelObject().delete();
						setResponsePage(HomePage.class);
					}
				}; 
				deleteLink.setModel(item.getModel());
				item.add(deleteLink);
				
				AjaxFallbackLink<Book> giveLink = new AjaxFallbackLink<Book>("give"){
					private static final long serialVersionUID = 1L;
					@Override
					public void onClick(AjaxRequestTarget target) {
						GiveBookPanel givePanel = null;
						try{
							givePanel = new GiveBookPanel("action", getModelObject());
						}catch (LdapException | CursorException e){
							e.printStackTrace();
							return;
						}
						givePanel.setOutputMarkupId(true);
						Label giveLabel = new Label("actionTitle", "Give Book");
						giveLabel.setOutputMarkupId(true);
						
						action.replaceWith(givePanel);
						actionTitle.replaceWith(giveLabel);
						
						action = givePanel;
						actionTitle = giveLabel;
						
						target.add(givePanel, giveLabel);
					}
				};
				giveLink.setModel(item.getModel());
				item.add(giveLink);
				
				WebComponent ivUpload = new WebComponent("upload"),
						     ivUploading = new WebComponent("uploading"),
						     ivDownload = new WebComponent("download");
				//These invisible components will be used to fill in the unused components.
				ivUpload.setVisible(false);
				ivUploading.setVisible(false);
				ivDownload.setVisible(false);
				//Components for file upload status.
				if (!item.getModelObject().isUploaded() && item.getModelObject().getRelPath() == null){
					//Item has no file.
					AjaxFallbackLink<Book> uploadLink = new AjaxFallbackLink<Book>("upload") {
						private static final long serialVersionUID = 1L;

						@Override
						public void onClick(AjaxRequestTarget target) {
							UploadBookFilePanel uploadPanel = new UploadBookFilePanel("action", getModelObject());
							uploadPanel.setOutputMarkupId(true);
							String labelStr = "Upload \"" + getModelObject().getBookInfo().getTitle() +	"\"";
							Label uploadLabel = new Label("actionTitle", labelStr);
							uploadLabel.setOutputMarkupId(true);

							action.replaceWith(uploadPanel);
							actionTitle.replaceWith(uploadLabel);

							action = uploadPanel;
							actionTitle = uploadLabel;

							target.add(action, actionTitle);
						}
					};
					uploadLink.setModel(item.getModel());
					item.add(uploadLink);
					
					item.add(ivUploading);
					item.add(ivDownload);
				}else if (!item.getModelObject().isUploaded()){
					//Item's file is currently uploading.
					WebMarkupContainer uploadingButton = new WebMarkupContainer("uploading");
					item.add(uploadingButton);
					
					item.add(ivUpload);
					item.add(ivDownload);
				}else{
					//Item's file is uploaded.
					DownloadLink dl;
					if ((dl = item.getModelObject().makeDownloadLink("download")) != null){
						item.add(dl);
					}else{
						item.add(ivDownload);
					}
					
					item.add(ivUpload);
					item.add(ivUploading);
				}
			}
		};
		ownedPossessedBooksView.setOutputMarkupId(true);
		add(ownedPossessedBooksView);
		
		final ListView<Book> borrowedBooksView =
		new ListView<Book>("borrowedBooks", userBorrowedBooks){
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<Book> item) {
				//add title
				item.add(new Label("title", new PropertyModel<Book>(item.getModel(), "bookInfo.title")));
				
				//Add link to return book to other user.
				AjaxFallbackLink<Book> returnLink = new AjaxFallbackLink<Book>("return"){
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick(AjaxRequestTarget target) {
						getModelObject().removeBorrow();
						setResponsePage(HomePage.class);
					}
				};
				returnLink.setModel(item.getModel());
 
				item.add(returnLink);			
			}
		};
		borrowedBooksView.setOutputMarkupId(true);
		add(borrowedBooksView);
		
		final ListView<Book> lentBooksView =
				new ListView<Book>("lentBooks", userLentBooks){
					private static final long serialVersionUID = 1L;

					@Override
					protected void populateItem(final ListItem<Book> item) {
						//add title
						item.add(new Label("title", new PropertyModel<Book>(item.getModel(), "bookInfo.title")));
						
						AjaxFallbackLink<Book> returnLink = new AjaxFallbackLink<Book>("return"){
							private static final long serialVersionUID = 1L;

							@Override
							public void onClick(AjaxRequestTarget target) {
								getModelObject().removeBorrow();
								setResponsePage(HomePage.class);
							}
						};
						returnLink.setModel(item.getModel());
		 
						item.add(returnLink);		
						
					}
				};
				lentBooksView.setOutputMarkupId(true);
				add(lentBooksView);

	}
}

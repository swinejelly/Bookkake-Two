package edu.rit.csh;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;

import edu.rit.csh.auth.UserWebSession;
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
	
	/**
	 * The link to search for a book.
	 */
	private AjaxLink searchBookLink;

	public HomePage(){
		super();
		action = new WebMarkupContainer("action");
		action.setOutputMarkupId(true);
		addBookLink = new AjaxLink("addBookLink"){
			private static final long serialVersionUID = 2908147205998969131L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				//construct the new SearchBookPanel
				WebMarkupContainer actionPanel = new SearchBookPanel("action");
				actionPanel.setOutputMarkupId(true);
				//Replace it in the page hierarchy
				action.replaceWith(actionPanel);
				//Communicate change to client
				target.add(actionPanel);
				action = actionPanel;
			}
			
		};
		
		searchBookLink = new AjaxLink("searchBookLink"){
			private static final long serialVersionUID = -263111810841234937L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				//Construct SearchOwnedBookPanel
				WebMarkupContainer actionPanel = new SearchOwnedBookPanel("action");
				actionPanel.setOutputMarkupId(true);
				//replace
				action.replaceWith(actionPanel);
				//return panel to client.
				target.add(actionPanel);
				action = actionPanel;
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
		List<Book> userOwnedPossessedBooks = new LinkedList<Book>(userOwnedBooks);
		userOwnedPossessedBooks.retainAll(userPossessedBooks);
		//List of all books LENT by the user. Displayed.
		//User can mark these books as returned to themselves.
		List<Book> userLentBooks = new LinkedList<Book>(userOwnedBooks);
		userLentBooks.removeAll(userPossessedBooks);
		//List of all books BORROWED by the user. Displayed.
		//User can mark these books as returned to the lender.
		List<Book> userBorrowedBooks = new LinkedList<Book>(userPossessedBooks);
		userBorrowedBooks.removeAll(userOwnedBooks);
		
		userLentBooks.removeAll(userPossessedBooks);
		final List<BookInfo> ownedPossessedBookInfos = new ArrayList<BookInfo>();
		final List<BookInfo> borrowedBookInfos = new ArrayList<BookInfo>();
		final List<BookInfo> lentBookInfos = new ArrayList<BookInfo>();
		for (Book b: userOwnedPossessedBooks){
			BookInfo info = BookInfo.getBookInfo(b.getIsbn());
			if (info != null){
				ownedPossessedBookInfos.add(info);
			}
		}
		for (Book b: userBorrowedBooks){
			BookInfo info = BookInfo.getBookInfo(b.getIsbn());
			if (info != null){
				borrowedBookInfos.add(info);
			}
		}
		for (Book b: userLentBooks){
			BookInfo info = BookInfo.getBookInfo(b.getIsbn());
			if (info != null){
				lentBookInfos.add(info);
			}
		}
		
		final ListView<BookInfo> ownedPossessedBooksView = 
		new ListView<BookInfo>("ownedPossessedBooks", ownedPossessedBookInfos){
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
							ownedPossessedBookInfos.remove(item.getModelObject());
							//parent is myBooks
							target.add(HomePage.this);
						}
				});
				
			}
		};
		ownedPossessedBooksView.setOutputMarkupId(true);
		add(ownedPossessedBooksView);
		
		final ListView<BookInfo> borrowedBooksView =
		new ListView<BookInfo>("borrowedBooks", borrowedBookInfos){
			private static final long serialVersionUID = 2736041904670215404L;

			@Override
			protected void populateItem(ListItem<BookInfo> item) {
				item.setOutputMarkupId(true);
				
				//add title
				item.add(new Label("title", new PropertyModel(item.getModel(), "title")));
				
				//Add link to return book to other user.
				item.add(new AjaxFallbackLink("return"){
					private static final long serialVersionUID = -7032759365273183041L;

					@Override
					public void onClick(AjaxRequestTarget target) {
						System.out.println("return to other user");
					}
					
				});
				
			}
		};
		borrowedBooksView.setOutputMarkupId(true);
		add(borrowedBooksView);
		
		final ListView<BookInfo> lentBooksView =
				new ListView<BookInfo>("lentBooks", lentBookInfos){
					private static final long serialVersionUID = 2736041904670215404L;

					@Override
					protected void populateItem(ListItem<BookInfo> item) {
						item.setOutputMarkupId(true);
						
						//add title
						item.add(new Label("title", new PropertyModel(item.getModel(), "title")));
						
						//Add link to return book to other user.
						item.add(new AjaxFallbackLink("return"){
							private static final long serialVersionUID = -7032759365273183041L;

							@Override
							public void onClick(AjaxRequestTarget target) {
								System.out.println("mark returned to yourself");
							}
							
						});
						
					}
				};
				lentBooksView.setOutputMarkupId(true);
				add(lentBooksView);

	}
}

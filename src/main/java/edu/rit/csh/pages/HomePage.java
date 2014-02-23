package edu.rit.csh.pages;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.DataGridView;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.PropertyPopulator;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import edu.rit.csh.auth.UserWebSession;
import edu.rit.csh.components.GiveBookPanel;
import edu.rit.csh.components.SearchBookPanel;
import edu.rit.csh.components.SearchOwnedBookPanel;
import edu.rit.csh.components.UploadBookFilePanel;
import edu.rit.csh.models.Book;

public class HomePage extends PageTemplate {
	private static final long serialVersionUID = 1L;
	private static final String scrollToActionPanelJS = "document.getElementById(\"action\").scrollIntoView();";
	
	private WebMarkupContainer action = new WebMarkupContainer("action");
	
	public HomePage(){
		super();
	
		action.setOutputMarkupId(true);
		add(action);
		
		/**
		 * Link to add a book.
		 */
		add(new AjaxLink<Void>("addBookLink"){
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				showAddPanel(target);
			}
		});
		
		add(new AjaxLink<Void>("searchBookLink"){
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				showBorrowPanel(target);
			}
		});
		
		
		
		String entryUUID = ((UserWebSession)getSession()).getUser().getEntryUUID();
		//List of all books OWNED by the user.
		List<Book> userOwnedBooks = Book.getOwnedBooks(entryUUID);
		//List of all books POSSESSED by the user.
		List<Book> userPossessedBooks = Book.getPossessedBooks(entryUUID);
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
		
		List<ICellPopulator<Book>> ownedPossessedColumns = new ArrayList<>();
		ownedPossessedColumns.add(new PropertyPopulator<Book>("bookInfo.title"));
		//Blank column
		ownedPossessedColumns.add(new ICellPopulator<Book>(){
			private static final long serialVersionUID = 1L;
			@Override
			public void detach() {}
			@Override
			public void populateItem(Item<ICellPopulator<Book>> cellItem,
					String componentId, IModel<Book> rowModel) {
				cellItem.add(new Label(componentId, ""));
			}
		});
		//Action column
		ownedPossessedColumns.add(new OwnedPossessedBookActionPopulator());
		
		add(new DataGridView<Book>("rows1", ownedPossessedColumns, new ListDataProvider<Book>(userOwnedPossessedBooks)));
		
		List<ICellPopulator<Book>> borrowedBooksColumns = new ArrayList<>();
		borrowedBooksColumns.add(new PropertyPopulator<Book>("bookInfo.title"));
		borrowedBooksColumns.add(new ICellPopulator<Book>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void detach() {}
			@Override
			public void populateItem(Item<ICellPopulator<Book>> cellItem,
					String componentId, IModel<Book> rowModel) {
				String text = String.format("Borrowed from %s: due on %s",
						rowModel.getObject().owner.getUid(),
						new SimpleDateFormat("MM/dd/yyyy").format(rowModel.getObject().getBorrowPeriod().getEnd().getTime()));
						
				cellItem.add(new Label(componentId, text));
			}
		});
		borrowedBooksColumns.add(new ReturnBookPopulator());
		add(new DataGridView<Book>("rows2", borrowedBooksColumns, new ListDataProvider<Book>(userBorrowedBooks)));
		
		ArrayList<ICellPopulator<Book>> lentColumns = new ArrayList<>();
		lentColumns.add(new PropertyPopulator<Book>("bookInfo.title"));
		lentColumns.add(new ICellPopulator<Book>(){
			private static final long serialVersionUID = 1L;
			@Override
			public void detach() {
			}
			@Override
			public void populateItem(Item<ICellPopulator<Book>> cellItem,
					String componentId, IModel<Book> rowModel) {
				String text = String.format("Borrowed by %s: due on %s", 
						rowModel.getObject().owner.getUid(),
						new SimpleDateFormat("MM/dd/yyyy").format(rowModel.getObject().getBorrowPeriod().getEnd().getTime()));
				cellItem.add(new Label(componentId, text));
			}
		});
		lentColumns.add(new ReturnBookPopulator());
		add(new DataGridView<Book>("rows3", lentColumns, new ListDataProvider<Book>(userBorrowedBooks)));
	}
	
	private void showAddPanel(AjaxRequestTarget target){
		replaceAction(target, new SearchBookPanel(action.getId(), "add"));
	}
	
	private void showBorrowPanel(AjaxRequestTarget target){
		replaceAction(target, new SearchOwnedBookPanel(action.getId()));
	}
	
	private void showGivePanel(AjaxRequestTarget target, final Book b){
		WebMarkupContainer givePanel = new AjaxLazyLoadPanel(action.getId()) {
			private static final long serialVersionUID = 1L;
			@Override
			public Component getLazyLoadComponent(String markupId) {
				try {
					return new GiveBookPanel(markupId, b);
				} catch (LdapException | CursorException e) {
					e.printStackTrace();
					return new Label(markupId, "Could not retrieve usernames.");
				}
			}
		};
		replaceAction(target, givePanel);
	}
	
	private void showUploadPanel(AjaxRequestTarget target, IModel<Book> model){
		replaceAction(target, new UploadBookFilePanel(action.getId(), model));
	}
	
	/**
	 * Replace the action panel on the page.
	 * @param newAction the panel to replace the action panel.
	 * @param title The new title for the panel.
	 */
	private void replaceAction(AjaxRequestTarget target, WebMarkupContainer newAction){
		newAction.setOutputMarkupId(true);
		//Replace it in the page hierarchy
		action.replaceWith(newAction);
		action = newAction;
		//Communicate change to client
		target.add(newAction);
		target.appendJavaScript(scrollToActionPanelJS);
	}
	
	private class OwnedPossessedBookActionPopulator implements ICellPopulator<Book>{
		private static final long serialVersionUID = 1L;

		@Override
		public void detach() {}

		@Override
		public void populateItem(Item<ICellPopulator<Book>> cellItem,
				String componentId, IModel<Book> rowModel) {
			AttributeModifier red   = AttributeModifier.replace("class", "mini red ui button"),
					          blue  = AttributeModifier.replace("class", "mini blue ui button"),
					          white = AttributeModifier.replace("class", "mini white ui button");
			
			RepeatingView v = new RepeatingView(componentId, rowModel);
			AjaxFallbackLink<Book> delete = new AjaxFallbackLink<Book>(v.newChildId(), rowModel){
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick(AjaxRequestTarget target) {
					getModelObject().delete();
					setResponsePage(HomePage.class);
				}
			};
			delete.add(red);
			delete.setBody(Model.of("Delete"));
			v.add(delete);
			
			AjaxFallbackLink<Book> give = new AjaxFallbackLink<Book>(v.newChildId(), rowModel){
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick(AjaxRequestTarget target) {
					showGivePanel(target, getModelObject());
				}
			};
			give.add(blue);
			give.setBody(Model.of("Give"));
			v.add(give);
			
			//handle adding the upload/download link.
			if (!rowModel.getObject().isUploaded() && rowModel.getObject().getRelPath() == null){
				//Item has no file.
				AjaxFallbackLink<Book> upload = new AjaxFallbackLink<Book>(v.newChildId(), rowModel) {
					private static final long serialVersionUID = 1L;
					@Override
					public void onClick(AjaxRequestTarget target) {
						showUploadPanel(target, getModel());
					}
				};
				upload.add(white);
				upload.setBody(Model.of("Upload"));
				v.add(upload);
			}else if (!rowModel.getObject().isUploaded()){
				//Item's file is currently uploading.
				Label uploading = new Label(v.newChildId(), "Uploading");
				uploading.add(white);
				v.add(uploading);
			}else{
				//Item's file is uploaded.
				DownloadLink dl = rowModel.getObject().makeDownloadLink(v.newChildId());
				if (dl != null){
					dl.setBody(Model.of("Download"));
					dl.add(white);
					v.add(dl);
				}
			}
			cellItem.add(v);
		}
	}
		
	private class ReturnBookPopulator implements ICellPopulator<Book>{
		private static final long serialVersionUID = 1L;
		@Override
		public void detach() {}
		@Override
		public void populateItem(Item<ICellPopulator<Book>> cellItem,
				String componentId, IModel<Book> rowModel) {
			AjaxFallbackLink<Book> returnLink = new AjaxFallbackLink<Book>(componentId, rowModel){
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick(AjaxRequestTarget target) {
					getModelObject().removeBorrow();
					setResponsePage(HomePage.class);
				}
			};
			returnLink.add(AttributeModifier.replace("class", "mini ui blue button"));
			returnLink.setBody(Model.of("Return"));
			cellItem.add(returnLink);
		}
	}
}

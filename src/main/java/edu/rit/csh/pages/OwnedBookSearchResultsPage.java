package edu.rit.csh.pages;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.datetime.StyleDateConverter;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import edu.rit.csh.auth.LDAPUser;
import edu.rit.csh.auth.UserWebSession;
import edu.rit.csh.models.Book;
import edu.rit.csh.models.BookInfo;

public class OwnedBookSearchResultsPage extends PageTemplate {
	private static final long serialVersionUID = 1L;

	public OwnedBookSearchResultsPage(){
		super();
	}
	
	public OwnedBookSearchResultsPage(PageParameters params) throws IOException, LdapException, CursorException{
		super();
		String isbn = params.get("isbn").toString();
		
		BookInfo info = BookInfo.getBookInfo(isbn);
		
		add(new Label("title", new PropertyModel<Object>(info, "title")));
		add(new Label("publisher", new PropertyModel<Object>(info, "publisher")));
		add(new Label("authors", new PropertyModel<Object>(info, "authors")));
		add(new Label("description", new PropertyModel<Object>(info, "description")));
		
		WebMarkupContainer img = new WebMarkupContainer("img");
		img.add(AttributeModifier.replace("src",
			new PropertyModel<Object>(info, "thumbnailURL")));
		add(img);
		
		List<Book> activeBooks = Book.getBooksByIsbn(isbn);
		final Map<String, LDAPUser> users = new TreeMap<>();
		for (Book b: activeBooks){
			if (!users.containsKey(b.getOwnerUID())){
				LDAPUser user = b.getOwner();
				users.put(b.getOwnerUID(), user);
			}
		}
		final LDAPUser user = ((UserWebSession)this.getSession()).getUser();
		
		//Remove all books owned or possessed by the user.
		List<Book> unpossessedBooks = Book.getUnpossessedBooks(user.getUidnumber());
		activeBooks.retainAll(unpossessedBooks);
		
		add(new ListView<Book>("book", activeBooks) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<Book> item) {
				Book b = item.getModelObject();
				Calendar now = Calendar.getInstance();
				String ownerUID = b.getOwnerUID();
				LDAPUser possessor = b.getPossessor(now);
				item.add(new Label("owner", new PropertyModel<Object>(users.get(b.getOwnerUID()), "givenname")));
				boolean borrowed = !ownerUID.equals(possessor.getUidnumber());
				if (!borrowed){
					item.add(new Label("status", "Owned by " + possessor.getUid()));
				}else{
					item.add(new Label("status", "Borrowed by " + possessor.getUid()));
				}
				
				if (!borrowed){
					final BorrowBookForm borrowForm = new BorrowBookForm("borrowDateSelect");
					borrowForm.setVisible(false);
					borrowForm.setOutputMarkupPlaceholderTag(true);
					AjaxLink<Void> borrowLink = 
							new AjaxLink<Void>("actions"){
						private static final long serialVersionUID = 1L;
						@Override
						public void onClick(AjaxRequestTarget target) {
							setVisible(false);
							target.add(this);
							borrowForm.setVisible(true);
							target.add(borrowForm);
						}
					};
					borrowLink.setOutputMarkupId(true);
					item.add(borrowLink);
					item.add(borrowForm);
				}else{
					WebComponent invisibleButton = new WebComponent("actions");
					invisibleButton.setVisible(false);
					item.add(invisibleButton);
					WebComponent invisibleForm = new WebComponent("borrowDateSelect");
					invisibleForm.setVisible(false);
					item.add(invisibleForm);
				}
				
				DownloadLink dlLink = b.makeDownloadLink("download");
				if (dlLink != null){
					item.add(dlLink);
				}else{
					WebComponent wc = new WebComponent("download");
					wc.setVisible(false);
					item.add(wc);
				}
			}
		});
	}
	
	private class BorrowBookForm extends Form<BorrowBookForm>{
		private static final long serialVersionUID = 1L;
		Date date;
		public BorrowBookForm(String id) {
			super(id);
			DateTextField dateField = new DateTextField("date",
					new PropertyModel<Date>(this, "date"),
					new StyleDateConverter(false));
			dateField.add(new ReturnDatePicker());
			add(dateField);
			setOutputMarkupId(true);
		}
		
		@Override
		public void onSubmit(){
			setResponsePage(HomePage.class);
			Book b = (Book)getParent().getDefaultModelObject();
			Calendar begin = Calendar.getInstance();
			Calendar end = Calendar.getInstance();
			end.setTime(date);
			UserWebSession sess = (UserWebSession)this.getSession();
			String uid = sess.getUser().getUidnumber();
			b.borrow(uid, begin, end);
		}
	}
	
	private class ReturnDatePicker extends DatePicker{
		private static final long serialVersionUID = 1L;
		
		public ReturnDatePicker(){
			super();
			setShowOnFieldClick(true);
			setAutoHide(true);
		}
		
		@Override
		protected void configure(Map<String, Object> widgetProperties,
							     IHeaderResponse response,
							     Map<String, Object> initVariables){
			/**
			 * Set the minimum and maximum dates for the YUI date picker.
			 */
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
			
			Calendar tomorrow = Calendar.getInstance();
			Calendar aYearFromTomorrow = Calendar.getInstance();
			
			tomorrow.add(Calendar.DAY_OF_YEAR, 1);
			aYearFromTomorrow.add(Calendar.DAY_OF_YEAR, 1);
			aYearFromTomorrow.add(Calendar.YEAR, 1);
			
			String tmrwDate = sdf.format(tomorrow.getTime());
			String ayftDate = sdf.format(aYearFromTomorrow.getTime());
			widgetProperties.put("mindate", tmrwDate);
			widgetProperties.put("maxdate", ayftDate);

			super.configure(widgetProperties, response, initVariables);
		}
	}
}

package edu.rit.csh;

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
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import edu.rit.csh.auth.LDAPUser;
import edu.rit.csh.auth.UserWebSession;
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
				final BorrowBookForm borrowForm = new BorrowBookForm("borrowDateSelect");
				borrowForm.setVisible(false);
				borrowForm.setOutputMarkupPlaceholderTag(true);
				AjaxLink borrowLink = 
					new AjaxLink("actions"){
						private static final long serialVersionUID = 633842873015083341L;
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
			}
		});
	}
	
	private class BorrowBookForm extends Form{
		private static final long serialVersionUID = 934961668590503050L;
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
		private static final long serialVersionUID = -3239044315676274739L;
		
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

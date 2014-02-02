package edu.rit.csh.components;

import java.util.Calendar;
import java.util.Date;

import org.apache.wicket.datetime.StyleDateConverter;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import edu.rit.csh.auth.UserWebSession;
import edu.rit.csh.models.BookInfo;
import edu.rit.csh.models.BookRequest;
import edu.rit.csh.pages.RequestPage;

public class RequestBookForm extends Form<BookInfo> {
	private static final long serialVersionUID = 1L;
	
	private Date date;

	public RequestBookForm(String id, IModel<BookInfo> model) {
		super(id, model);
		DateTextField dateField = new DateTextField("date",
				new PropertyModel<Date>(this, "date"),
				new StyleDateConverter(false));
		dateField.add(new ReturnDatePicker());
		add(dateField);
	}
	
	@Override
	public void onSubmit(){
		setResponsePage(RequestPage.class);
		BookInfo b = getModelObject();
		Calendar end = Calendar.getInstance();
		end.setTime(date);
		UserWebSession sess = (UserWebSession)this.getSession();
		String uid = sess.getUser().getUidnumber();
		BookRequest.createBookRequest(b.getIsbn(), uid, end);
	}
}

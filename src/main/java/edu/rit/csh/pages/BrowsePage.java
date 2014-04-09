package edu.rit.csh.pages;

import edu.rit.csh.Resources;
import edu.rit.csh.auth.LDAPUser;
import edu.rit.csh.auth.UserWebSession;
import edu.rit.csh.components.ImagePanel;
import edu.rit.csh.components.UserLinkPanel;
import edu.rit.csh.models.Book;
import edu.rit.csh.wicketmodels.DefaultModel;
import edu.rit.csh.wicketmodels.TextTruncateModel;
import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import java.text.SimpleDateFormat;
import java.util.List;

public class BrowsePage extends PageTemplate {
	private static final long serialVersionUID = 1L;

    public BrowsePage(){
        this(Book.getBooks(), "Browse All Books");
    }

    public BrowsePage(LDAPUser user){
        this(Book.getOwnedBooks(user.getUidnumber()),
                String.format("Browse %s's Books", user.getUid()));
    }

	public BrowsePage(List<Book> books, String header){
        super();
        add(new Label("headertitle", header));

        add(new ListView<Book>("books", books){
            @Override
            protected void populateItem(ListItem<Book> item){
                IModel<Book> b = item.getModel();
                Book book = b.getObject();
                item.add(new Label("title",       new PropertyModel<String>(b, "bookInfo.title")));
                item.add(new Label("publisher",   new PropertyModel<String>(b, "bookInfo.publisher")));
                item.add(new Label("authors",     new PropertyModel<String>(b, "bookInfo.authors")));
                item.add(new Label("description",
                        new DefaultModel<String>(
                                new TextTruncateModel<String>(
                                        new PropertyModel<String>(b, "bookInfo.description"), 4),
                                "No description available.")));
                String ownerUidnum = "(user not found)", borrowerUidnum = "(user not found)";
                WebMarkupContainer ownerLink = null, borrowerLink = null;
                try{
                    ownerLink = new UserLinkPanel("owner", Resources.ldapProxy.getUser(book.getOwnerUID()));
                    if (book.getBorrowPeriod() != null){
                        borrowerLink = new UserLinkPanel("borrower",
                                Resources.ldapProxy.getUser(book.getBorrowPeriod().getBorrowerUID()));
                    }
                }catch (CursorException | LdapException e){

                }
                if (ownerLink == null){
                    ownerLink = new WebMarkupContainer("owner");
                    ownerLink.add(new WebMarkupContainer("ownerName"));
                    ownerLink.setVisible(false);
                }
                if (borrowerLink == null){
                    borrowerLink = new WebMarkupContainer("borrower");
                    borrowerLink.add(new WebMarkupContainer("borrowerName"));
                    borrowerLink.setVisible(false);
                }

                Label returnDate;
                if (book.getBorrowPeriod() != null){
                    returnDate = new Label("date", new SimpleDateFormat().format(book.getBorrowPeriod().getEnd().getTime()));
                }else{
                    returnDate = new Label("date", "");
                    returnDate.setVisible(false);
                }
                item.add(ownerLink);
                item.add(borrowerLink);
                item.add(returnDate);

                OwnedBookSearchResultsPage.BorrowBookForm form =
                        new OwnedBookSearchResultsPage.BorrowBookForm("borrowDateSelect");
                item.add(form);
                if (book.getBorrowPeriod() != null || book.getOwnerUID().equals(
                        ((UserWebSession)getSession()).getUser().getUidnumber())){
                    form.setVisible(false);
                }

                item.add(new ImagePanel("img", b.getObject().getBookInfo().getThumbnailURL()));
                DownloadLink dl = book.makeDownloadLink("download");
                if (dl != null){
                    item.add(dl);
                }else{
                    WebMarkupContainer wc = new WebMarkupContainer("download");
                    wc.setVisible(false);
                    item.add(wc);
                }
            }
        });
	}
}

package edu.rit.csh.pages;

import edu.rit.csh.auth.LDAPUser;
import edu.rit.csh.auth.UserWebSession;
import edu.rit.csh.components.BookStatusPanel;
import edu.rit.csh.components.ImagePanel;
import edu.rit.csh.models.Book;
import edu.rit.csh.wicketmodels.DefaultModel;
import edu.rit.csh.wicketmodels.TextTruncateModel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

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
                item.add(new BookStatusPanel("status", item.getModel()));

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

package edu.rit.csh.components;

import edu.rit.csh.auth.LDAPUser;
import edu.rit.csh.auth.UserWebSession;
import edu.rit.csh.models.Book;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by scott on 4/9/14.
 */
public class BookStatusPanel extends Panel{
    private String variant;
    public BookStatusPanel(String id, IModel<Book> model) {
        super(id, model);
        //This panel renders differently depending on the relationship between the owner
        //and the current user of the session.
        Book b = (Book)getDefaultModel().getObject();
        LDAPUser owner = b.owner;
        LDAPUser viewer = ((UserWebSession)getSession()).getUser();
        LDAPUser possessor = b.getPossessor(Calendar.getInstance());
        boolean belongsToViewer = viewer.equals(owner);
        boolean lentToViewer = !belongsToViewer && possessor != null && possessor.equals(viewer);
        boolean borrowed = !owner.equals(possessor);
        if (borrowed){
            add(new Label("returndate", new SimpleDateFormat("MM/dd/yyyy")
                    .format(b.getBorrowPeriod().getEnd().getTime())));
        }
        if (belongsToViewer && borrowed) {
            variant = "borrowedby";
            add(new UserLinkPanel("name", possessor));
        }else if (belongsToViewer && !borrowed) {
            variant = "empty";
        }else if (!belongsToViewer && borrowed && !lentToViewer){
            variant = "lenttofrom";
            add(new UserLinkPanel("name1", possessor));
            add(new UserLinkPanel("name2", owner));
        }else if (!belongsToViewer && borrowed && lentToViewer) {
            variant = "borrowedfrom";
            add(new UserLinkPanel("name", owner));
        }else if (!belongsToViewer && !borrowed){
            variant = "ownedby";
            add(new UserLinkPanel("name", owner));
        }
    }

    @Override
    public String getVariation(){
        return variant;
    }
}
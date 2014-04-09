package edu.rit.csh.components;

import edu.rit.csh.auth.LDAPUser;
import edu.rit.csh.pages.BrowsePage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * Created by scott on 4/8/14.
 */
public class UserLinkPanel extends Panel {
    public UserLinkPanel(String id, final LDAPUser user){
        super(id);
        Link l = new Link("link"){
            @Override
            public void onClick() {
                setResponsePage(new BrowsePage(user));
            }
        };
        l.add(new Label("name", user.getUid()));
        add(l);
    }
}

package edu.rit.csh.components;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import edu.rit.csh.auth.UserWebSession;
import edu.rit.csh.pages.HomePage;

public class Header extends Panel {
	private static final long serialVersionUID = -1301661843720137382L;

	public Header(String id) {
		super(id);
		add(new Link("homeLink"){
			private static final long serialVersionUID = 2053508690580582210L;
			@Override
			public void onClick(){
				setResponsePage(HomePage.class);
			}
		});
		
		add(new SwitchUserForm("switchUser"));
	}

	
	public class SwitchUserForm extends Form{
		private static final long serialVersionUID = 7878165819846406503L;
		private String uid;

		public SwitchUserForm(String id) {
			super(id);
			TextField<String> uidInput = new TextField<>("uidInput", new PropertyModel<String>(this, "uid"));
			add(uidInput);
		}
		
		@Override
		public void onSubmit(){
			UserWebSession sess = ((UserWebSession)this.getSession());
			sess.setUser(uid);
			setResponsePage(HomePage.class);
		}
		
	}
}

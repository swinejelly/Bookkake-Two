package edu.rit.csh.components;

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import edu.rit.csh.auth.UserWebSession;
import edu.rit.csh.pages.HomePage;
import edu.rit.csh.pages.RequestPage;

public class Header extends Panel {
	private static final long serialVersionUID = 1L;

	public Header(String id) {
		super(id);
		add(new Link<Void>("homeLink"){
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(){
				setResponsePage(HomePage.class);
			}
		});
		
		add(new Link<Void>("requestsLink"){
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(){
				setResponsePage(RequestPage.class);
			}
		});
		
		if (Application.get().usesDevelopmentConfig()){
			add(new SwitchUserForm("switchUser"));
		}else{
			WebComponent wc = new WebComponent("switchUser");
			wc.setVisibilityAllowed(false);
			add(wc);
		}
		
	}

	
	public class SwitchUserForm extends Form<Void>{
		private static final long serialVersionUID = 1L;
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

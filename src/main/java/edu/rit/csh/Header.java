package edu.rit.csh;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

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
	}

}

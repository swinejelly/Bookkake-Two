package edu.rit.csh;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;

public class HomeContent extends Panel {
	private static final long serialVersionUID = -4786241879788195647L;
	
	/**The current action of the HomeContent.
	 * Defaults to a blank WebMarkupContainer, but can be replaced
	 * via AJAX to something more complicated.
	 */
	private WebMarkupContainer action;
	
	/**The link to add a book.
	 * 
	 */
	private AjaxLink addBookLink;

	public HomeContent(String id) {
		super(id);
		action = new WebMarkupContainer("action");
		action.setOutputMarkupId(true);
		addBookLink = new AjaxLink("addBookLink"){
			private static final long serialVersionUID = 2908147205998969131L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				//construct the new AddBookPanel
				WebMarkupContainer actionPanel = new AddBookPanel("action");
				actionPanel.setOutputMarkupId(true);
				//Replace it in the page heirarchy
				action.replaceWith(actionPanel);
				//Communicate change to client
				target.add(actionPanel);
				action = actionPanel;
			}
			
		};
		
		add(action);
		add(addBookLink);
		
	}

}

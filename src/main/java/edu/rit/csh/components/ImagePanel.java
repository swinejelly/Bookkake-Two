package edu.rit.csh.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;


public class ImagePanel extends Panel {
	private static final long serialVersionUID = 1L;
	public ImagePanel(String id, String path) {
    	super(id);
    	WebMarkupContainer w = new WebMarkupContainer("image");
        w.add(AttributeModifier.replace("src", path));
        add(w);
    }
}
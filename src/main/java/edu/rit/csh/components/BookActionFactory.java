package edu.rit.csh.components;

import java.io.Serializable;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import edu.rit.csh.models.BookInfo;

public interface BookActionFactory extends Serializable {
	public WebMarkupContainer getActions(String id, IModel<BookInfo> b);
}

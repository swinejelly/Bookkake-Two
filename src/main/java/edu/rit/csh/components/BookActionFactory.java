package edu.rit.csh.components;

import java.io.Serializable;

import org.apache.wicket.markup.repeater.RepeatingView;

import edu.rit.csh.models.BookInfo;

public interface BookActionFactory extends Serializable {
	public RepeatingView getActions(String id, BookInfo b);
}

package edu.rit.csh.components;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.markup.head.IHeaderResponse;

public class ReturnDatePicker extends DatePicker{
	private static final long serialVersionUID = 1L;
	
	public ReturnDatePicker(){
		super();
		setShowOnFieldClick(true);
		setAutoHide(true);
	}
	
	@Override
	protected void configure(Map<String, Object> widgetProperties,
						     IHeaderResponse response,
						     Map<String, Object> initVariables){
		/**
		 * Set the minimum and maximum dates for the YUI date picker.
		 */
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		
		Calendar tomorrow = Calendar.getInstance();
		Calendar aYearFromTomorrow = Calendar.getInstance();
		
		tomorrow.add(Calendar.DAY_OF_YEAR, 1);
		aYearFromTomorrow.add(Calendar.DAY_OF_YEAR, 1);
		aYearFromTomorrow.add(Calendar.YEAR, 1);
		
		String tmrwDate = sdf.format(tomorrow.getTime());
		String ayftDate = sdf.format(aYearFromTomorrow.getTime());
		widgetProperties.put("mindate", tmrwDate);
		widgetProperties.put("maxdate", ayftDate);

		super.configure(widgetProperties, response, initVariables);
	}
}
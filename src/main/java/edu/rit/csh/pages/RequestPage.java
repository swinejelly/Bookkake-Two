package edu.rit.csh.pages;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.DataGridView;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.PropertyPopulator;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;

import edu.rit.csh.components.ImagePanel;
import edu.rit.csh.components.SearchBookPanel;
import edu.rit.csh.models.BookRequest;

public class RequestPage extends PageTemplate {
	private static final long serialVersionUID = 1L;
	
	public RequestPage(){
		super();
		add(new SearchBookPanel("requestBookPanel", "request"));
		
		ArrayList<ICellPopulator<BookRequest>> columns = new ArrayList<>();
		columns.add(new ICellPopulator<BookRequest>(){
			private static final long serialVersionUID = 1L;
			@Override
			public void detach() {			}
			@Override
			public void populateItem(
					Item<ICellPopulator<BookRequest>> cellItem,
					String componentId, IModel<BookRequest> rowModel) {
				cellItem.add(new ImagePanel(componentId, rowModel.getObject().getBookInfo().getThumbnailURL()));
			}
		});
		columns.add(new PropertyPopulator<BookRequest>("bookInfo.title"));
		columns.add(new PropertyPopulator<BookRequest>("requester.uid"));
		columns.add(new ICellPopulator<BookRequest>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void detach() {}
			@Override
			public void populateItem(
					Item<ICellPopulator<BookRequest>> cellItem,
					String componentId, IModel<BookRequest> rowModel) {
				cellItem.add(new Label(componentId, new SimpleDateFormat("yyyy-MM-dd").format(rowModel.getObject().getEnd().getTime())));
			}
		});
		add(new DataGridView<BookRequest>("rows", columns, new ListDataProvider<BookRequest>(BookRequest.allBookRequests())));
	}
}

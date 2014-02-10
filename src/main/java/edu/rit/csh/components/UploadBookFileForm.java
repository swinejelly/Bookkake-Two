package edu.rit.csh.components;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Bytes;

import edu.rit.csh.models.Book;
import edu.rit.csh.pages.HomePage;

public class UploadBookFileForm extends Form<Book> {
	private static final long serialVersionUID = 1L;
	private final Button submitButton;
	private final FileUploadField fileField;

	public UploadBookFileForm(String id, IModel<Book> model){
		super(id, model);
		setMultiPart(true);
		setMaxSize(Bytes.gigabytes(1L));
		
		submitButton = new Button("uploadFileSubmit"); 
		
		fileField = new FileUploadField("file");
		fileField.setRequired(true);
		add(submitButton);
		add(fileField);
	}
	
	@Override
	public void onSubmit(){
		FileUpload upload = fileField.getFileUpload();
		getModelObject().upload(upload.getClientFileName(), upload.getBytes());
		setResponsePage(HomePage.class);
	}
}

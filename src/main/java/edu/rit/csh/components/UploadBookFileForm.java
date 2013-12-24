package edu.rit.csh.components;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.util.lang.Bytes;

import edu.rit.csh.models.Book;
import edu.rit.csh.pages.HomePage;

public class UploadBookFileForm extends Form<Void> {
	private static final long serialVersionUID = 474413093694451263L;
	private final Button submitButton;
	private final FileUploadField fileField;
	private Book book;

	public UploadBookFileForm(String id, Book book){
		super(id);
		setMultiPart(true);
		setMaxSize(Bytes.gigabytes(1L));
		this.book = book;
		
		submitButton = new Button("uploadFileSubmit"); 
		
		fileField = new FileUploadField("file");
		fileField.setRequired(true);
		add(submitButton);
		add(fileField);
	}
	
	@Override
	public void onSubmit(){
		FileUpload upload = fileField.getFileUpload();
		book.upload(upload.getClientFileName(), upload.getBytes());
		setResponsePage(HomePage.class);
	}
}

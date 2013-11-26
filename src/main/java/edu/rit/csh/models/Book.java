package edu.rit.csh.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "BOOKS")
public class Book {
	private Long id;
	
	private String isbn;
	
	private String ownerUID;
	
	public Book(){
		
	}
	
	public Book(String isbn, String uid){
		this.setIsbn(isbn);
		this.setOwnerUID(uid);
	}
	
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy="increment")
	public Long getId(){
		return id;
	}
	
	@SuppressWarnings("unused")
	private void setId(Long id){
		this.id = id;
	}

	public String getIsbn() {
		return isbn;
	}

	private void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public String getOwnerUID() {
		return ownerUID;
	}

	private void setOwnerUID(String ownerUID) {
		this.ownerUID = ownerUID;
	}
}

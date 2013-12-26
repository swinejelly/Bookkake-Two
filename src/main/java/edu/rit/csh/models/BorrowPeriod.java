package edu.rit.csh.models;

import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.SessionFactory;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "BORROWPERIODS")
public class BorrowPeriod implements Serializable{
	private static final long serialVersionUID = 1L;
	private static SessionFactory sessFact;
	public static void setSessFact(SessionFactory fact){
		sessFact = fact;
	}

	private long id;
	
	private String borrowerUID;
	
	private Calendar begin, end;
	
	private Book book;
	
	public BorrowPeriod(){
		
	}
	
	public BorrowPeriod(Calendar end){
		this.end = end;
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

	public String getBorrowerUID() {
		return borrowerUID;
	}

	public void setBorrowerUID(String borrowerUID) {
		this.borrowerUID = borrowerUID;
	}

	@Type(type = "calendar_date")
	public Calendar getBegin() {
		return begin;
	}

	public void setBegin(Calendar begin) {
		this.begin = begin;
	}
	
	@Type(type = "calendar_date")
	public Calendar getEnd() {
		return end;
	}

	public void setEnd(Calendar end) {
		this.end = end;
	}
	
	@OneToOne(optional = false, mappedBy="borrowPeriod")
	public Book getBook() {
		return book;
	}

	public void setBook(Book book) {
		this.book = book;
	}
	
	
}

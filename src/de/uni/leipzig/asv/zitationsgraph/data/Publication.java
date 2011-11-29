package de.uni.leipzig.asv.zitationsgraph.data;

import java.util.Date;
import java.util.Vector;

/**
 * Basic object, holing all necessary information, like authors, title ... to identify a publication.
 * @author Sascha Haseloff
 *
 */
public class Publication {
	
	private Vector<String> authors;
	private String title;
	private Date year;
	private String department;
	private String venue;
	
	public Publication(Vector<String> authors, String title) {
		this.authors = authors;
		this.title = title;
	}
	
	public Vector<String> getAuthors() {
		return authors;
	}
	
	public void setAuthors(Vector<String> authors) {
		this.authors = authors;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public Date getYear() {
		return year;
	}

	public void setYear(Date year) {
		this.year = year;
	}

	public String getVenue() {
		return venue;
	}

	public void setVenue(String venue) {
		this.venue = venue;
	}
	
}

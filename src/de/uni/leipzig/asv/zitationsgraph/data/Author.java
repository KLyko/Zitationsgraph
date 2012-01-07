package de.uni.leipzig.asv.zitationsgraph.data;

/**
 * Basic object, represents an author of a paper
 * @author Christoph Schultz
 *
 */
public class Author {

	private String name;
	private String affiliation;
	
	public Author(String name){
		this.name=name;
	}
	
	public void setName(String name){
		this.name=name;
	}
	
	public void setAffiliation(String affiliation){
		this.affiliation=affiliation;
	}
	
	public String getName(){
		return name;
	}
	
	public String getAffiliation(){
		return affiliation;
	}
	
}

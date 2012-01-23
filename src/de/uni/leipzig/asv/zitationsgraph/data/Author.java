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
		this.name = name;
		this.affiliation = "";
	}
	
	public void setName(String name){
		this.name=name;
	}
	
	public void setAffiliation(String affiliation){
		if(this.affiliation.length() == 0){
			this.affiliation = affiliation;
		}else{
			this.affiliation = this.affiliation + "; " + affiliation;
		}
	}
	
	public String getName(){
		return name;
	}
	
	public String getAffiliation(){
		return affiliation;
	}
}

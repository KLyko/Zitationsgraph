package de.uni.leipzig.asv.zitationsgraph.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Vector;

/**
 * Basic object, represents an author of a paper
 * @author Christoph Schultz
 *
 */
public class Author implements Serializable{

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
	
	private synchronized void writeObject( java.io.ObjectOutputStream s ) throws IOException {
		s.writeObject(this.name);
		s.writeObject(this.affiliation);
		
	}
	
	private synchronized void readObject (java.io.ObjectInputStream s) throws ClassNotFoundException, IOException{
		this.name = (String) s.readObject();
		this.affiliation = (String) s.readObject();
	}
	
	public String toString (){
		return name;
	}
	
	
	public boolean equals(Object o2){
		Author a2 = (Author) o2;
		if (name.equals(a2.name))
			return true;
		else return false;	
	}

	@Override
	public int hashCode (){
		return name.trim().hashCode();
		
	}
}

package de.uni.leipzig.asv.zitationsgraph.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

/**
 * Basic object, representing a single Citation in a Publication.
 * @author Sascha Haseloff
 *
 */
public class Citation implements Serializable{

	private Publication publication;
	private String tag;
	private Vector<String> textphrases;
	
	public Citation(Publication publication) {
		this.publication = publication;
	}
	
	public Publication getPublication() {
		return publication;
	}
	
	public void setPublication(Publication publication) {
		this.publication = publication;
	}
	
	public String getTag() {
		return tag;
	}
	
	public void setTag(String tag) {
		this.tag = tag;
	}
	
	public Vector<String> getTextphrases() {
		return textphrases;
	}
	
	public void setTextphrases(Vector<String> textphrases) {
		this.textphrases = textphrases;
	}
	
	private synchronized void writeObject( java.io.ObjectOutputStream s ) throws IOException {
		s.writeObject(this.publication);
		s.writeObject(tag);
		s.writeObject(this.textphrases);
		
	}
	
	private synchronized void readObject (java.io.ObjectInputStream s) throws ClassNotFoundException, IOException{
		this.publication = (Publication) s.readObject();
		this.tag = (String) s.readObject();
		this.textphrases = (Vector<String>) s.readObject();
	}
	
	public String toString(){
		return tag+" publication:"+this.publication.toString();
		
	}
	
	
}

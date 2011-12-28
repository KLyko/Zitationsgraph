package de.uni.leipzig.asv.zitationsgraph.data;

import java.util.Arrays;
import java.util.Vector;

/**
 * Basic object, representing a single Citation in a Publication.
 * @author Sascha Haseloff
 *
 */
public class Citation {

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
	
	public String toString(){
		return tag+" publication:"+this.publication.toString()
		;
	}
}

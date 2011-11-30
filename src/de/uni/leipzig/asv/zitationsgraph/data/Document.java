package de.uni.leipzig.asv.zitationsgraph.data;

import java.util.Vector;

/**
 * Basic object, representing the parsed information for a whole publication-file. It's handed over in all extraction-stages.
 * @author Sascha Haseloff
 *
 */
public class Document {
	
	private Publication publication;
	private String _abstract;
	private Vector<Citation> citations;

	public Document(Publication publication) {
		this.publication = publication;
	}
	
	public Publication getPublication() {
		return publication;
	}
	
	public void setPublication(Publication publication) {
		this.publication = publication;
	}
	
	public String get_abstract() {
		return _abstract;
	}
	
	public void set_abstract(String _abstract) {
		this._abstract = _abstract;
	}
	
	public Vector<Citation> getCitations() {
		return citations;
	}
	
	public void setCitations(Vector<Citation> citations) {
		this.citations = citations;
	}
}

package de.uni.leipzig.asv.zitationsgraph.preprocessing;

import org.apache.lucene.document.Document;
/**
 * Class to 
 * @author Klaus Lyko
 *
 */
public class BaseDoc {
	public static final String HEAD = "head";
	public static final String BODY = "body";
	public static final String REFERENCES = "references";
	
	private String fileName;
	
	public BaseDoc(String fileName) {
		super();
		this.setFileName(fileName);
	}
	
	/**
	 * Method to process the separation of a file.
	 * @TODO implement.
	 */
	public void process() {
		
	}
	
	/**
	 * Method returns fields of a loaded Document.
	 * @param name Name of the field.
	 * @return Corresponding text.
	 */
	public String get(String name) {
		String answer = "";
		if(name.equals(HEAD)) {
			answer = HEAD;
		}
		else if(name.equalsIgnoreCase(BODY)) {
			answer = BODY;
		}
		else if(name.equalsIgnoreCase(REFERENCES)) {
			answer = REFERENCES;
		}
		return answer;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}
	
	
}

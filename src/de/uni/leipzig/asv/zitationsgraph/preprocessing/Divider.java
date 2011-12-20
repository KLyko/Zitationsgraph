package de.uni.leipzig.asv.zitationsgraph.preprocessing;

import java.util.StringTokenizer;
import java.util.logging.Logger;

public class Divider {
	
	Logger logger = Logger.getLogger("ZitGraph");
	
	/**
	 * Holding full text of a scientific paper to be divided.
	 */
	private String fullText;

	String head, body, tail;
	String introName, extroName;
	
	
	public Divider(String fullText) {
		super();
		this.fullText = fullText;
	}

	public void setFullText(String fullText) {
		this.fullText = fullText;
	}

	public String getFullText() {
		return fullText;
	}
	
	
	/**
	 * Determines quality of brute force method.
	 * @return
	 */
	public int determineBruteForceMethod() {
		int quality = 0;
		
		int headCount = fullText.split("Introduction").length-1;
		int referenceCount = fullText.split("References").length-1;
		int bibliographyCount = fullText.split("Bibliography").length-1;
		if(headCount < 1) {
			logger.warning("Unable to determine Introduction without any doubt.");
		} else {
			introName = "Introduction";
			if(referenceCount == 0 && bibliographyCount == 0) {
				logger.warning("Wasn't able to find either 'References' or 'Bibliography' to mark tail.");
			}
			if(referenceCount == 1 && bibliographyCount == 0) {
				logger.info("Using 'Introduction' and 'References'");
				extroName = "References";
			//	splitBy("Introduction", "References");
				
			}
			if(referenceCount == 0 && bibliographyCount == 1) {
				logger.info("Using 'Introduction' and 'Bibliography'");
				extroName = "Bibliography";
				//splitBy("Introduction", "Bibliography");
			}
			if(referenceCount > 0 && bibliographyCount>0) {
				logger.info("Both appearing 'References' and 'Bibliography' atleast once.");
				logger.info("'References' count = "+referenceCount);
				logger.info("'Bibliography' count = "+bibliographyCount);
				if(referenceCount <= bibliographyCount) {
					logger.info("Using 'Introduction' and 'References'");
					extroName = "References";
				}
				else {
					logger.info("Using 'Introduction' and 'Bibliography'");
					extroName = "Bibliography";
				}
					
			}
		}
		
		quality += headCount + referenceCount + bibliographyCount;
		
		return quality;
	}
	
	/**
	 * Splits by brute force method.
	 */
	public void splitByBruteForce() {
		if(introName==null && extroName==null)
			determineBruteForceMethod();
		splitBy(introName, extroName);
	}
	/**
	 * Splits fullText into head, body and tail using exact string with the specified words. 
	 * @param intro String dividing head and body, taking first occurrence.
	 * @param extro String dividing body and tail, taking last occurrence.
	 */
	public void splitBy(String intro, String extro) {
		//defaults
		body = fullText;
		head = fullText;
		tail = fullText;
		//first try to find references
		int ref = fullText.lastIndexOf(extro);
		if(ref > -1) {
			tail = fullText.substring(ref+extro.length());
			body = fullText.substring(0, ref);
		}else {
			logger.info("Wasn't able to find '"+extro+"' to split tail and body.");
		}
		
		//try to get head
		int introPos = fullText.indexOf(intro);
		if(introPos>-1) {
			head = body.substring(0, introPos);
			body = body.substring(introPos);
		}
		else {
			logger.info("Wasn't able to find '"+intro+"' to split head and body.");
		}
	}
	
}

package de.uni.leipzig.asv.zitationsgraph.preprocessing;

import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to split scientific papers into the three parts. We assume that we have only the plain text with line
 * breaks. So we only can use this inherent informations. Any splitter taking into account additional meta data
 * such as layout command within a PDF file would by of a higher order.
 * As of now, we only support a splitting by a brute force algorithm looking for "Introduction" and "References"
 * or "Bibliography" as exact string matches in the full text.
 * Future work: Take into account, that these parts are captions and therefore occurring in a single line, possibly
 * with a heading numeration.
 * 
 * @version 0.1
 * @author Klaus Lyko
 *
 */
public class Divider {
	
	Logger logger = Logger.getLogger("ZitGraph");
	
	//public static String[] refBoundaries = {"Notes", "Note", "Appendix"};
	
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
		
		int headCount = countOccurrenceOfHeading(fullText, "Introduction");
		int referenceCount = countOccurrenceOfHeading(fullText, "References");
		int bibliographyCount = countOccurrenceOfHeading(fullText, "Bibliography");
		//logger.info("\nReferencesCount = "+referenceCount + "\nbibliographyCount="+bibliographyCount+"\nheadCount"+headCount);
		introName = "Introduction";
			if(referenceCount == 0 && bibliographyCount == 0) {
				logger.warning("Wasn't able to find either 'References' or 'Bibliography' to mark tail.");
			}
			if(referenceCount >= 1 && bibliographyCount == 0) {
				logger.info("Using 'Introduction' and 'References'");
				extroName = "References";
			//	splitBy("Introduction", "References");
				
			}
			if(referenceCount == 0 && bibliographyCount >= 1) {
				logger.info("Using 'Introduction' and 'Bibliography'");
				extroName = "Bibliography";
				//splitBy("Introduction", "Bibliography");
			}
			if(referenceCount > 0 && bibliographyCount > 0) {
				logger.info("Both appearing 'References' and 'Bibliography' atleast once.");
				logger.info("'References' count = "+referenceCount);
				logger.info("'Bibliography' count = "+bibliographyCount);				
				// As of now we suspect that the lower occurrence points to the better solution
				// TODO use just the last occurrence of either part
				if(referenceCount <= bibliographyCount) {
					logger.info("Using 'Introduction' and 'References'");
					extroName = "References";
				}
				else {
					logger.info("Using 'Introduction' and 'Bibliography'");
					extroName = "Bibliography";
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
		
		//split references
		splitTail(extro);
		
		//try to get head
		int introPos = body.indexOf(intro);
	//	logger.info("Split Intro ar "+introPos);
		if(introPos>-1) {
			head = body.substring(0, introPos);
			body = body.substring(introPos);
		}
		else {
			logger.info("Wasn't able to find '"+intro+"' to split head and body. So we divde By Headings.");
			splitByHeading();
		}	
	}
	
	private void splitTail(String extro)  {
		//first try to find references
		Pattern pattern = Pattern.compile("\\s[0-9]*"+extro+"\\s");
		Matcher matcher = pattern.matcher(fullText);		
		if(matcher.find())  {
			matcher.reset();
			while(matcher.find()){
				tail = fullText.substring(matcher.start());
				body = fullText.substring(0, matcher.start());
			}
		//limit 
		int limitOffSet = -1;
		// for each possible heading of the limit
	//	for(String limitHeading : refBoundaries) {
			//create patter, get first occurrence in tail
			Pattern limitPattern = Pattern.compile("^(Note|Notes|Appendix ).{0,5}$", Pattern.MULTILINE);
			// Pattern limitPattern = Pattern.compile("\\s[0-9]*"+limitHeading+"\\s");
			Matcher limitMatcher = limitPattern.matcher(tail);
			while(limitMatcher.find()) {
				if(limitOffSet == -1)
					limitOffSet = limitMatcher.start();
				if(limitOffSet > limitMatcher.start())
					limitOffSet = limitMatcher.start();
			}
	//	}
			if(limitOffSet > -1) {
				logger.info("Limiting Reference part until "+limitOffSet+" that is "+tail.substring(limitOffSet, limitOffSet+12));
				tail = tail.substring(0, limitOffSet);
			}
		}else {
			logger.info("Wasn't able to find '"+extro+"' to split tail and body.");
		}
	}

	/**
	 * Method to split a text by headings.
	 * As of now we assume a Heading has a leading number followed by a whitespace character 
	 * and some text beginning with upper case letters, such as "3 Related Work"
	 */
	private void splitByHeading() {
		Pattern pattern = Pattern.compile("^[0-9]+\\s[A-Z].*", Pattern.MULTILINE);
		Matcher matcher;
		int add = 0;
		// try to find headings after abstract
		Pattern abstractPattern = Pattern.compile("^(Abstract).{0,5}$", Pattern.MULTILINE);
		Matcher abstractMatcher = abstractPattern.matcher(body);
		if(abstractMatcher.find()) {
			add = abstractMatcher.end();
			matcher = pattern.matcher(body.substring(abstractMatcher.end()));
		} else {
			matcher = pattern.matcher(body);
		}		
		
		if(matcher.find()) {
			// found at least once
			logger.info("Splitting by heading at "+add+matcher.start()+ " which is the heading: "+matcher.group());
			head = body.substring(0, add+matcher.start());
			body = body.substring(add+matcher.start());
		}
	}
	
	private int countOccurrenceOfHeading(String text, String heading) {
		int count = 0;
		Pattern pattern = Pattern.compile("\\s[0-9]*"+heading+"\\s");
		Matcher matcher = pattern.matcher(text);
		while(matcher.find())
			count++;
		return count;
	}
}

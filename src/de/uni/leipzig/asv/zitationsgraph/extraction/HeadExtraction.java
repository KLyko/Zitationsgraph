package de.uni.leipzig.asv.zitationsgraph.extraction;

// general Java imports
import java.util.Vector;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// internal project imports
import de.uni.leipzig.asv.zitationsgraph.data.Author;
import de.uni.leipzig.asv.zitationsgraph.data.Publication;
import de.uni.leipzig.asv.zitationsgraph.preprocessing.BaseDoc;


public class HeadExtraction {

	//private static final String TEST_YEAR = "blablablasdsdblubb 1981 asjdkj 2011 adjnasd. in 2000.";
	private static final String TEST_HEAD = "\n\nComplex Linking in a Nutshell\n" +
			"Axel-Cyrille Ngonga Ngomo 1 , Timofey Ermilov 1\nUniversity of Leipzig\n" +
			"AKSW Group\nJohannisgasse 26, 04103 Leipzig\n\n\n" +
			"Abstract.  The Web of Data is growing at a stunning rate. Yet, the percentage " +
			"of statements that are links between knowledge bases remains remarkably low." +
			"This is partly due to the process of specifying links between knowledge bases being so tedious." +
			"In this demo, we present the COLANUT interface, an intelligent interface that can suggest link" +
			"specifications and allows to edit them graphically. We present the different steps from specifying" +
			"the knowledge bases that are to be linked to a semi-automatically generated link specification.\n\n";
	
	//some Pattern to find the striking text passages
	private static final Pattern yearPattern = Pattern.compile
			("[1-2][0-9]{3}");
	private static final Pattern forenamePattern = Pattern.compile
			("");
	private static final Pattern surnamePattern = Pattern.compile
			("");
	private static final Pattern abstractPattern = Pattern.compile
			("Abstract\\s{0,2}(:|\\.|((\\r)?\\n))?");
	private static final Pattern titleWithoutParagraphsPattern = Pattern.compile
			("([A-Z][a-z]?|\\d+?)" + //First word (first is upper char) or a number
			"\\s{1,2}(\\w|)(\\p{Punct}&&[^\\.])"); //noch fertig machen ;)
	private static final Pattern proceedingsPattern = Pattern.compile
			("Proceeding(s)?");
	private static final Pattern volNumPattern = Pattern.compile
			("(Volume|Vol.)\\s{0,2}[0-9]{1,2}\\s{0,2}((Number|No.)\\s{0,2}[0-9]{1,2})?");
	
	//includes the complete current head text
	private String headPlaintext;
	
	//includes the separated head text 
	private String[] sepHead;
	
	//includes the special text passages of the current document
	private String currentAbstract;
	private String currentHead;
	private String currentTitle;
	private Author currentAuthor;
	private Vector<Author> authors;
	
	public HeadExtraction(){
		headMining(TEST_HEAD);
	}
	
	/**
	 * Starts the Text Mining on the head of the document.
	 * @param headString
	 * @throws IOException
	 */
	public void headMining(String headString){
		headPlaintext = headString;
		this.headSeparator();
		//this.findTitleAndAuthors();
		//this.findYear();
		//this.findInstitute();
		
		//test output
		System.out.println("Title:\n" + currentHead);
		System.out.println("Abstract:\n" + currentAbstract);
	}
	
	private void headSeparator(){
		//delete leading and trailing whitespace
		headPlaintext = headPlaintext.trim();		
		
		//removes multiple paragraphs
		headPlaintext = headPlaintext.replaceAll("((\\r)?\\n){2,}?","\n");
		
		Matcher m = abstractPattern.matcher(headPlaintext);
		if(m.find()){
			//removes the possible whitespace between the 'Abstract' and its content
			headPlaintext = headPlaintext.replaceAll("Abstract\\s{0,2}(:|\\.|((\\r)?\\n))+?", "4357R4C7Abstract:");
			
			//splits the head text into two parts: the title with authors etc. AND the abstract
			sepHead = headPlaintext.split("4357R4C7");
			currentHead = sepHead[0];
			currentAbstract = sepHead[1];
		}else{
			System.out.println("ERROR-MESSAGE: No Abtract found.\n");
			currentHead = headPlaintext;
			currentAbstract = null;
		}		
	}
	
	private void findTitleAndAuthors(){
		
	}
	
	private void findYear(){
		
	}
	
	private void findInstitute(){
		
	}

	/**
	 * main method for testing the class
	 * @param args
	 */
	public static void main(String[] args){
		HeadExtraction he = new HeadExtraction();		
	}
}

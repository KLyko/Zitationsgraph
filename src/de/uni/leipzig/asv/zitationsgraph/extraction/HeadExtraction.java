package de.uni.leipzig.asv.zitationsgraph.extraction;

// general Java imports
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// internal project imports
import de.uni.leipzig.asv.zitationsgraph.data.Author;
//import de.uni.leipzig.asv.zitationsgraph.data.Publication;
//import de.uni.leipzig.asv.zitationsgraph.preprocessing.BaseDoc;

//Stanford CRF-NER imports
import edu.stanford.nlp.ie.crf.*;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;

// jUnit imports
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Class for extract the head information of a paper.
 * Note: In case of searching on a large text corpora let it work in a single instance.
 * (Because of the classifier loading time - approximately 3 sec)
 * @author Christoph
 */
public class HeadExtraction {
	
	//some Pattern to find the striking text passages
	private static final Pattern yearPattern = Pattern.compile
			("(( )|(\\r)?\\n|\\(|\\[|\\.)+[1-2]{1}[0-9]{3}(( )|(\\r)?\\n|\\)|\\])+");		//symbolized a date only by the year
	private static final Pattern abstractPattern = Pattern.compile
			("Abstract( ){0,2}(:|\\.|((\\r)?\\n))");		//symbolized the start of the Abstract-Part
	private static final Pattern titleWithoutParagraphsPattern = Pattern.compile
			("([A-Z]{1}(\\w|-|:){0,30}|\\d+)" + 				//first word (first is upper char) or a number
			"(( ){1,2}(\\w|\\d|\\p{Punct}){1,30}){1,50}" +		//additional words 
			"((\\r)?\\n){1}"); 									//paragraph at the end is a must-have for the title
	private static final Pattern institutePattern = Pattern.compile
			("(Institute(s)?|(State\\s)?University|Department|Dept\\." + 
			"|(Science\\s)?Laboratory|Center\\s(of|for)|Graduate\\sSchool|School\\s(of|for))" +
			"( of| for)?");
	private static final Pattern lowLvlInstitutePattern = Pattern.compile
			("(Society|Program|Division)( of| for)?");
	private static final Pattern proceedingsPattern = Pattern.compile
			("(Proceedings |Journal of )");			//proceedings
	private static final Pattern volNumPattern = Pattern.compile
			("(Volume|Vol.)( ){0,2}[0-9]{1,2}" +			//volume + short digit-sequence
			"( ){0,2}((Number|No.)( ){0,2}[0-9]{1,2})?");	//number + short digit-sequence
	private static final Pattern emailPattern = Pattern.compile
			("\\{?((\\w|!|#|$|%|&|'|\\*|\\+|-|/|=|\\?|^|_|`|\\{|\\||\\}|~){1,64}(, )?){1,15}\\}?" +	//local-part
			"(@|\\[at\\])(\\w|-|_|\\.){1,184}\\.[a-z]{2,4}");										//@domain-part
	private static final Pattern beginPersonPattern = Pattern.compile("\\<PERSON\\>");
	private static final Pattern endPersonPattern = Pattern.compile("\\</PERSON\\>");
	
	//includes the complete current head text
	private String headPlaintext;
	
	//includes the separated head text 
	private String[] sepHead;
	private String nerHead;
	
	//includes the special text passages of the current document
	private String currentAbstract;
	private String currentHead;
	private String currentTitle;
	private String year;
	private String[] proceedings;
	private Vector<Author> authors;
	
	//classifier for the author recognition
	private AbstractSequenceClassifier classifier;
	private static final String serializedClassifier = "lib/all.3class.distsim.crf.ser";
	
	public HeadExtraction(){
		classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);
		headPlaintext = null;
		sepHead = null;
		nerHead = null;
		currentAbstract = null;
		currentHead = null;
		currentTitle = null;
		year = null;
		authors = new Vector<Author>();
	}
	
	//TODO returns a publication
	/**
	 * Starts the Text Mining on the head of the document.
	 * @param headString
	 */
	public void headMining(String headString){
		//counts the used time
		long starttime = (System.currentTimeMillis());
		
		//starts the search for the required information
		headPlaintext = headString;
		this.headSeparator();
		this.findYear();
		this.findAuthors();
		//this.findInstitute();
		//testTitlePattern();
		//this.findTitle();

		//test output
		System.out.println("### FOUND ===> Title:\n" + currentTitle);
		System.out.println("### FOUND ===> Authors:");
		for(int i=0;i<authors.size();i++){
			System.out.print(authors.elementAt(i).getName() + "; ");
		}
		//System.out.println("\n### FOUND ===> Abstract:\n" + currentAbstract);
		System.out.println("\n### FOUND ===> Date:\n" + year);
		System.out.println("### FOUND ===> Rest of head:\n" + currentHead);	//rest
		
		//prints the used time
		System.out.print("### TIME ===> ");
		System.out.print(System.currentTimeMillis()-starttime);
		System.out.print(" ms");
	}
	
	private void headSeparator(){
		//delete leading and trailing whitespace
		headPlaintext = headPlaintext.trim();		
		
		//removes multiple paragraphs
		headPlaintext = headPlaintext.replaceAll("((\\r)?\\n){2,}?", "\n");
		
		Matcher m = abstractPattern.matcher(headPlaintext);
		if(m.find()){
			//removes the possible whitespace between the 'Abstract' and its content
			headPlaintext = headPlaintext.replaceAll("Abstract\\s{0,2}(:|\\.|((\\r)?\\n))+?", "4357R4C7");
			
			//splits the head text into two parts: the title with authors etc. AND the abstract
			sepHead = headPlaintext.split("4357R4C7");
			currentHead = sepHead[0];
			currentAbstract = sepHead[1].trim();
		}else{
			System.out.println("ERROR-MESSAGE: No Abtract found.\n");
			currentHead = headPlaintext;
			currentAbstract = null;
		}
		if(m.find()) System.out.println("ERROR-MESSAGE: Another Abtract-Match was found.\n");
	}
	
	private void findYear(){
		Matcher m = yearPattern.matcher(currentHead);
		int matchCount = 0;
		//if there is at least one date save this/these at the variable year
		while(m.find()){
			matchCount++;
			if(matchCount==1) year = m.group().trim();
			if(matchCount>1) year = year + ", " + m.group().trim();
showMatch(m);
		}
		//if there isnt any date set year=null
		if(matchCount==0){
			System.out.println("INFO-MESSAGE: No Date found.\n");
			year = null;
		}else{
			//delete all date information from head
			currentHead = currentHead.replaceAll("(\\(|\\d{1,2}\\.){0,2}[1-2][0-9]{3}\\)?", "");
		}
	}
	
	private void findAuthors(){
		nerHead = classifier.classifyWithInlineXML(currentHead);
	    authors.clear();
		Matcher m1 = beginPersonPattern.matcher(nerHead);
		Matcher m2 = endPersonPattern.matcher(nerHead);
		int count = 0;
		int lastEnd = -1;
		while(m1.find() && m2.find()){
			count++;
		    int start = m1.end();
		    int end = m2.start();
		    Author currentAuthor = new Author(nerHead.substring(start,end).trim());
		    authors.add(currentAuthor);
			if(count == 1) currentHead = nerHead.substring(0,m1.start());
			if(count > 1 && lastEnd > -1) currentHead = currentHead + nerHead.substring(lastEnd, m1.start());
			lastEnd = m2.end();
	    }
		//clean up
		currentHead = currentHead.replaceAll("(\\<){1}(/?ORGANIZATION|/?LOCATION){1}(\\>){1}", "");
	}
	
	private void findInstitute(){
		
	}
	
	//TODO compare with matches of the titleWithoutParagraphsPattern AND exclude with proceedings-/journal-/institute- pattern
	private void findTitle(){
		sepHead = null;
		currentHead = currentHead.replaceFirst("(\\r)?\\n", "F1R57_L1N3");
		sepHead = currentHead.split("F1R57_L1N3");
		//title is the first line of the head
		if(sepHead != null){
			currentTitle = sepHead[0];
			currentHead = sepHead[1];
		}
	}
	
	private void testTitlePattern(){
		Matcher m = titleWithoutParagraphsPattern.matcher(currentHead);
		while(m.find()){
			showMatch(m);
		}
	}

	
	private void showMatch(Matcher m){
		//test output
		System.out.print("### Start index: " + m.start());
		System.out.print(", End index: " + m.end() + " ");
		System.out.print(", Match: " + m.group() + "\n");
	}
	
	//TODO clear-method
	public void clear(){
		headPlaintext = null;
		sepHead = null;
		currentAbstract = null;
		currentHead = null;
		currentTitle = null;
		year = null;
		authors.clear();
	}
	
	/**
	 * main method for testing the class
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args){
		//file paths to test files
		String[] testFiles = new String []{
				"examples/headTest/journal.pone.0027856.txt",							// 0
				"examples/headTest/Ngonga Ermilov - Complex Linking in a Nutshell.txt",	// 1
				"examples/headTest/79-373-2-PB.txt",									// 2
				"examples/headTest/81-404-1-PB_withoutHeaderInfo.txt",					// 3
				"examples/headTest/81-404-1-PB.txt"										// 4
		};
		
		try {
			//read in the test files
			StringBuffer buffer = new StringBuffer();
			BufferedReader reader = new BufferedReader (new FileReader(testFiles[0]));
			while (reader.ready()){
				buffer.append(reader.readLine()+System.getProperty("line.separator"));
			}
			
			//starts the headExtraction
			HeadExtraction he = new HeadExtraction();
			he.headMining(buffer.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//some jUnit tests for the pattern
	@Test public void testAbstractPattern(){
		//true: 'Abstract.'
		String s = "abcdefgh Abstract. ijklmnop";
		Matcher m = abstractPattern.matcher(s);
		assertTrue(m.find());
		//true: 'Abstract:'
		s = "abcdefgh Abstract: ijklmnop";
		m.reset(s);
		assertTrue(m.find());
		//true: 'Abstract  :'
		s = "abcdefgh Abstract  : ijklmnop";
		m.reset(s);
		assertTrue(m.find());
		//true: 'Abstract\\n'
		s = "abcdefgh Abstract\r\nijklmnop";
		m.reset(s);
		assertTrue(m.find());
		//false: 'abstract'
		s = "Abstracdefgh abstract class ijklmnop";
		m.reset(s);
		assertFalse(m.find());		
		//false: 'Abstract   :'
		s = "abcdefgh Abstract   : ijklmnop";
		m.reset(s);
		assertFalse(m.find());
	}
	
	@Test public void testYearPattern(){
		//true: '1902\\s'
		String s = "abcdefgh 1902 ijklmop";
		Matcher m = yearPattern.matcher(s);
		assertTrue(m.find());
		//true: '2074\\s'
		s = "abcdefgh 2074 ijklmop";
		m.reset(s);
		assertTrue(m.find());
		//true: '2368\\n'
		s = "abcdefgh 2368\nijklmop";
		m.reset(s);
		assertTrue(m.find());
		//true: '(1988)'
		s = "abcdefgh (1988) ijklmop";
		m.reset(s);
		assertTrue(m.find());
		//true: '31.12.1995'
		s = "abcdefgh 31.12.1995 ijklmop";
		m.reset(s);
		assertTrue(m.find());
		//false: '19032'
		s = "abcdefgh 19032 ijklmop";
		m.reset(s);
		assertFalse(m.find());
		//false: '42145'
		s = "abcdefgh 42145 ijklmop";
		m.reset(s);
		assertFalse(m.find());
	}
}

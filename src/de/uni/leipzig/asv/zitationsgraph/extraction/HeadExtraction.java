package de.uni.leipzig.asv.zitationsgraph.extraction;

/*
 * general Java imports
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * internal project imports
 */
import de.uni.leipzig.asv.zitationsgraph.data.Author;
import de.uni.leipzig.asv.zitationsgraph.data.Publication;
import de.uni.leipzig.asv.zitationsgraph.data.Document;

/*
 * Stanford CRF-NER imports
 */
import edu.stanford.nlp.ie.crf.*;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;

/*
 * jUnit imports
 */
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Class for extracting the head information of a science paper.
 * You get these information if you start the 'headMining' method (which requires a string with the complete head text).
 * The output is a document object (which includes the main information like title, author, abstract, etc).
 * This class works mainly with pattern for get the striking text passages successively. In-between there will be found
 * some candidates for the title of the paper. Finally one of these candidates will be set to the title of the paper.
 * Note: In case of searching on a large text corpora let it work in a single instance.
 * (Because of the classifier loading time - approximately 3 sec)
 * @author Christoph Schultz
 * @version 1.0a
 */
public class HeadExtraction {
	/**
	 * A pattern which represents a character sequence of a year.
	 */
	private static final Pattern yearPattern = Pattern.compile
			("(( )|(\\r)?\\n|\\(|\\[|\\.)+[1-2]{1}[0-9]{3}(( )|(\\r)?\\n|\\)|\\])+");		//symbolized a date only by the year
	/**
	 * A pattern which represents a character sequence of an abstract beginning.
	 */
	private static final Pattern abstractPattern = Pattern.compile
			("Abstract( ){0,2}(:|\\.|((\\r)?\\n))");		//symbolized the start of the Abstract-Part
	/**
	 * A pattern which represents a character sequence of a name of an institute.
	 */
	private static final Pattern institutePattern = Pattern.compile
			("(\\w| |\\d|'|-)*" +
			"((National )?Institute(s)?|(State |City )?(University|Univ.)|Department|Dept\\." + 
			"|College (of|for)|Council on|Academy (of|for)" +
			"|Corporation|Faculty (of|for)|Research Centre" +
			"|(Science )?Laboratory|Center (of|for)|Graduate School|School (of|for)){1}" +
			"( of| for)?" +
			"(\\w| |\\d|\\p{Punct})*((\\r)?\\n){1}");
	/**
	 * A pattern which represents a character sequence of a name of an institute.
	 */
	private static final Pattern lowLvlInstitutePattern = Pattern.compile
			("(\\w| |\\d|\\p{Punct})*" +
			"(Society|Program|Division|Group)( of| for)?" +
			"(\\w| |\\d|\\p{Punct})*((\\r)?\\n){1}");
	/**
	 * A pattern which represents a character sequence of a proceedings line.
	 */
	private static final Pattern proceedingsPattern = Pattern.compile
			("(Proceedings |Journal of )(\\w| |\\d|\\p{Punct})+((\\r)?\\n){1}");
	/**
	 * A pattern which represents a character sequence of a volume/number specification.
	 */
	private static final Pattern volNumPattern = Pattern.compile
			("(Volume|Vol.)( ){0,2}[0-9]{1,2}" +			//volume + short digit-sequence
			"( ){0,2}((Number|No.)( ){0,2}[0-9]{1,2})?");	//number + short digit-sequence
	/**
	 * A pattern which represents a character sequence of an Email-address.
	 */
	private static final Pattern emailPattern = Pattern.compile
			("(\\{)?((\\w|!|#|$|%|&|'|\\*|\\+|-|/|=|\\?|^|_|`|\\{|\\||\\}|~|\\.){1,64}(, )?){1,15}(\\})?" +	//local-part
			"\\@{1}(\\w|-|_|\\.){1,184}\\.[a-z]{2,4}");														//@domain-part
	/**
	 * A pattern which represents a character sequence of the tag <PERSON>.
	 */
	private static final Pattern beginPersonPattern = Pattern.compile("\\<PERSON\\>");
	/**
	 * A pattern which represents a character sequence of the tag </PERSON>.
	 */
	private static final Pattern endPersonPattern = Pattern.compile("\\</PERSON\\>");
	/**
	 * A pattern which represents a character sequence of a separating structure consisting of points.
	 */
	private static final Pattern sepHeadPattern = Pattern.compile("(\\.){5,}((\\r)?\\n){1}");
	
	/**
	 * Includes the complete current head text.
	 */
	private String headPlaintext;
	/**
	 * Includes separated head text while processing.
	 */
	private String[] sepHead;
	/**
	 * Includes the tagged head text after the NER process finished.
	 */
	private String nerHead;
	/**
	 * Includes the candidate for the title of a paper as all head text before the first recognized author is named.  
	 */
	private String beforeAuthor;
	/**
	 * Includes the candidate for the title of a paper as all head text before the first separating structure of points.
	 */
	private String beforeSeparator;
	/**
	 * Includes the candidate for the title of a paper as the first line of the head text.
	 */
	private String firstLineTitle;
	
	/**
	 * Includes the current Abstract while processing.
	 */
	private String currentAbstract;
	/**
	 * Includes the current head text while processing.
	 */
	private String currentHead;
	/**
	 * Includes the current proceedings while processing.
	 */
	private String currentProceedings;
	/**
	 * Includes the current affiliation while processing.
	 */
	private String currentAffiliation;
	/**
	 * Includes the current title while processing.
	 */
	private String title;
	/**
	 * Includes the current year while processing.
	 */
	private String year;
	/**
	 * Includes the current authors while processing.
	 */
	private Vector<Author> authors;
	/**
	 * Includes the current Email-addresses while processing.
	 */
	private Vector<String> emailAdresses;
	
	/**
	 * Includes the classifier for the Stanford-CRF-NER.
	 */
	private AbstractSequenceClassifier classifier;
	/**
	 * Includes the path of the classifier for the Stanford-CRF-NER.
	 */
	private static final String serializedClassifier = "lib/all.3class.distsim.crf.ser";
	
	/**
	 * Constructor for the head extraction class, which starts the Stanford-CRF-NER and sets all class variables back.
	 */
	public HeadExtraction(){
		classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);
		headPlaintext = null;
		sepHead = null;
		nerHead = null;
		beforeAuthor = null;
		beforeSeparator = null;
		currentAbstract = null;
		currentHead = null;
		firstLineTitle = null;
		currentProceedings = null;
		currentAffiliation = null;
		title = null;
		year = null;
		authors = new Vector<Author>();
		emailAdresses = new Vector<String>();
	}
	
	//TODO returns a document
	/**
	 * Starts the Text Mining on the head of the document.
	 * @param headString the given head text
	 * @return Document-Object with all extracted informations from the head text.
	 */
	public void headMining(String headString){
		//counts the used time
		long starttime = (System.currentTimeMillis());
		
		//cleaning up the memory
		this.clear();
		
		//starts the methods to search for the required information
		headPlaintext = headString;
		this.headSeparator();
		this.findEmail();
		this.findYear();
		this.findInstitute(true);
		this.findProceedings();
		this.findVolNum();
		this.findAuthors();
		this.findTitle();
		this.findInstitute(false);
		
		//write all extracted information in one document object
		/*Publication pub = new Publication(authors, title);
		pub.setYearString(year);
		pub.setDepartment(authors.elementAt(0).getAffiliation());
		pub.setVenue(currentProceedings);
		Document doc = new Document(pub);
		doc.set_abstract(currentAbstract);*/

		//test output
		System.out.println("### FOUND ===> Title (final):\n" + title);
		System.out.println("### FOUND ===> Title first Line:\n" + firstLineTitle);
		System.out.println("### FOUND ===> Title before first Author:\n" + beforeAuthor);
		System.out.println("### FOUND ===> Title before first separating Structure:\n" + beforeSeparator);
		System.out.println("### FOUND ===> Authors:");
		for(int i=0;i<authors.size();i++){
			System.out.print(authors.elementAt(i).getName() + "; ");
		}
		if(authors.size() > 0) System.out.println("\n### FOUND ===> Affiliations:\n" + authors.elementAt(0).getAffiliation());
		System.out.println("### FOUND ===> Email:");
		for(int i=0;i<emailAdresses.size();i++){
			System.out.print(emailAdresses.elementAt(i) + "; ");
		}
		System.out.println("\n### FOUND ===> Proceedings:\n" + currentProceedings);
		System.out.println("### FOUND ===> Abstract:\n" + currentAbstract);
		System.out.println("### FOUND ===> Date:\n" + year);
		System.out.println("### FOUND ===> Rest of head:\n" + currentHead);	//rest
				
		//prints the used time
		System.out.print("### TIME ===> ");
		System.out.print(System.currentTimeMillis()-starttime);
		System.out.print(" ms");
		
		//return doc;
	}
	
	/**
	 * This method splits the text mainly in two parts: rest of the head AND abstract.
	 * Also a candidate for the title is searched with a separating structure as a line of points.
	 * (especially for paper of the Lit Linguist Computing Conference needed)
	 * Furthermore the head text is cleaned of multiple paragraphs and leading or trailing whitespace.
	 */
	private void headSeparator(){
		//delete leading and trailing whitespace and remove multiple paragraphs
		headPlaintext = headPlaintext.trim();		
		headPlaintext = headPlaintext.replaceAll("((\\r)?\\n){2,}?", "\n");
		
		//potentially title before the first separating structure like a lot of points
		Matcher n = sepHeadPattern.matcher(headPlaintext);
		if(n.find()){
			beforeSeparator = headPlaintext.substring(0, n.start()).replaceAll("(\\r)?\\n", " ");
			headPlaintext = headPlaintext.replaceAll("(\\.){5,}((\\r)?\\n){1}", "\n");
		}
		
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
		if(m.find()) System.out.println("ERROR-MESSAGE: Another Abstract-Match was found.\n");
	}
	
	/**
	 * This method finds a possible year of publication.
	 */
	private void findYear(){
		Matcher m = yearPattern.matcher(currentHead);
		int matchCount = 0;
		//if there is at least one date save this/these at the variable year
		while(m.find()){
			matchCount++;
			if(matchCount==1) year = m.group().trim();
			if(matchCount>1) year = year + ", " + m.group().trim();
		}
		//if there isnt any date set year=null
		if(matchCount==0){
			year = null;
		}else{
			//delete all date information from head
			currentHead = currentHead.replaceAll("(\\(|\\d{1,2}\\.){0,2}[1-2][0-9]{3}\\)?", "");
		}
	}
	
	/**
	 * This method finds author names by using the Stanford-CRF-NER.
	 * Also a title candidate is set as all lines before the first author is named.
	 */
	private void findAuthors(){
		//starts the stanford-crf-ner
		nerHead = classifier.classifyWithInlineXML(currentHead);
		authors.clear();
		//searching for the right tags
		Matcher m1 = beginPersonPattern.matcher(nerHead);
		Matcher m2 = endPersonPattern.matcher(nerHead);
		int count = 0;
		int lastEnd = -1;
		while(m1.find() && m2.find()){
			count++;
		    int start = m1.end();
		    int end = m2.start();
		    //if the match includes a paragraph, split this in two authors. if not, just add the match to the author list
		    if(nerHead.substring(start,end).contains("\n")){
		    	String[] sepAuthors = nerHead.substring(start,end).split("((\\r)?\\n)+");
		    	for(int i=0; i<sepAuthors.length; i++){
			    	if(sepAuthors[i].length() > 1) authors.add(new Author(sepAuthors[i]));
		    	}
		    }else{
		    	authors.add(new Author(nerHead.substring(start,end).trim()));
		    }		    
			if(count == 1){
				currentHead = nerHead.substring(0,m1.start());
				//text before the first author is named
				beforeAuthor = currentHead.replaceAll("(\\<){1}(/?ORGANIZATION|/?LOCATION){1}(\\>){1}", "");
			}
			if(count > 1 && lastEnd > -1) currentHead = currentHead + nerHead.substring(lastEnd, m1.start());
			lastEnd = m2.end();
	    }
		if(lastEnd > -1){
			currentHead = currentHead + nerHead.substring(lastEnd);
		}		
		//clean up
		currentHead = currentHead.replaceAll("(\\<){1}(/?ORGANIZATION|/?LOCATION){1}(\\>){1}", "");
	}
	
	/**
	 * This method extracts all email-addresses contained in the given head text.
	 */
	private void findEmail(){
		if(currentHead.contains("@")){
			Matcher m = emailPattern.matcher(currentHead);
			String s = "";
			int count = 0;
			int lastEnd = -1;
			while(m.find()){
				count++;
				emailAdresses.add(m.group().trim());
				if(count == 1) s = currentHead.substring(0, m.start());
				if(count > 1 && lastEnd > -1) s = s + currentHead.substring(lastEnd, m.start());
				lastEnd = m.end();
			}
			if(lastEnd > -1) currentHead = s + currentHead.substring(lastEnd);
		}
	}
	
	/**
	 * This method finds probably all affiliation text passages in the given head text.
	 * It works in two ways: Either it uses the pattern which uses clearly prominent keywords or
	 * it uses the pattern which uses less prominent keywords for finding affiliations.
	 * @param highLvl as an boolean it decides which pattern is used.
	 */
	private void findInstitute(boolean highLvl){
		Matcher m;
		if(highLvl){
			m = institutePattern.matcher(currentHead);
		}else{
			m = lowLvlInstitutePattern.matcher(currentHead);
		}
		String s1 = "";
		String s2 = "";
		int count = 0;
		int lastEnd = -1;
		while(m.find()){
			count++;
			if(count == 1){
				s1 = m.group().trim();
				s2 = currentHead.substring(0, m.start());
			}
			if(count > 1 && lastEnd > -1){
				if(s1.indexOf(m.group().trim()) == -1) s1 = s1 + "; " + m.group().trim();
				s2 = s2 + currentHead.substring(lastEnd, m.start());
			}
			lastEnd = m.end();
		}
		//set the affiliation as an attribute of the first author
		if(highLvl){
			currentAffiliation = s1;
		}else{
			currentAffiliation = currentAffiliation + "; " + s1;
			if(authors.size()>0) authors.elementAt(0).setAffiliation(currentAffiliation);
		}
		if(lastEnd > -1) currentHead = s2 + currentHead.substring(lastEnd);
	}

	/**
	 * This method finds all proceedings sequences of the given head text.
	 * Also it deletes eventually the found proceedings from the title candidate beforeAuthor.
	 */
	private void findProceedings(){
		Matcher m = proceedingsPattern.matcher(currentHead);
		String s1 = "";
		String s2 = "";
		int count = 0;
		int lastEnd = -1;
		while(m.find()){
			count++;
			if(count == 1){
				s1 = m.group().trim();
				s2 = currentHead.substring(0, m.start());
			}
			if(count > 1 && lastEnd > -1){
				s1 = s1 + "; " + m.group().trim();
				s2 = s2 + currentHead.substring(lastEnd, m.start());
			}
			lastEnd = m.end();
			//deleting the match from beforeAuthor
			if(beforeAuthor != null){
				if(beforeAuthor.indexOf(m.group().trim()) >= 0){
					beforeAuthor = 	beforeAuthor.substring(0, beforeAuthor.indexOf(m.group().trim())) + 
									beforeAuthor.substring(beforeAuthor.indexOf(m.group().trim()) + m.group().trim().length()).trim();
				}
			}
		}
		if(lastEnd > -1){
			currentProceedings = s1;
			currentHead = s2 + currentHead.substring(lastEnd);
		}		
	}
	
	/**
	 * This method finds all additional Volume/Number (Vol./No.) data from the given head text and
	 * deletes the matches eventually from the title candidate beforeAuthor.
	 */
	private void findVolNum(){
		Matcher m = volNumPattern.matcher(currentHead);
		String s1 = "";
		String s2 = "";
		int count = 0;
		int lastEnd = -1;
		while(m.find()){
			count++;
			if(count == 1){
				s1 = m.group().trim();
				s2 = currentHead.substring(0, m.start());
			}
			if(count > 1 && lastEnd > -1){
				s1 = s1 + "; " + m.group().trim();
				s2 = s2 + currentHead.substring(lastEnd, m.start());
			}
			lastEnd = m.end();
			//deleting the match from beforeAuthor
			if(beforeAuthor != null){
				if(beforeAuthor.indexOf(m.group().trim()) >= 0){
					beforeAuthor = 	beforeAuthor.substring(0, beforeAuthor.indexOf(m.group().trim())) + 
									beforeAuthor.substring(beforeAuthor.indexOf(m.group().trim()) + m.group().trim().length()).trim();
				}
			}
		}
		if(lastEnd > -1){
			currentProceedings = currentProceedings + " (" + s1 + ") ";
			currentHead = s2 + currentHead.substring(lastEnd).trim();
		}		
	}
	
	/**
	 * This method set up all title candidates and decides which is the best choice for a title.
	 */
	private void findTitle(){
		//(A) title includes all lines before the first author is named
		if(beforeAuthor != null) beforeAuthor = beforeAuthor.replaceAll("((\\r)?\\n|( )+)", " ");
		
		//(B) title includes the first line of the head
		currentHead = currentHead.replaceFirst("(\\r)?\\n", "F1R57_L1N3");
		sepHead = currentHead.split("F1R57_L1N3");
		switch(sepHead.length){
			case 0:
				System.out.println("ERROR-Message: Current head text has only one line.");
				firstLineTitle = currentHead;
				break;
			case 1:
				firstLineTitle = sepHead[0];
				currentHead = sepHead[0];
				break;
			case 2:
				firstLineTitle = sepHead[0];
				currentHead = sepHead[1];
				break;
			default:
				System.out.println("ERROR-Message: Head splitting is wrong.");
		}
		//(C) title includes all lines before the first separating structure
		
		//(FINAL) decision which one is the real title
		if(beforeSeparator != null){
			title = beforeSeparator;
		}else{
			if(beforeAuthor != null){
				title = beforeAuthor;
			}else{
				if(firstLineTitle != null){
					title = firstLineTitle;
				}else{
					System.out.println("ERROR-Message: There isnt any title extracted.");
					title = "";
				}
			}
		}
	}
	
	/*
	 * This method shows every match is given by a matcher on the console.
	 * @param m the matcher of which the match should print out.
	 */
	private void showMatch(Matcher m){
		System.out.print("### Start index: " + m.start());
		System.out.print(", End index: " + m.end() + " ");
		System.out.print(", Match: " + m.group() + "\n");
	}
	
	/*
	 * This method cleans up the class by setting back all variables.
	 */
	public void clear(){
		headPlaintext = null;
		sepHead = null;
		nerHead = null;
		beforeAuthor = null;
		beforeSeparator = null;
		currentAbstract = null;
		currentHead = null;
		firstLineTitle = null;
		currentProceedings = null;
		currentAffiliation = null;
		title = null;
		year = null;
		authors.clear();
		emailAdresses.clear();
	}
	
	/**
	 * Main-method for testing the class.
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
				"examples/headTest/81-404-1-PB.txt",									// 4
				"examples/headTest/DigitalHumanities_2011_page42.txt",					// 5
				"examples/headTest/DigitalHumanities_2011_page47.txt",					// 6
				"examples/preprocessed/Lit Linguist Computing-2011-Brierley-279-84.pdf_head.txt",	// 7
				"examples/preprocessed/Lit Linguist Computing-2011-Garrard-389-405.pdf_head.txt",	// 8
				"examples/preprocessed/Lit Linguist Computing-2011-Gregory-297-314.pdf_head.txt"	// 9
		};
		
		try {
			//read in the test files
			StringBuffer buffer = new StringBuffer();
			BufferedReader reader = new BufferedReader (new FileReader(testFiles[9]));
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
	
	/**
	 * JUnit test method for the abstractPattern.
	 */
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
	
	/**
	 * JUnit test method for the yearPattern.
	 */
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

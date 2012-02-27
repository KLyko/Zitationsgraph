package de.uni.leipzig.asv.zitationsgraph.extraction;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.experimental.theories.Theories;

import de.uni.leipzig.asv.zitationsgraph.data.Author;
import de.uni.leipzig.asv.zitationsgraph.data.Citation;
import de.uni.leipzig.asv.zitationsgraph.data.Publication;
import de.uni.leipzig.asv.zitationsgraph.extraction.templates.AuthorTemplateEntity;
import de.uni.leipzig.asv.zitationsgraph.extraction.templates.BasicTemplates;
import de.uni.leipzig.asv.zitationsgraph.extraction.templates.TemplateBuilder;
import de.uni.leipzig.asv.zitationsgraph.extraction.templates.TemplateEntity;
import de.uni.leipzig.asv.zitationsgraph.preprocessing.BaseDoc;
import de.uni.leipzig.asv.zitationsgraph.preprocessing.NotSupportedFormatException;
import de.uni.leipzig.asv.zitationsgraph.util.PosAnalyzer;

/**
 * This class find the references with the authors, title and year in a reference Part string
 * of scientific Paper. The main method is the {@code referenceMining (String referenceString)},
 * which receive a reference part as string. The result is a list of {@link de.uni.leipzig.asv.zitationsgraph.data.Citation} 
 * @author loco
 *@version 0.1
 */

public class ReferenceExtraction{

	
	
	public static final String YEAR = "year";
	
	public static final String TITLE = "title";
	
	public static final String AUTHOR_PART = "authorPart";
	
	/**
	 * templatename in the template builder map for this pattern:<br>
	 *  authors title rest year
	 */
	public static final String MLA_STYLE = "mlaStyle";
	
	
	/**
	 * templatename in the template builder map for this pattern:<br>
	 *  authors year title rest
	 */
	public static final String BIOI_STYLE = "bioiStyle";
	
	
	
	

	public static final long TIMEOUT = 1000;
	
	private static final Logger log = Logger.getLogger(ReferenceExtraction.class.getName());

	/**
	 * tree for the line tokens with the position as key 
	 */
	protected TreeMap<Integer,String> lineTokens;
	
	/**
	 * tree for the founded citations with the position as key
	 */
	protected static TreeMap<Integer,String> referenceMap;
	
	/**
	 * List of pattern for reference recognition
	 */
	protected List<CustomPattern> citationMatcherList;
	
	/**
	 * instance, which is responsible for the name recognition
	 */
	protected AuthorNameRecognition nameRecognizer;
	
	
	/**
	 * best appropriating reference pattern
	 */
	protected Pattern applyingReferencePattern;
	
	/**
	 * citation prefix pattern
	 */
	protected Pattern citationPrefixPattern;
	
	/**
	 * reference part 
	 */
	private String currentText;
	
	/**
	 * true if a prefix is recognized,
	 * false else
	 */
	private boolean hasPrefix;
	
	/**
	 * true if a reference style  is recognized,
	 * false else
	 */
	private boolean citPatternIsRecognized;
	
	protected TemplateBuilder tb;
	
	
	/**
	 * list of Citations as result of this class
	 */
	private Vector<Citation> citationVector;
	
	
		
	
	public ReferenceExtraction(){
		nameRecognizer = new AuthorNameRecognition();
		lineTokens = new TreeMap<Integer,String>();
		referenceMap = new TreeMap<Integer,String>();
		citationMatcherList = new ArrayList<CustomPattern>();
		this.initTemplateBuilder();
		CustomPattern c = new CustomPattern(tb.getTemplate(MLA_STYLE).getTemplate(),0.9f);
		CustomPattern c1 = new CustomPattern(tb.getTemplate(BIOI_STYLE).getTemplate());
		
		
		this.citationMatcherList.add(c1);this.citationMatcherList.add(c);
	}
	
	/**
	 * main method <br>
	 * <b>setup</b><br>
	 * create empty Collections and fill the {@code authorMatcherList} and {@code citationMatcherList}
	 * with CustomMatcher instances, which include the manual created Patterns<br> 
	 * <b>first step</b><br>
	 * tokenize lines, save the position of each line and try to find a prefix<br>
	 * <b>second step</b><br>
	 * test the patterns for references and author names and sort according 
	 * to the matchCount the lists, which include the patterns<br>
	 * <b>third step</b><br>
	 * optional divide the refernce string in single references,
	 * if a prefix is recognized<br>
	 * <b>fourth step</b><br>
	 * find names<br>
	 * <b>fifth step </b><br>
	 * tokenize references based on author name occurrence or on  author name occurrence
	 *and reference style <br>
	 *<b>sixth step</b><br>
	 * remove names witch match the author pattern, but occur behind the author part<br>
	 *<b>seventh step </b><br>
	 *find the title in each reference and the year 	 
	 * @param referenceString the reference part of a scientific paper
	 */
	public void referenceMining (String referenceString){
		
		
		lineTokens.clear();
		referenceMap.clear();
		citationVector = new Vector<Citation>();
		hasPrefix = false;
		this.citPatternIsRecognized = false;
		nameRecognizer.resetRecognizer();
		currentText = referenceString;
		if (referenceString!= null){
			this.lineTokenize();
			log.info("line Tokenize READY");
			if (hasPrefix)
				this.tokenizeCitationsBasedOnPrefix();
			else 
				this.testReferencePatterns();
			log.info("test Reference Patterns READY");
			this.recognizeNames();
			log.info("recognize Names READY");	
			//this.printNames();
			this.tokenizeCitations();
			log.info("tokenize citations READY");	
			this.removeWrongNames();
			log.info("remove wrong names READY");	
			this.findTitles();
			log.info("find titles READY");	
			
		}
	}

	/**
	 * This split the reference string based on the line.separator and store each line in the
	 * lineTokens tree with the position as key for a comfortable navigation.
	 * The first lines will check, if a prefix exist
	 */
	protected  void lineTokenize(){
		int lineNr = 0;
		StringBuffer sb = new StringBuffer();
		lineNr = sb.length();
		int firstLines = 0;
		
		currentText = currentText.trim();
		
		
		String [] lines = currentText.split(System.getProperty("line.separator"));
		log.info("line split "+lines.length);
		for (String line :lines){
			//if (!line.endsWith(" "))
			line = line.replaceAll("\\s+", " "); //remove multiple spaces 
			
			line = line.trim();
			// try to find separator for citations
			if (firstLines<2){
				
				Matcher m = BasicTemplates.squareBracketPattern.matcher(line);
				if (m.find()){
					
					hasPrefix = true;
					this.citationPrefixPattern = BasicTemplates.squareBracketPattern;
				}else {
						Matcher m3 = BasicTemplates.numericalPattern.matcher(line);
						if (m3.find()){
							hasPrefix = true;
							this.citationPrefixPattern = BasicTemplates.numericalPattern;
						}
					}
				
			}
			firstLines ++;
			sb.append(line+" ");
			lineTokens.put(lineNr, line);
			lineNr = sb.length(); // set to next position of the next line
			}
			log.info("has Prefix:"+hasPrefix);
			currentText = sb.toString(); // text without multiple spaces
			
	}
	
	
	/**
	 * This method test each ReferencePattern to find a appropriating style.
	 * The test based on the match Count and a weight value for each pattern. 
	 * The weight should reward specific patterns and punish general patterns.
	 * After processing the list of the citation Patterns is sorted according to the product
	 * of the match Count and the weight. <br> 
	 * Version 0.2 use a Thread cause sometimes it could be possible, that the match waste too
	 * much time. A reason could be a long referencestring, which include e.g. an appendix, 
	 * which was not deleted.
	 * The thread run stop, if the task is finished or the timeout runned out.
	 */
	protected void testReferencePatterns (){
		/*
		int citCountMatch =0;		
		Matcher m ;
		for (CustomPattern cm : this.citationMatcherList){
			m = cm.getPattern().matcher(currentText);
			citCountMatch = 0;
			log.info(cm.getPattern().toString());
			while( m.find()) {
				citCountMatch++;
			}
			if (citCountMatch>2){
				this.citPatternIsRecognized =true;
			}
			cm.setMatchCount(citCountMatch);
			//log.warning(cm.getPattern()+"matches"+cm.getMatchCount());
		}
		
		if (citPatternIsRecognized){
			
			this.applyingReferencePattern = Collections.max(this.citationMatcherList).getPattern();
			
		}*/
		
		ReferenceTestTask testTask = new ReferenceTestTask (this.currentText,this.citationMatcherList);
		Thread testRefThread = new Thread (testTask);
		testRefThread.start();
		long startTime = System.currentTimeMillis();
		long time = 0;
		while (time<TIMEOUT&&testRefThread.isAlive()){
			time = System.currentTimeMillis()-startTime;
		}
		if (time>=TIMEOUT){
			log.info("task was interrupted");
				testRefThread.stop();
		}
	}
	
	/**
	 * This method build with a instance of the 
	 * {@linkplain TemplateBuilder} and the basic patterns of the {@linkplain BasicTemplates}
	 * interface the complex pattern for the author part and the references
	 */
	protected void initTemplateBuilder (){
		tb = new TemplateBuilder();
		tb.addTemplate(YEAR, new TemplateEntity(BasicTemplates.YearPattern));
		tb.addTemplate(TITLE, new TemplateEntity(BasicTemplates.titlePattern));
		
		
		AuthorTemplateEntity ate = new AuthorTemplateEntity ();
		ate.generateMultiTemplate(new Pattern[]{BasicTemplates.surForenameShortPattern,
				BasicTemplates.surForenameCompletePattern});
		AuthorTemplateEntity ate2 = new AuthorTemplateEntity();
		ate2.generateMultiTemplate(new Pattern[]{BasicTemplates.surForenameShortPattern,
				BasicTemplates.surForenameCompletePattern,BasicTemplates.allCompletePattern,BasicTemplates.forenameShortSurNamePattern});
		ate2.concatTemplate(0, 7, ate.getTemplate().pattern(),
				AuthorTemplateEntity.DEFAULT_SEP, AuthorTemplateEntity.DEFAULT_SUFFIX);
		tb.addTemplate(AUTHOR_PART, ate2);
		
		tb.mergeTemplates(3, false,false, "(\\s?\\W\\s?)?",
				AUTHOR_PART, false, TITLE, false, MLA_STYLE);
		tb.mergeTemplates(300, false,false, "(\\s?\\W\\s?)?", MLA_STYLE, false, YEAR, false, MLA_STYLE);
		//tb.mergeTemplates(4, false, false, "(\\s?\\W\\s?)?", MLA_STYLE, false, PAGE, false, MLA_STYLE);
		
		tb.mergeTemplates(6, false, false, "(\\s?\\W\\s?)?",AUTHOR_PART, false, YEAR, false, BIOI_STYLE);
		tb.mergeTemplates(4, false, false, "(\\s?\\W\\s?)?", BIOI_STYLE, false,TITLE,false, BIOI_STYLE);
		/*Matcher m = tb.getTemplate(MLA_STYLE).getTemplate().matcher(currentText);
		
		while(m.find()){
			log.info(m.group());
		}*/
	}

	/**
	 * Either the citations are separated based on the author name occurrences and the 
	 *reference style pattern or only on the author name occurrences, if no reference style
	 *pattern was found
	 */
	protected void tokenizeCitations(){
		if (!hasPrefix){
			if (this.citPatternIsRecognized&&!this.nameRecognizer.getFirstAuthorEntry().isEmpty()){
				this.tokenizeReferencesBasedOnPatternAut();
			}else{
				this.tokenizeCitationBasedOnNames();
			}
		}
	}
	
	/**
	 * If a prefix was found, the text will divided in the discrete reference sections.
	 *The finding of a citation based on the found prefix pattern. For each line the algorithm try to find
	 * a match. If a match was found, a new reference begin otherwise the line belongs to the current reference.
	 * The citationMap store the position as key and the String as citation.   
	 */
	protected void tokenizeCitationsBasedOnPrefix(){
			int lineCount  = 0;
			Matcher sepMatcher;
			int currentMatchKey = 0;
			for (Entry<Integer,String> lineEntry: lineTokens.entrySet()){
				lineCount++;
				sepMatcher = this.citationPrefixPattern.matcher(lineEntry.getValue());
				if (sepMatcher.find()){ // match a new citation section
					
					referenceMap.put(lineEntry.getKey(), lineEntry.getValue());
						currentMatchKey = lineEntry.getKey(); 
				}else{ // line belongs to the current citation section
					referenceMap.put(currentMatchKey,referenceMap.get(currentMatchKey)+
							""+ lineEntry.getValue());
				}
				if (lineCount>15)
					break;
			}
		
		//for (String cit: referenceMap.values()){
		//	log.info(cit);
		//}
	}
	
	/**
	 * tokenize the referencelist string in separate references based on the most suitable pattern style,
	 *which was recognized.<br>
	 *This methode create a matcher with the referencelist string and the recognized pattern.
	 *A reference will be found, if the matcher find a match. Every line belongs to this reference,which
	 *has a line position, which is less than the next match.
	 *@deprecated
	 */
	protected void tokenizeCitationBasedOnPattern(){
		if (this.citPatternIsRecognized){
			Matcher m = this.applyingReferencePattern.matcher(currentText);
			int currentMatch =-1;
			int prevMatch =-1;
			int beginLineKey;
			int endLineKey;
			SortedMap <Integer, String>subLines; 
			while (m.find()){
				log.info(m.group());
				if (prevMatch!=-1){
					
					currentMatch = m.start();
					beginLineKey = lineTokens.floorKey(prevMatch);
					endLineKey = lineTokens.ceilingKey(currentMatch);
					subLines = lineTokens.subMap(beginLineKey, endLineKey);
					for (String line : subLines.values()){
						if (!referenceMap.containsKey(prevMatch))
							referenceMap.put(prevMatch, line );
						else
							referenceMap.put(prevMatch, referenceMap.get(prevMatch)+" "+line);
					}
				}
				prevMatch = m.start();
				
			}
			//last citation
			subLines = lineTokens.subMap(prevMatch,true,lineTokens.lastKey(),true);
			for (String line : subLines.values()){
				if (!referenceMap.containsKey(prevMatch))
					referenceMap.put(prevMatch, line );
				else
					referenceMap.put(prevMatch, referenceMap.get(prevMatch)+" "+line);
			}
			log.info("-------recognize Citations");
			for (String cit :referenceMap.values()){
				log.info(cit);
			}
		}
	}
	
	/**
	 * test print methode to check the right position for lines
	 */
	public void testPos(){
		int endLine ;
		for (Entry<Integer,String> line :lineTokens.entrySet()){
			endLine = (line.getKey()+20<currentText.length())?line.getKey()+20:currentText.length()-1;
			log.info("line#"+ currentText.substring(line.getKey(), endLine));
		}
	}
	
	/**
	 * This recognize the names in Citations, if the reference part string is already separated,
	 * otherwise the hole text is used.
	 */
	private void recognizeNames(){
		if (referenceMap.isEmpty()){
			nameRecognizer.recognizeNamesWithMatcher(currentText, lineTokens,
					tb.getTemplate(AUTHOR_PART).getTemplate().pattern());
		}else 
			nameRecognizer.recognizeNamesInCitation(referenceMap, this.citationPrefixPattern);
	}
	
	
	/**
	 * divide the text in single references based on the occurrence of the found names.
	 * Every line belongs to one reference until a name occurs at the beginning of a line 
	 */
	private void tokenizeCitationBasedOnNames(){
		if (!(hasPrefix ||this.citPatternIsRecognized)){
			if (referenceMap!= null){
				referenceMap.clear();
			}else{
				referenceMap = new TreeMap <Integer,String>();
			}
			
			String reference;
			int lineBeginKey=0;
			int lineEndKey =0;
			
			for (Entry <Integer, Token> entry: nameRecognizer.getNameTree().entrySet()){
				
				if (entry.getValue().isLineBegin()||entry.getKey() == nameRecognizer.getNameTree().lastKey()){
					
					if (entry.getKey() == nameRecognizer.getNameTree().lastKey())
						lineEndKey = currentText.length();
					else
						lineEndKey = entry.getKey()-1;
					
					if (lineEndKey >lineBeginKey){
						reference = currentText.substring(lineBeginKey, lineEndKey);
						referenceMap.put(lineBeginKey, reference);
						//log.info(reference);
					}
					lineBeginKey = entry.getKey();
				}
			}
		}
	}
	
	/**
	 * This method divide the reference part string in single reference based on the 
	 * style pattern and the potential author names.
	 * A matcher, which was created with the found style pattern and the reference string
	 * start at the position of the first author.<br>
	 * If the matcher found something, the position will stored temporally.
	 * The new start position of the matcher will set to the next position of an 
	 * a author name, which occurs at the beginning of a line.
	 * The temporally position and the next match position represent a reference.
	 * The single reference is created by the retrieval of the lines based on the
	 * positions in the "lineTokens" tree.
	 * Each match will checked, if it start with a author name.  
	 */
	private void tokenizeReferencesBasedOnPatternAut(){
		if (referenceMap.isEmpty()){
			int authorPos ;
			if (nameRecognizer.getFirstAuthorEntry().size()!=0){
				authorPos= nameRecognizer.getFirstAuthorEntry().remove(0);
			}else
				authorPos = 0;
			String firstAuthor = "NoAuthor";
			Matcher citMatcher= this.applyingReferencePattern.matcher(currentText);
			boolean hasMatch = false;
			String citationBegin;
			int lineCitStartKey;
			int lineCitEndKey;
			SortedMap <Integer, String> citMap;
			int currentMatch = -1;
			int previousMatch = -1;
			int endMatchPosition = -1;
			do {
				if (citMatcher.find(authorPos)){
					hasMatch = true;
					firstAuthor = nameRecognizer.getNameTree().get(authorPos).getValue();
					citationBegin =citMatcher.group();
					
					if (citationBegin.startsWith(" "+firstAuthor)||citationBegin.startsWith(firstAuthor)){
						
						if (previousMatch !=-1 ){
							currentMatch = citMatcher.start();
							endMatchPosition = currentMatch +citationBegin.length();
							lineCitStartKey = lineTokens.floorKey(previousMatch);
							lineCitEndKey = lineTokens.ceilingKey(currentMatch);
							citMap = lineTokens.subMap(lineCitStartKey, lineCitEndKey);
							
							for (String cit: citMap.values()){
								if (!referenceMap.containsKey(lineCitStartKey)){
									referenceMap.put(lineCitStartKey, cit+" ");
								}else{
									if (!citMap.get(citMap.lastKey()).equals(cit))
										referenceMap.put(lineCitStartKey,referenceMap.get(lineCitStartKey)+cit+" ");
									else
										referenceMap.put(lineCitStartKey,referenceMap.get(lineCitStartKey)+cit);
								}
							}
						}
						previousMatch = citMatcher.start();
					}
					do{
						if (nameRecognizer.getFirstAuthorEntry().size() !=0)
							authorPos = nameRecognizer.getFirstAuthorEntry().remove(0);
						else
							authorPos = currentText.length()-1;
					}while(authorPos<endMatchPosition);
				}else{
					hasMatch = false;
				}
			}while (hasMatch);
			//log.info("preMatch"+previousMatch+" lastKey"+lineTokens.lastKey());
				citMap = lineTokens.subMap(lineTokens.floorKey(previousMatch),true,lineTokens.lastKey(),true);
				for (String line : citMap.values()){
					if (!referenceMap.containsKey(lineTokens.floorKey(previousMatch)))
						referenceMap.put(lineTokens.floorKey(previousMatch), line );
					else
						referenceMap.put(lineTokens.floorKey(previousMatch), referenceMap.get(lineTokens.floorKey(previousMatch))+" "+line);
				}
			
			//log.info("-------recognize Citations");
			//for (String cit :referenceMap.values()){
			//	log.info(cit);
			//}
		}
	}
	
	/**
	 * This find the title of each found reference and build the vector of citations
	 * objects, which include a Publication with the authors, title and year.<br>
	 * The extraction based on a regular expression and start with the search after
	 * the last author name. The regular expression match String, which start with a 
	 * upper case and are more than 3 characters and less than 300 long. 
	 * The match could be too long, because the Regex is greedy.
	 * I had chosen a greedy manner, cause a comma could be a enumeration of sth. or 
	 * a separator, so that it avoid to get a too short title.
	 * Due to this manner a title is cleaned with the cleanTitle method. 
	 * 
	 */
	private void findTitles(){
		int nextCitKey;
		
		String includeTitle;
		String title;
		String year;
		int titleEnd;
		int endIndex;
		
		Matcher titleMatcher;
		Matcher tagMatcher;
		SortedMap<Integer, Token> authorMap;
		Entry<Integer,Token> lastAuthorEntry;
		
		for (Entry<Integer, String> citEntry: referenceMap.entrySet()){ // overall references
			nextCitKey = (referenceMap.higherKey(citEntry.getKey())!= null)?
					referenceMap.higherKey(citEntry.getKey()):currentText.length();
					// get the next position where a new reference begin
			
			authorMap = nameRecognizer.getNameTree().subMap(citEntry.getKey(), nextCitKey);
			/*get all authors of the current reference by use of the nameTree
			 * the authors are between the start of the current reference and the next reference
			 */
			lastAuthorEntry = nameRecognizer.getNameTree().lowerEntry(nextCitKey);
			// get the last author of the reference
			try {
			endIndex = citEntry.getValue().indexOf(lastAuthorEntry.getValue().getValue());
			endIndex+=lastAuthorEntry.getValue().getValue().length();
			// get the position where the last author end
			includeTitle = citEntry.getValue().substring(endIndex);
			//a substring of the reference without the authors
			titleMatcher = BasicTemplates.titlePattern.matcher(includeTitle);
			if(titleMatcher.find()){ // title matching
				if (!authorMap.isEmpty()){
					 // build author list for this reference
					Vector <Author> authors = new Vector <Author>();
					for (Token t : authorMap.values()){
						char lastChar =  t.getValue().charAt(t.getValue().length()-1);
						Author author;
						if (lastChar == ':'||lastChar == ',')
							author =new Author( t.getValue().
									substring(0, t.getValue().length()-1));
						else
							author =new Author (t.getValue());
						authors.add(author);
					}
					
					title = titleMatcher.group();
					title = this.cleanTitle(title);
						title = title.replaceAll("(“|”|\")", "");
						//use match as title 
					
					titleEnd = titleMatcher.start()+title.length()+3;
					year = getYear (includeTitle);
					Publication p = new Publication(authors,title);
					p.setYearString(year);
					Citation c = new Citation(p);
					// set possible prefix 
					if (hasPrefix){
						tagMatcher = citationPrefixPattern.matcher(citEntry.getValue());
						if (tagMatcher.find())
							c.setTag(tagMatcher.group());
					}
					citationVector.add(c);
					/*
					 * sometime a matched reference include more references, if the same authors wrote different
					 * publications
					 */
					
					if (includeTitle.contains("”")||includeTitle.contains("”")){
						if (titleEnd<includeTitle.length()){
							Matcher titleMatch = Pattern.compile("\\s{0,3}?(“|”|\")([A-Z]|\\W)(.|"+
									System.getProperty("line.separator")+"){5,500}?(”|\")")
									.matcher(includeTitle);
							String subTitleString;
							String year2;
							while(titleMatch.find(titleEnd)){
								title = titleMatch.group();
								titleEnd = titleMatch.start()+title.length();
								subTitleString = includeTitle.substring(titleEnd);
								year2 = getYear(subTitleString);
								Publication p2 = new Publication(authors, title);
								p2.setYearString(year2);
								Citation c2 = new Citation(p2);
								citationVector.add(c2);
								log.info(title);
							}
						}//find multiple references for one author
					}else if (includeTitle.contains("-")){
						//citBegin = includeTitle.indexOf("-", citBegin);
					
					}
				}//END authorMap is not empty for current citation
			}//END if titleMatcher find
			}catch (NullPointerException noAuthor){
				log.info("no Author:"+citEntry.getValue());
			}
		}//for each citation
	}
	
	
	/**
	 * clean a match title String 
	 * The assumption are naive. The first assumption is , if a "?" was found it is also the end of a title
	 * The second assumption use the keyword "vol.", which doesn't belongs to a title
	 * The third assumption use the word length by splitting the title by ","
	 * All words, which occur between "," should have a word length more than  4  
	 * @param title
	 * @return cleaned title 
	 */
	private String cleanTitle(String title){
		String cleanTitle = title;
		int sepIndex =title.lastIndexOf("?"); 
		if (sepIndex !=-1){
			cleanTitle = title.substring(0,sepIndex+1);
		}else {
			int end = (title.length()>4)?title.length()-4:0;
			sepIndex = title.lastIndexOf("vol.",end);
			if(sepIndex!=-1){
				cleanTitle = title.substring(0,end);
			}else {
				String[] titleParts = title.split(",");
				if (titleParts.length>0){
					String[] words = titleParts[0].split("\\s+");
					if (words.length>4){
						cleanTitle = titleParts[0];
					}
				}
			}
			
		}
		
		return cleanTitle;
	}

	/**
	 * This method remove potential names in the "nameTree", which occurs
	 * after the author part in a single reference. This method will only applied, if a style was recognized
	 */
	private void removeWrongNames(){
		Matcher authorSepMatcher ;
		int end,start;
		List <Integer> removeKeys = new ArrayList<Integer>();
		SortedMap <Integer, Token> authorMap;
		if (citPatternIsRecognized){
			for (Entry <Integer,String> cit:referenceMap.entrySet()){
				removeKeys.clear();
				authorSepMatcher = tb.getTemplate(AUTHOR_PART).getTemplate().matcher(cit.getValue());
				if (authorSepMatcher.find()){ //first match
					if (authorSepMatcher.start()<=2){
						end =authorSepMatcher.end();
					
						authorMap = nameRecognizer.getNameTree().subMap(cit.getKey()+end, cit.getKey()+cit.getValue().length());
						
						for (int key: authorMap.keySet()){
							removeKeys.add(key);
						}
					}
				}
				for (int key :removeKeys){
					nameRecognizer.getNameTree().remove(key);
				}
			}//for citations
		}//citation Pattern true
	}
	
	public void printNames(){
		System.out.println("found names:" +nameRecognizer.getNameTree().size());
		for (int key :nameRecognizer.getNameTree().keySet()){
			System.out.println(key +"\t"+nameRecognizer.getNameTree().get(key));
		}
	}
	
	/**
	 * This method use the first match of a regular expression as year
	 * @param referenceString
	 * @return year as string
	 */
	private String getYear(String reference) {
		String year = null;
		Matcher yearMatcher = BasicTemplates.YearPattern.matcher(reference);
		if (yearMatcher.find()){
			year = yearMatcher.group();
		
			
		}
		return year;
	}

	public  void testPrintCitations(){
		for (Citation c: citationVector){
			System.out.println(c.toString());
		}
	}
	
	
	
	/**
	 * serialization of the citation vector
	 * @param fileName
	 */
	
	private void saveCitationList(String fileName){
		try {
			FileOutputStream file = new FileOutputStream( "examples/serializedTestRefList/"+fileName+".ref");
			ObjectOutputStream o = new ObjectOutputStream( file );
			o.writeObject(citationVector);
			o.flush();
			o.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
	}
	
	
	
	public  Vector<Citation> getCitationVector() {
		return citationVector;
	}

	public static void setCitationVector(Vector<Citation> citationVector) {
		citationVector = citationVector;
	}

	public String getCurrentText() {
		return currentText;
	}

	public void setCurrentText(String currentText) {
		this.currentText = currentText;
	}

	private class ReferenceTestTask implements Runnable{

		private String plainText;
		private  List <CustomPattern> patList;
	
		
		public ReferenceTestTask(String plainText,List<CustomPattern> referencePat){
			this.plainText = plainText;
			this.patList = referencePat;
			
		}
		
		@Override
		public void run() {
			
			int citCountMatch =0;
			
			
			Matcher m ;
			for (CustomPattern cm : this.patList){
				m = cm.getPattern().matcher(plainText);
				citCountMatch = 0;
				while( m.find()) {
					citCountMatch++;
				}
				if (citCountMatch>2){
					citPatternIsRecognized =true;
				}
				cm.setMatchCount(citCountMatch);
				//log.warning(cm.getPattern()+"matches"+cm.getMatchCount());
			}
			
			if (citPatternIsRecognized){
				
				applyingReferencePattern = Collections.max(patList).getPattern();
			}
			
		}
	}
	
	public static void main (String[] args){
		String[] test = new String[]{
/*0*/				"examples/Ngonga Ermilov - Complex Linking in a Nutshel.pdf",
/*1*/				"examples/journal.pone.0027856.pdf"	,
/*2*/					"examples/Lit Linguist Computing-2010-Craig-37-52.pdf",
/*3*/					"examples/87-398-1-SM.pdf",
/*4*/					"examples/65-314-1-PB.pdf",
/*5*/					"examples/Digital Humanities 2008 Book of Abstracts.pdf",
/*6*/					"examples/Lit Linguist Computing-2008-Windram-443-63.pdf",
/*7*/					"examples/Lit Linguist Computing-2008-Wandl-Vogt-201-17.pdf",
						"examples/DH/2007/ADescriptiveClassification.txt",
						"examples/DH/" +
						"2008/AnInterdisciplinaryPerspective.txt",
						"examples/Lit/2010/Lit Linguist Computing-2010-Holmes-179-97.pdf"

		};
			
			BaseDoc bd = new BaseDoc ("C:/Users/loco/examples/examples/" +
					"Lit/2010/Lit Linguist Computing-2010-Hellwig-105-18.pdf");
			try {
				//bd.process();
				//bd.splitFullText();
				//System.out.println(bd.get(BaseDoc.REFERENCES));
				
				
				StringBuffer sb = new StringBuffer();
				BufferedReader br = new BufferedReader (new FileReader("C:/Users/loco/examples" +
						"/examples/referenceTestPart/" +
						"5-204-1-PB.pdf.ref"));
				while (br.ready()){
					sb.append(br.readLine()+System.getProperty("line.separator"));
				}
				
				ReferenceExtraction cer = new ReferenceExtraction();
				cer.referenceMining(sb.toString());
				//cer.saveCitationList("LiteraryandLinguisticComputingCraig");
				//ReferenceExtraction.getTestList("LiteraryandLinguisticComputingCraig");
				cer.testPrintCitations();
			} catch (IOException e) {
				
				e.printStackTrace();
			} 
	}
}

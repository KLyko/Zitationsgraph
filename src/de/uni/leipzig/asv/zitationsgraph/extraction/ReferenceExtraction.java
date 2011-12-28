package de.uni.leipzig.asv.zitationsgraph.extraction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import de.uni.leipzig.asv.zitationsgraph.data.Citation;
import de.uni.leipzig.asv.zitationsgraph.data.Publication;
import de.uni.leipzig.asv.zitationsgraph.preprocessing.BaseDoc;

/**
 * This class find the references with the authors, title and year in a reference Part string
 * of scientific Paper. The main method is the {@code referenceMining (String referenceString)},
 * which receive a reference part as string. The result is a list of {@link de.uni.leipzig.asv.zitationsgraph.data.Citation} 
 * @author loco
 *@version 0.1
 */

public class ReferenceExtraction {


	private static final int MAX_DISTANCE = 100;
	
	private static final int HIGHVALUE =2000000;
	
	private static final int ALLOWED_DISTANCE = 7;
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat ("dd-MM-yyyy");
	
	
	private static final Pattern squareBracketPattern = Pattern.compile("(\\s{0,2}?\\[.*\\])");
	private static final Pattern roundBracketPattern = Pattern.compile("^(\\s?\\(?.{1,5}\\))");
	private static final Pattern numericalPattern = Pattern.compile("^(\\s?[1-9][0-9]{0,1}[\\.])");
	
	private static final Pattern surForenameShortPattern = Pattern.
	compile("((Mc|van den?|Van den?|de|De|Ó)\\s{0,2}?)?"+ //prefix
			"([A-Z][\\w|ä|ü|ö|’|é|á]{1,30}\\s{0,2}?){1,5}"+ //surname
			"(\\s{0,2}?-[A-Z]?[\\w|ä|ü|ö|é|á]{2,30}){0,2}\\s{0,2}?" + //optional with bindestrich
			",(\\s{0,2}?[A-Z](\\.|[^\\w])-?){1,3}"+//forename is separated with comma and the name is with punct
			"(\\s{0,2}?(de|la)){0,2}(\\s{0,2}?di\\s[A-Z]\\w{3,10})?"); //spanish names
	/*
	private static final Pattern surForenameShortWithoutPunctPattern =Pattern.
	compile("((Mc|van den?|Van den?|de|De|Ó)\\s?)?"+ //prefix optional
			"([A-Z][\\w|’|é|á]{1,20}\\s?){1,5}"+ //surName
			"(\\s?-[A-Z]?[\\w|é|á]{1,20}){0,2}"+ // with "bindestrich" optional
			"\\s?([A-Z]){1,3}(\\s|,|:)"); // forename for example AB
 */
	private static final Pattern forenameShortSurNamePattern = Pattern.
	compile("([A-Z]\\.[\\s{0,2}?|-]){1,3}\\s{0,2}?" +// forename
			"((Mc|van den?|de|De|Van den?|Ó)\\s{0,2}?)?" + //prefix optional
			"([A-Z][\\w|ä|ü|ö|’|é|á]{1,30}\\s{0,2}?){1,5}" + // surname
			"(\\s{0,2}?-[A-Z]?[\\w|ä|ü|ö|’|é|á]{2,30}){0,2}");//with bindestrich
	
	private static final Pattern surForenameCompletePattern = Pattern.
	compile("((Mc|van den?|Van den?|de|De|Ó)\\s{0,2}?)?" +//prefix optional
			"([A-Z][\\w|ä|ü|ö|’|é|á]{1,30}\\s{0,2}?)" +//surname
			"(\\s{0,2}?-[A-Z]?[\\w|ä|ü|ö|’|é|á]{1,30}){0,2}\\s{0,2}?" +//with Bindestrich optional
			",\\s{0,2}?[A-Z][\\w|ä|ü|ö|’|é|á]{3,30}(\\s{0,2}?[A-Z]\\.)?"); //forname complete
	
	private static final Pattern allCompletePattern = Pattern.
	compile("([A-Z][\\w]{1,30}\\s{0,2}?){1,2}"+ //firstname complete
			"(\\s{0,2}?-[A-Z]?[\\w|ä|ü|ö|’|é|á]{1,30}){0,2}"+ //for names like Anne-Marie
			"(\\s{0,2}?(Mc|van den?|Van den?|de|De|Ó)\\s{0,2}?)?"+ // prefix
			"(\\s{0,2}?[A-Z][\\w|’|ä|ü|ö|é|á]{1,30}\\s{0,2}?){1,5}"+ // surname
			"(\\s{0,2}?-[A-Z]?[\\w|ä|ü|ö|’|é|á]{1,30}){0,2}" // surname with Bindestrich
			);
	
	private static final Pattern titlePattern = Pattern.compile("[A-Z](\\w|[\\W{Punct}&&[^\\.]]){5,300}[\\.|\\?]");
	
	private static final Pattern BIOIStylePattern = Pattern.compile(
			"(Mc|van den?|Van den?|de|De|[A-Z]|Ó)(\\D){5,300}?\\(([1-2][0-9]{3}[a-e]?|eds|n\\.d\\.)\\)");
	
	private static final Pattern JCBStylePattern = Pattern.compile("(Mc|van den?|Van den?|de|De|[A-Z]|Ó).{5,300}?[1-2][0-9]{3}\\..+");
	private static final Pattern MISQStylePattern = Pattern.compile("(Mc|van den?|Van den?|de|De|[A-Z]|Ó).{5,500}?(“|”|\")([A-Z]|\\W)(.|"+
			System.getProperty("line.separator")+"|[^([1-2][0-9]{3})]){5,500}?(”|\")");


	private static final Pattern YearPattern = Pattern.compile("[1-2][0-9]{3}");
	private static final Logger log = Logger.getLogger(ReferenceExtraction.class.getName());

	/**
	 * tree for the line tokens with the position as key 
	 */
	private static TreeMap<Integer,String> lineTokens;
	
	
	/**
	 * tree for the founded citations with the position as key
	 */
	private static TreeMap<Integer,String> referenceMap;
	
	/**
	 * tree for the founded names with the applying author pattern
	 * the key is the position and the value is a potential author name
	 */
	private TreeMap<Integer,Token> nameTree;
	
	/**
	 * List of position values for the occurence of authors at the beginning of a line
	 */
	private List<Integer>firstAuthorEntry;
	/**
	 * List of Pattern for the author recognition
	 */
	private List<CustomMatcher> authorMatcherList ;
	
	/**
	 * List of pattern for reference recognition
	 */
	private List<CustomMatcher> citationMatcherList;
	
	
	/**
	 * best appropriating author pattern
	 */
	private Pattern applyingAuthorPattern;
	
	/**
	 * best appropriating reference pattern
	 */
	private Pattern applyingReferencePattern;
	
	/**
	 * citation prefix pattern
	 */
	private Pattern citationPrefixPattern;
	
	
	/**
	 * if the references have a dominant style, the author part and the title part
	 * are separated with a specific pattern 
	 */
	private Pattern authorSeparationPattern;
	
	/**
	 * reference part 
	 */
	private static String currentText;
	
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
	
	
	/**
	 * list of Citations as result of this class
	 */
	private static Vector<Citation> citationVector;
		
	public ReferenceExtraction(){
		
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
	 * @throws IOException
	 */
	public void referenceMining (String referenceString){
		lineTokens = new TreeMap<Integer,String>();
		referenceMap = new TreeMap<Integer,String>();
		citationVector = new Vector<Citation>();
		
		authorMatcherList = new ArrayList<CustomMatcher>();
		citationMatcherList = new ArrayList<CustomMatcher>();
		CustomMatcher a = new CustomMatcher(surForenameShortPattern);
		CustomMatcher a1 = new CustomMatcher(forenameShortSurNamePattern);
	
		CustomMatcher a3 = new CustomMatcher(surForenameCompletePattern,0.9f);
		CustomMatcher a4 = new CustomMatcher (allCompletePattern,0.75f);
		authorMatcherList.add(a);
		authorMatcherList.add(a1);authorMatcherList.add(a3);authorMatcherList.add(a4);
		
		CustomMatcher c = new CustomMatcher(MISQStylePattern,1.5f);
		CustomMatcher c1 = new CustomMatcher(BIOIStylePattern);
		CustomMatcher c2 = new CustomMatcher (JCBStylePattern);
		this.citationMatcherList.add(c);this.citationMatcherList.add(c1);this.citationMatcherList.add(c2);
		
		
		currentText = referenceString;
			this.lineTokenize();
			
			this.testReferencePatterns();
			
			this.testAuthorPatterns();
			
			if (hasPrefix)
			this.tokenizeCitationsBasedOnPrefix();
			
			this.recognizeNames();
			//this.printNames();
			this.tokenizeCitations();
			
			this.removeWrongNames();
		
			this.findTitles();
			
	}

	/**
	 * This split the reference string based on the line.separator and store each line in the
	 * lineTokens tree with the position as key for a comfortable navigation.
	 * The first lines will check, if a prefix exist
	 */
	private  void lineTokenize(){
		int lineNr = 0;
		StringBuffer sb = new StringBuffer();
		
		int firstLines = 0;
		
		currentText = currentText.trim();
		
		
		String [] lines = currentText.split("\\r\\n");
		log.info("line split "+lines.length);
		for (String line :lines){
			
			line = line.replaceAll("\\s+", " "); //remove multiple spaces 
			
			// try to find separator for citations
			if (firstLines<2){
				
				Matcher m = squareBracketPattern.matcher(line);
				if (m.find()){
					
					hasPrefix = true;
					this.citationPrefixPattern = squareBracketPattern;
				}else{
					Matcher m2 = roundBracketPattern.matcher(line);
					if (m2.find()){
						
						hasPrefix = true;
						this.citationPrefixPattern = roundBracketPattern;
					}else {
						Matcher m3 = numericalPattern.matcher(line);
						if (m3.find()){
							hasPrefix = true;
							this.citationPrefixPattern = numericalPattern;
						}
					}
				}
				
				firstLines ++;
			}
			sb.append(line+" "); 
			
			lineTokens.put(lineNr, line);
			lineNr = sb.length(); // set to next position of the next line
			}
			currentText = sb.toString(); // text without line separators
			//log.warning("has Prefix "+hasPrefix);
	}
	
	
	/**
	 * This method test each ReferencePattern to find a appropriating style.
	 * The test based on the match Count and a weight value for each pattern. 
	 * The weight should reward specific patterns and punish general patterns.
	 * After processing the list of the citation Patterns is sorted according to the product
	 * of the match Count and the weight. <br> 
	 * The authorSeparationPattern is created based on the found dominant reference pattern.
	 */
	private void testReferencePatterns (){
		int citCountMatch =0;
		
		
		Matcher m ;
		for (CustomMatcher cm : this.citationMatcherList){
			m = cm.getPattern().matcher(currentText);
			citCountMatch = 0;
			while(m.find()){
				citCountMatch++;
				if (citCountMatch>2){
					this.citPatternIsRecognized =true;
				}
			}
			cm.setMatchCount(citCountMatch);
			log.warning(cm.getPattern()+"matches"+cm.getMatchCount());
		}
		
		if (citPatternIsRecognized){
			this.applyingReferencePattern = Collections.max(this.citationMatcherList).getPattern();
			/*
			 * find Pattern which separate the authors and the title
			 */
			if (applyingReferencePattern == BIOIStylePattern){
				this.authorSeparationPattern = Pattern.compile("\\(([1-2][0-9]{3}[a-e]?|eds|n\\.d\\.)\\)");
			}else if (applyingReferencePattern == JCBStylePattern){
				this.authorSeparationPattern = Pattern.compile("[1-2][0-9]{3}\\.");
			}else if(applyingReferencePattern == MISQStylePattern) {
				this.authorSeparationPattern = Pattern.compile("(“|”|\")");
			}
			log.info(this.applyingReferencePattern.toString());
		}
		
		
	
	}
	
	/**
	 * Either the citations are separated based on the author name occurrences and the 
	 *reference style pattern or only on the author name occurrences, if no reference style
	 *pattern was found
	 */
	private void tokenizeCitations(){
		if (!hasPrefix){
			if (this.citPatternIsRecognized){
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
	private void tokenizeCitationsBasedOnPrefix(){
			
			Matcher sepMatcher;
			int currentMatchKey = 0;
			for (Entry<Integer,String> lineEntry: lineTokens.entrySet()){
				sepMatcher = this.citationPrefixPattern.matcher(lineEntry.getValue());
				if (sepMatcher.find()){ // match a new citation section
					
					referenceMap.put(lineEntry.getKey(), lineEntry.getValue());
						currentMatchKey = lineEntry.getKey(); 
				}else{ // line belongs to the current citation section
					referenceMap.put(currentMatchKey,referenceMap.get(currentMatchKey)+
							""+ lineEntry.getValue());
				}
			}
		
		for (String cit: referenceMap.values()){
			log.info(cit);
		}
	}
	
	/**
	 * 
	 * tokenize the referencelist string in separate references based on the most suitable pattern style,
	 *which was recognized.<br>
	 *This methode create a matcher with the referencelist string and the recognized pattern.
	 *A reference will be found, if the matcher find a match. Every line belongs to this reference,which
	 *has a line position, which is less than the next match.
	 *@deprecated 
	 */
	private void tokenizeCitationBasedOnPattern(){
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
	 * 
	 * This method test each author pattern similar like {@code testReferencePattern()}
	 * But the tested Strings are the starts of a line, cause a author occurs at the
	 * beginning of a reference 
	 */
	private  void testAuthorPatterns(){
		Matcher m ;
		String subString;
		int end;
		for (CustomMatcher ap :this.authorMatcherList){
			
			for (String line : this.lineTokens.values()){
				
				end = (line.length()>60)? 60 : line.length();
				subString = line.substring(0, end);
				m = ap.getPattern().matcher(subString);
				if (m.find()){
					//System.out.println("pattern"+ entry.getKey().toString());
					
					ap.setMatchCount(ap.getMatchCount()+1);
				}
				
			}
		}
		Collections.sort((List<CustomMatcher>)this.authorMatcherList);
		this.applyingAuthorPattern = authorMatcherList.get(authorMatcherList.size()-1).getPattern();
		for (CustomMatcher ap : this.authorMatcherList){
			log.info( "number of Matches "+ap.getPattern().toString()+" number " +ap.getMatchCount());
		}
		
	}
	
	/**
	 * This recognize the names in Citations, if the reference part string is already separated,
	 * otherwise the hole text is used.
	 */
	private void recognizeNames(){
		if (referenceMap.isEmpty()){
			this.recognizeNamesWithMatcher();
		}else 
			recognizeNamesInCitation();
	}
	
	/**
	 * 
	 */
	private void recognizeNamesInCitation(){
		if (nameTree==null){
			nameTree = new TreeMap<Integer,Token>();
		}
		nameTree.clear();
		String authorPart;
		int authorPartIndex;
		Matcher bestAuthMatcher;
		Matcher secondAuthMatcher;
		Matcher authorPartMatcher;
		int globalKey;
		int minPos =0;
		int tempMinPos,tempMinPos2;
		String foundName;
		
		boolean isFirstLine;
		int previousAuthorKey;
		int previousAuthorDistance;
		Token previousToken;
		
		
		boolean hasMatch;
		for (Entry<Integer,String> citationEntry :referenceMap.entrySet()){
			authorPart = citationEntry.getValue();
			if (authorSeparationPattern!= null){
			authorPartMatcher = this.authorSeparationPattern.matcher(citationEntry.getValue());
			// get the author part string of the reference
				if (authorPartMatcher.find()){
					authorPartIndex = authorPartMatcher.start();
					authorPart = citationEntry.getValue().substring(0, authorPartIndex);
				}
			}
			// best author matcher
			bestAuthMatcher = this.applyingAuthorPattern.matcher(authorPart);
			/*
			 *  second best matcher one style use two style for the authors, therefore I use 
			 *  a second matcher
			 */
			secondAuthMatcher = this.authorMatcherList.get(
					authorMatcherList.size()-2).getPattern().matcher(authorPart);
			minPos = 0;
			do{
				tempMinPos =tempMinPos2 =HIGHVALUE;
				hasMatch = false;
				if (bestAuthMatcher.find(minPos)){
					tempMinPos = bestAuthMatcher.start();
					hasMatch = true;
				}
				if (secondAuthMatcher.find(minPos)){
					tempMinPos2 = secondAuthMatcher.start();
					hasMatch = true;
				}
				if (!hasMatch){
					secondAuthMatcher = this.authorMatcherList.get(authorMatcherList.size()-3)
					.getPattern().matcher(authorPart);
					if (secondAuthMatcher.find(minPos)){
						tempMinPos2 = secondAuthMatcher.start();
						hasMatch = true;
					}
				}
				if (hasMatch){
					if (tempMinPos <=tempMinPos2){
						foundName = bestAuthMatcher.group();
						minPos = tempMinPos;
					}else {
						foundName = secondAuthMatcher.group();
						minPos = tempMinPos2;
					}
					
					globalKey = minPos+citationEntry.getKey();
					previousAuthorKey = (nameTree.lowerKey(globalKey)!=null)
					? nameTree.lowerKey(globalKey):-1;
					previousToken = nameTree.get(previousAuthorKey);
					previousAuthorDistance =(previousToken != null)
					?Math.abs(globalKey-(previousAuthorKey+previousToken.getValue().length())):MAX_DISTANCE;
					
					if (hasPrefix){
						int tagEnd;
						Matcher  tagMatcher  = this.citationPrefixPattern.matcher(citationEntry.getValue());
						if (tagMatcher.find()){
							tagEnd = tagMatcher.end();
							if (Math.abs(minPos-tagEnd)<=1)
								isFirstLine = true;
							else 
								isFirstLine = false;
						}else {
							isFirstLine = false;
						}	
					}else {
						if (minPos<=1)
							isFirstLine = true;
						else
							isFirstLine = false;
					}
					if (isFirstLine||previousAuthorDistance< ALLOWED_DISTANCE){
						Token t = new Token (foundName,Token.NAME);
						t.setLineBegin(isFirstLine);
						
						nameTree.put(globalKey, t);
						
					}
					minPos +=foundName.length();
				}
			}while (hasMatch);
		}
	}
	/**
	 * This method find potential author names based on the first and second dominant
	 * author pattern.<br>
	 * Each matcher start at position 0. If one matcher find something, it will check which
	 * matcher has the first match. The matcher with the first match will use.
	 * A match is accepted, if it match at the start of a line or the
	 * distance between a previous match is smaller than a given threshold. The assumption
	 * is, that the author occurs in a neighborhood and not randomly.
	 *<li>check of the linebeginninng test work as following:<br>
	 * <ol>use the match.start() to get the beginning position of the match in the text</ol>
	 * <ol>retrieve the line, which include the match string with the {@code floorKey} 
	 * method of the "lineTokens" tree. If the line start with the match string, it could be a
	 * potential name and is stored in the "nameTree" with the position in the reference 
	 * part string </ol></li>
	 * <li>distance check:
	 * <ol>retrieve the previous match with the {@code lowerKey} method</ol>
	 * <ol>check the number of characters between the current match and the previous match</ol>
	 * </li> 
	 * Each matcher start at the position after the current match position for the next match. 
	 */
	private void recognizeNamesWithMatcher(){
		if (nameTree==null){
			nameTree = new TreeMap<Integer,Token>();
		}
		nameTree.clear();
		this.firstAuthorEntry = new ArrayList<Integer>();
		
		String text = currentText;
		
		int previousKey;
		int previousDistance;
		Token previousToken;
		int lineKey;
		int begin,begin2;
		int minPosition =0;
		int tempMinPos =10000; 
	
		String name ="";
		String line;
		
		boolean patternMatch;
		
		Matcher secondAuthMatcher;
		Matcher bestAuthMatcher;
		
		Matcher currentMatcher;
		boolean isFirstLine;
		bestAuthMatcher = applyingAuthorPattern.matcher(text);
		
		secondAuthMatcher = this.authorMatcherList.get(authorMatcherList.size()-2).
			getPattern().matcher(text);
		//for (String line : lineTokens.values()){	
			
		
		do{	
			//begin=begin2=begin3=begin4=begin5 =0;
			tempMinPos = HIGHVALUE;
			currentMatcher = null;
			patternMatch = false;
			
			if(bestAuthMatcher.find(minPosition)){
				begin = bestAuthMatcher.start();
				tempMinPos = begin;
				currentMatcher =bestAuthMatcher;
				patternMatch = true;
			}
			
			if (secondAuthMatcher.find(minPosition)){
				
				begin2 = secondAuthMatcher.start();
				if (begin2 <tempMinPos){
					tempMinPos = begin2;
					currentMatcher = secondAuthMatcher;
					patternMatch = true;
				}
				
			}
			minPosition = tempMinPos;
			if (patternMatch){
				name = currentMatcher.group();
				previousKey = (nameTree.lowerKey(minPosition)!=null)
				? nameTree.lowerKey(minPosition):-1;
				previousToken = nameTree.get(previousKey);
				previousDistance =(previousToken != null)
				?Math.abs(minPosition-(previousKey+previousToken.getValue().length())):MAX_DISTANCE;
				lineKey = lineTokens.floorKey(minPosition);
				line =lineTokens.get(lineKey);
				
				 if(Math.abs(minPosition-lineKey)<=1){
					 isFirstLine = true;
				}else {
					isFirstLine = false;
				}
				
				
				if (isFirstLine||previousDistance< ALLOWED_DISTANCE){
					Token t = new Token (name,Token.NAME);
					
					t.setLineBegin(isFirstLine);
					if (isFirstLine){
						this.firstAuthorEntry.add(minPosition);
					}
					nameTree.put(minPosition, t);
					
				}
				minPosition += name.length()-1;
				}
				
			}while (patternMatch);
		
	}
	
	
	public void printNames(){
		System.out.println("found names:" +nameTree.size());
		for (int key :nameTree.keySet()){
			System.out.println(key +"\t"+nameTree.get(key));
		}
	}
	
	
	
	
	
	
	/**
	 * divide the text in single references based on the occurrence of the found names
	 * Every line belongs to one reference until a name occurs at the beginning of a line 
	 */
	private void tokenizeCitationBasedOnNames(){
		if (!(hasPrefix ||this.citPatternIsRecognized)){
			if (referenceMap!= null){
				referenceMap.clear();
			}else{
				referenceMap = new TreeMap <Integer,String>();
			}
			String line;
			String reference;
			int lineBeginKey=0;
			int lineEndKey =0;
			
			for (Entry <Integer, Token> entry: nameTree.entrySet()){
				
				if (entry.getValue().isLineBegin()||entry.getKey() == nameTree.lastKey()){
					
					if (entry.getKey() == nameTree.lastKey())
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
		if (this.referenceMap.isEmpty()){
			int authorPos = this.firstAuthorEntry.remove(0);
			String firstAuthor;
			Matcher citMatcher= this.applyingReferencePattern.matcher(currentText);
			boolean hasMatch = false;
			String citationBegin;
			int lineCitStartKey;
			int lineCitEndKey;
			SortedMap <Integer, String> citMap;
			int currentMatch = -1;
			int previousMatch = -1;
			do {
				if (citMatcher.find(authorPos)){
					hasMatch = true;
					firstAuthor = nameTree.get(authorPos).getValue();
					citationBegin =citMatcher.group();
					
					if (citationBegin.startsWith(" "+firstAuthor)||citationBegin.startsWith(firstAuthor)){
						//log.info(citationBegin);
						if (previousMatch !=-1){
							currentMatch = citMatcher.start();
							lineCitStartKey = lineTokens.floorKey(previousMatch);
							lineCitEndKey = lineTokens.ceilingKey(currentMatch);
							citMap = lineTokens.subMap(lineCitStartKey, lineCitEndKey);
							for (String cit: citMap.values()){
								if (!this.referenceMap.containsKey(lineCitStartKey)){
									referenceMap.put(lineCitStartKey, cit);
								}else{
									referenceMap.put(lineCitStartKey,referenceMap.get(lineCitStartKey)+cit);
								}
							}
						}
						previousMatch = citMatcher.start();
					}
					if (firstAuthorEntry.size() !=0)
						authorPos = this.firstAuthorEntry.remove(0);
					else
						authorPos = currentText.length()-1;
				}else{
					hasMatch = false;
				}
			}while (hasMatch);
			citMap = lineTokens.subMap(lineTokens.floorKey(previousMatch),true,lineTokens.lastKey(),true);
			for (String line : citMap.values()){
				if (!referenceMap.containsKey(lineTokens.floorKey(previousMatch)))
					referenceMap.put(lineTokens.floorKey(previousMatch), line );
				else
					referenceMap.put(lineTokens.floorKey(previousMatch), referenceMap.get(lineTokens.floorKey(previousMatch))+" "+line);
			}
			log.info("-------recognize Citations");
			for (String cit :referenceMap.values()){
			}
		}
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
				authorSepMatcher = this.authorSeparationPattern.matcher(cit.getValue());
				if (authorSepMatcher.find()){ 
					end =authorSepMatcher.end();
					start = authorSepMatcher.start();
					authorMap = this.nameTree.subMap(cit.getKey()+end, cit.getKey()+cit.getValue().length());
					
					for (int key: authorMap.keySet()){
						removeKeys.add(key);
					}
				}
				for (int key :removeKeys){
					this.nameTree.remove(key);
				}
			}//for citations
		}//citation Pattern true
	}
	
	/**
	 * This find the title of each found reference based on a regular expression
	 */
	private void findTitles(){
		int nextCitKey;
		String lastAuthor;
		String includeTitle;
		String title;
		String year;
		int titleEnd;
		int endIndex;
		
		Matcher titleMatcher;
		Matcher tagMatcher;
		SortedMap<Integer, Token> authorMap;
		Entry<Integer,Token> lastAuthorEntry;
		
		for (Entry<Integer, String> citEntry: referenceMap.entrySet()){
			nextCitKey = (referenceMap.higherKey(citEntry.getKey())!= null)?referenceMap.higherKey(citEntry.getKey()):currentText.length();
			authorMap = nameTree.subMap(citEntry.getKey(), nextCitKey);
			lastAuthorEntry = nameTree.lowerEntry(nextCitKey);
			try {
			endIndex = citEntry.getValue().indexOf(lastAuthorEntry.getValue().getValue());
			endIndex+=lastAuthorEntry.getValue().getValue().length();
			includeTitle = citEntry.getValue().substring(endIndex);
			log.info(includeTitle);
			if (this.applyingReferencePattern == MISQStylePattern){
				titleMatcher = Pattern.compile("\\s?(“|”)([A-Z]|\\W)(.|"+
						System.getProperty("line.separator")+"){5,400}?(”)")
						.matcher(includeTitle);
				
			}else{
				titleMatcher = titlePattern.matcher(includeTitle);
			}
			if(titleMatcher.find()){
				if (!authorMap.isEmpty()){
					
					Vector <String> authors = new Vector <String>();
					for (Token t : authorMap.values()){
						char lastChar =  t.getValue().charAt(t.getValue().length()-1);
						String author;
						if (lastChar == ':'||lastChar == ',')
							author = t.getValue().substring(0, t.getValue().length()-1);
						else
							author =t.getValue();
						authors.add(author);
					}
					title = titleMatcher.group();
					if (title.contains("“")||title.contains("”")){
						
						title = title.replaceAll("(“|”)", "");
					}
					
					titleEnd = titleMatcher.start()+title.length()+3;
					year = getYear (includeTitle);
					Publication p = new Publication(authors,title);
					p.setYearString(year);
					Citation c = new Citation(p);
					if (hasPrefix){
						tagMatcher = citationPrefixPattern.matcher(citEntry.getValue());
						if (tagMatcher.find())
							c.setTag(tagMatcher.group());
					}
					citationVector.add(c);
					
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
							}
						}//find multiple references for one author
					}else if (includeTitle.contains("-")){
						//citBegin = includeTitle.indexOf("-", citBegin);
					
					}
				}//END authorMap is not empty for current citation
			}//END if titleMatcher find
			}catch (NullPointerException noAuthor){
				System.out.println(citEntry.getValue());
			}
		}//for each citation
	}
	
	/**
	 * 
	 * @param includeTitle substring of a refernce string
	 * @return year as string
	 */
	private String getYear(String includeTitle) {
		log.info(includeTitle);
		String year = null;
		Matcher yearMatcher = YearPattern.matcher(includeTitle);
		if (yearMatcher.find()){
			year = yearMatcher.group();
			log.info(year);
			
		}
		return year;
	}

	public void testPrintCitations(){
		for (Citation c: citationVector){
			System.out.println(c.toString());
		}
	}
	
	public static void main (String[] args){
		String[] test = new String[]{
/*0*/				"examples/Ngonga Ermilov - Complex Linking in a Nutshel.pdf",
/*1*/				"examples/journal.pone.0027856.pdf"	,
					"examples/Lit Linguist Computing-2010-Craig-37-52.pdf",
					"examples/87-398-1-SM.pdf",
					"examples/65-314-1-PB.pdf",
					"examples/Digital Humanities 2008 Book of Abstracts.pdf",
					"Lit Linguist Computing-2007-García-49-66.pdf",
		};
			
			BaseDoc bd = new BaseDoc (test[2]);
			try {
				//bd.process();
				//bd.splitFullText();
				//System.out.println(bd.get(BaseDoc.REFERENCES));
				
				
				StringBuffer sb = new StringBuffer();
				BufferedReader br = new BufferedReader (new FileReader("examples/quotaexample.txt"));
				while (br.ready()){
					sb.append(br.readLine()+"\r\n");
				}
				
				ReferenceExtraction cer = new ReferenceExtraction();
				cer.referenceMining(sb.toString());
				cer.testPrintCitations();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			
			
				
			
	}
}

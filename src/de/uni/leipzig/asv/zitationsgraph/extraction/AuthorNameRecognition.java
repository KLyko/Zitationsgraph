package de.uni.leipzig.asv.zitationsgraph.extraction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni.leipzig.asv.zitationsgraph.extraction.templates.BasicTemplates;

/**
 * 
 * @author loco
 *@version 0.2
 */
public class AuthorNameRecognition {
	

	/*
	private static final Pattern surForenameShortWithoutPunctPattern =Pattern.
	compile("((Mc|van den?|Van den?|de|De|Ó)\\s?)?"+ //prefix optional
			"([A-Z][\\w|’|é|á]{1,20}\\s?){1,5}"+ //surName
			"(\\s?-[A-Z]?[\\w|é|á]{1,20}){0,2}"+ // with "bindestrich" optional
			"\\s?([A-Z]){1,3}(\\s|,|:)"); // forename for example AB
 */
	
	private static final Logger log = Logger.getLogger(AuthorNameRecognition.class.getName());
	
	private static final MatchComparator comparator = new MatchComparator();

	/**
	 * weight, which represent the impact that a pattern will choose
	 */
	public static final float DEFAULT_AUTHOR_WEIGHT = 1f;

	private static final int MAX_DISTANCE = 100;
	
	
	
	private static final int ALLOWED_DISTANCE = 7;
	
	
	/*
	private static final Pattern surForenameShortWithoutPunctPattern =Pattern.
	compile("((Mc|van den?|Van den?|de|De|�)\\s?)?"+ //prefix optional
			"([A-Z][\\w|�|�|�]{1,20}\\s?){1,5}"+ //surName
			"(\\s?-[A-Z]?[\\w|�|�]{1,20}){0,2}"+ // with "bindestrich" optional
			"\\s?([A-Z]){1,3}(\\s|,|:)"); // forename for example AB
 */
	
	
	/**
	 * tree for the founded names with the applying author pattern
	 * the key is the position and the value is a potential author name
	 */
	private TreeMap<Integer,Token> nameTree;
	
	/**
	 * List of Pattern for the author recognition
	 */
	private List<CustomPattern> authorMatcherList ;
	
	private Pattern bestAuthorPattern ;
	
	private Pattern secondAuthorPattern;


	private ArrayList<Integer> firstAuthorEntry;


	private Pattern authorSeparationPattern;
	

	public AuthorNameRecognition(){
		nameTree = new TreeMap<Integer,Token>();
		firstAuthorEntry = new ArrayList<Integer>();
		authorMatcherList = new ArrayList<CustomPattern>();
		CustomPattern a = new CustomPattern(BasicTemplates.surForenameShortPattern);
		CustomPattern a1 = new CustomPattern(BasicTemplates.forenameShortSurNamePattern);
		CustomPattern a3 = new CustomPattern(BasicTemplates.surForenameCompletePattern,0.9f);
		CustomPattern a4 = new CustomPattern (BasicTemplates.allCompletePattern,0.85f);
		authorMatcherList.add(a);
		authorMatcherList.add(a1);authorMatcherList.add(a3);authorMatcherList.add(a4);
	}
	
	
	/**
	 * 
	 * This method test each author pattern similar like {@code testReferencePattern()}
	 * But the tested Strings are the starts of a line, cause a author occurs at the
	 * beginning of a reference 
	 * @deprecated cause some papers include a very heterogeneous style and the method fail.
	 * The {@code recognizeNameWithMatcher} method use in version 0.2 all pattern for the authorpart of
	 * a reference
	 */
	
	public void testAuthorPatterns(TreeMap<Integer,String> lineTokens){
		Matcher m ;
		String subString;
		int end;
		for (CustomPattern ap :this.authorMatcherList){
			ap.setMatchCount(0);
			for (String line : lineTokens.values()){
				
				end = (line.length()>60)? 60 : line.length();
				subString = line.substring(0, end);
				m = ap.getPattern().matcher(subString);
				if (m.find()){
					//System.out.println("pattern"+ entry.getKey().toString());
					
					ap.setMatchCount(ap.getMatchCount()+1);
				}
				
			}
		}
		Collections.sort((List<CustomPattern>)this.authorMatcherList);
		this.bestAuthorPattern = authorMatcherList.get(authorMatcherList.size()-1).getPattern();
		this.secondAuthorPattern = authorMatcherList.get(authorMatcherList.size()-2).getPattern();
		//for (CustomPattern ap : this.authorMatcherList){
		//	log.info( "number of Matches "+ap.getPattern().toString()+" number " +ap.getMatchCount());
		//}
		
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
	public void recognizeNamesWithMatcher(String plainText, TreeMap <Integer,String> lineTokens,String autPartReg){
		if (nameTree==null){
			nameTree = new TreeMap<Integer,Token>();
		}
		nameTree.clear();
		this.firstAuthorEntry = new ArrayList<Integer>();
		int minPosition =0;
		String name ="";
		Matcher autPartMatcher = Pattern.compile(autPartReg).matcher(plainText);
		boolean isFirstLine;
		int previousKey;
		int previousDistance;
		Token previousToken;
		int lineKey;
		String authorPart;
		boolean isNameMatch;
		boolean isAutPartMatch;
		int autPartBegin;
		do{
			isAutPartMatch = false;
			if (autPartMatcher.find()){
				authorPart = autPartMatcher.group();
				autPartBegin = autPartMatcher.start();
				minPosition = 0;
				isAutPartMatch =true;
				lineKey = lineTokens.floorKey(autPartBegin);
				
				if (Math.abs(autPartBegin-lineKey)<=2){ //author part at line begin
					do{	
						for (CustomPattern cp:authorMatcherList ){
							Matcher m = cp.getPattern().matcher(authorPart);
							cp.setGeneratedMatcher(m);
						}
						
						for (CustomPattern cp : this.authorMatcherList){
							cp.setMatchPosition(Integer.MAX_VALUE);
							cp.setMatchValue("");		
						}
						isNameMatch = false;
						for (CustomPattern cp : this.authorMatcherList){
							if (cp.getGeneratedMatcher().find(minPosition)){
								name = cp.getGeneratedMatcher().group();
								cp.setMatchPosition(cp.getGeneratedMatcher().start());
								cp.setMatchLength(name.length());
								cp.setMatchValue(name);
							}
						}
						CustomPattern firstMatch = Collections.min(this.authorMatcherList, comparator);
						name = firstMatch.getMatchValue();
						minPosition = firstMatch.getMatchPosition();
						if (minPosition!=Integer.MAX_VALUE){
							isNameMatch = true;
						}
						
						if (isNameMatch){
							previousKey = (nameTree.lowerKey(minPosition+autPartBegin)!=null)
							? nameTree.lowerKey(minPosition+autPartBegin):-1;
							previousToken = nameTree.get(previousKey);
							previousDistance =(previousToken != null)
							?Math.abs((minPosition+autPartBegin)-(previousKey+previousToken.getValue().length())):MAX_DISTANCE;
							lineKey = lineTokens.floorKey(minPosition+autPartBegin);
						
							
							 if(Math.abs((minPosition+autPartBegin)-lineKey)<=2){
								 isFirstLine = true;
							}else {
								isFirstLine = false;
							}
							if (isFirstLine||previousDistance< ALLOWED_DISTANCE){
								Token t = new Token (name,Token.NAME);
								
								t.setLineBegin(isFirstLine);
								if (isFirstLine){
									this.firstAuthorEntry.add(minPosition+autPartBegin);
									
								}
								
								nameTree.put(minPosition+autPartBegin, t);
								//log.info(name);
							}
							minPosition += name.length()-1;
						}		
					}while (isNameMatch);
				}
			}
		}while (isAutPartMatch );
	}
	
	/**
	 * This recognize the names in divided references and not the hole text
	 * @param citationPrefixPattern 
	 * 
	 */
	public void recognizeNamesInCitation(TreeMap <Integer,String> referenceMap, Pattern citationPrefixPattern){
		if (nameTree==null){
			nameTree = new TreeMap<Integer,Token>();
		}
		nameTree.clear();
		String authorPart;
		String name="";
		int globalKey;
		int minPos =0;
		boolean isFirstLine;
		int previousAuthorKey;
		int previousAuthorDistance;
		Token previousToken;
		boolean isNameMatch;
		for (Entry<Integer,String> citationEntry :referenceMap.entrySet()){
			authorPart = citationEntry.getValue();
			for (CustomPattern cp:authorMatcherList ){
				Matcher m = cp.getPattern().matcher(authorPart);
				cp.setGeneratedMatcher(m);
			}
			
			minPos = 0;
			do{
				name ="";
				// reset 
				for (CustomPattern cp : this.authorMatcherList){
					cp.setMatchPosition(Integer.MAX_VALUE);
					cp.setMatchValue("");		
				}
				
				isNameMatch = false;
				for (CustomPattern cp : this.authorMatcherList){
					if (cp.getGeneratedMatcher().find(minPos)){
						name = cp.getGeneratedMatcher().group();
						cp.setMatchPosition(cp.getGeneratedMatcher().start());
						cp.setMatchLength(name.length());
						cp.setMatchValue(name);
					}
				}
		
				CustomPattern cp = Collections.min(this.authorMatcherList, comparator);
				
				name = cp.getMatchValue();
				minPos = cp.getMatchPosition();
				if (minPos != Integer.MAX_VALUE){
					isNameMatch= true;
				}
				if (isNameMatch){
					globalKey = minPos+citationEntry.getKey();
					previousAuthorKey = (nameTree.lowerKey(globalKey)!=null)
					? nameTree.lowerKey(globalKey):-1;
					previousToken = nameTree.get(previousAuthorKey);
					previousAuthorDistance =(previousToken != null)
					?Math.abs(globalKey-(previousAuthorKey+previousToken.getValue().length())):MAX_DISTANCE;
					
					if (citationPrefixPattern != null){
						int tagEnd;
						Matcher  tagMatcher  = citationPrefixPattern.matcher(citationEntry.getValue());
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
						Token t = new Token (name,Token.NAME);
						t.setLineBegin(isFirstLine);
						
						nameTree.put(globalKey, t);
						
					}
					minPos +=name.length();
				}
			}while (isNameMatch);
		}
	}
	
	public void resetRecognizer(){
		this.nameTree.clear();
		this.bestAuthorPattern = null;
		this.secondAuthorPattern = null;
		this.firstAuthorEntry.clear();
		
	}
	
	/**
	 * @return the nameTree
	 */
	public TreeMap<Integer,Token> getNameTree() {
		return nameTree;
	}


	public ArrayList<Integer> getFirstAuthorEntry() {
		return firstAuthorEntry;
	}

	public Pattern getAuthorSeparationPattern() {
		return authorSeparationPattern;
	}

	/**
	 * set a pattern which separate the author part from the title part
	 * of a reference. It reduce the search space for the name recognition
	 * @param authorSeparationPattern
	 */
	
	public void setAuthorSeparationPattern(Pattern authorSeparationPattern) {
		this.authorSeparationPattern = authorSeparationPattern;
	}
	
	
	
	/**
	 * add a new Author Pattern
	 */
	public void addNewPattern (Pattern newAuthorPattern, float weight){
		CustomPattern cm = new CustomPattern (newAuthorPattern,weight);
		this.authorMatcherList.add(cm);
	}


	public List<CustomPattern> getAuthorMatcherList() {
		return authorMatcherList;
	}
	
}

class MatchComparator implements Comparator <CustomPattern>{

	@Override
	public int compare(CustomPattern o1, CustomPattern o2) {
		int comp = ((Integer)o1.getMatchPosition()).compareTo((Integer)o2.getMatchPosition());
		if (comp!= 0){
			return comp;
		}else {
			if (o1.getMatchLength()>o2.getMatchLength()){
				return -1;
			}else if (o1.getMatchLength()<o2.getMatchLength()){
				return 1;
			}else 
				return 0;
		}
		
	}
	
}

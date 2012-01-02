package de.uni.leipzig.asv.zitationsgraph.extraction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthorNameRecognition {
	
	
	private static final Logger log = Logger.getLogger(AuthorNameRecognition.class.getName());

	/**
	 * weight, which represent the impact that a pattern will choose
	 */
	public static final float DEFAULT_AUTHOR_WEIGHT = 1f;

	private static final int MAX_DISTANCE = 100;
	
	
	
	private static final int ALLOWED_DISTANCE = 7;
	
	private static final Pattern surForenameShortPattern = Pattern.
	compile("((Mc|van den?|Van den?|de|De|Ó)\\s{0,2}?)?"+ //prefix
			"([A-Z][a-z|ä|ü|ö|’|é|á|ó|ñ]{1,30}\\s{0,2}?){1,5}"+ //surname
			"(\\s{0,2}?-[A-Z]?[a-z|ä|ü|ö|é|á|ó|ñ]{2,30}){0,2}\\s{0,2}?" + //optional with bindestrich
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
			"([A-Z][a-z|ä|ü|ö|’|é|á|ó|ñ]{1,30}\\s{0,2}?){1,5}" + // surname
			"(\\s{0,2}?-[A-Z]?[a-z|ä|ü|ö|’|é|á|ó|ñ]{2,30}){0,2}");//with bindestrich
	
	private static final Pattern surForenameCompletePattern = Pattern.
	compile("((Mc|van den?|Van den?|de|De|Ó)\\s{0,2}?)?" +//prefix optional
			"([A-Z][a-z|ä|ü|ö|’|é|á|ó|ñ]{1,30}\\s{0,2}?)" +//surname
			"(\\s{0,2}?-[A-Z]?[a-z|ä|ü|ö|’|é|á|ó|ñ]{1,30}){0,2}\\s{0,2}?" +//with Bindestrich optional
			",(\\s{0,2}?[A-Z][a-z|ä|ü|ö|’|é|á|ó|ñ]{1,30}){1,2}(\\s{0,2}?[A-Z]\\.)?"); //forname complete
	
	private static final Pattern allCompletePattern = Pattern.
	compile("([A-Z][\\w]{1,30}\\s{0,2}?){1,2}"+ //firstname complete
			"(\\s{0,2}?-[A-Z]?[a-z|ä|ü|ö|’|é|á|ó|ñ]{1,30}){0,2}"+ //for names like Anne-Marie
			"(\\s{0,2}?(Mc|van den?|Van den?|de|De|Ó)\\s{0,2}?)?"+ // prefix
			"(\\s{0,2}?[A-Z][a-z|’|ä|ü|ö|é|á|ó|ñ]{1,30}\\s{0,2}?){1,5}"+ // surname
			"(\\s{0,2}?-[A-Z]?[a-z|ä|ü|ö|’|é|á|ó|ñ]{1,30}){0,2}" // surname with Bindestrich
			);
	
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
		CustomPattern a = new CustomPattern(surForenameShortPattern);
		CustomPattern a1 = new CustomPattern(forenameShortSurNamePattern);
		CustomPattern a3 = new CustomPattern(surForenameCompletePattern,0.9f);
		CustomPattern a4 = new CustomPattern (allCompletePattern,0.75f);
		authorMatcherList.add(a);
		authorMatcherList.add(a1);authorMatcherList.add(a3);authorMatcherList.add(a4);
	}
	
	
	/**
	 * 
	 * This method test each author pattern similar like {@code testReferencePattern()}
	 * But the tested Strings are the starts of a line, cause a author occurs at the
	 * beginning of a reference 
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
		/*for (CustomMatcher ap : this.authorMatcherList){
			log.info( "number of Matches "+ap.getPattern().toString()+" number " +ap.getMatchCount());
		}*/
		
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
	public void recognizeNamesWithMatcher(String plainText, TreeMap <Integer,String> lineTokens){
		if (nameTree==null){
			nameTree = new TreeMap<Integer,Token>();
		}
		nameTree.clear();
		this.firstAuthorEntry = new ArrayList<Integer>();
		
		
		
		int previousKey;
		int previousDistance;
		Token previousToken;
		int lineKey;
		int begin,begin2, begin3;
		int minPosition =0;
		int tempMinPos =10000; 
	
		String name ="";
		String line;
		
		boolean patternMatch;
		
		Matcher secondAuthMatcher;
		Matcher bestAuthMatcher;
		Matcher thirdMatcher;
		
		Matcher currentMatcher;
		boolean isFirstLine;
		bestAuthMatcher = bestAuthorPattern.matcher(plainText);
		secondAuthMatcher = secondAuthorPattern.matcher(plainText);
		//thirdMatcher = this.authorMatcherList.get(this.authorMatcherList.size()-3).getPattern().matcher(plainText);
		
		
		do{	
			begin=begin2= Integer.MAX_VALUE;
			tempMinPos = Integer.MAX_VALUE;
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
			/*
			if (thirdMatcher.find(minPosition)){
				begin3 = thirdMatcher.start();
				if (begin3< tempMinPos){
					tempMinPos = begin3;
					currentMatcher = thirdMatcher;
					patternMatch = true;
				}
			}*/
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
				
				 if(Math.abs(minPosition-lineKey)<=2){
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
					log.info(name);
				}
				minPosition += name.length()-1;
				}
				
			}while (patternMatch);
		
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
			bestAuthMatcher = this.bestAuthorPattern.matcher(authorPart);
			/*
			 *  second best matcher one style use two style for the authors, therefore I use 
			 *  a second matcher
			 */
			secondAuthMatcher = secondAuthorPattern.matcher(authorPart);
			minPos = 0;
			do{
				tempMinPos =tempMinPos2 =Integer.MAX_VALUE;
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
						Token t = new Token (foundName,Token.NAME);
						t.setLineBegin(isFirstLine);
						
						nameTree.put(globalKey, t);
						
					}
					minPos +=foundName.length();
				}
			}while (hasMatch);
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

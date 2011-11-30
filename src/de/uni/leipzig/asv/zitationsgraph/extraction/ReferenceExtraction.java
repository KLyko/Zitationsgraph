package de.uni.leipzig.asv.zitationsgraph.extraction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.HashMap;

import java.util.TreeMap;
import java.util.Map.Entry;
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

import de.uni.leipzig.asv.zitationsgraph.preprocessing.BaseDoc;



public class ReferenceExtraction {


	private static final int MAX_DISTANCE = 100;
	
	private static final int HIGHVALUE =2000000;
	
	private static final int ALLOWED_DISTANCE = 6;
	
	
	private static final Pattern squareBracketPattern = Pattern.compile("^\\s?\\[.*\\]");
	private static final Pattern roundBracketPattern = Pattern.compile("^\\s?\\(.*\\)");
	private static final Pattern numericalPattern = Pattern.compile("^\\s?[1-9][0-9]{0,1}[\\.|\\s]");
	
	private static final Pattern surForenameShortPattern = Pattern.
	compile("((Mc|van|Van|de|De)\\s?)?"+ //prefix
			"([A-Z][a-z|ä|ö|ü|ß|\\']{1,30}\\s?){1,5}"+ //surname
			"(\\s?-[A-Z]?[a-z|ä|ö|ü|ß]{1,30}){0,2}\\s?" + //optional with bindestrich
			",\\s?([A-Z]\\.[\\s|-]?){1,3}");//forename is separated with comma and the name is with punct
	
	private static final Pattern surForenameShortWithoutPunctPattern =Pattern.
	compile("((Mc|van|Van|de|De)\\s?)?"+ //prefix optional
			"([A-Z][a-z|ä|ö|ü|ß|\\']{1,30}\\s?){1,5}"+ //surName
			"(\\s?-[A-Z]?[a-z|ä|ö|ü|ß]{1,30}){0,2}"+ // with "bindestrich" optional
			"\\s?([A-Z]){1,3}(\\s|,|:)"); // forename for example AB
	
	private static final Pattern forenameShortSurNamePattern = Pattern.
	compile("([A-Z]\\.[\\s?|-]){1,3}\\s?" +// forename
			"((Mc|van|de|De|Van)\\s?)?" + //prefix optional
			"([A-Z][a-z|ä|ö|ü|ß|\\']{1,30}\\s?){1,5}" + // surname
			"(\\s?-[A-Z]?[a-z|ä|ö|ü|ß]{1,30}){0,2}");//with bindestrich
	
	private static final Pattern surForenameCompletePattern = Pattern.
	compile("((Mc|van|Van|de|De)\\s)?" +//prefix optional
			"([A-Z][a-z|ä|ö|ü|ß|\\']{1,30}\\s?){1,5}" +//surname
			"(\\s?-[A-Z]?[a-z|ä|ö|ü|ß]{1,30}){0,2}\\s?" +//with Bindestrich optional
			",\\s?[A-Z][a-z|ä|ö|ü|ß]{1,30}(\\s?[A-Z]\\.)?"); //forname complete
	
	private static final Pattern allCompletePattern = Pattern.
	compile("([A-Z][a-z|ä|ö|ü|ß]{1,30}\\s?){1,2}"+
			"(\\s?-[A-Z]?[a-z|ä|ö|ü|ß]{1,30}){0,2}"+
			"(\\s?(Mc|van|Van|de|De)\\s)?"+
			"(\\s?[A-Z][a-z|ä|ö|ü|ß|\\']{1,30}\\s?){1,5}"+
			"(\\s?-[A-Z]?[a-z|ä|ö|ü|ß]{1,30}){0,2}"
			);
			
	private static final Pattern titlePattern = Pattern.compile("[A-Z][\\w|\\s|,|:|-]*\\.");
	private static final String REFERENCE_FIELD = "referenceList";
	private static final Logger log = Logger.getLogger(ReferenceExtraction.class.getName());
	
	private static final StandardAnalyzer sa = new StandardAnalyzer(Version.LUCENE_33);
	
	private static final IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_33,sa);
	
	

	
	private static TreeMap<Integer,String> lineTokens;
	
	private static TreeMap <Integer,String> citationMap;
	private TreeMap<Integer,Token> nameSet;
	
	private static HashMap <Pattern,Integer> patternMap;
	
	private Pattern applyingPattern;
	
	private Pattern citationSepPattern;
	
	private File currentFile ;
	
	private static String currentText;
	
	private boolean hasPrefix;
	
	/*
	 * lucene stuff
	 */
	
	private IndexWriter indexWriter;
	private RAMDirectory indexDirectory ;
	private Document doc;
	
	public ReferenceExtraction(){
		
		
		
		
	}
	
	public  void referenceMining (String referenceString) throws IOException{
		lineTokens = new TreeMap<Integer,String>();
		citationMap = new TreeMap<Integer,String>();
		patternMap = new HashMap<Pattern, Integer>();
		patternMap.put(surForenameShortPattern, 0);
		patternMap.put(forenameShortSurNamePattern, 0);
		patternMap.put(surForenameShortWithoutPunctPattern, 0);
		patternMap.put(surForenameCompletePattern, 0);
		currentText = referenceString;
			
			this.lineTokenize();
			this.tokenizeCitations();
			this.recognizeNamesWithMatcher();
			this.tokenizeCitationBasedOnNames();
			this.findTitles();
			
	}
	
	public void luceneAnalyze () throws FileNotFoundException, IOException{
		
		indexDirectory = new RAMDirectory();
		indexWriter = new IndexWriter(indexDirectory,iwc);
		doc = new Document();
		doc.add(new Field (REFERENCE_FIELD,currentText,Field.Store.YES, Field.Index.ANALYZED));
		indexWriter.addDocument(doc);
		indexWriter.close();
		
		
	}
	
	private  void lineTokenize() throws IOException{
		int lineNr = 0;
		StringBuffer sb = new StringBuffer();
		
		int firstLines = 0;
		
		currentText = currentText.trim();
		
		
		String [] lines = currentText.split("\\n");
		log.info("line split "+lines.length);
		for (String line :lines){
			
			line = line.replaceAll("\\s+", " "); //remove multiple spaces 
			
			// try to find separator for citations
			if (firstLines<2){
				
				Matcher m = squareBracketPattern.matcher(line);
				if (m.find()){
					
					hasPrefix = true;
					this.citationSepPattern = squareBracketPattern;
				}else{
					Matcher m2 = roundBracketPattern.matcher(line);
					if (m2.find()){
						
						hasPrefix = true;
						this.citationSepPattern = roundBracketPattern;
					}else {
						Matcher m3 = numericalPattern.matcher(line);
						if (m3.find()){
							hasPrefix = true;
							this.citationSepPattern = numericalPattern;
						}
					}
				}
				log.info("has prefix"+hasPrefix);
				firstLines ++;
			}
			sb.append(line);
			
			lineTokens.put(lineNr, line);
			lineNr+= line.length();
			}
		
			currentText = sb.toString();
			
	}
	
	/**
	 * If a prefix was found, the text will divided in the discrete reference sections.
	 *The finding of a citation based on the found prefix pattern. For each line the algorithm try to find
	 * a match. If a match was found, a new reference begin otherwise the line belongs to the current reference.
	 * The citationMap store the position as key and the String as citation.   
	 */
	private void tokenizeCitations(){
		if (hasPrefix){
			if (citationMap != null)
				citationMap.clear();
			else{
				citationMap = new TreeMap<Integer,String>();
			}
			
			
			Matcher sepMatcher;
			int currentMatchKey = 0;
			for (Entry<Integer,String> lineEntry: lineTokens.entrySet()){
				sepMatcher = this.citationSepPattern.matcher(lineEntry.getValue());
				if (sepMatcher.find()){ // match a new citation section
						citationMap.put(lineEntry.getKey(), lineEntry.getValue());
						currentMatchKey = lineEntry.getKey(); 
				}else{ // line belongs to the current citation section
					citationMap.put(currentMatchKey,citationMap.get(currentMatchKey)+
							""+ lineEntry.getValue());
				}
			}
		}
	
	}
	
	public void testPos(){
		int endLine ;
		for (Entry<Integer,String> line :lineTokens.entrySet()){
			endLine = (line.getKey()+20<currentText.length())?line.getKey()+20:currentText.length()-1;
			log.info("line#"+ currentText.substring(line.getKey(), endLine));
		}
	}
	
	
	
	
	
	private  void testPatterns(){
		Matcher m ;
		String subString;
		int end;
		for (Entry<Pattern,Integer> entry:this.patternMap.entrySet()){
			
			for (String line : this.lineTokens.values()){
				
				end = (line.length()>50)? 50 : line.length();
				subString = line.substring(0, end);
				m = entry.getKey().matcher(subString);
				if (m.find()){
					//System.out.println("pattern"+ entry.getKey().toString());
					
					entry.setValue(entry.getValue()+m.groupCount());
				}
				
			}
		}
		
		int numberOfMatches=-1;
		for (Entry<Pattern,Integer>e : this.patternMap.entrySet()){
			log.info( "number of Matches "+ e.getKey().toString()+" number " +e.getValue());
			if (e.getValue()>numberOfMatches){
				this.applyingPattern = e.getKey();
				numberOfMatches = e.getValue();
				
			}
		}
		
	}
	
	private void recognizeNamesWithMatcher(){
		if (nameSet==null){
			nameSet = new TreeMap<Integer,Token>();
		}
		nameSet.clear();
		nameSet = new TreeMap<Integer,Token>();
		
		
		String text = currentText;
		int previousKey;
		int previousDistance;
		Token previousToken;
		int lineKey;
		int begin,begin2,begin3,begin4,begin5;
		int minPosition =0;
		int tempMinPos =10000; 
	
		String name ="";
		String line;
		
		boolean patternMatch;
		Matcher m;
		Matcher m2;
		Matcher m3;
		Matcher m4;
		Matcher m5;
		Matcher currentMatcher;
		boolean isFirstLine;
		m = surForenameCompletePattern.matcher(text);
		m2 = forenameShortSurNamePattern.matcher(text);
		m3 = surForenameShortPattern.matcher(text);
		m4 = surForenameShortWithoutPunctPattern.matcher(text);	
		m5 = allCompletePattern.matcher(text);
		//for (String line : lineTokens.values()){	
			
		
		do{	
			begin=begin2=begin3=begin4=begin5 =0;
			tempMinPos = HIGHVALUE;
			currentMatcher = null;
			patternMatch = false;
			
			if(m.find(minPosition)){
				begin = m.start();
				tempMinPos = begin;
				currentMatcher =m;
				patternMatch = true;
			}
			if (m2.find(minPosition)){
				
				begin2 = m2.start();
				if (begin2 <tempMinPos){
					tempMinPos = begin2;
					currentMatcher = m2;
					patternMatch = true;
				}
				
			}
			if (m3.find(minPosition)){
				begin3 = m3.start();
				if (begin3 <tempMinPos){
					tempMinPos = begin3;
					currentMatcher = m3;
					patternMatch = true;
				}
				
			}
			if (m4.find(minPosition)){
				
				begin4 = m4.start();
				if (begin4 < tempMinPos){
					tempMinPos = begin4;
					currentMatcher = m4;
					patternMatch = true;
				}
			}
			if (m5.find(minPosition)){
				
				begin5 = m5.start();
				if (begin5 < tempMinPos){
					tempMinPos = begin5;
					currentMatcher = m5;
					patternMatch = true;
				}
				
			}
			
			minPosition = tempMinPos;
			
			
			if (patternMatch){
				name = currentMatcher.group();
				
				previousKey = (nameSet.lowerKey(minPosition)!=null)
				? nameSet.lowerKey(minPosition):-1;
				previousToken = nameSet.get(previousKey);
				previousDistance =(previousToken != null)
				?Math.abs(minPosition-(previousKey+previousToken.getValue().length())):MAX_DISTANCE;
				lineKey = lineTokens.floorKey(minPosition);
				line =lineTokens.get(lineKey);
				
				if (hasPrefix){
					Matcher  tagMatcher  = this.citationSepPattern.matcher(line);
					if (tagMatcher.find()){
						line = line.replaceFirst(tagMatcher.group(),"");
						if (line.startsWith(" "+name)||line.startsWith(name))
							isFirstLine = true;
						else 
							isFirstLine = false;
					}else {
						isFirstLine = false;
					}	
				}else if(line.startsWith(" "+name)||line.startsWith(name)){
					 isFirstLine = true;
				}else {
					isFirstLine = false;
				}
				
				if (isFirstLine||previousDistance< ALLOWED_DISTANCE){
					Token t = new Token (name,Token.NAME);
					
					nameSet.put(minPosition, t);
				}
				minPosition += name.length()-1;
				}
				
			}while (patternMatch);
		
	}
	
	
	public void testPrintNames(){
		System.out.println("found names:" +nameSet.size());
		for (int key :nameSet.keySet()){
			System.out.println(key +"\t"+nameSet.get(key));
		}
	}
	
	
	
	
	
	
	/**
	 * divide the text in single references based on the occurrence of the found names
	 * Every line belongs to one reference until a name occurs at the beginning of a line 
	 */
	private void tokenizeCitationBasedOnNames(){
		if (!hasPrefix){
			if (citationMap!= null){
				citationMap.clear();
			}else{
				citationMap = new TreeMap <Integer,String>();
			}
			String line;
			String reference;
			int lineBeginKey=0;
			int lineEndKey =0;
			
			for (Entry <Integer, Token> entry: nameSet.entrySet()){
				
				if (entry.getValue().isLineBegin()||entry.getKey() == nameSet.lastKey()){
					
					if (entry.getKey() == nameSet.lastKey())
						lineEndKey = currentText.length();
					else
						lineEndKey = entry.getKey()-1;
					
					if (lineEndKey >lineBeginKey){
						reference = currentText.substring(lineBeginKey, lineEndKey);
						citationMap.put(lineBeginKey, reference);
					}
					lineBeginKey = entry.getKey();
				}
			}
		}
	}
	
	public void findTitles(){
		int nextCitKey;
		String lastAuthor;
		String includeTitle;
		int endIndex;
		Matcher titleMatcher;
		Entry<Integer,Token> lastAuthorEntry;
		for (Entry<Integer, String> citEntry: citationMap.entrySet()){
			nextCitKey = (citationMap.higherKey(citEntry.getKey())!= null)?citationMap.higherKey(citEntry.getKey()):currentText.length();
			lastAuthorEntry = nameSet.floorEntry(nextCitKey);
			endIndex = citEntry.getValue().lastIndexOf(lastAuthorEntry.getValue().getValue());
			endIndex+=lastAuthorEntry.getValue().getValue().length();
			includeTitle = citEntry.getValue().substring(endIndex);
			titleMatcher = this.titlePattern.matcher(includeTitle);
			if (titleMatcher.find())
			log.info(citEntry.getValue()+"\n"+
					titleMatcher.group());
		}
	}
	
	public void cleanData (){
		
	}
	
	public static void main (String[] args){
		String[] test = new String[]{
/*0*/				"examples/Ngonga Ermilov - Complex Linking in a Nutshel.pdf",
/*1*/				"examples/journal.pone.0027856.pdf"	
		};
		
			BaseDoc bd = new BaseDoc (test[1]);
			try {
				bd.process();
				bd.splitFullText();
				//System.out.println(bd.get(BaseDoc.REFERENCES));
				//BufferedReader br = new BufferedReader(new FileReader("examples/test.txt"));
				//StringBuffer sb = new StringBuffer();
				//while (br.ready()){
				//	sb.append(br.readLine());
				//}
				
				ReferenceExtraction cer = new ReferenceExtraction();
				cer.referenceMining(bd.get(bd.REFERENCES));
				cer.testPrintNames();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			
			
				
			
	}
}

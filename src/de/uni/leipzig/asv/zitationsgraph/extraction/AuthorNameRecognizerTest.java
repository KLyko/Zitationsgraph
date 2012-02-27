package de.uni.leipzig.asv.zitationsgraph.extraction;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Vector;
import java.util.regex.Matcher;

import de.uni.leipzig.asv.zitationsgraph.data.Author;
import de.uni.leipzig.asv.zitationsgraph.data.Citation;
import de.uni.leipzig.asv.zitationsgraph.extraction.templates.BasicTemplates;

import junit.framework.TestCase;

public class AuthorNameRecognizerTest extends TestCase{
	
	ReferenceExtraction ref ;
	AuthorNameRecognition nameRecognizer;
	
	protected void setUp(){
		ref = new ReferenceExtraction();
		try {
			ref.setCurrentText(readFile("examples/referenceTestPart/DH20084.ref.txt"));
			ref.lineTokenize();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		nameRecognizer = new AuthorNameRecognition();
	}
	
	public void testNamePatterns (){
		Matcher testMatcher = BasicTemplates.surForenameCompletePattern.matcher("Müller, Anne-Marie");
		assertTrue (testMatcher.find());
		testMatcher.usePattern(BasicTemplates.allCompletePattern);
		testMatcher.reset("Anne-Marie Müller");
		assertTrue (testMatcher.find());
		testMatcher.reset("Kim Jong Ill");
		assertTrue (testMatcher.find());
		testMatcher.usePattern(BasicTemplates.forenameShortSurNamePattern);
		testMatcher.reset("G. Heyer");
		assertTrue (testMatcher.find());
		testMatcher.reset("g. heyer");
		assertFalse (testMatcher.find());
	}
	
	public void testNameRecogintion(){
		nameRecognizer.recognizeNamesWithMatcher(ref.getCurrentText(),ref.lineTokens,
				ref.tb.getTemplate(ReferenceExtraction.AUTHOR_PART).getTemplate().toString());
		HashSet<String> foundNames = new HashSet<String>();
		for (Token t :nameRecognizer.getNameTree().values()){
			foundNames.add(t.getValue());
			
		}
		HashSet<String> testSetName = new HashSet<String>();
		String[] testNames = new String[]{"Baayen, Harald","Burrows, John F.",
				"Hoover, David L.","Juola, Patrick","John Sofko",
				"Patrick Brennan"};
		for (String name: testNames){
			testSetName.add(name);
		}
		
		assertTrue(foundNames.containsAll(testSetName));
		
	}
	
	
	
	
	String readFile(String path) throws IOException{
		FileReader fr = new FileReader(path);
		BufferedReader br= new BufferedReader(fr);
		StringBuffer sb = new StringBuffer();
		while (br.ready()){
			sb.append(br.readLine()+System.getProperty("line.separator"));
		}
		br.close();
		return sb.toString();
	}
}

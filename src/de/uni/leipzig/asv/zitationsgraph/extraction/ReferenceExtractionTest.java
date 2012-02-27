package de.uni.leipzig.asv.zitationsgraph.extraction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class ReferenceExtractionTest extends TestCase{
	
	ReferenceExtraction ref;
	
	protected void setUp (){
		ref = new ReferenceExtraction();
		try {
			ref.setCurrentText(readFile("examples/referenceTestPart/DH20084.ref.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * testing the key as position and the corresponded value as line
	 */
	public void testLineTokenize(){
		ref.lineTokenize();
		
		String subText;
		String wrongSubText; 
		for (int key :ref.lineTokens.keySet()){
			subText = ref.getCurrentText().substring(key);
			wrongSubText = ref.getCurrentText().substring(key+4);
			assertTrue (subText.startsWith(ref.lineTokens.get(key)));
			assertFalse(wrongSubText.startsWith(ref.lineTokens.get(key)));
			
		}	
	}
	
	public void testTestReferencePattern(){
		ref.testReferencePatterns();
		assertSame (ref.applyingReferencePattern ,ref.tb.getTemplate(
				ReferenceExtraction.BIOI_STYLE).getTemplate());
		try {
			ref.setCurrentText(readFile("examples/referenceTestPart/quotaexample.txt"));
			ref.testReferencePatterns();
			assertEquals(ref.applyingReferencePattern,ref.tb.getTemplate(
					ReferenceExtraction.MLA_STYLE).getTemplate());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

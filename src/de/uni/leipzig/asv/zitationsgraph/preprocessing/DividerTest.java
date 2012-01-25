package de.uni.leipzig.asv.zitationsgraph.preprocessing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import junit.framework.TestCase;


public class DividerTest extends TestCase{
	Divider divider;
	
	String testFiles[]= {"examples/preprocessingTest/test1.txt", "examples/preprocessingTest/test2.txt"};
	String test1, test2;
	
	@Override public void setUp() throws Exception {		
		test1 = readFile(testFiles[0]);
		test2 = readFile(testFiles[1]);
	}
	
	public void testSplitByBruteForce() {
		divider = new Divider(test1);
		divider.splitByBruteForce();
		assertTrue(divider.body.trim().startsWith("1 Introduction"));
		assertTrue(divider.head.trim().startsWith("Annotated Facsimile Editions:"));
		assertTrue(divider.tail.trim().startsWith("Carlin, C., Haswell, E., and Holms, M. (2005). Image"));
		
		divider = new Divider(test2);
		divider.splitByBruteForce();
		assertTrue(divider.body.trim().startsWith("Introduction"));
		assertTrue(divider.head.trim().startsWith("The Other Side of  the Panopticon:"));
		assertTrue(divider.tail.trim().startsWith("Allsop, Kenneth. Hard Travellin"));
	}
	
	public static String readFile(String path) {
		BaseDoc doc = new BaseDoc(path);
		try {
			doc.process();
			return doc.getFullText();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void testSplitHead() {
		System.out.println();
		divider = new Divider(test1);
		divider.splitByBruteForce();
		assertTrue(divider.head.trim().startsWith("Annotated Facsimile Editions:"));
		assertTrue(divider.body.trim().startsWith("1 Introduction"));
	}
}

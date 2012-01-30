package de.uni.leipzig.asv.zitationsgraph.preprocessing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import junit.framework.TestCase;


public class DividerTest extends TestCase{
	Divider divider;
	/*
	 * 1st test a LIT paper (Introduction & References)
	 * 2nd test a Chicago Colloquium on Digital Humanities and Computer Science paper (Introduction & Bibliography)
	 * 3rd test a LIT 2009 paper:Lit Linguist Computing-2009-Fraistat-9-18 (no Introduction, references null)
	 */
	String testFiles[]= {"examples/preprocessingTest/test1.txt", "examples/preprocessingTest/test2.txt",
			"examples/preprocessingTest/test3.txt", "examples/preprocessingTest/test4.txt"};
	String test1, test2, test3, test4;
	
	@Override public void setUp() throws Exception {		
		test1 = readFile(testFiles[0]);
		test2 = readFile(testFiles[1]);
		test3 = readFile(testFiles[2]);
		test4 = readFile(testFiles[3]);
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
		
		divider = new Divider(test3);
		divider.splitByBruteForce();
		assertTrue(divider.head.trim().startsWith("Editing Environments: The"));
		assertTrue(divider.body.trim().startsWith("This essay was written in 1997."));
		assertNull(divider.tail);
		
		divider = new Divider(test4);
		divider.splitByBruteForce();
		assertTrue(divider.head.trim().startsWith("E-Carrel: An Environment for Collaborative Textual Scholarship"));
		assertTrue(divider.body.trim().startsWith("1. Introduction"));
		assertTrue(divider.tail.trim().startsWith("Arts and Humanities Data Service."));
		
	}
	
	public static String readFile(String path) {
		BaseDoc doc = new BaseDoc(path);
		try {
			doc.process();
			return doc.getFullText();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (NotSupportedFormatException e) {
			// TODO Auto-generated catch block
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

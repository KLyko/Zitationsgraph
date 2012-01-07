package de.uni.leipzig.asv.zitationsgraph.preprocessing;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.Splitter;

/**
 * Tries to divides entire books provided as PDFs according to the "Table of contents"(TOC) 
 * and a page offset, pointing to the numerical page of the pdf at which 
 * the first mentioned article of the TOC starts.
 * This is necessary because the proceedings of the <i>Digital Humanities Conference</i> are
 * provided as entire books. Therefore we first need to split them into the several parts.
 * TODO Implementing what to do with parts: save as parts.
 * @version 0.3
 * @author Klaus Lyko
 */
public class BookSplitter {
	
	Logger logger = Logger.getLogger("ZitGraph");
	// Page in the PDF of the first article (with a numerical page number) in the list of contents.
	private int pageThreshold;
	// Path to the PDF of the proceeding
	private String pathToFile;
	// PDDocument the pdf file
	private PDDocument fullPDDoc;
	
	public BookSplitter() {
		this.pageThreshold=0;
	}
	
	public BookSplitter(int pageThreshold) {
		this.pageThreshold=pageThreshold-1;
	}
	
	/**
	 * Returns the table of contents out of a book. This is done by looking for "Table of Contens"
	 * and the Index of Authors as its boundaries.
	 * @param fulltext
	 * @return String containing the table of contents.
	 */
	public String getTableOfContents(String fulltext) {
		String output = "";
		int start=-1;
		int end=-1;
		// start
		// Table of Contents
		Pattern patternStart = Pattern.compile("\\sTable of Contents\\s");
		Matcher matcherStart = patternStart.matcher(fulltext);
		if(matcherStart.find()) {
			start = matcherStart.end();
		}
		// end
		// Index of Authors............
		Pattern patternEnd = Pattern.compile("\\sIndex of Authors");
		Matcher matcherEnd = patternEnd.matcher(fulltext);
		if(matcherEnd.find()) {
			end = matcherEnd.end();
		}
		if(start>-1 && end>-1 && end>start)
			output = fulltext.substring(start, end+100);
		return output;
	}
	
	/**
	 * Method to read the table of contents of a pdf file.
	 * @param filePath
	 * @throws IOException
	 */
	public String process_pdf(String filePath) throws IOException {
		FileInputStream input = new FileInputStream( filePath );
		fullPDDoc = parseDocument( input );
		return getTableOfContents(BaseDoc.getTextFromPDF(fullPDDoc));
	}
	/**
	 * Method to parse the extracted text of the Table of Content into a List of IndexEntries.
	 * @param table String holding extracted text of the table of contents,
	 * @return List<IndexEntry> each item having the most relevant field startPage, number of the page as in the table of Contents.
	 */
	public List<IndexEntry> parseTable(String table) {
		// to find lines with pages
	//	table = table.substring(0, 1994);
		String regex = ".+ \\.+ [0-9]+";
		List<IndexShortEntry> lines = new LinkedList<IndexShortEntry>();
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(table);
		while(m.find()) {
			IndexShortEntry iSE = new IndexShortEntry();
			iSE.entry = m.group();
			iSE.start = m.start();
			iSE.end = m.end();
			lines.add(iSE);
		}	
		return parseTableEntries(table, lines);
	}
	
	/**
	 * Parsing the extracted short entries (title ........ pagenumber) in combination with
	 * the whole Table of Contents to extract all relevant data per entry. Most notable the 
	 * page numbers. But, this method first tries to read the full entry (title lines before, 
	 * following lines stating authors), in order to make an extraction of the titles and authors
	 * possible.
	 * @param table String of the Table of Contents.
	 * @param entryList List of short entries, e.g. "This is only the Subtitle ............... 15".
	 * @return List<IndexEntry> each item having the most relevant field startPage, number of the page as in the table of Contents.
	 */
	private List<IndexEntry> parseTableEntries(String table, List<IndexShortEntry> entryList) {
		List<IndexEntry> entries = new LinkedList<IndexEntry>();
		int startNext = 0;
		for(int i = 0; i<entryList.size()-1; i++) {
			IndexShortEntry e = entryList.get(i);
			IndexShortEntry e2 = entryList.get(i+1);
		
			// between two entries is a comma separated author list and possibly the beginning of the next entry
			
			String between = table.substring(e.end+1, e2.start);
			// try to reconstruct full entry
			String fullEntry = table.substring(startNext, e.end+1);
			// Pattern should identify Authors and no parts of titles. 
			Pattern authorPattern = Pattern.compile("[^, :]+ [^, :][, [^, :]+ [^, :]+]*,?");
			String[] lines= between.split("\\r?\\n");
			int addedLength = 0;
			for(int j = 0; j<lines.length; j++) {				
				Matcher m2 = authorPattern.matcher(lines[j]);
				if(m2.matches()) {
					fullEntry += "\n"+lines[j];
					addedLength+=lines[j].length()+1;
				}
			}
			
			startNext = e.end + addedLength + 1;
			
			entries.add(parseEntry(fullEntry));
		}
		return entries;
	}
	
	/**
	 * Method to parse an (full) entry of the form: <code>title ..... page\n authors</code> into an
	 * IndexEntry. Thereby, trying to determine title, page and the authors of the stated article.
	 * @param full String of the (full) entry in the Table of Contents.
	 * @return IndexEntry holding parsed informations.
	 */
	private IndexEntry parseEntry(String full) {
		IndexEntry e = new IndexEntry();
		// divide title and page with authors
		Pattern p = Pattern.compile("\\.+");
		Matcher m = p.matcher(full);
		if(m.find()) {
			e.title = full.substring(0, m.start());
			Pattern p2 = Pattern.compile("[0-9IV]+");
			String leftover = full.substring(m.end()+1);
			Matcher m2 = p2.matcher(leftover);
			if(m2.find()) {
				try{
					e.startPage = Integer.parseInt(m2.group());
				}catch(NumberFormatException ex) {
					e.startPage = -1;
				}
				for(String s: leftover.substring(m2.end()).split(","))
					e.authors.add(s);
			}
		}		
		e.title = full;
		return e;
	}
	
	public static void main (String args[]) {
		BookSplitter sp = new BookSplitter(17);
		String file = "C:/Users/Lyko/Desktop/Textmining datasets/Publikationsdaten/" +
				"Digital Humanities Conference/2008/Digital Humanities 2008 Book of Abstracts.pdf";
		if(args.length == 1) {
			file = args[0];
		}
		try {
			String table = sp.process_pdf(file);
			List<IndexEntry> lIE = sp.parseTable(table);
			sp.processAllPapers(lIE);		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method to save all papers defined by their IndexEntry.
	 * @param papers Parsed Table of Contents.
	 * @throws IOException 
	 */
	public void processAllPapers(List<IndexEntry> papers) throws IOException {
		for(int paperNum = 0; paperNum < papers.size()-1; paperNum++) {
			IndexEntry first = papers.get(paperNum);
			IndexEntry last = papers.get(paperNum+1);
			if(first.startPage>0 && last.startPage>0 && first.startPage<=last.startPage) {
				PDDocument doc = getPart(pageThreshold+first.startPage, last.startPage-first.startPage+1);
				processDocument(doc);
			}
		}
	}
	
	/**
	 * Method to Process a single extracted part of the whole file.
	 * @TODO implement some functionality.
	 * @param doc
	 */
	protected void processDocument(PDDocument doc) {
// ---------  do something ---------------
		return;
	}
	
	/**
	 * Returns a part of a PDDocument. The part is defined by it's (real) start page number and a length.
	 * E.g. if you want to extract only page 5 call <code>getPart(5, 1)</code>.
	 * @param startPage Start page of the part.
	 * @param length Number of Pages to extract. If length <= 0 the complete rest of the document is used.
	 * @return PDDocument of the part.
	 * @throws IOException
	 */
	public PDDocument getPart(int startPage, int length) throws IOException {
		Splitter splitter = new Splitter();
		List<PDDocument> documents = null;
		List<PDDocument> documents2 = null;
		PDDocument result = null;		
		splitter.setSplitAtPage( startPage );
		// a splitter splits a document every split number of pages
        documents = splitter.split( fullPDDoc );
        logger.info("Splitted first into "+documents.size()+" docs");
        if(documents.size()>=2) {
        	
        	PDDocument doc = (PDDocument)documents.get( 1 );
        	if(length > 0) {
        		length = doc.getNumberOfPages();
        	}
	        splitter.setSplitAtPage(length);
	        documents2 = splitter.split(doc);
	        logger.info("Splitted then into "+documents2.size()+" docs");
	        result = (PDDocument) documents2.get(0);
        }
        else {
        	result = (PDDocument) documents.get(0);
        }        	
        for(PDDocument d : documents)
        	d.close();
        for(int i = 1; i<documents2.size(); i++)
        	documents2.get(i).close();
        return result;
	}
	
	
	/**
	 * Nested class to hold Entries of the table of contents.
	 * @author Klaus Lyko
	 *
	 */
	private class IndexEntry {
		public String title;
		public List<String> authors;
		public int startPage;		
		public IndexEntry() {
			super();
			authors = new LinkedList<String>();
		}
		public String toString() {
			String o = "TITLE="+title+"\n";
			o += "STARTPAGE="+startPage+"\n";
			o += "AUTHORS=";
			for(String a : authors){
				o+=a+", ";
			}
			return o;
		}
	}
	/**
	 * Nested class to memorize shorter Index entries: only those lines where the page numbers are.
	 * @author Klaus Lyko
	 *
	 */
	private class IndexShortEntry {
		public String entry;
		public int start;
		public int end;
		public String toString() {
			return ""+start+"#"+entry+"#"+end;
		}
	}
	
	/**
	 * Static method to parse a PDF out of an InputStream.
	 * @param input InputStream.
	 * @return PDDocument.
	 * @throws IOException
	 */
	private static PDDocument parseDocument( InputStream input )throws IOException {
		PDFParser parser = new PDFParser( input );
	    parser.parse();
	    return parser.getPDDocument();
	}
	
	/**
	 * Deconstructor functionality, needed to close open PDDocuments.
	 */
	protected void finalize() throws Throwable
	{
	  fullPDDoc.close();
	  super.finalize(); 
	} 
}

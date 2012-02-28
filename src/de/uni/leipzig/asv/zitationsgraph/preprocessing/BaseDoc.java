package de.uni.leipzig.asv.zitationsgraph.preprocessing;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.Splitter;
import org.xml.sax.SAXException;

import de.uni.leipzig.asv.zitationsgraph.data.Document;

/**Central class of the <code>preprocessing</code> package.
 * Provides method to read supported file formats (pdf, plain text) and split the scientific papers into
 * the parts HEAD, BODY and REFERENCES.
 * 
 * Note we also support XML formatted (as from DHQ) files, but as processing those is detached from the normal,
 * parsing is done by a separate class: the DHQXMLParser. 
 * 
 * As for other input formats (pdf, plain text) the dividing is done by the Divider class.
 * If the head returned by the Divider is null or too large (nearly equal size of body, more then 20% of 
 * the fulltext) we use a heuristic approach: either use the first two pages of a pdf as head part, or if
 * plain text was submitted the first 20 lines of text.
 * 
 * To read or process a file initialize an instance of this class with the path to the 
 * file and call the process method.
 *
 * @version 0.2
 * @author Klaus Lyko
 *
 */
public class BaseDoc {
	public static final String HEAD = "head";
	public static final String BODY = "body";
	public static final String REFERENCES = "references";
	public static final boolean debug = false;
	Logger logger = Logger.getLogger("ZitGraph");
	
	private String fileName;
	private String fullText;
	private String head, body, references;
	
	private boolean isDHQXML = false;
	private Document dhqParsedDoc = null;
	
	PDDocument document = null;
    	
	public BaseDoc(String fileName) {
		super();
		setFileName(fileName);
	}
	
	/**
	 * Method to process the separation of a file.
	 * @throws IOException 
	 */
	public void process() throws IOException, NotSupportedFormatException {
		String split[] = fileName.split("\\.");
	
		if(split[split.length-1].equalsIgnoreCase("pdf")) {
			logger.info("Reading text from PDF file...");
			process_pdf();
		}
		else if (split[split.length-1].equalsIgnoreCase("xml")) {
			
			try {
				DHQXMLParser dhqP = new DHQXMLParser(fileName);
				dhqP.processXMLFile(fileName);
				dhqParsedDoc = dhqP.processXMLFile(fileName);
				isDHQXML=true;
				
			} catch (ParserConfigurationException e) {
				throw new NotSupportedFormatException("We got a ParserConfigurationException trying to parse file '"+fileName+"':\n"+e.getMessage());
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;			
		} 
		else {
			// try to read plain text
			logger.info("No PDF or XML file. Trying to read plain text from file...");
			process_plainTextFile();
		}
		
		if(fullText != null && fullText.length() > 0) {
			splitFullText();
		} else {
			logger.warning("Not able to split the document.");
		}
	}
// ------ ------ ------   PDF preprocessing ------ ------ ------	
	/**
	 * Reading and parsing a PDF file.
	 * @throws IOException
	 */
	public PDDocument process_pdf() throws IOException {
		FileInputStream file = null;
        try
        {
            file = new FileInputStream(fileName);
            PDFParser parser = new PDFParser( file );
            parser.parse();
            document = parser.getPDDocument();
            if( document.isEncrypted() )
            {
                try
                {
                    document.decrypt( "" );
                }
                catch( InvalidPasswordException e )
                {
                    logger.warning( "Error: Document is encrypted with a password." );
                    System.exit( 1 );
                } catch (CryptographyException e) {
					e.printStackTrace();
				}
            }
            // get full text
            
            setFullText(getTextFromPDF(document));
            return document;
        }
        finally
        {
            if( file != null )
            {
                file.close();
            }
            if( document != null )
            {
                document.close();
            }
        }
	}
	
	
    /**
     * This method reads a PDF file as plain text.
     * @param document The PDDocument to get the data from.
     * @throws IOException If there is an error getting the page count.
     */
    protected static String getTextFromPDF( PDDocument document ) throws IOException
    {
    	PDFTextStripper stripper = new PDFTextStripper();
		return stripper.getText(document);
    }
 
 // ------ ------ ------   plain text preprocessing ------ ------ ------    
    /**
     * Method to read plain text files.
     * @throws IOException 
     */
    protected void process_plainTextFile() throws IOException {
    	BufferedReader reader = new BufferedReader(new FileReader(fileName));
    	String zeile = reader.readLine();
        StringBuffer buffer = new StringBuffer("");
    	while (zeile != null) {
    		buffer.append(zeile+System.getProperty("line.separator"));
    		zeile = reader.readLine();
    	}
    	fullText=buffer.toString();
    }
    
    /**
	 * Method returns fields of a loaded Document.
	 * @param name Name of the field.
	 * @return Corresponding text.
	 */
	public String get(String name) {
		if(name.equals(HEAD)) {
			return head;
		}
		else if(name.equalsIgnoreCase(BODY)) {
			return body;
		}
		else if(name.equalsIgnoreCase(REFERENCES)) {
			return references;
		}
		return "";
	}
	/**
	 * Set the path to file which to process.
	 * @param fileName
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	/**
	 * Get the path to the file to process.
	 */
	public String getFileName() {
		return fileName;
	}

    
	public void setFullText(String fullText) {
		this.fullText = fullText;
	}
	
	public String getFullText() {
		return fullText;
	}	
	
	/**
	 * Method to split a paper.
	 */
	public void splitFullText() {
		Divider div = new Divider(fullText);
		int bruteForceCertainty = div.determineBruteForceMethod();
		if(debug)
			logger.info("BruteForceValue = "+bruteForceCertainty);
		if(bruteForceCertainty >= 0) {
			if(debug)
				logger.info("Applying brute force algorithm.");
			div.splitByBruteForce();
			head = div.head;
			body = div.body;
			references = div.tail;
			if(head == null) {
				determineHeadByHeuristic();
			}
			else if(head.length()>= body.length() || head.length()>=(0.2*fullText.length())) {
				determineHeadByHeuristic();
			}
		}
		else {
			if(debug)
				logger.warning("No splitting performed");
		}		
	}
	
	/**
	 * Method to split head by a heuristic. If the input is was a PDF document, we will use the first two pages.
	 * If the input was a plain String, we simply take the first 20 lines.
	 */
	private void determineHeadByHeuristic() {
		if(debug)
			logger.info("Try to Split head by heuristic!");
		// head are the first two sites of the pdf
		if(document != null) {
			Splitter splitter = new Splitter();
			splitter.setSplitAtPage(2);
			try {
				List<PDDocument> docList = splitter.split(document);
				if(docList.size() >= 2) {
					head = getTextFromPDF(docList.get(0));
					if(head.trim().length()>0)
						logger.info(docList.size()+"Splitted Head from PDF by heuristic, that is the first two pages.");
				}
				for(PDDocument doci : docList)
					doci.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(head.trim().length()==0) {
				splitHeadFromFirstLines(10);
			}
		}
		else {
			splitHeadFromFirstLines(10);
		}
	}
	
	/**
	 * Method for splitting the head by simply using the first lines.
	 * @param lines Specifies how man lines to split.
	 */
	private void splitHeadFromFirstLines(int lines) {
		StringTokenizer tokenizer = new StringTokenizer(body, System.lineSeparator());
		head = "";
		for(int i = 0; i<Math.min(tokenizer.countTokens(), lines); i++)
			head+=tokenizer.nextToken()+System.lineSeparator();
	}

	public static void main(String args[]) throws IOException, CryptographyException {
		String filePath = "examples/journal.pone.0027856.pdf";
		filePath = "examples/Ngonga Ermilov - Complex Linking in a Nutshel.pdf";
		filePath = "examples/text.txt";
		filePath = "examples/Lit Linguist Computing-2010-Craig-37-52.pdf";
		filePath = "examples/Lit Linguist Computing-2008-Windram-443-63.pdf";
		filePath = "examples/Lit/2011/323.full.pdf";
		//filePath = "examples/Lit/2011/Lit Linguist Computing-2011-Sainte-Marie-329-34.pdf";
		filePath = "examples/Lit/2009/Lit Linguist Computing-2009-Fraistat-9-18.pdf";
	//	filePath = "examples/Lit/2009/Lit Linguist Computing-2009-Sutherland-99-112.pdf";
	//	filePath = "examples/Lit/2011/285.full.pdf";
	//	filePath = "examples/Lit/2009/Lit Linguist Computing-2009-Lavagnino-63-76.pdf";
	//	filePath = "examples/Lit/2009/Lit Linguist Computing-2009-Audenaert-143-51.pdf";
		filePath = "examples/DH/2009/AccessibilityUsabilityand.txt";
	//	filePath = "examples/79-373-2-PB.pdf";
		filePath = "examples/DHCS/63-303-1-PB.pdf";
		BaseDoc doc = new BaseDoc(filePath);
		try {
			doc.process();
			System.out.println(doc.get(HEAD));
			System.out.println("=======================");
	//		System.out.println(doc.get(BODY));
			System.out.println("=======================");
			System.out.println(doc.get(REFERENCES));
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NotSupportedFormatException e) {
			e.printStackTrace();
		}
	}
	
	public void setField(String name, String value) {
		if(name.equalsIgnoreCase(HEAD))
			head = value;
		else if (name.equalsIgnoreCase(BODY))
			body = value;
		else if (name.equalsIgnoreCase(REFERENCES))
			references=value;
		else 
			if(debug)
				logger.warning("Trying to set unknown field "+name);
		
	}
	
	/**
	 * Method indicating, we can skip most of the processing pipeline, as the input is a XML file.
	 * If so we can directly parse it into a de.uni.leipzig.asv.zitationsgraph.data.Document via the
	 * DHQXMLParser.
	 * @return True if we should parse a .xml file of DHQ format. False otherwise.
	 */
	public boolean isDHQDoc() {
		return isDHQXML;
	}
	
	/**
	 * Method to directly get the parsed de.uni.leipzig.asv.zitationsgraph.data.Document.
	 * @return Parsed de.uni.leipzig.asv.zitationsgraph.data.Document if the input
	 * file was a XML file conform to the DHQ format. null otherwise!
	 */
	public Document getParsedDHQDocument() {
		return dhqParsedDoc;
	}
}

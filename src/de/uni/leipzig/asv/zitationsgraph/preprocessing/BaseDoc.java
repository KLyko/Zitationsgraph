package de.uni.leipzig.asv.zitationsgraph.preprocessing;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.util.PDFTextStripper;

/** Class to 
 * @author Klaus Lyko
 *
 */
public class BaseDoc {
	public static final String HEAD = "head";
	public static final String BODY = "body";
	public static final String REFERENCES = "references";
	
	Logger logger = Logger.getLogger("ZitGraph");
	
	private String fileName;
	private String fullText;
	private String head, body, references;
	
	public BaseDoc(String fileName) {
		super();
		this.setFileName(fileName);
	}
	
	/**
	 * Method to process the separation of a file.
	 * @throws IOException 
	 * @TODO implement.
	 */
	public void process() throws IOException {
		String split[] = fileName.split("\\.");
	
		if(split[split.length-1].equalsIgnoreCase("pdf")) {
			process_pdf();
		}
		else if (split[split.length-1].equalsIgnoreCase("xml")) {
			// TODO implement xml parser
		} 
		else {
			// try to read plain text
			
			// else throw NoSupportedFormatException
		}
	}
	/**
	 * Reading and parsing a pdf.
	 * @throws IOException
	 */
	public PDDocument process_pdf() throws IOException {
		PDDocument document = null;
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
                    System.err.println( "Error: Document is encrypted with a password." );
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

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}
	


    /**
     * This method reads a PDF file as plain text.
     * @param document The PDDocument to get the data from.
     * @throws IOException If there is an error getting the page count.
     */
    public String getTextFromPDF( PDDocument document ) throws IOException
    {
    	PDFTextStripper stripper = new PDFTextStripper();
		return stripper.getText(document);
    }

	public void setFullText(String fullText) {
		this.fullText = fullText;
	}

	public String getFullText() {
		return fullText;
	}	
	
	public void splitFullText() {
		Divider div = new Divider(fullText);
		int bruteForceCertainty = div.determineBruteForceMethod();
		logger.info("BruteForceValue = "+bruteForceCertainty);
		if(bruteForceCertainty >= 2) {
			logger.info("Applying brute force algorithm.");
			div.splitByBruteForce();
			head = div.head;
			body = div.body;
			references = div.tail;
		}
		
	}
	
	public void printMetaData(PDDocument document) throws IOException {
	//	document.getDocumentCatalog().
		  PDDocumentInformation info = document.getDocumentInformation();
	      System.out.println( "Page Count=" + document.getNumberOfPages() );
	      System.out.println( "Title=" + info.getTitle() );
	      System.out.println( "Author=" + info.getAuthor() );
	      System.out.println( "Subject=" + info.getSubject() );
	      System.out.println( "Keywords=" + info.getKeywords() );
	      System.out.println( "Creator=" + info.getCreator() );
	      System.out.println( "Producer=" + info.getProducer() );
	      System.out.println( "Creation Date=" + info.getCreationDate() );
	      System.out.println( "Modification Date=" + info.getModificationDate());
	      System.out.println( "Trapped=" + info.getTrapped() ); 
	}
	
	
	public static void main(String args[]) throws IOException, CryptographyException {
		String filePath = "examples/journal.pone.0027856.pdf";
	//	filePath = "examples/Ngonga Ermilov - Complex Linking in a Nutshel.pdf";
	
		// Books need to be split.
		//	filePath = "C:/Users/Lyko/Desktop/Textmining datasets/Publikationsdaten/Digital Humanities Conference/2007/dh2007abstractsrevised.pdf";
		BaseDoc doc = new BaseDoc(filePath);
		try {
			doc.process();
			doc.splitFullText();
			System.out.println(doc.get(HEAD));
			System.out.println("=======================");
		/*	System.out.println(doc.get(BODY));
			System.out.println("=======================");
			System.out.println(doc.get(REFERENCES));
		*/
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}

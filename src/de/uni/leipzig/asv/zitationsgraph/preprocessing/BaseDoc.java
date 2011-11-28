package de.uni.leipzig.asv.zitationsgraph.preprocessing;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

/** Class to 
 * @author Klaus Lyko
 *
 */
public class BaseDoc {
	public static final String HEAD = "head";
	public static final String BODY = "body";
	public static final String REFERENCES = "references";
	
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
	}
	/**
	 * Reading and parsing a pdf.
	 * @throws IOException
	 */
	private void process_pdf() throws IOException {
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
     * This will print the documents data to System.out.
     *
     * @param document The document to get the metadata from.
     *
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
		/*
		 * 
		 * 
		StringTokenizer tokenizer = new StringTokenizer(fullText, "\n");
		while(tokenizer.hasMoreTokens()) {
			System.out.println(tokenizer.nextToken());
		}
		*/
		
		//first try to find references
		int ref = fullText.lastIndexOf("References");
		if(ref > -1) {
			references = fullText.substring(ref+10);
			body = fullText.substring(0, ref);
		}
		
		//try to get head
		int intro = fullText.indexOf("Introduction");
		if(intro>-1) {
			head = fullText.substring(0, intro);
			body = fullText.substring(intro);
		}
		// if both was found body is between
		if(ref > -1 && intro > -1) {
			body = fullText.substring(intro, ref);
		}		
	}
	
	public static void main(String args[]) throws IOException, CryptographyException {
		String filePath = "examples/journal.pone.0027856.pdf";
		//filePath = "examples/Ngonga Ermilov - Complex Linking in a Nutshel.pdf";
		BaseDoc doc = new BaseDoc(filePath);
		try {
			doc.process();
			doc.splitFullText();
			System.out.println(doc.get(HEAD));
			System.out.println("=======================");
			System.out.println(doc.get(BODY));
			System.out.println("=======================");
			System.out.println(doc.get(REFERENCES));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}

package de.uni.leipzig.asv.zitationsgraph.preprocessing;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.util.PDFOperator;

public class BoldAnnotater {

	
	/**
	 * anntotate String which are bold in the document, that the get text
	 * method include the annotations
	 * @param document for annotations
	 */
	private static String boldAnnotation ="<b>"; 
	
	public static void annotateBasedOnFont(PDDocument document){
		try {
			
			PDDocumentOutline outline= new PDDocumentOutline();
			document.getDocumentCatalog().setDocumentOutline(outline);
			List <Object> tokens = null;
			
			String currentBoldString;
			COSString firstBoldCOS;
			for (PDPage page : (List<PDPage>)document.getDocumentCatalog().getAllPages()){
			PDFStreamParser parser = new PDFStreamParser (page.getContents().getStream());
				
					parser.parse();
					tokens = parser.getTokens();
					Object token;
					int top;
					int left;
					for (int pos =0; pos<tokens.size();pos++ ){
						token = tokens.get(pos);
						if (token instanceof PDFOperator){
							
							PDFOperator op = (PDFOperator)token;
							 if(op.getOperation().equals("Tf")){
								COSName arr = (COSName) tokens.get(pos-2);
								PDFont font = (PDFont) page.getResources().getFonts().get(arr.getName());
								if (font.getBaseFont().contains("Bold")){
									
									if (tokens.get(pos+1) instanceof COSArray){
										COSArray array = (COSArray) tokens.get(pos+1);
										currentBoldString ="";
										firstBoldCOS =null;
										
										
					
										for (int i =0; i<array.size();i++){
											if (array.get(i) instanceof COSString){
												if (i==0)
													firstBoldCOS = (COSString) array.get(0);
												COSString string = (COSString) array.get(i);
												currentBoldString+=string.getString();
											}
										}
										String tempString =firstBoldCOS.getString();
										firstBoldCOS.reset();
										firstBoldCOS.append((boldAnnotation+tempString).getBytes());
										
											
									}		
								}
							}//operator TF
						}//token operator end
					}//for tokens end
				parser.close();
				PDStream updatedStream = new PDStream(document);  
				OutputStream out = updatedStream.createOutputStream();  
				ContentStreamWriter tokenWriter = new ContentStreamWriter(out);  
				tokenWriter.writeTokens(tokens);
				page.setContents(updatedStream);
				}//for end
				
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
		
	}//method end

	/**
	 * @param boldAnnotation the boldAnnotation to set
	 */
	public void setBoldAnnotation(String boldAnnotation) {
		this.boldAnnotation = boldAnnotation;
	}

	/**
	 * @return the boldAnnotation
	 */
	public String getBoldAnnotation() {
		return boldAnnotation;
	}
}

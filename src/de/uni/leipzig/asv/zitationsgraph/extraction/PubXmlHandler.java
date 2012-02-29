package de.uni.leipzig.asv.zitationsgraph.extraction;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Stack;
import java.util.Vector;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.uni.leipzig.asv.zitationsgraph.data.Author;
import de.uni.leipzig.asv.zitationsgraph.data.Citation;
import de.uni.leipzig.asv.zitationsgraph.data.Document;
import de.uni.leipzig.asv.zitationsgraph.data.Publication;


/**
 * This class find all needed entities in a XML file of the Digital Humanities quarterly
 * It override the DefaulHandler, that is used by parsing a xml source with a SAXParser
 * @author loco
 * TODO generalize schema currently it's very specific
 * A good idea would be to load the namespace and map this with our entity names
 * The map could be based on synonyms or String based approaches
 */
public class PubXmlHandler  extends DefaultHandler{
	
	
	private boolean inElement;
	private String tempAuthor;
	
	private Document currentDoc;
	private Vector <Citation> currentCits;
	private static final Logger log = Logger.getLogger(PubXmlHandler.class.getName());
	
	private static final SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");
	private static final int AUTHOR = 1;
	
	private static final int TITLE =2;
	
	private static final int HEAD = 3;
	
	private static final int SURNAME = 4;
	
	private static final int REFERENCES = 5;
	
	private static final int PUBSTMT = 6;
	
	private static final int YEAR = 7;
	
	private static final int OTHER =0;
	private static final int REF = 8;
	
	private static final int QUOTE =9;
	
	private static final int AUT_INFO =10;
	
	/**
	 * tempMap for the collected Entities
	 */
	private HashMap<String,String> tempMap;
	
	/**
	 * stack for the element tags to handle the current element
	 */
	private Stack<Integer> xmlStack;
	
	/**
	 *current element 
	 */
	private int type;
	
	/**
	 * name recognition if the author name is not tagged in the bib entry
	 */
	private AuthorNameRecognition ane;
	
	public PubXmlHandler (){
		super();
		ane = new AuthorNameRecognition();
		xmlStack = new Stack<Integer>();
		tempMap = new HashMap<String,String>();
		type =-1;
		
	}
	
	/**
	 * General we push the specific constant for the read elementName.
	 * The year exist sometimes as attribute, that is not necessary to push it 
	 * on the stack.
	 * @param String uri 
	 * @param localName
	 * @param qName elementeName
	 * @param attributes
	 */
	 public void startElement (String uri, String localName,
             String qName, Attributes attributes)
	 throws SAXException
	 {
		
		 if (qName.equals("titleStmt")){
			xmlStack.push(HEAD); 
		 }else if (qName.equals("author")||qName.equals("editor")){
			 xmlStack.push(AUTHOR);
		 }else if (qName.equals("family")){
			 xmlStack.push(SURNAME);
		 }else if (qName.equals("listBibl")){
			 xmlStack.push(REFERENCES);
			 currentCits = new Vector<Citation>();
		 }else if (qName.equals("title")){
			 xmlStack.push(TITLE);
			 
		 }else if(qName.equals("publicationStmt")){
			 xmlStack.push(PUBSTMT);
		 }else if (qName.equals("date")){
			 if (attributes.getLength()!=0){
				 String year =attributes.getValue("when");
				 if (year !=null){
				 year = year.split("-")[0];
					tempMap.put("year", year);
				 }
			 }else {
				 xmlStack.push(YEAR);
			 }
		 }else if (qName.equals("bibl")&&xmlStack.contains(REFERENCES)){
			 tempMap.put("title", null);
			 tempMap.put("author", null);
			 tempMap.put("year", null);
			 tempMap.put("bib",null);
			 tempMap.put("tag", attributes.getValue("xml:id"));
			 ane.resetRecognizer();
			 xmlStack.push(REF);
		 }else if (qName.equals("quote")&&xmlStack.contains(TITLE)){
			 xmlStack.push(QUOTE);
		 }else if (qName.equals("dhq:authorInfo")){
			 
			 xmlStack.push(AUT_INFO);
		 }else
		 {
			 xmlStack.push(OTHER);
			 
		 }
	 }
	 
	 /**
	  * build the entities.
	  * The type of the object depends on the element on the top of the stack
	  */
	   public void endElement (String uri, String localName, String qName)
     throws SAXException
     {	
		   if (!xmlStack.isEmpty())
		   type = xmlStack.pop();
		   else type = -1;
		   switch (type){
		   	case HEAD:
		   		String titlePub = tempMap.get("title");
		   		if (titlePub != null){
		   			titlePub = titlePub.replaceAll("\\s+", " ");
		   			
		   		}
		   		currentDoc.getPublication().setTitle(titlePub);
		   		Vector <Author> authors = new Vector<Author>();
		   		if (tempMap.get("author")!=null){
			   		for (String aut :tempMap.get("author").split("#")){
			   			authors.add(new Author(aut));
			   		}
			   		tempMap.put("author", null);
			   		tempMap.put("title",null);
		   		}
		   		currentDoc.getPublication().setAuthors(authors);
		   		break;
		   	case PUBSTMT:
		   		currentDoc.getPublication().setYearString(tempMap.get("year"));
		   		tempMap.put("year", null);
		   		break;
		   	case REF:
		   		if (xmlStack.contains(REFERENCES)){
			   		Vector <Author> authorsRef = new Vector<Author>();
			   		if (tempMap.get("author")!=null){
				   		for (String aut :tempMap.get("author").split("#")){
				   			authorsRef.add(new Author(aut));
				   		}
			   		}else if (tempMap.get("bib")!= null) {
			   			String bib = tempMap.get("bib");
			   			bib = bib.replaceAll("\\s+", " ");
			   			if (bib.length()>15){
			   				ane.recognizeNamesWithMatcher(bib);
			   				for (Token name: ane.getNameTree().values()){
			   					log.info(name.getValue());
			   					authorsRef.add(new Author(name.getValue()));
			   				}
			   			}
			   		}
			   		String title = tempMap.get("title");
			   		if (title != null){
			   			title = title.replaceAll("\\s+", " ");
			   		}
			   		String year = tempMap.get("year");
			   		String tag = tempMap.get("tag");
			   		Publication pub = new Publication(authorsRef,title);
			   		pub.setYearString(year);
			   		Citation cit = new Citation(pub);
			   		cit.setTag(tag);
			   		if (title!= null)
			   		this.currentCits.add(cit);
		   		}
		   		break;
		   	case REFERENCES:
		   		
		   		this.currentDoc.setCitations(this.currentCits);
		   		break;
		   	default: 
		   	   break;
		   }
     }
	 
	 public void startDocument ()
     throws SAXException
     {
		 currentDoc = null;
		 currentDoc = new Document(new Publication(null, null));
		
		 currentDoc.setCitations(new Vector<Citation>()); // avoid nullpointer ex
     }

	 public void endDocument ()
     throws SAXException
     {
     // no op
     }
	 
	 /**
	  * fill the tempMap with the current read text data and the current stack value
	  */
	 public void characters(char[] ch, int start, int length) throws SAXException {
		 int type;
		 if (!xmlStack.isEmpty())
		type = this.xmlStack.peek();
		 else type = -1;
		switch (type){
		case AUTHOR:
			if ((xmlStack.contains(REFERENCES)||xmlStack.contains(HEAD)))
			if (tempMap.get("author")==null)
				this.tempMap.put("author",new String(ch,start,length));
			else
				this.tempMap.put("author",tempMap.get("author")+"#"+new String(ch,start,length));
			break;
		case TITLE:
			if (!xmlStack.contains(AUT_INFO)){
				if (tempMap.get("title")!= null){
					tempMap.put("title",tempMap.get("title")+
							" "+new String (ch,start,length));
				}else
					tempMap.put("title", new String (ch,start,length));
			}
			break;
		case QUOTE:
			if (tempMap.get("title")!=null){
				tempMap.put("title",tempMap.get("title")+
						" "+new String (ch,start,length));
			}else
				tempMap.put("title", new String (ch,start,length));
		case YEAR:
			String year = new String (ch, start,length);
			year = year.split("-")[0];
			tempMap.put("year",year);
			break;
		case REF:
			if (tempMap.get("bib")==null){
				tempMap.put("bib",new String (ch,start,length));
			}
			
			
		}
		
	}

	 
	 public Document getCurrentDoc(){
		 return this.currentDoc;
	 }
}

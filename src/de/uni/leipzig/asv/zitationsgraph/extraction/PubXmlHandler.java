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
	private static final int CIT = 11;
	
	private static final int PTR =12;
	
	private HashMap<String,String> phrasePointer;
	
	private StringBuffer tempBuffer;
	
	private HashMap<String,String> tempMap;
	
	private Stack<Integer> xmlStack;
	
	private int type;
	
	private AuthorNameRecognition ane;
	
	public PubXmlHandler (){
		super();
		ane = new AuthorNameRecognition();
		xmlStack = new Stack<Integer>();
		tempMap = new HashMap<String,String>();
		tempBuffer = new StringBuffer();
		phrasePointer = new HashMap<String,String>();
		type =-1;
		
	}
	
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
			 String id  =attributes.getValue("xml:id");
			 if (id !=null){
				 tempMap.put("id", id);
			 }
			 tempMap.put("title", null);
			 tempMap.put("author", null);
			 tempMap.put("year", null);
			 tempMap.put("bib",null);
			 ane.resetRecognizer();
			 xmlStack.push(REF);
		 }else if (qName.equals("quote")&&xmlStack.contains(TITLE)){
			 xmlStack.push(QUOTE);
		 }else if (qName.equals("dhq:authorInfo")){
			 xmlStack.push(AUT_INFO);
		 }else if (qName.equals("cit")){
			 xmlStack.push(CIT);
		 }else if (qName.equals("ptr")||qName.equals("ref")){
			 if (attributes.getValue("target")!= null){
				 String pointer = attributes.getValue("target");
				 if (pointer.length()<20){
					 pointer = pointer.replace("#", "");
					 String [] splitText = this.tempBuffer.toString().split("\\.");
					 String phrase;
					 if (splitText.length!= 0){
						phrase = splitText[splitText.length-1].replaceAll("\\s+", " ");
					 }else {
						phrase = tempBuffer.toString().replaceAll("\\s+", " ");
					 }
					 this.phrasePointer.put(pointer, phrase);
					 tempBuffer.delete(0, tempBuffer.length()-1);
				 }
			 }
		 }else{
			 xmlStack.push(OTHER); 
		 } 
	 }
	 
	 
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
			   		Vector<String> phrases = new Vector<String>();
			   		if ( tempMap.get("id")!=null)
			   		phrases.add(tempMap.get("id"));
			   		String title = tempMap.get("title");
			   		if (title != null){
			   			title = title.replaceAll("\\s+", " ");
			   		}
			   		String year = tempMap.get("year");
			   		Publication pub = new Publication(authorsRef,title);
			   		pub.setYearString(year);
			   		Citation cit = new Citation(pub);
			   		cit.setTextphrases(phrases);
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
	 
	 public void characters(char[] ch, int start, int length) throws SAXException {
		 int type;
		 tempBuffer.append(new String(ch,start,length));
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

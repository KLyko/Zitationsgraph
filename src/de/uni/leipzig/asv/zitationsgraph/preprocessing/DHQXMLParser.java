package de.uni.leipzig.asv.zitationsgraph.preprocessing;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import de.uni.leipzig.asv.zitationsgraph.data.Document;
import de.uni.leipzig.asv.zitationsgraph.data.Author;
import de.uni.leipzig.asv.zitationsgraph.data.Citation;
import de.uni.leipzig.asv.zitationsgraph.data.Publication;

/**
 * Class to process the XML documents of DHQ. We may try to detect if the 
 * XML file is formatted according to the DHQ schema.
 * If so we have all informations (authors, title, citations etc.) already
 * as structured data within the XML. So, we only need to transform all 
 * informations into our data structure.
 * As of now, we're not able to get the sentences, of the cited bibliography
 * items. This is subject to a future version.
 * 
 * 
 * TODO Implement the body processing in order to get text passages of the citations.
 * TODO Check if the given file is formatted according to the DHQ schema.
 * TODO Immunize to failures in DHQ-XML format.
 * FIXME Package Preprocessing is not the best solution.
 * @version 0.2 Able to Parse title, authors, venue and the bibliography.
 * @author Klaus Lyko
 * 
 * 
 */
public class DHQXMLParser {
	Logger logger = Logger.getLogger("ZitGraph");
	
	private String XMLFile;
	List<Author> authors;
	String title;
	// pub is holding information about the paper this XML representes
	Publication pub;
	// citations for the bibliography entries
	Vector<Citation> citations;
	
	
// --- Basic setters, constructor and main functions ------------------------------------------------------	
	public void setFile(String pathToXMLFile) {
		init();
		XMLFile = pathToXMLFile;
	}
	public DHQXMLParser(String pathToXMLFile) {
		setFile(pathToXMLFile);
	}
	/**
	 * Init for  possibly a new document.
	 */
	private void init() {
		 authors = new LinkedList<Author>();
		 pub = new Publication(new Vector<String>(), "foobar");
		 citations = new Vector<Citation>();
	}
	
	/**
	 * Method to initialize parsing.
	 * @param pathToXMLFile XML file to parse.
	 * @return de.uni.leipzig.asv.zitationsgraph.data.Document holding all information, we were able to parse out of the XML file.
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public Document processXMLFile(String pathToXMLFile) 
	throws SAXException, IOException, ParserConfigurationException {
		this.setFile(pathToXMLFile);
		org.w3c.dom.Document D = DocumentBuilderFactory
		.newInstance()
		.newDocumentBuilder()
		.parse(new File(pathToXMLFile));
		processDocument( D );		
		return buildDocument();
	}
	
	public static void main(String args[]) {
		String example;		
		example = "examples/dhq_000098.xml";
		example = "examples/dhq_000023.xml";
		
		DHQXMLParser read = new DHQXMLParser(example);
			
		try
		{
			de.uni.leipzig.asv.zitationsgraph.data.Document doc = read.processXMLFile("examples/dhq_000023.xml");
			System.out.println("TITLE:"+read.pub);
			
			System.out.println("AUTHORS:");
			for(Author a : read.authors) {
				System.out.println("\t"+a.getName()+" - "+a.getAffiliation());
			}
			for(Citation cit : read.citations) {
				System.out.println("CIT - "+cit);
			}
			// no toString() method implemented for Document class
			// System.out.println(doc);
		}	
		catch( Exception e )
		{
			e.printStackTrace();
			System.out.println( "XML-Datei nicht gef. od. sonst. Fehler");
			System.exit(0);
		} 
	}
// --- Global processing of XML elements ------------------------------------------------------
	/**
	 * Parse the XML to a Document
	 * @param docu
	 */
	protected void processDocument( org.w3c.dom.Document docu )
	{
		Element E = docu.getDocumentElement();
		processNodeList(E.getChildNodes());		
	}	
	
	/**
	 * Recursive function to iterate over all nodes
	 * @param nL
	 */
	private void processNodeList(NodeList nL) {
		for(int i=0; i< nL.getLength(); i++ ) {
			Node n = nL.item(i);
			boolean deeper = processNode(n);
			if(deeper)
				processNodeList(n.getChildNodes());
		}
	}

	/**
	 * Method determines whether the given node is fixture of a subordinate processing step. If so, it calls the specific
	 * method to handle the parsing, e. g. of head, body or bibliographic informations.
	 * @param node An XML node.
	 * @return True, if processing is done by a subordinate processing step, so we don't need to recursively check the nodes children. 
	 * 		   False otherwise.
	 */
	private boolean processNode(Node node) {
		if(node.getNodeName().equalsIgnoreCase("teiHeader")) {
			traverseHeadNodes(node.getChildNodes());
			return false;
		}			
		else if(node.getNodeName().equalsIgnoreCase("listBibl")) {
			parseBiblioItems(node);
			return false;
		}
		// parse body to get phrases for citations
		return true;
	}
	
	/**
	 * Method to build a de.uni.leipzig.asv.zitationsgraph.data.Documents, once
	 * every parsing operation is done.
	 * @return A new Document, wrapping around all informations.
	 */
	private Document buildDocument() {
		// wrap around buggy author definition in de.uni.leipzig.asv.zitationsgraph.data.Document
		Vector<String> wrapedAuthors = new Vector<String>();
		for(Author a:authors)
			wrapedAuthors.add(a.getName());
		pub.setAuthors(wrapedAuthors);
		de.uni.leipzig.asv.zitationsgraph.data.Document doc = new de.uni.leipzig.asv.zitationsgraph.data.Document(pub);
		//add phrases to citations		
		doc.setCitations(citations);
		return doc;
	}
	
// --- Processing of header informations (authors, title)----------------------------------------------------
	
	/**
	 * Recursive function to process the descendant nodes of the <teiHeader> node 
	 * to parse authors, title and venue of this document.
	 * @param headNodes
	 */
	private void traverseHeadNodes(NodeList headNodes) {
		for(int i=0; i< headNodes.getLength(); i++ ) {
			Node n = headNodes.item(i);
			if(n.getNodeName().equalsIgnoreCase("dhq:authorInfo")) {
				authors.add(parseAuthorInfo(n));
			}
			if(n.getNodeName().equalsIgnoreCase("titleStmt")) {
				this.title = parseTitle(n);
				this.pub.setTitle(title);
			}
			if(n.getNodeName().equalsIgnoreCase("publicationStmt")) {
				parsePublication(n);
			}
			traverseHeadNodes(n.getChildNodes());
		}
	}
	
	/**
	 * Parses title out of a <titleStmt> node
	 * @param n
	 */
	private String parseTitle(Node n) {
		NodeList childs = n.getChildNodes();
		for(int i = 0; i<childs.getLength(); i++) {
			if(childs.item(i).getNodeName().equalsIgnoreCase("title")) {
				return childs.item(i).getTextContent();
			}
		}
		return "";
	}
	

	/**
	 * Parse expression like <code>
	 *  <dhq:authorInfo>
          <dhq:author_name>Aaron <dhq:family>Kashtan</dhq:family></dhq:author_name>
          <dhq:affiliation>Department of English University of Florida </dhq:affiliation>
          <email>akashtan@ufl.edu</email>
          <dhq:bio>
            <p>Aaron Kashtan is an ABD Ph.D. student in the Department of English at the University
              of Florida. His dissertation considers handwriting as a figure for nostalgic
              opposition to the transparent aspects of digital culture.</p>
          </dhq:bio>
        </dhq:authorInfo>
	 * </code>
	 * @param n
	 * @return
	 */
	private Author parseAuthorInfo(Node n) {
		String name = "";
		String affiliation ="";
		NodeList childs = n.getChildNodes();
		for(int i=0; i<childs.getLength(); i++) {
			Node child=childs.item(i);
			if(child.getNodeName().equalsIgnoreCase("dhq:author_name")) {
				name = child.getTextContent().trim();
			}
			else
				if(child.getNodeName().equalsIgnoreCase("dhq:affiliation")) {
					affiliation = child.getTextContent().trim();
				}
		}
		Author author = new Author(name);
		author.setAffiliation(affiliation);
		return author;
	}
	
	/**
	 * Parse information of the <publicationStmt> Node
	 *  <idno type="DHQarticle-id">000098</idno>
        <idno type="volume">005</idno>
        <idno type="issue">3</idno>
        <date when="2011-11-15">15 November 2011</date>
	 * @param n
	 */
	private void parsePublication(Node n) {
		NodeList childs = n.getChildNodes();
		Date date = null; String dateText=null,volume=null,issue=null;
		for(int i = 0; i<childs.getLength(); i++) {
			Node child = childs.item(i);
			if(child.getNodeName().equalsIgnoreCase("#text"))
				continue;			
			if(child.getNodeName().equalsIgnoreCase("idno")) {
				NamedNodeMap map = child.getAttributes();
				for(int j=0;j<map.getLength();j++) {
					if(map.item(j).getNodeValue().equalsIgnoreCase("volume"))
						volume = child.getTextContent().trim();
					else if(map.item(j).getNodeValue().equalsIgnoreCase("issue"))
						issue = child.getTextContent().trim();
				}
			}
			else if(child.getNodeName().equalsIgnoreCase("date")) {
				dateText = child.getTextContent().trim();
				pub.setYearString(dateText);
				if(child instanceof Element) {
					Element elem = (Element) child;
					DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				    try {
						date = (Date)formatter.parse(elem.getAttribute("when"));
						pub.setYear(date);
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}
		}
		// Apparently, we have no fields for volume and issue of a journal
		pub.setVenue("Digital Humanities Quaterly");
		pub.setYear(date);
	}

// --- Processing the bibliography ----------------------------------------------------	
	
	/**
	 * Method to parse bibliography list, e.g.
	 * <code>
	 *  <listBibl>
         <bibl label="Alt 2002" xml:id="alt2002">
         Alt, Casey. <title rend="quotes">The Materialities of Maya.</title>
          <title rend="italic">Configurations</title> 10 (2002): 387–422.
        </bibl>
		...
		</listBibl>
		</code>
	 * @param n Node <listBibl>
	 */
	private void parseBiblioItems(Node n) {
		for(int i = 0; i<n.getChildNodes().getLength(); i++) {
			Node bib = n.getChildNodes().item(i);
			if(bib.getNodeName().equalsIgnoreCase("bibl") && bib instanceof Element) {
				citations.add(parseBibliographyEntry((Element) bib));
			}
		}
	}
	
	/**
	 * Method to parse a single entry in the bibliography. E.g.
	 * <bibl label="Birkerts 2006" xml:id="birkerts2006">
	 * Birkerts, Sven. <title rend="italic">The
     * Gutenberg Elegies: The Fate of Reading in an Electronic Age</title>. 2nd edition.
     * Macmillan, 2006.
     * </bibl>
	 * @param e Element <
	 * @return
	 */
	private Citation parseBibliographyEntry(Element e) {
		Citation cit = new Citation(null);
		String label = e.getAttribute("label");
		String id = e.getAttribute("xml:id");
		NodeList childs = e.getChildNodes();
		String authorsString=null, title=null, venue=null;
		// 1st #text child are the authors (surname ", " forename(s)) separated by " and "
		// 2nd title child is the title of the pub
		// following childs are ./ In <title>VENUE</title> ...
		for(int i = 0; i<childs.getLength(); i++) {
			Node n = childs.item(i);			
			if(n.getNodeType() == Node.TEXT_NODE && i == 0) {
				authorsString = n.getTextContent().trim();
			}
			else if(n.getNodeName().equalsIgnoreCase("title") && i<2) {
				title = n.getTextContent().trim();
			}
			else if(n.getNodeName().equalsIgnoreCase("title") && i>=2) {
				venue = n.getTextContent().trim();
			}
		}
		if(title == null)
			title = e.getTextContent().trim();
		if(authorsString == null || authorsString.trim().length()==0)
			authorsString = label;
		// create referenced publication
		Publication pub = new Publication(divideAuthors(authorsString), title);
		// try to determine date
		Pattern p = Pattern.compile("\\d+");
		Matcher matcher = p.matcher(label);
		if(matcher.find()) {
			Date d = new Date();
			d.setYear(Integer.parseInt(matcher.group()));
			pub.setYear(d);
		}
		// if we got a venue;	
		if(venue != null)
			pub.setVenue(venue);
		cit.setPublication(pub);
		cit.setTag(id);
		return cit;
	}
	
	/**
	 * Authors are given as a String. Multiple authors are divided by " and ".
	 * @param authorString
	 * @return Vector holding all authors
	 * TODO return a Vector of authors, once the Publication class supports it.
	 */
	private Vector<String> divideAuthors(String authorString) {
		Vector<String> authors = new Vector<String>();
		String[] as = authorString.split(" and ");
		for(String s : as)
			authors.add(new Author(s).getName());
		return authors;
	}
	
}



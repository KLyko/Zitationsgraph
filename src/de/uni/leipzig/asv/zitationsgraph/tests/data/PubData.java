package de.uni.leipzig.asv.zitationsgraph.tests.data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.TreeMap;
import java.util.Vector;

import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

import de.uni.leipzig.asv.zitationsgraph.data.Author;
import de.uni.leipzig.asv.zitationsgraph.data.Citation;
import de.uni.leipzig.asv.zitationsgraph.data.Document;
import de.uni.leipzig.asv.zitationsgraph.data.Publication;
import de.uni.leipzig.asv.zitationsgraph.extraction.BodyExtraction;
import de.uni.leipzig.asv.zitationsgraph.extraction.HeadExtraction;
import de.uni.leipzig.asv.zitationsgraph.extraction.ReferenceExtraction;
import de.uni.leipzig.asv.zitationsgraph.preprocessing.BaseDoc;
import de.uni.leipzig.asv.zitationsgraph.preprocessing.FolderReader;
import de.uni.leipzig.asv.zitationsgraph.db.DBLoader;

public class PubData {

	private static final Logger log = Logger.getLogger(PubData.class.getName());
	public static final String NEW_REF_VECTOR ="newReferences";
	public static final String NEW_DOC = "newDocument";
	public static final String NEW_REF_PART ="newReferencePart";
	public static final String NEW_HEAD_PART = "newHeadPart";
	public static final String RESET = "Reset";
	public static final String NEW_HEAD_ENTITIES = "newHeadEntities";
	public static final String ALL_STEPS = "allSteps";

	private String currentFile;
 

	public static final String NEW_DATA = "newData";
	private TreeMap<String, Publication> pubMap;
	private TreeMap <String,List<String>> citeMap;
	private PropertyChangeSupport propertyChange;
	private FolderReader folExtractor;
	private ReferenceExtraction refExtractor;
	private HeadExtraction headExtractor;
	private BodyExtraction bodyExtractor;
	private boolean isGraphVis = true;
	private boolean isStoreInDb = false;
	private DBLoader dbloader = null;

	public PubData(FolderReader folderExtractor, ReferenceExtraction refExtraction, HeadExtraction headExtraction, BodyExtraction bodyExtraction){
		this.folExtractor = folderExtractor;
		this.refExtractor = refExtraction ;
		this.headExtractor = headExtraction;
		this.bodyExtractor = bodyExtraction;
		pubMap = new TreeMap<String,Publication>(new PubComparator());
		citeMap = new TreeMap<String,List<String>>(new PubComparator());
		propertyChange = new PropertyChangeSupport(this);

	}

	public void  initProcess(String[] folders){
		//startDB-loader if needed
		if (isStoreInDb)
			dbloader = new DBLoader();

		try {
			for (String folder: folders){

			BaseDoc [] docs;
			if (!folder.contains("."))
				docs= this.folExtractor.processFolder(folder);
			else
				docs = new BaseDoc[]{this.folExtractor.processFile(new File(folder))};
				for (BaseDoc d : docs){
					propertyChange.firePropertyChange(NEW_DOC, "",d.getFileName());
					currentFile = d.getFileName();
					System.out.println("work on:" +currentFile);

					if (d.get(BaseDoc.HEAD)!=null && !d.isDHQDoc())
						propertyChange.firePropertyChange(NEW_HEAD_PART, "", d.get(BaseDoc.HEAD));
					if (d.get(BaseDoc.REFERENCES)!=null && !d.isDHQDoc())
						propertyChange.firePropertyChange(NEW_REF_PART, "", d.get(BaseDoc.REFERENCES));

					Document doc = null;
					if (d.get(BaseDoc.HEAD)!=null&&!d.isDHQDoc()){
						this.headExtractor.headMining(d.get(BaseDoc.HEAD));
						Vector <Author> authors = new Vector <Author> (this.headExtractor.getAuthors());
						doc = new Document(new Publication(
								authors, this.headExtractor.getTitle()));

						propertyChange.firePropertyChange(NEW_HEAD_ENTITIES, null, doc.getPublication());
						this.refExtractor.referenceMining(d.get(BaseDoc.REFERENCES));
						doc.setCitations(refExtractor.getCitationVector());


						propertyChange.firePropertyChange(NEW_REF_VECTOR,null , refExtractor.getCitationVector());
						if (d.get(BaseDoc.BODY)!= null){
							this.bodyExtractor.setText(d.get(BaseDoc.BODY));
							this.bodyExtractor.extractQuotes(refExtractor.getCitationVector());		
						}
					}else if(d.isDHQDoc()) {
						doc = d.getParsedDHQDocument();
						//	propertyChange.firePropertyChange(NEW_HEAD_PART, "", doc);
						//	propertyChange.firePropertyChange(NEW_REF_PART, "", d.get(BaseDoc.REFERENCES));

						propertyChange.firePropertyChange(NEW_HEAD_ENTITIES, null, doc.getPublication());
						propertyChange.firePropertyChange(NEW_REF_VECTOR,null , doc.getCitations());
					}// else xml parsing	
					if (this.isStoreInDb){
						dbloader.saveDocument(doc);
					}
					if (this.isGraphVis){
						this.addPublication(doc);
					}
				}// BaseDoc iteration
			}//source iteration

			if (this.isGraphVis)
			propertyChange.firePropertyChange(NEW_DATA,false,true);
			//testPrint();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addPublication (Document doc){
		String key = doc.getPublication().getTitle();
		if (!pubMap.containsKey(key)){
			if (doc.getPublication().getTitle()==null){
				log.info(currentFile);
			}

			pubMap.put(doc.getPublication().getTitle(), doc.getPublication());
		}
		this.addCitations(key, doc.getCitations());

	}

	private void addCitations (String citingPub,Vector<Citation> citations){
		List<String> citedList = citeMap.get(citingPub);
		if (citedList== null){
			citedList = new ArrayList<String>();

		}
		for (Citation cit :citations){
			if (!pubMap.containsKey(cit.getPublication().getTitle())){
				pubMap.put(cit.getPublication().getTitle(), cit.getPublication());
			}
			citedList.add(cit.getPublication().getTitle());
		}
		citeMap.put(citingPub, citedList);
	}

	public void testPrint (){
		for (Entry <String,List<String>> e : this.citeMap.entrySet()){
			Publication pub = pubMap.get(e.getKey());
			System.out.println("title:"+e.getKey()+"authors:"
					+Arrays.toString(pub.getAuthors()
							.toArray(new Author[0])));
			List<String>citList = e.getValue();
			System.out.println("---Citing--------");
			System.out.print("[");
			if (citList!=null){
			for (String cit: citList){
				if (pubMap.get(cit)==null){
					log.info(cit);
				}
				List<Author> authors = pubMap.get(cit).getAuthors();
				System.out.println ("authors"+Arrays.toString(authors
						.toArray(new Author[0]))+"title"+cit);
			}
			System.out.println("]");
			}
		}
	}
	/**
	 * @param pubMap the pubMap to set
	 */
	public void setPubMap(TreeMap<String,Publication> pubMap) {
		this.pubMap = pubMap;
	}

	/**
	 * @return the pubMap
	 */
	public TreeMap<String, Publication> getPubMap() {
		return pubMap;
	}



	/**
	 * @return the citeMap
	 */
	public TreeMap <String,List<String>> getCiteMap() {
		return citeMap;
	}

	/**
	 * @param isGraphVis the isGraphVis to set
	 */
	public void setGraphVis(boolean isGraphVis) {
		this.isGraphVis = isGraphVis;
	}

	/**
	 * @return the isGraphVis
	 */
	public boolean isGraphVis() {
		return isGraphVis;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener){
		propertyChange.addPropertyChangeListener(listener);
	}

	/**
	 * @param isStoreInDb the isStoreInDb to set
	 */
	public void setStoreInDb(boolean isStoreInDb) {
		this.isStoreInDb = isStoreInDb;
	}

	/**
	 * @return the isStoreInDb
	 */
	public boolean isStoreInDb() {
		return isStoreInDb;
	}
}
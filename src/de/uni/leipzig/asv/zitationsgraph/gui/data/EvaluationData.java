package de.uni.leipzig.asv.zitationsgraph.gui.data;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

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

public class EvaluationData {
	
	private static final Logger log = Logger.getLogger(EvaluationData.class.getName());
	
	public static final String SPLIT_STEP = "splitStep";
	public static final String HEAD_STEP = "headSteps";
	public static final String REF_STEPS = "referenceSteps";
	public static final String PHR_STEPS = "phraseSteps";
	
	public static final String NEW_HEAD_ENTITIES = "newHeadEntities";
	public static final String NEW_REF_VECTOR ="newReferences";
	
	public static final String NEW_DOC = "newDocument";
	public static final String NEW_PART ="newPart";
	public static final String NEW_HEAD_TEST_ENTITIES ="newHeadTestEntities";
	public static final String NEW_SENTENCES ="newBodySentences";
	public static final String NEW_SENTENCES_TEST ="newBodySentencesTest";
	public static final String NEW_REF_TEST_VECTOR = "newRefTestVector";
	public static final String NEW_TEST_DOC = "newTestInstDoc";
	
	public static final String PRECISION = "precision";
	
	public static final String RECALL = "recall";
	
	public static final String AUTHOR_EVAL  = "author#Evaluation";
	
	public static final String TITLE_EVAL = "title#Evaluation";
	
	public static final String YEAR_EVAL ="year#Evaluation";
	
	public static final String PHRASE_EVAL ="phrase#Evaluation";
	
	public static final String OVERALL_EVAL = "overall#Evaluation";
	
	public static final String EVAL = "Evaluation";

	private ReferenceExtraction refExtractor;
	
	private HeadExtraction headExtractor;
	
	private BodyExtraction bodyExtractor;
	
	private Levenshtein sim;
	
	
	private List<String> refSource ;
	private HashMap<String,String> partMap;
	private HashMap <String, String> methodSerMap ;
	private HashMap <String,Object> testSerMap;
	private HashMap <String, Object> testMethodMap;
	private PropertyChangeSupport propertyChange ;
	private String type;
	
	
	public EvaluationData (){
		propertyChange = new PropertyChangeSupport(this);
		refExtractor = new ReferenceExtraction();
		headExtractor = new HeadExtraction();
		bodyExtractor = new BodyExtraction();
		setPartMap(new HashMap<String,String>());
		methodSerMap = new HashMap<String,String>();
		testMethodMap = new HashMap<String,Object>();
		testSerMap = new HashMap <String,Object>();
		sim = new Levenshtein();
		setRefSource(new ArrayList<String>());
		
	}
	
	public void addPropertyChangeListener (PropertyChangeListener listener){
		propertyChange.addPropertyChangeListener(listener);
	}
	
	public void removePropertyChangeListener (PropertyChangeListener listener){
		propertyChange.removePropertyChangeListener(listener);
	}
	
	public void initSubProcess(List<String> sources) throws IOException {
		File dir;
		BufferedReader br;
		StringBuffer dataBuffer ;
		StringBuffer refBuffer;
		
		FilenameFilter filter = new FilenameFilter(){

			@Override
			public boolean accept(File dir, String name) {
				if (type.equals(HEAD_STEP)){
					if (name.endsWith(Constants.HEAD_PART))
						return true;
				}else if (type.equals(REF_STEPS)){
					if (name.endsWith(Constants.REF_PART))
						return true;
				}else if (type.equals(PHR_STEPS)){
					if (name.endsWith(Constants.BODY_PART))
						return true;
				}
				return false;
			}

		};
		for (int i = 0 ;i<sources.size();i++){
			String source = sources.get(i);
			dir = new File(source);

			File[] files = dir.listFiles(filter);
			if (files ==null){
				files = new File[]{dir};
			}
			for (File f :files){
				br = new BufferedReader(new FileReader(f));
				dataBuffer = new StringBuffer();
				while(br.ready()){
					dataBuffer.append(br.readLine()+System.getProperty("line.separator"));
				}
				this.propertyChange.firePropertyChange(NEW_DOC, "", f.getName());
					
				if (type.equals(HEAD_STEP)){
					this.partMap.put(f.getName(), dataBuffer.toString());
					this.headExtractor.headMining(dataBuffer.toString());
					Vector<Author> authors = new Vector<Author>(headExtractor.getAuthors());
					Publication pub = new Publication (authors,
							headExtractor.getTitle());
					this.testMethodMap.put(f.getName(),pub);
					this.propertyChange.firePropertyChange(NEW_HEAD_ENTITIES, null, pub);
				}else if (type.equals(REF_STEPS)){
					this.partMap.put(f.getName(), dataBuffer.toString());
					refExtractor.referenceMining(dataBuffer.toString());
					this.testMethodMap.put(f.getName(), refExtractor.getCitationVector());
					this.propertyChange.firePropertyChange(NEW_REF_VECTOR, null, refExtractor.getCitationVector());
				}else if (type.equals(PHR_STEPS)){
						
					br = new BufferedReader(new FileReader(this.refSource.get(i)));
					refBuffer = new StringBuffer();
					while (br.ready()){
						refBuffer.append(br.readLine()+System.getProperty("line.separator"));
					}
					this.partMap.put(f.getName(), dataBuffer.toString());
					refExtractor.referenceMining(refBuffer.toString());
					bodyExtractor.setText(dataBuffer.toString());
					bodyExtractor.extractQuotes(refExtractor.getCitationVector());
					HashSet<String> sentences = new HashSet<String>();
					for (Citation cit :refExtractor.getCitationVector())
						sentences.addAll(cit.getTextphrases());
					this.testMethodMap.put(f.getName(),sentences);
					this.propertyChange.firePropertyChange(NEW_SENTENCES, null, sentences);		
						
				}
				
			}

		}
	}

	public void initTestInstances(List<String> sources) throws IOException, ClassNotFoundException{
		this.testSerMap.clear();
		String testSource;
		for (String source: sources){
			
			FileInputStream fis = new FileInputStream (source);
			ObjectInputStream ois = new ObjectInputStream (fis);
			String[]tempPath = source.split("\\\\");
			testSource = tempPath[tempPath.length-1];
			this.propertyChange.firePropertyChange(NEW_TEST_DOC, "", testSource);
			if (type.equals(REF_STEPS)){
				Vector<Citation> citVector = (Vector<Citation>) ois.readObject();
				this.testSerMap.put(testSource, citVector);
				this.propertyChange.firePropertyChange(NEW_REF_TEST_VECTOR, null, citVector);
			}else if (type.equals(HEAD_STEP)){
				Publication pub = (Publication)ois.readObject();
				this.testSerMap.put(testSource, pub);
				this.propertyChange.firePropertyChange(NEW_HEAD_TEST_ENTITIES, null,pub);
			}else if (type.equals(PHR_STEPS)){
				HashSet<String> sentences = (HashSet<String>)ois.readObject();
				this.testSerMap.put(testSource, sentences);
				this.propertyChange.firePropertyChange(NEW_HEAD_TEST_ENTITIES, null,sentences);
			}
			ois.close();
			
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public void compareOverall (){
		String testSerKey;
		HashSet<Object> truePositives = new HashSet<Object>();
		HashSet<Object> falsePositives = new HashSet<Object> ();
		HashSet<Object> falseNegatives = new HashSet<Object>();
		HashSet<Object> trueAuthorPositives = new HashSet<Object>();
		HashSet<Object> methAuthorSet = new HashSet<Object>();
		HashSet<Object> serAuthorSet = new HashSet<Object>();
		HashSet<Object> trueTitlePositives = new HashSet<Object>();
		HashSet<Object> methTitleSet = new HashSet<Object>();
		HashSet<Object> serTitleSet = new HashSet<Object>();
		HashSet<Object> trueYearPositives = new HashSet<Object>();
		HashSet<Object> methYearSet = new HashSet<Object>();
		HashSet<Object> serYearSet = new HashSet<Object>();
		
		
		for (Entry<String, Object> methodEntry: this.testMethodMap.entrySet()){
			if (type.equals(REF_STEPS)){
				Vector<Citation> citMethVec = (Vector<Citation>) methodEntry.getValue();
				//HashSet are faster
				HashSet <Citation>  citMethSet= new HashSet<Citation> (citMethVec);
				
				testSerKey = this.methodSerMap.get(methodEntry.getKey());
				Vector<Citation> citSerVec = (Vector<Citation>) this.testSerMap.get(testSerKey);
				HashSet<Citation> citSerSet = new HashSet<Citation> (citSerVec);
				for (int i =0; i<citMethVec.size(); i++){
					if (citSerSet.contains(citMethVec.get(i))){
						truePositives.add(citMethVec.get(i));
						trueAuthorPositives.addAll(citMethVec.get(i).getPublication().getAuthors());
						trueTitlePositives.add(citMethVec.get(i).getPublication().getTitle());
						trueYearPositives.add(citMethVec.get(i).getPublication().getYearString());
					}else{
						falsePositives.add(citMethVec.get(i));
						methAuthorSet.addAll(citMethVec.get(i).getPublication().getAuthors());
						methTitleSet.add(citMethVec.get(i).getPublication().getTitle());
						methYearSet.add(citMethVec.get(i).getPublication().getYearString());
					}
				}
				for(int i =0; i<citSerVec.size(); i++){
					if (!citMethSet.contains(citSerVec.get(i))){
						falseNegatives.add(citSerVec.get(i));
						serAuthorSet.addAll(citSerVec.get(i).getPublication().getAuthors());
						serTitleSet.add(citSerVec.get(i).getPublication().getTitle());
						serYearSet.add(citSerVec.get(i).getPublication().getYearString());
					}
				}// find false negatives
				
			} else if (type.equals(HEAD_STEP)){
				Publication pubMeth = (Publication) methodEntry.getValue();
				
				testSerKey = this.methodSerMap.get(methodEntry.getKey());
				Publication pubSer = (Publication) this.testSerMap.get(testSerKey);
				if (pubMeth.equals(pubSer)){
					truePositives.add(pubSer);
					trueAuthorPositives.addAll(pubMeth.getAuthors());
					trueTitlePositives.add(pubMeth.getTitle());
					
					
				}else{
					falseNegatives.add(pubSer);
					falsePositives.add(pubMeth);
					methAuthorSet.addAll(pubMeth.getAuthors());
					methTitleSet.add(pubMeth.getTitle().trim());
					serAuthorSet.addAll(pubSer.getAuthors());
					serTitleSet.add(pubSer.getTitle().trim());
					
				}
				
			}else if (type.equals(PHR_STEPS)){
				HashSet<String> sentencesMeth = (HashSet<String>) methodEntry.getValue();
				testSerKey = this.methodSerMap.get(methodEntry.getKey());
				HashSet<String> sentencesSer = (HashSet<String>)this.testSerMap.get(testSerKey);
				float simValue;
				for (String sentence : sentencesMeth){
					for (String sentenceSer: sentencesSer){
						simValue = this.sim.getSimilarity(sentence, sentenceSer);
						if (simValue>0.8){
							truePositives.add(sentence);
						}else{
							falsePositives.add(sentence);
						}
					}
				}
				
				for (String sentenceSer :sentencesSer ){
					for (String sentenceTest: sentencesMeth){
						simValue = this.sim.getSimilarity(sentenceTest, sentenceSer);
						if (simValue<=0.8){
						
							falseNegatives.add(sentenceSer);
						}
					}
				}
			}
		}// for each testFile
		
		if (type.equals(REF_STEPS)||type.equals(HEAD_STEP)){
			HashMap<String,Float> authorEvalMap = this.compareEntities(methAuthorSet, serAuthorSet,trueAuthorPositives);
			log.info("precision authors: "+ authorEvalMap.get(PRECISION)+System.getProperty("line.separator")
					+"recall authors:"+authorEvalMap.get(RECALL));
			propertyChange.firePropertyChange(AUTHOR_EVAL, null, authorEvalMap);
			HashMap<String,Float> titleEvalMap = this.compareEntities(methTitleSet, serTitleSet, trueTitlePositives);
			propertyChange.firePropertyChange(TITLE_EVAL, null, titleEvalMap);
			log.info("precision title: "+ titleEvalMap.get(PRECISION)+System.getProperty("line.separator")
					+"recall title:"+titleEvalMap.get(RECALL));
		}
		if (type.equals(REF_STEPS)){
			HashMap<String,Float> yearEvalMap = this.compareEntities(methTitleSet, serTitleSet, trueTitlePositives);
			propertyChange.firePropertyChange(YEAR_EVAL, null, yearEvalMap);
			log.info("precision year: "+ yearEvalMap.get(PRECISION)+System.getProperty("line.separator")
					+"recall year:"+yearEvalMap.get(RECALL));
		}
		Map <String,Float> evalMap = new HashMap<String, Float>();
		float precision =(float) truePositives.size()/(float)(truePositives.size()+falsePositives.size());
		float recall = (float) truePositives.size()/(float)(truePositives.size()+falseNegatives.size());
		evalMap.put(PRECISION, precision);
		evalMap.put(RECALL, recall);
		propertyChange.firePropertyChange(OVERALL_EVAL, null, evalMap);
		log.info("precision:"+ precision+System.getProperty("line.separator")+
				"recall:"+recall);
	}
		
	private HashMap<String,Float> compareEntities(Set<Object> methSet,Set <Object> serSet,
			Set<Object> trueAuthors){
		HashMap <String,Float> evalMap = new HashMap<String, Float>();
		HashSet<Object> truePositives = new HashSet<Object>();
		HashSet<Object> falsePositives = new HashSet<Object> ();
		HashSet<Object> falseNegatives = new HashSet<Object>();
		truePositives.addAll(trueAuthors);
		for (Object a : methSet){
			if (serSet.contains(a)){
				truePositives.add(a);
			}else{
				falsePositives.add(a);
			}
		}
		for  (Object a : serSet){
			if (!methSet.contains(a)){
				falseNegatives.add(a);
			}
		}
		float precision =(float) truePositives.size()/(float)(truePositives.size()+falsePositives.size());
		float recall = (float) truePositives.size()/(float)(truePositives.size()+falseNegatives.size());
		evalMap.put(PRECISION, precision);
		evalMap.put(RECALL, recall);
		return evalMap;
	}
	

	
	/**
	 * initialize the map of the serialized test instance and the tested file 
	 * @param testFileSources
	 * @param caseSources
	 */
	public void generateInstanceCaseMap (List<String> testFileSources,
			List<String> testSerSources){
		this.methodSerMap.clear();
		String testFileSource;
		String testSerSource;
		for (int i = 0; i<testFileSources.size();i++){
			String[]tempPath = testFileSources.get(i).split("\\\\");
			testFileSource = tempPath[tempPath.length-1];
			tempPath = testSerSources.get(i).split("\\\\");
			testSerSource = tempPath[tempPath.length-1];
			this.methodSerMap.put(testFileSource, testSerSource);
		}
	}
	
	public void reset(){
		this.testMethodMap.clear();
		this.testSerMap.clear();
		this.methodSerMap.clear();
		this.partMap.clear();
	}
	/**
	 * @param partMap the partMap to set
	 */
	public void setPartMap(HashMap<String,String> partMap) {
		this.partMap = partMap;
	}

	/**
	 * @return the partMap
	 */
	public HashMap<String,String> getPartMap() {
		return partMap;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @param refSource the refSource to set
	 */
	public void setRefSource(List<String> refSource) {
		this.refSource = refSource;
	}

	/**
	 * @return the refSource
	 */
	public List<String> getRefSource() {
		return refSource;
	}
	
}

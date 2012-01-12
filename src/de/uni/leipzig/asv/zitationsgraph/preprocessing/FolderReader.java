package de.uni.leipzig.asv.zitationsgraph.preprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Class to read all files of a folder and preprocess them, aka create BaseDocs. Holds also method to dump the splitted
 * parts into text files and a method to recreate BaseDocs out of dumped files.
 * @author Klaus Lyko
 *
 */
public class FolderReader {
	
	public static String defaultInputFolder = "examples/Lit/2011";
	public static String defaultOutputFolder = "examples/preprocessed";
	private String inputFolder;
	private String outputFolder;
	Logger logger = Logger.getLogger("ZitGraph");
	
	public FolderReader () {
		inputFolder = defaultInputFolder;
		outputFolder = defaultOutputFolder;
	}
	/**
	 * Constructor to specify folders for in and output.
	 * @param inputFolder
	 * @param outputFolder
	 */
	public FolderReader (String inputFolder, String outputFolder) {
		this.inputFolder = inputFolder;
		this.outputFolder = outputFolder;
	}
	
	/**
	 * What to do with a single file of a folder, here initialize preprocessing and create BaseDoc.
	 * @param f File to process.
	 * @return Created BaseDoc.
	 * @throws IOException
	 */
	public BaseDoc processFile(File f) throws IOException {
		BaseDoc doc = new BaseDoc(f.getAbsolutePath());
		logger.info("\n\n Processing file "+f.getName());
		doc.process();
		return doc;
	}
	/**
	 * Process a whole folder. Read files and create BaseDocs.
	 * @return
	 * @throws IOException
	 */ 
	public BaseDoc[] processFolder(String inputFolder) throws IOException {
		// get all files of folder
		this.inputFolder = inputFolder;
		File f = new File(inputFolder);
		File[] fileArray = f.listFiles();
		BaseDoc[] docs = new BaseDoc[fileArray.length];
		// do something with them
		for(int i = 0; i<fileArray.length; i++) {
			docs[i] = processFile(fileArray[i]);
		}
		return docs;
	}
	
	public BaseDoc[] processFolder() throws IOException {
		// get all files of folder
		
		File f = new File(inputFolder);
		File[] fileArray = f.listFiles();
		BaseDoc[] docs = new BaseDoc[fileArray.length];
		// do something with them
		for(int i = 0; i<fileArray.length; i++) {
			docs[i] = processFile(fileArray[i]);
		}
		return docs;
	}
	
	/**
	 * Method to dump processed files into text files. A single BaseDoc is dumped into 3 text files,
	 * named <i>SOURCENAME_PART.text</i>, whereupon PART is either BaseDoc.HEAD, BaseDoc.BODY or BaseDoc.REFERENCES.
	 * @param docs Array of BaseDocs to be dumped to the output folder.
	 * @throws IOException
	 */
	public void saveSplitsToTextFiles(BaseDoc[] docs) throws IOException {
		FileWriter writer;
		File file;
		// create outputFolder if necessary
		file = new File(outputFolder);
		file.mkdirs();
		for(BaseDoc doc : docs) {
			file = new File(doc.getFileName());
			String docName = file.getName();
			logger.info("Writing files for " + docName);
			// write head
			file = new File(this.outputFolder+"/"+docName+"_"+BaseDoc.HEAD+".txt");
			writer = new FileWriter(file, false);
			writer.write(doc.get(BaseDoc.HEAD));
			writer.flush();
			writer.close();
			// write body
			file = new File(this.outputFolder+"/"+docName+"_"+BaseDoc.BODY+".txt");
			writer = new FileWriter(file, false);
			writer.write(doc.get(BaseDoc.BODY));
			writer.flush();
			writer.close();
			// write references
			file = new File(this.outputFolder+"/"+docName+"_"+BaseDoc.REFERENCES+".txt");
			writer = new FileWriter(file, false);
			writer.write(doc.get(BaseDoc.REFERENCES));
			writer.flush();
			writer.close();
		}
	}
	/**
	 * Try to recreate BaseDoc out of dumped text files in a folder.
	 * @param folder where the dumped files a saved.
	 * @return A List of created BaseDocs.
	 */
	public List<BaseDoc> loadDumpedParts(String folder) {
		List<BaseDoc> docList = new LinkedList<BaseDoc>();
		File f = new File(folder);
		HashSet<String> dumpedDocs = new HashSet<String>();
		File[] fileArray = f.listFiles();
		// first get all documents to create.
		for(File file : fileArray) {
			int offSet = Math.max(file.getName().lastIndexOf("_"+BaseDoc.HEAD+".txt"),
					Math.max(file.getName().lastIndexOf("_"+BaseDoc.BODY+".txt"),
							file.getName().lastIndexOf("_"+BaseDoc.REFERENCES+".txt")));
			if (offSet == -1)
				continue;
			else {
				dumpedDocs.add(file.getName().substring(0, offSet));				
			}
		}
		// for all documents read all 3 text files.
		for(String docName : dumpedDocs) {
			BaseDoc bd = new BaseDoc(docName);
			String fileName = folder+"/"+docName+"_"+BaseDoc.HEAD+".txt";
			try {
				// read head
				fileName = folder+"/"+docName+"_"+BaseDoc.HEAD+".txt";
				bd.setField(BaseDoc.HEAD, readFile(fileName));
				// read body
				fileName = folder+"/"+docName+"_"+BaseDoc.BODY+".txt";
				bd.setField(BaseDoc.BODY, readFile(fileName));
				// read tail
				fileName = folder+"/"+docName+"_"+BaseDoc.REFERENCES+".txt";
				bd.setField(BaseDoc.REFERENCES, readFile(fileName));

			} catch (IOException e) {
				logger.warning("Unable to read "+fileName);
				continue;
			}
			// we can assume everything went smoothly
			logger.info("Read doc "+docName);
			docList.add(bd);
		}
		return docList;
	}
	
	/**
	 * Little helper method to read text of a file as a String.
	 * @param name File to read.
	 * @return Content of the file as a String.
	 * @throws IOException
	 */
	private String readFile(String name) throws IOException {
		// read head
		FileReader reader = new FileReader(new File(name));
		BufferedReader br = new BufferedReader(reader);
		String line;
		StringBuffer content = new StringBuffer();		
		int lineNumber = 0;
		do
	    {
	      line = br.readLine();
	      if(lineNumber>0)
	    	  content.append(System.getProperty("line.separator"));//recreate line breaks
	      content.append(line);
	      lineNumber++;
	    }
	    while (line != null);
		return content.toString();
	}
	
	/**
	 * Method demonstrates handling.
	 * @param args
	 */
	public static void main(String args[]) {		
		FolderReader fR = new FolderReader();
		try {
			fR.saveSplitsToTextFiles(fR.processFolder(FolderReader.defaultInputFolder));
			// read dumps
			List<BaseDoc> doc = fR.loadDumpedParts(FolderReader.defaultOutputFolder);
			System.out.println("Successfully read "+doc.size()+" dumped BaseDocs");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

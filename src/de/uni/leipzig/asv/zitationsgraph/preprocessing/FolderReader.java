package de.uni.leipzig.asv.zitationsgraph.preprocessing;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class FolderReader {
	
	public static String folder = "C:/Users/Lyko/Desktop/Textmining datasets/Lit/2011";
	
	Logger logger = Logger.getLogger("ZitGraph");
	
	public BaseDoc processFile(File f) throws IOException {
		BaseDoc doc = new BaseDoc(f.getAbsolutePath());
		logger.info("\n\n Processing file "+f.getName());
		doc.process();
		logger.info("\nHeadPart:\n"+doc.get(BaseDoc.HEAD));
		
		logger.info("\nRefPart:\n"+doc.get(BaseDoc.REFERENCES));
		return doc;
	}
	
	public BaseDoc[] processFolder(String folderPath) throws IOException {
		// get all files of folder
		File f = new File(folder);
		File[] fileArray = f.listFiles();
		BaseDoc[] docs = new BaseDoc[fileArray.length];
		// do something with them
		for(int i = 0; i<fileArray.length; i++) {
			docs[i] = processFile(fileArray[i]);
		}
		return docs;
	}
	
	public static void main(String args[]) {		
		FolderReader fR = new FolderReader();
		try {
			fR.processFolder(folder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

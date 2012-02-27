package de.uni.leipzig.asv.zitationsgraph.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class PosAnalyzer {
	public static HashMap<String, String> dictionary;
	
	
	public static HashMap<String,String> loadDictionary (String file) throws IOException, ClassNotFoundException{
		FileInputStream fis = new FileInputStream (file);
		ObjectInputStream ois = new ObjectInputStream(fis);
		HashMap<String,String>dictionary = (HashMap<String, String>) ois.readObject();
		ois.close();
		return dictionary;
	}
	
	public static HashMap<String,String> loadDictionary() throws IOException, ClassNotFoundException{
		FileInputStream fis = new FileInputStream ("lib/posdic.ser");
		ObjectInputStream ois = new ObjectInputStream(fis);
		HashMap<String,String> dictionary = (HashMap<String, String>) ois.readObject();
		ois.close();
		return dictionary;
	}
	void writeDictionary (String sourceList,String targetFile) throws IOException{
		FileReader reader = new FileReader (sourceList);
		BufferedReader br = new BufferedReader (reader);
		String [] values;
		HashMap <String,String> dic = new HashMap<String,String>();
		while (br.ready()){
			values = br.readLine().split("\\t");
			dic.put(values[0], values[1]);
		}
		FileOutputStream fos = new FileOutputStream(targetFile);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(dic);
		oos.close();
	}
	
	public static void main (String[] args){
		PosAnalyzer pos = new PosAnalyzer ();
		try {
			pos.writeDictionary("lib/part-of-speech.txt", "lib/posdic.ser");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}



package de.uni.leipzig.asv.zitationsgraph.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;

public class NameDictionary {

	
	
	public  NameDictionary() {
	}
	
	
	public static HashSet<String> getNameDic() throws ClassNotFoundException, IOException {
		return getNameDic("lib/nameDic.ser");
	}
	
	public static HashSet<String> getNameDic(String fileName) throws ClassNotFoundException, IOException{
		FileInputStream fis = new FileInputStream (fileName);
		ObjectInputStream ois = new ObjectInputStream(fis);
		HashSet<String> nameDic = (HashSet<String>) ois.readObject();
		return nameDic;
	}
	
	
	void writeDictionary (String sourceList,String targetFile) throws IOException{
		FileReader reader = new FileReader (sourceList);
		BufferedReader br = new BufferedReader (reader);
	
		HashSet<String> dic = new HashSet<String>();
		while (br.ready()){
			
			dic.add(br.readLine());
		}
		FileOutputStream fos = new FileOutputStream(targetFile);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(dic);
		oos.close();
	}
	
	public static void main (String[] args){
		NameDictionary nd = new  NameDictionary ();
		try {
			nd.writeDictionary("lib/male-names", "lib/nameDic.ser");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	
}

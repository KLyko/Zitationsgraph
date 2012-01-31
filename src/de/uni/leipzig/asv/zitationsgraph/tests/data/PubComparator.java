package de.uni.leipzig.asv.zitationsgraph.tests.data;

import java.util.Comparator;
import java.util.logging.Logger;

import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

public class PubComparator implements Comparator<String> {

	private static final Levenshtein sim = new Levenshtein();
	
	final Logger log = Logger.getLogger(PubComparator.class.getName());
	@Override
	public int compare(String o1, String o2) {
		String temp1 = o1.toLowerCase();
		temp1 = temp1.replaceAll("\\s+", "");
		String temp2 = o2.toLowerCase();
		temp2 = temp2.replaceAll("\\s+", "");
		int length = (temp1.length()>temp2.length())?temp2.length():temp1.length();
		temp1 = temp1.substring(0, Math.round(length*0.9f));
		temp2 = temp2.substring(0,Math.round(length*0.9f));
		
		/*if (o1.toLowerCase().equals(o2.toLowerCase())){
			return 0;
		}else {
			*/
			//float similarity =sim.getSimilarity(o1.toLowerCase(), o2.toLowerCase());
			if (temp1.equals(temp2)){
				return 0;
			}
		//}
		return temp1.compareTo(temp2);
	}
}

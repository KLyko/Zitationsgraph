package de.uni.leipzig.asv.zitationsgraph.tests.data;

import java.util.Comparator;
import java.util.logging.Logger;

import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

public class PubComparator implements Comparator<String> {

	private static final Levenshtein sim = new Levenshtein();
	
	final Logger log = Logger.getLogger(PubComparator.class.getName());
	@Override
	public int compare(String o1, String o2) {
		if (o1.toLowerCase().equals(o2.toLowerCase())){
			return 0;
		}else {
			float similarity =sim.getSimilarity(o1.toLowerCase(), o2.toLowerCase());
			if (0.9f<=similarity){
				return 0;
			}
		}
		return o1.toLowerCase().compareTo(o2.toLowerCase());
	}
}

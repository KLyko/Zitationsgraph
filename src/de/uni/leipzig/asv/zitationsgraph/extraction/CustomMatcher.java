package de.uni.leipzig.asv.zitationsgraph.extraction;

import java.util.regex.Pattern;
/**
 * Class to store the pattern and the match count, which represent 
 * how appropriate the pattern to the reference part
 * @author loco
 *
 */
public class CustomMatcher implements Comparable{

	private Pattern pattern;
	
	private float matchCount ;
	
	private float matchWeight;
	
	
	public CustomMatcher (Pattern p){
		this.setPattern(p);
		this.setMatchCount(0);
		this.matchWeight = 1f;
	}


	public CustomMatcher(Pattern p, float f) {
		this.setPattern(p);
		this.matchWeight = f;
	}


	/**
	 * @param pattern the pattern to set
	 */
	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}


	/**
	 * @return the pattern
	 */
	public Pattern getPattern() {
		return pattern;
	}


	/**
	 * @param matchCount the matchCount to set
	 */
	public void setMatchCount(float matchCount) {
		this.matchCount = matchCount*matchWeight;
	}


	/**
	 * @return the matchCount
	 */
	public float getMatchCount() {
		return matchCount;
	}


	@Override
	public int compareTo(Object o) {
		CustomMatcher o2 = (CustomMatcher) o;
		CustomMatcher o1 = this;
		if (o1.getMatchCount()>o2.getMatchCount()){
			return 1;
		}else if (o1.getMatchCount()<o2.getMatchCount()){
			return -1;
		}else
			return 0;
		
	}
	
}

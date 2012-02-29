package de.uni.leipzig.asv.zitationsgraph.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Class to store the pattern and the match count, which represent 
 * how appropriate the pattern to the reference part.<br>
 * The match position and the match lenght is necessary to sort the matches
 * of different CustomPattern instances
 * @author loco
 *
 */
public class CustomPattern implements Comparable{

	/**
	 * used pattern
	 */
	private Pattern pattern;
	
	/**
	 * hits for this pattern in the current reference part
	 */
	private float matchCount ;
	
	/**
	 * a weight value to punish general pattern and to reward specific
	 * patterns
	 */
	private float matchWeight;
	
	/**
	 * the current start position by a match of this pattern
	 */
	private int matchPosition;
	
	/**
	 * the length of the match
	 */
	private int matchLength;
	
	/**
	 * matched String
	 */
	private String matchValue;
	
	
	/**
	 * generatedMatcher of this pattern
	 */
	private Matcher generatedMatcher;
	
	
	public CustomPattern (Pattern p){
		this.setPattern(p);
		this.setMatchCount(0);
		this.matchWeight = 1f;
	}


	public CustomPattern(Pattern p, float f) {
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

/**
 * override compareTo to sort a list of CustomPattern by their hits to find a 
 * appropriate one
 */
	@Override
	public int compareTo(Object o) {
		CustomPattern o2 = (CustomPattern) o;
		CustomPattern o1 = this;
		if (o1.getMatchCount()>o2.getMatchCount()){
			return 1;
		}else if (o1.getMatchCount()<o2.getMatchCount()){
			return -1;
		}else
			return 0;
		
	}


	/**
	 * @param matchPosition the matchPosition to set
	 */
	public void setMatchPosition(int matchPosition) {
		this.matchPosition = matchPosition;
	}


	/**
	 * @return the matchPosition
	 */
	public int getMatchPosition() {
		return matchPosition;
	}


	/**
	 * @param matchLength the matchLength to set
	 */
	public void setMatchLength(int matchLength) {
		this.matchLength = matchLength;
	}


	/**
	 * @return the matchLength
	 */
	public int getMatchLength() {
		return matchLength;
	}


	/**
	 * @param matchValue the matchValue to set
	 */
	public void setMatchValue(String matchValue) {
		this.matchValue = matchValue;
	}


	/**
	 * @return the matchValue
	 */
	public String getMatchValue() {
		return matchValue;
	}


	/**
	 * @param generatedMatcher the generatedMatcher to set
	 */
	public void setGeneratedMatcher(Matcher generatedMatcher) {
		this.generatedMatcher = generatedMatcher;
	}


	/**
	 * @return the generatedMatcher
	 */
	public Matcher getGeneratedMatcher() {
		return generatedMatcher;
	}
	
}

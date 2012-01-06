package de.uni.leipzig.asv.zitationsgraph.lucenesearch;


/**
 * data class for match results of a search
 * This class store the matched value and the start position in the original text 
 * to get the neighborhood around the matching term.
 * @author loco
 *
 */
public class MatchResult implements Comparable{

	private String value;
	private int startPosition;
	private float score;
	
	public MatchResult(String value, int startPosition, float score) {
		super();
		this.value = value;
		this.startPosition = startPosition;
	}
	
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @param startPosition the startPosition to set
	 */
	public void setStartPosition(int startPosition) {
		this.startPosition = startPosition;
	}
	/**
	 * @return the startPosition
	 */
	public int getStartPosition() {
		return startPosition;
	}
	/**
	 * @param score the score to set
	 */
	public void setScore(float score) {
		this.score = score;
	}
	
	/**
	 * @return the score
	 */
	public float getScore() {
		return score;
	}

	@Override
	public int compareTo(Object o) {
		MatchResult o1 =this;
		MatchResult o2 = (MatchResult) o;
		if (o1.getScore()>o2.getScore()){
			return 1;
		}else if (o1.getScore()<o2.getScore()){
			return -1;
		}else
		return 0;
	}
	
	@Override
	public String toString(){
		return "match "+value+" at "+startPosition;
	}
	
	
}

package de.uni.leipzig.asv.zitationsgraph.extraction;

  class Token {
	
	public static final String NAME = "name";
	public static final String TITLE = "title";
	
	
	
	private int position;
	private boolean isLineBegin;
	private boolean isNeighborName;
	
	String type;
	private String value;
	/**
	 * @param position the position to set
	 */
	
	protected Token (String value,String type){
		this.value = value;
		this.type = type;
		this.isLineBegin = false;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	/**
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
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
	 * @param isLineBegin the isLineBegin to set
	 */
	public void setLineBegin(boolean isLineBegin) {
		this.isLineBegin = isLineBegin;
	}
	/**
	 * @return the isLineBegin
	 */
	public boolean isLineBegin() {
		return isLineBegin;
	}
	@Override 
	public String toString (){
		return this.value;
	}
	/**
	 * @param isNeighborName the isNeighborName to set
	 */
	public void setNeighborName(boolean isNeighborName) {
		this.isNeighborName = isNeighborName;
	}
	/**
	 * @return the isNeighborName
	 */
	public boolean isNeighborName() {
		return isNeighborName;
	}

}

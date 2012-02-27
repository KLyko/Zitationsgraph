package de.uni.leipzig.asv.zitationsgraph.extraction.templates;


import java.util.List;
import java.util.regex.Pattern;

import de.uni.leipzig.asv.zitationsgraph.extraction.CustomPattern;

/**
 * basic class for the template building
 * @author loco
 *
 */

public class TemplateEntity {

	/**
	 * default separation regex
	 */
	public static final String DEFAULT_SEP = "(\\s{0,2},\\s{0,2}|\\s{0,2};\\s{0,2}|\\{0,2}|\\.|,?;?\\s{0,2}and\\s{0,2})";

	/**
	 * pattern which represent the template
	 */
	Pattern template;
	
	
	
	public TemplateEntity (){
		
	}
	
	public TemplateEntity ( Pattern template){
		
		this.template = template;
	}
	

	public Pattern getTemplate() {
		return template;
	}

	public void setTemplate(Pattern template) {
		this.template = template;
	}
	/**
	 * add a suffix to the pattern
	 * @param suffixReg
	 */
	public void addSuffix (String suffixReg){
		this.template = Pattern.compile(template+suffixReg);
	}
	
	/**
	 * add a prefix to the pattern
	 * @param prefix
	 */
	public void addPrefix (String prefix){
		this.template = Pattern.compile(prefix+this.template);
	}
	
	/**
	 * concatenate this pattern with themselves
	 * subclasses can override this method
	 * Default do nothing
	 * @param min  minimal repetition
	 * @param max maximum repetition
	 * @param prefixReg  prefix before the concatenation start
	 * @param regexSep  separation regex between the repetition
	 * @param suffixEnd suffix
	 * @return
	 */
	public  Pattern concatTemplate(int min, int max,String prefixReg, String regexSep, String suffixEnd) {
	 return template;
	}
	
	
	/**
	 * subclasses can ovverride this
	 * @param patterns
	 * @return
	 */
	public Pattern generateMultiTemplate(Pattern[] patterns) {
		return template;
		//default nothing
	}
}

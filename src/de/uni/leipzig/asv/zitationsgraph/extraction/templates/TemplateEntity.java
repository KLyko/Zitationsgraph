package de.uni.leipzig.asv.zitationsgraph.extraction.templates;


import java.util.List;
import java.util.regex.Pattern;

import de.uni.leipzig.asv.zitationsgraph.extraction.CustomPattern;

public class TemplateEntity {

	public static final String DEFAULT_SEP = "(\\s{0,2},\\s{0,2}|\\s{0,2};\\s{0,2}|\\{0,2}|\\.|,?;?\\s{0,2}and\\s{0,2})";

	Pattern template;
	boolean isBasic;
	
	
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
	public void addSuffix (String suffixReg){
		this.template = Pattern.compile(template+suffixReg);
	}
	
	public void addPrefix (String prefix){
		this.template = Pattern.compile(prefix+this.template);
	}
	
	public  Pattern concatTemplate(int min, int max,String prefixReg, String regexSep, String suffixEnd) {
	 return template;
	}
	
	public Pattern generateMultiTemplate(Pattern[] patterns) {
		return template;
		//default nothing
	}
}

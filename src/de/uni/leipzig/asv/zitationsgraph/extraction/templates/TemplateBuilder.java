package de.uni.leipzig.asv.zitationsgraph.extraction.templates;

import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * This class can combine different Templates and organize them in a Map
 * @author loco
 *
 */
public class TemplateBuilder {

	
	private HashMap<String,TemplateEntity> templateMap;
	
	public TemplateBuilder()
	{
		templateMap = new HashMap<String,TemplateEntity>();
	}

	/**
	 * @param templateMap the templateMap to set
	 */
	public void addTemplate(String name ,TemplateEntity te) {
		this.templateMap.put(name, te);
	}

	/**
	 * @return the templateMap
	 */
	public TemplateEntity getTemplate(String name) {
		return templateMap.get(name);
	}
	
	public void resetBuilder(){
		this.templateMap.clear();
	}
	
	public void mergeTemplates (int tolerance,boolean lineBegin,boolean lineEnd,String regexSep,
			String name,boolean optional, String name2,boolean optional2,String targetName){
		TemplateEntity te1 = this.templateMap.get(name);
		TemplateEntity te2 = this.templateMap.get(name2);
		StringBuffer sb = new StringBuffer();
			
				sb.append(((lineBegin)?"^":"")+te1.getTemplate()+
						((optional&&!lineBegin)?"?":"")+regexSep+".{0,"+tolerance+"}?"
						+te2.getTemplate()+((lineEnd)?".{0,3}":"")+((optional2)?"?":""+((lineEnd)?"$":"")));
		Pattern p = Pattern.compile(sb.toString(),Pattern.MULTILINE|Pattern.DOTALL);
		this.templateMap.put(targetName, new TemplateEntity(p));
	}
	
	public void mergeTemplates (int tolerance,boolean lineBegin,boolean lineEnd,String regexSep,
			TemplateEntity te1,boolean optional, TemplateEntity te2,boolean optional2,String targetName){
		
		StringBuffer sb = new StringBuffer();
			
				sb.append(((lineBegin)?"^":"")+te1.getTemplate()+
						((optional)?"?":"")+regexSep+".{0,"+tolerance+"}?"
						+te2.getTemplate()+((lineEnd)?".{0,3}":"")+((optional2)?"?":""+((lineEnd)?"$":"")));
		Pattern p = Pattern.compile(sb.toString(),Pattern.MULTILINE|Pattern.DOTALL);
		this.templateMap.put(targetName, new TemplateEntity(p));
	}
}

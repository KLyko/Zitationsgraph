package de.uni.leipzig.asv.zitationsgraph.extraction.templates;



import java.util.List;
import java.util.regex.Pattern;

import de.uni.leipzig.asv.zitationsgraph.extraction.CustomPattern;

/**
 * subclass of {@linkplain TemplateEntity} to find authorparts and authors
 * This class is able to generate multivariants for a template
 * and can concat itself for precise definition of the author part in a reference 
 * @author loco
 *
 */
public class AuthorTemplateEntity extends TemplateEntity{

	

/**
 * Default suffix regex for the author part
 */
public static final String DEFAULT_SUFFIX = "(\\s?,?\\s?\\set\\.?\\s?al\\.?)?";
	
	public AuthorTemplateEntity ( Pattern template){
		super (template);
	}
	
	
	
	
	

	public AuthorTemplateEntity() {
		super();
	}

	
	/**
	 * generate a pattern which include different variants of a set of patterns
	 * In this case this methode will use to generate a pattern, which find the author part
	 * of a reference
	 */
	public Pattern generateMultiTemplate(Pattern[] patterns) {
		
		StringBuffer sb = new StringBuffer ();;
		sb.append("(");
		
		for (int i = 0;i<patterns.length;i++){
		
			sb.append("("+patterns[i].pattern()+")"); 
			if (i !=patterns.length-1){
				sb.append("|");
			}
		}
		sb.append(")");
		this.template = Pattern.compile(sb.toString());
		return template;
	}
	


	/**
	 * override the method of the superclass
	 */
	@Override
	public Pattern concatTemplate(int min, int max ,String prefixReg,String regexSep,String suffixEnd) {
		
		int minimum = (min!=0)?(min-1):0;
		int maximum = (max-1);
		if (template!= null){
			if (maximum>= minimum){
				template = Pattern.compile(
						prefixReg+"("+regexSep+"("+template.pattern()+regexSep+")" +
								"{"+minimum+","+maximum+"}"+template.pattern()+"){0,1}"+suffixEnd
								);
				return template;
			}
			
				
		}
		return null;
	}
	
	/**
	 * use default separator regex and default suffix regex
	 * @param min
	 * @param max
	 * @return
	 */
	public Pattern concatTemplate(int min, int max ) {
		
		return this.concatTemplate(min, max,"",DEFAULT_SEP,DEFAULT_SUFFIX);
	}
	
}

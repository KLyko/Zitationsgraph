package de.uni.leipzig.asv.zitationsgraph.extraction.templates;

import java.util.regex.Pattern;

public interface BasicTemplates {
	
	static final Pattern surForenameShortPattern = Pattern.
	compile("((Mc|van den?|Van den?|de|De|Ó)\\s{0,2}?)?"+ //prefix
			"([A-Z][a-z|ä|ü|ö|’|é|á|ó|ñ]{1,30}\\s{0,2}?){1,5}"+ //surname
			"(\\s{0,2}?-[A-Z]?[a-z|ä|ü|ö|é|á|ó|ñ]{2,30}){0,2}\\s{0,2}?" + //optional with bindestrich
			",(\\s{0,2}?[A-Z](\\.|[^\\w])-?){1,3}"+//forename is separated with comma and the name is with punct
			"(\\s{0,2}?(de|la)){0,2}(\\s{0,2}?di\\s[A-Z]\\w{3,10})?"); //spanish names
	
	static final Pattern forenameShortSurNamePattern = Pattern.
	compile("([A-Z]\\.[\\s{0,2}?|-]){1,3}\\s{0,2}?" +// forename
			"((Mc|van den?|de|De|Van den?|Ó)\\s{0,2}?)?" + //prefix optional
			"([A-Z][a-z|ä|ü|ö|’|é|á|ó|ñ]{1,30}\\s{0,2}?){1,5}" + // surname
			"(\\s{0,2}?-[A-Z]?[a-z|ä|ü|ö|’|é|á|ó|ñ]{2,30}){0,2}");//with bindestrich

	static final Pattern surForenameCompletePattern = Pattern.
	compile("((Mc|van den?|Van den?|de|De|Ó)\\s{0,2}?)?" +//prefix optional
			"([A-Z][a-z|ä|ü|ö|’|é|á|ó|ñ]{1,30}\\s{0,2}?)" +//surname
			"(\\s{0,2}?-[A-Z]?[a-z|ä|ü|ö|’|é|á|ó|ñ]{1,30}){0,2}\\s{0,2}?" +//with Bindestrich optional
			",(\\s{0,2}?[A-Z][a-z|ä|ü|ö|’|é|á|ó|ñ]{1,30}){1,2}(\\s{0,2}?[A-Z]\\.)?"); //forname complete

	static final Pattern allCompletePattern = Pattern.
	compile("([A-Z][\\w]{1,30}\\s{0,2}?){1,2}"+ //firstname complete
			"(\\s{0,2}?-[A-Z]?[a-z|ä|ü|ö|’|é|á|ó|ñ]{1,30}){0,2}"+ //for names like Anne-Marie
			"(\\s{0,2}?(Mc|van den?|Van den?|de|De|Ó)\\s{0,2}?)?"+ // prefix
			"(\\s{0,2}?[A-Z][a-z|’|ä|ü|ö|é|á|ó|ñ]{1,30}\\s{0,2}?){1,5}"+ // surname
			"(\\s{0,2}?-[A-Z]?[a-z|ä|ü|ö|’|é|á|ó|ñ]{1,30}){0,2}" // surname with Bindestrich
			);
	
	public static final Pattern squareBracketPattern = Pattern.compile("(\\s{0,2}?\\[.*\\])");
	public static final Pattern roundBracketPattern = Pattern.compile("^(\\s?\\(?.{1,5}\\))");
	public static final Pattern numericalPattern = Pattern.compile("^(\\s?[1-9][0-9]{0,1}[\\.])");
	
	public static final Pattern titlePattern = Pattern.compile("(“|”|\")?[A-Z](\\w|[\\W{Punct}&&[^\\.]]){3,300}?(\\.|\\?)(”|\")?");
	
	public static final Pattern BIOIStylePattern = Pattern.compile(
			"((\\(([1-2][0-9]{3}[a-e]?|n\\.d\\.)\\)))");
	
	public static final Pattern JCBStylePattern = Pattern.compile("([1-2][0-9]{3}\\.)");
	public static final Pattern MISQStylePattern = Pattern.compile("((“|”|\")([A-Z]|\\W)(."+
			"){5,600}?(”|\"))");


	
	public static final Pattern YearPattern = Pattern.compile("[1-2][0-9]{3}[abcde]?");
	
	public static final Pattern PubLocation = Pattern.compile("(" +
			"In?\\w{0,50}Proc\\w{0,50}|.{0,50}Magazine.{0,50}|.{0,50}Press.{0,50}|" +
			")");
	
	public static final Pattern page = Pattern.compile("([1-9][0-9]{0,3}(\\s?-\\s?[1-9][0-9]{0,3})?)");
}

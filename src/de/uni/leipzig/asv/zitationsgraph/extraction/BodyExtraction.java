package src.de.uni.leipzig.asv.zitationsgraph.extraction;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni.leipzig.asv.zitationsgraph.data.Citation;

public class BodyExtraction
{
	public static void extractQuotes(String text, Collection<Citation> references)
	{
		for (Citation reference: references)
		{
			BodyExtraction.extractQuotes(text, reference);		
		}
	}
	
	public static void extractQuotes(String text, Citation reference)
	{
		Matcher mat = BodyExtraction.createPattern(reference).matcher(text);
		while (mat.find())
		{
			System.out.println(mat.group(1));
		}
	}
	
	private static Pattern createPattern(Citation reference)
	{
		String s = null;
		if (reference.getTag() != null)
		{
			String tag = reference.getTag();
			s = 
				"[.][”]?\\s*" +
				"(" +
				"\\p{Upper}(?:(?:[.][.][.])|[^.])*" +
				"[.][”]?"  +
				")" +
				tag;
		}
		else
		{
			String author = "Dué";
			String year = "2001";
			s = 
				"[.]\\s*" +
				"(" +
				"[^.]*" +
				")" +
				"[(]" + author + " " + year + "[)]";
		}
		return Pattern.compile(s);
	}
	
	public static void main(String[] args) {
		Citation reference = new Citation(null);
		//reference.setTag("8");
		BodyExtraction.extractQuotes(args[0], reference);
	}
}

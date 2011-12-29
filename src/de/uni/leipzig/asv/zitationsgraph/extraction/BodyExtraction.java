package src.de.uni.leipzig.asv.zitationsgraph.extraction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni.leipzig.asv.zitationsgraph.data.Citation;

/**
 * Class used to extract text phrases from a text corpus.
 * 
 * @author Sergej Sintschilin
 *
 */
public class BodyExtraction {
	
	/**
	 * Sentences of the current text.
	 */
	private List<String> sentences;

	/**
	 * Constructs a new class.
	 */
	public BodyExtraction() {
		this.sentences = new ArrayList<String>();
	}

	/**
	 * Sets the current text.
	 * 
	 * @param text - the string in which will be searched for the text phrases.
	 */
	public void setText(String text) {
		if (text == null)
			throw new NullPointerException();
		text = text.replaceAll("‘", "'").replaceAll("’", "'").replaceAll("“", "\"").replaceAll("”", "\"");
		this.splitTextIntoSentences(text);
	}

	/**
	 * Splits a text into a list of sentences.
	 * 
	 * @param text - the
	 */
	private void splitTextIntoSentences(String text) {
		Pattern pat = Pattern.compile("(((?<=([.?!]|([.]\")))\\s+)|(\\s*\\r\\n?\\s*))(?=\\p{Upper}|\\d)");
		Matcher mat = pat.matcher(text);
		while (mat.find()) {
			StringBuffer sb = new StringBuffer();
			mat.appendReplacement(sb, "");
			this.sentences.add(sb.toString());
			//TEST BEGIN
			System.out.println(sb.toString());
			System.out.println("=========================================================");
			//TEST END
		}
	}

	/**
	 * Finds text phrases in the current text for each reference.
	 * 
	 * @param references - a vector of references, that are used to find text phrases.
	 */
	public void extractQuotes(Collection<Citation> references) {
		if (this.sentences.isEmpty())
			return;
		for (Citation reference : references) {
			this.extractQuotes(reference);
		}
	}

	/**
	 * Finds text phrases in the current text for the reference.
	 * 
	 * @param reference - used to find text phrases.
	 */
	public void extractQuotes(Citation reference) {
		if (this.sentences.isEmpty())
			return;
		Vector<String> quotes = new Vector<String>();
		Pattern pat = this.createPattern(reference);
		if (pat == null)
			return;
		for (String sentence: this.sentences) {
			Matcher mat = pat.matcher(sentence);
			if (mat.find()) {
				quotes.add(sentence);
				//TEST BEGIN
				System.out.println(sentence);
				System.out.println("=========================================================");
				//TEST END
			}
		}
		reference.setTextphrases(quotes);
	}

	/**
	 * Creates a pattern to find out if a sentence is a searched text phrase.
	 * 
	 * @param reference for lookup.
	 * @return pattern.
	 */
	private Pattern createPattern(Citation reference) {
		String regex = null;
		String tag = reference.getTag();
		if (tag != null) {
			regex = tag + "$";
		} else {
			String author = extractAuthorLastName(reference.getPublication().getAuthors().firstElement());
			String year = reference.getPublication().getYearString();
			if ((author != null) && (year != null)) {
				regex = "[(][^()]*" + author + "\\s+" + year + "[^()]*[)]";
			}
		}
		if (regex != null) {
			return Pattern.compile(regex, Pattern.MULTILINE);
		} else {
			return null;
		}
	}
	
	/**
	 * 
	 * @param authorName
	 * @return
	 */
	private String extractAuthorLastName(String authorName)
	{
		Pattern pat = Pattern.compile("((?:^[^,\\s]+(?=,))|(?:[^,\\s]+$))");
		Matcher mat = pat.matcher(authorName);
		if (mat.find()) {
			return mat.group(1);
		} else {
			return null;
		}
	}

	public static void main(String[] args) {
		Citation reference = new Citation(null);
		// reference.setTag("8");
		BodyExtraction be = new BodyExtraction();
		be.setText(args[0]);
		System.out.println();
		System.out.println();
		System.out.println();
		be.extractQuotes(reference);
	}
}

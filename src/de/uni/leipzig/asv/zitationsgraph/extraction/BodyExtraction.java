package src.de.uni.leipzig.asv.zitationsgraph.extraction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni.leipzig.asv.zitationsgraph.data.Citation;

/**
 * Class used to extract text phrases from a text corpus.
 * 
 * @author Sergej Sintschilin
 */
public class BodyExtraction
{

	/**
	 * Sentences of the current text.
	 */
	private List<String> sentences;

	/**
	 * Constructs a new class.
	 */
	public BodyExtraction()
	{
		this.sentences = new ArrayList<String>();
	}

	/**
	 * Sets the current text.
	 * 
	 * @param text
	 *            - the string in which will be searched for the text phrases.
	 */
	public void setText(String text)
	{
		if (text == null)
			throw new NullPointerException();
		this.sentences.clear();
		text = text.replaceAll("[‘’]", "'").replaceAll("“”", "\"");
		this.splitTextIntoSentences(text);
	}

	/**
	 * Splits a text into a list of sentences.
	 * 
	 * @param text
	 *            - the
	 */
	private void splitTextIntoSentences(String text)
	{
		Pattern pat = Pattern.compile("(((?<=([.?!]|([.]\")))\\s+)|(\\s*\\r\\n?\\s*))(?=\\p{Upper}|\\d)");
		Matcher mat = pat.matcher(text);
		while (mat.find())
		{
			StringBuffer sb = new StringBuffer();
			mat.appendReplacement(sb, "");
			this.sentences.add(sb.toString());
			// TEST BEGIN
			System.out.println(sb.toString());
			System.out.println("=========================================================");
			// TEST END
		}
	}

	/**
	 * Finds text phrases in the current text for each reference.
	 * 
	 * @param references
	 *            - a vector of references, that are used to find text phrases.
	 */
	public void extractQuotes(Collection<Citation> references)
	{
		if (this.sentences.isEmpty())
			return;
		for (Citation reference : references)
		{
			this.extractQuotes(reference);
		}
	}

	/**
	 * Finds text phrases in the current text for the reference.
	 * 
	 * @param reference
	 *            - used to find text phrases.
	 */
	public void extractQuotes(Citation reference)
	{
		if (this.sentences.isEmpty())
			return;
		Vector<String> quotes = new Vector<String>();
		if (reference.getTag() != null)
		{
			try
			{
				int tag = Integer.parseInt(reference.getTag());
				quotes.addAll(this.extractQuotesByTag(tag));
			}
			catch (NumberFormatException e)
			{}
		}
		else
		{
			String author = extractAuthorLastName(reference.getPublication().getAuthors().firstElement());//!!!!!!!!!!!!!!!!!!!
			String year = reference.getPublication().getYearString();
			quotes.addAll(this.extractQuotesByAuthorAndYear(author, year));
		}
		reference.setTextphrases(quotes);
	}

	private List<String> extractQuotesByTag(int tag)
	{
		List<String> quotes = new ArrayList<String>();
		Pattern pat = Pattern.compile("");
		for (String sentence : this.sentences)
		{
			Matcher mat = pat.matcher(sentence);
			if (mat.find())
			{
				// TEST BEGIN
				System.out.println(mat.group());
				// TEST END
				quotes.add(sentence);
			}
		}
		return quotes;
	}

	private List<String> extractQuotesByAuthorAndYear(String author, String year)
	{
		List<String> quotes = new ArrayList<String>();
		Pattern pat = Pattern.compile(author + ".+" + year);
		for (String sentence : this.sentences)
		{
			Matcher mat = pat.matcher(sentence);
			if (mat.find())
			{
				// TEST BEGIN
				System.out.println(mat.group());
				// TEST END
				quotes.add(sentence);
			}
		}
		return quotes;
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
		if (mat.find())
		{
			return mat.group(1);
		}
		else
		{
			return null;
		}
	}

	public static void main(String[] args)
	{
		BodyExtraction be = new BodyExtraction();

		be.sentences.add("The system displays translation candidates from the dictionaries incorporated into the system [Sanseido 2004; Eijiro 2006].");

		be.sentences.add("Prior studies using text mining for analyzing variation in language use among different classes of authors have succeeded in identifying meaningful linguistic features distinguishing author gender, age, and personality type (e.g. Argamon et al. 2003, Koppel et al. 2002).");
		be.sentences.add("The experimental protocol which we have been developing for this purpose, as applied by, e.g., Argamon et al. (2003), addresses both goals using techniques from machine learning, supplemented by more traditional computer-assisted text analysis.");
		be.sentences.add("Another approach is to use a function that measures the 'distinguishability' of a feature without regard to other features, such as information gain or binormal separation (Forman et al. 2003).");
		be.sentences.add("We conducted preliminary tests using the SVM-Light system (Joachims 1999) with PGPDT (Zanghirati 2004, Zanni 2006) to build the models.");
		be.sentences.add("While the accuracy of identification of gender in significant in all the cases we example, we expect that using some sort of feature set selection, as in, for example, Hota, Argamon, and Chung (2006), will improve the precision of the identification.");

		be.sentences.add("It is well documented that men and women use informal language, such as conversation and correspondence, in rather different ways, reflecting a wide variety of cultural forces and practices (Tannen 1990, Eckert & McConnell-Ginet 2003).");
		be.sentences.add("Koppel, Argamon, et al (2002) have shown that gender of author can be accurately predicted between 70 and 80 percent of cases of published samples from the British National Corpus, using machine learning and text mining techniques.");
		be.sentences.add("Using simple statistical and collocation techniques, Olsen (2005) has argued that there are distinct gender differences in literary French from the 17th to the early 20th centuries.");
		be.sentences.add("He (Olsen: 2004) further proposed that the differences between male and female writing during this time does not appear to support a \"two cultures\" model (Liberman 2004), but that it was a more conscious political activity of domain specific writing in which men and women deployed the same language.");
		be.sentences.add("We are currently working with the Weka (Witten & Frank 2005) implementation, using a linear kernel and the default parameters.");
		be.sentences.add("Once classified by the SVM method, we apply the information gain and other metrics (Forman et al. 2003) to identify those features that are most relevant to the classification task.");
		be.sentences.add("");
		be.sentences.add("");
		be.sentences.add("");
		be.sentences.add("");
		be.sentences.add("");

		System.out.println(be.extractQuotesByAuthorAndYear("Sanseido", "2004"));
		System.out.println(be.extractQuotesByAuthorAndYear("Eijiro", "2006"));

		System.out.println(be.extractQuotesByAuthorAndYear("Argamon", "2003"));
		System.out.println(be.extractQuotesByAuthorAndYear("Koppel", "2002"));
		System.out.println(be.extractQuotesByAuthorAndYear("Forman", "2003"));
		System.out.println(be.extractQuotesByAuthorAndYear("Joachims", "1999"));
		System.out.println(be.extractQuotesByAuthorAndYear("Zanghirati", "2004"));
		System.out.println(be.extractQuotesByAuthorAndYear("Zanni", "2006"));
		System.out.println(be.extractQuotesByAuthorAndYear("Hota", "2006"));
		// System.out.println(be.extractQuotesByAuthorAndYear("", ""));
	}
}

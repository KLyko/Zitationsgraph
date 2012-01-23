package de.uni.leipzig.asv.zitationsgraph.extraction;

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
	 * Constructs a new body extraction object with no text set.
	 */
	public BodyExtraction()
	{
		this.sentences = new ArrayList<String>();
	}

	/**
	 * Sets the current text. In this text will be looked for citations.
	 * 
	 * @param text
	 *            the string in which will be searched for the text phrases.
	 */
	public void setText(String text)
	{
		if (text == null)
			throw new NullPointerException();
		this.sentences.clear();
		text = text.replaceAll("[‘’]", "'").replaceAll("“”", "\"").replaceAll("\\s*(?:(?:\\r)|(?:\\n)|(?:\\r\\n))", " ");
		this.splitTextIntoSentences(text);
	}

	/**
	 * Splits a text into a list of sentences.
	 * 
	 * @param text
	 *            the text which will be split.
	 */
	private void splitTextIntoSentences(String text)
	{
		Pattern pat = Pattern.compile("(((?<=([.?!]|([.]\")))\\s+)|(\\s*(?:(?:\\r)|(?:\\n)|(?:\\r\\n))\\s*))(?=\\p{Upper}|\\d)");
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
	 *            vector of references that are used to find text phrases.
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
	 *            reference that is used to find text phrases.
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
			String author = extractAuthorLastName(reference.getPublication().getAuthors().firstElement().getName());
			String year = reference.getPublication().getYearString();
			quotes.addAll(this.extractQuotesByAuthorAndYear(author, year));
		}
		reference.setTextphrases(quotes);
	}

	/**
	 * Finds quotes by tag.
	 * 
	 * @param tag
	 *            number used to find quotes.
	 * @return found quotes.
	 */
	private List<String> extractQuotesByTag(int tag)
	{
		List<String> quotes = new ArrayList<String>();
		String regA = "\\d+";
		String regB = "(" + regA + ")\\-(" + regA + ")";
		String regC = "(" + regB + ")|(" + regA + ")";
		String regD = "(?:" + regC + ")(?:\\,(?:" + regC + "))*";
		String regE = "(?:[^\\s]\\s*\\((" + regD + ")\\))";
		String regF = "(?:[^\\s]\\s*\\[(" + regD + ")\\])";
		String regG = "(?:[^\\s\\(\\[](" + regA + "))";
		String regH = regE + "|" + regF + "|" + regG;
		Pattern pat1 = Pattern.compile(regH);
		Pattern pat2 = Pattern.compile(regC);
		for (String sentence : this.sentences)
		{
			boolean found = false;
			Matcher mat1 = pat1.matcher(sentence);
			while (mat1.find())
			{
				// TEST BEGIN
				for (int i = 0; i <= mat1.groupCount(); i++)
					System.out.println(i + " === " + mat1.group(i));
				// TEST END
				Matcher mat2 = pat2.matcher((mat1.group(1) != null) ? mat1.group(1) : ((mat1.group(10) != null) ? mat1.group(10) : mat1.group(19)));
				// ===========================================
				while (mat2.find())
				{
					if (mat2.group(1) != null)
					{
						try
						{
							int n1 = Integer.parseInt(mat2.group(2));
							int n2 = Integer.parseInt(mat2.group(3));
							if ((tag >= n1) && (tag <= n2))
							{
								found = true;
								break;
							}
						}
						catch (NumberFormatException e)
						{}
					}
					if (mat2.group(4) != null)
					{
						try
						{
							int n = Integer.parseInt(mat2.group(4));
							if (tag == n)
							{
								found = true;
								break;
							}
						}
						catch (NumberFormatException e)
						{}
					}
				}
				if (found)
				{
					quotes.add(sentence);
					break;
				}
				// ========================================
			}
		}
		return quotes;
	}

	/**
	 * Finds quotes by author surname and year of publication.
	 * 
	 * @param author
	 *            surname of the author.
	 * @param year
	 *            year of the publication.
	 * @return found quotes
	 */
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
	 * Finds the surname from full name.
	 * 
	 * @param authorName
	 *            full name of the author.
	 * @return surname of the author.
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
		be.sentences.add("(22) Hfjd shdfs jdfsj sfdfgd fgsdf. ");
		be.sentences.add("Hfjd shdfs jdfsj22 sfdfgd fgsdf. ");
		be.sentences.add("Hfjd shdfs jdfsj (1,21-33) sfdfgd fgsdf. ");
		be.sentences.add("Hfjd shdfs jdfsj [3,6,16-22] sfdfgd fgsdf. ");
		System.out.println(be.extractQuotesByTag(22));
	}
}

package de.uni.leipzig.asv.zitationsgraph.lucenesearch;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.regex.RegexQuery;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import de.uni.leipzig.asv.zitationsgraph.preprocessing.BaseDoc;
/**
 * Wrapper class for the search action on a text. 
 * At first the directory of the text must created with the {@code createIndex} method.
 * If the directory was created, it's possible to search for regex or key words.
 * The search request should not include uppercase letters, cause the standard analyzer
 * make all to lowercase 
 * 
 * @author loco
 *
 */
public class LuceneSearcher {

private RAMDirectory directory;
private StandardAnalyzer analyzer;
private IndexWriterConfig config;
private IndexWriter indexWriter;
private Document doc;
private String plainText;
private List <MatchResult> searchHits;

public LuceneSearcher() throws CorruptIndexException, LockObtainFailedException, IOException{
	directory = new RAMDirectory();
	analyzer = new StandardAnalyzer(Version.LUCENE_34);
	config = new IndexWriterConfig (Version.LUCENE_34, analyzer);
	indexWriter = new IndexWriter(directory, config);
}


/**
 * This create an index of the specific text with the standard analyzer, that means
 * a set of stopwords will ignore and the 
 * @param fieldName fieldName
 * @param text search space
 * @throws CorruptIndexException
 * @throws IOException
 */
private void createIndex(String fieldName,String text) throws CorruptIndexException, IOException {
		indexWriter.deleteAll();
		doc = new Document();
		plainText = text;
		Field body =new Field (fieldName, text, Store.YES	, Index.ANALYZED,TermVector.WITH_POSITIONS);
		doc.add(body);
		indexWriter.addDocument(doc);
		indexWriter.commit();
		
			
	}
	
	/**search method
	 * The result of a search will store in the "searchHits", 
	 * which contains instances of {@link de.uni.leipzig.asv.zitationsgraph.lucenesearch.MatchResult}
	 * If you use a Regex, keep in mind, that you DON'T use .* at the beginning.
	 * @param field the search field name 
	 * @param termRegex regular expression or the search word
	 * @throws CorruptIndexException
	 * @throws IOException
	 * @throws ParseException
	 * @throws InvalidTokenOffsetsException
	 */
	public  void search (String field,String termRegex ) throws CorruptIndexException, IOException, ParseException, InvalidTokenOffsetsException{
		Term t = new Term (field,termRegex);
		RegexQuery rq = new RegexQuery(t);
		QueryScorer scorer = new QueryScorer (rq,IndexReader.open(directory), field);
		ResultFormatter rf= new ResultFormatter ();
		Highlighter highlighter =new Highlighter(rf,scorer);
		highlighter.getBestFragment(analyzer, field,plainText);
		this.searchHits = rf.getMatch();
	}
	
	/**
	 * @param searchHits the searchHits to set
	 */
	public void setSearchHits(List <MatchResult> searchHits) {
		this.searchHits = searchHits;
	}

	/**
	 * @return the searchHits
	 */
	public List <MatchResult> getSearchHits() {
		return searchHits;
	}

	
	public static void main (String [] args){
		try {
			BufferedReader br = new BufferedReader(new FileReader("examples/referenceTestPart/DH2008.ref.txt"));
			StringBuffer sb =new StringBuffer();
			while(br.ready()){
				sb.append(br.readLine()+ " ");
			}
			LuceneSearcher lpe = new LuceneSearcher();
			
			lpe.createIndex(BaseDoc.BODY,sb.toString());
			lpe.search(BaseDoc.BODY,"solut.*");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LockObtainFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidTokenOffsetsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

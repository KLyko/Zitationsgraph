package de.uni.leipzig.asv.zitationsgraph.lucenesearch;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.TokenGroup;

public class ResultFormatter implements Formatter{
	private List <MatchResult> results;
	
	public ResultFormatter (){
		results = new ArrayList<MatchResult>();
	}
	@Override
	public String highlightTerm(String originalText, TokenGroup tokenGroup) {
		if (tokenGroup.getTotalScore()>0){
			results.add(new MatchResult(tokenGroup.getToken(0).toString(),
					tokenGroup.getToken(0).startOffset(),tokenGroup.getTotalScore()));
		}
		return  originalText;
	}
	
	/**
	 * @return the match
	 */
	public List<MatchResult> getMatch() {
		return results;
	}

}

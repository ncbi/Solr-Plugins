/**
 * 
 */
package com.agi.pubsolr.search;

import java.io.IOException;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.SyntaxError;

/**
 * @author Rafis
 *
 */
public class TextQParser extends QParser {
	private final String field;

	public TextQParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req, String field) {
		super(qstr, localParams, params, req);
		this.field = field;
	}

	@Override
	public Query parse() throws SyntaxError {
	    try (TokenStream input = req.getSchema().getQueryAnalyzer().tokenStream(field, qstr)) {
			BooleanQuery query = new BooleanQuery();
			CharTermAttribute termAtt = input.addAttribute(CharTermAttribute.class);
			KeywordAttribute keyAtt = input.addAttribute(KeywordAttribute.class);
			PositionLengthAttribute lenAtt = input.addAttribute(PositionLengthAttribute.class);
			input.reset();
			while (input.incrementToken()) {
				String term = termAtt.toString();
				TermQuery termQuery = new TermQuery(new Term(field, term));
				int len = lenAtt.getPositionLength();
				if (keyAtt.isKeyword()) {
					termQuery.setBoost(len * 5.0f);
				}
				query.add(termQuery, len == 1 ? Occur.MUST : Occur.SHOULD);
			}
			input.end();
			return query.clauses().isEmpty() ? null : query;
		} catch (IOException e) {
			throw new SyntaxError("Cannot parse text query", e);
		}
	}

}

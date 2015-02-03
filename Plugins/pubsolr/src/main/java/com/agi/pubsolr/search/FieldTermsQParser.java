package com.agi.pubsolr.search;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.SyntaxError;

import com.agi.pubsolr.analysis.TermFilter;

/**
 * This query parser splits query string by standard tokenizer and lower-cases them,
 * then tries to find all combinations from {@code 1} up to {@code maxLength} tokens,
 * which exist in the fields terms of the current index.
 * @author Rafis
 */
public class FieldTermsQParser extends QParser {
	private final String field;
	private final int maxLength;
	private final float boost;

	/**
	 * Constructor
     * @param qstr The part of the query string specific to this parser
     * @param localParams The set of parameters that are specific to this QParser.  See http://wiki.apache.org/solr/LocalParams
     * @param params The rest of the {@link org.apache.solr.common.params.SolrParams}
     * @param req The original {@link org.apache.solr.request.SolrQueryRequest} used to access field terms.
	 * @param field Field name
	 * @param maxLength The maximum number of words in compound term
	 * @param boost The boost factor which will be multiplied by term length (in words)
	 */
	public FieldTermsQParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req,
			String field, int maxLength, float boost) {
		super(qstr, localParams, params, req);
		this.field = field;
		this.maxLength = maxLength;
		this.boost = boost;
	}

	@Override
	public Query parse() throws SyntaxError {
		try {
			BooleanQuery query = new BooleanQuery();
			Terms terms = req.getSearcher().getAtomicReader().terms(field);
			if (terms == null)
				return null;
			TermsEnum termsUnum = terms.iterator(null);
			StandardTokenizerFactory factory = new StandardTokenizerFactory(Collections.<String,String>emptyMap());
			Tokenizer tokenizer = factory.create(new StringReader(qstr));
			TokenStream lowFilter = new LowerCaseFilter(tokenizer);
			try (TokenStream input = new TermFilter(lowFilter, termsUnum, maxLength)) {
				CharTermAttribute termAtt = input.addAttribute(CharTermAttribute.class);
				PositionLengthAttribute lenAtt = input.addAttribute(PositionLengthAttribute.class);
				input.reset();
				while (input.incrementToken()) {
					int len = lenAtt.getPositionLength();
					//if (len > 1) {
						String term = termAtt.toString();
						TermQuery termQuery = new TermQuery(new Term(field, term));
						termQuery.setBoost(len * boost);
						query.add(termQuery, Occur.SHOULD);
					//}
				}
				input.end();
			}
			return query.clauses().isEmpty() ? null : query;
		} catch (IOException e) {
			throw new SyntaxError("Cannot read '"+field+"' field", e);
		}
	}

}

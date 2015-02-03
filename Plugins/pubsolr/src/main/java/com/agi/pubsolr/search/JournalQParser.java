/**
 * 
 */
package com.agi.pubsolr.search;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
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
 * @author Rafis
 *
 */
public class JournalQParser extends QParser {
	private final String field;

	public JournalQParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req, String field) {
		super(qstr, localParams, params, req);
		this.field = field;
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
			try (TokenStream input = new TermFilter(lowFilter, termsUnum, 8)) {
				CharTermAttribute termAtt = input.addAttribute(CharTermAttribute.class);
				input.reset();
				while (input.incrementToken()) {
					TermQuery termQuery = new TermQuery(new Term(field, termAtt.toString()));
					//termQuery.setBoost(0.1f);
					query.add(termQuery, Occur.SHOULD);
				}
				input.end();
			}
			return query.clauses().isEmpty() ? null : query;
		} catch (IOException e) {
			throw new SyntaxError("Cannot read '"+field+"' field", e);
		}
	}

}

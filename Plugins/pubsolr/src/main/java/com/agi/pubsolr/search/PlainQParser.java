/**
 * 
 */
package com.agi.pubsolr.search;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.SyntaxError;

/**
 * @author Rafis
 */
public class PlainQParser extends QParser {
	private final FieldTermsQParser authorParser;
	private final FieldTermsQParser journalParser;
	private final TextQParser textParser;

	public PlainQParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
		super(qstr, localParams, params, req);
		authorParser = new FieldTermsQParser(qstr, localParams, params, req, "author", 5, 0.6f);
		journalParser = new FieldTermsQParser(qstr, localParams, params, req, "journal", 8, 0.5f);
		textParser = new TextQParser(qstr, localParams, params, req, "text");
	}

	/* (non-Javadoc)
	 * @see org.apache.solr.search.QParser#parse()
	 */
	@Override
	public Query parse() throws SyntaxError {
	    String qstr = getString();
	    if (qstr == null || qstr.length()==0)
	    	return null;
	    
	    if (StringUtils.isNumeric(qstr)) {
	    	//search by pubmed id
	    	return new TermQuery(new Term("id", qstr));
	    }

	    /*String defaultField = getParam(CommonParams.DF);
	    if (defaultField==null) {
	      defaultField = getReq().getSchema().getDefaultSearchFieldName();
	    }*/

	    //Operator op = QueryParsing.getQueryParserDefaultOperator(getReq().getSchema(), getParam(QueryParsing.OP));
	    //Occur oc = Occur.SHOULD; //op == Operator.AND ? Occur.MUST : Occur.SHOULD;

		BooleanQuery query = new BooleanQuery();
	    Query textQuery = textParser.parse();
	    if (textQuery != null) {
	    	query.clauses().addAll(((BooleanQuery)textQuery).clauses());
	    	//query.add(textQuery, oc);
	    }
	    Query authorQuery = authorParser.parse();
	    if (authorQuery != null) {
	    	query.clauses().addAll(((BooleanQuery)authorQuery).clauses());
	    	//authorQuery.setBoost(0.1f);
	    	//query.add(authorQuery, oc);
	    }
	    Query journalQuery = journalParser.parse();
	    if (journalQuery != null) {
	    	query.clauses().addAll(((BooleanQuery)journalQuery).clauses());
	    	//journalQuery.setBoost(0.1f);
	    	//query.add(journalQuery, oc);
	    }
	    return query.clauses().isEmpty() ? null : query;
	}

}

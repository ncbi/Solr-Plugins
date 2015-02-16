/**
 * 
 */
package com.agi.pubsolr.search;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.FieldParams;
import org.apache.solr.search.QParser;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.util.SolrPluginUtils;

/**
 * @author Rafis
 */
public class PlainQParser extends QParser {
	private static final String BF = "bf";
	
	private final FieldTermsQParser[] boostingParsers;
	private final TextQParser textParser;

	public PlainQParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
		super(qstr, localParams, params, req);
		// Some of the parameters may come in through localParams, so combine them with params.
		SolrParams solrParams = SolrParams.wrapDefaults(localParams, params);
		
		//create main field parser
	    String defaultField = getParam(CommonParams.DF);
	    if (defaultField == null) {
	    	defaultField = req.getSchema().getDefaultSearchFieldName();
	    	if (defaultField == null)
	    		defaultField = "text";
	    }
		textParser = new TextQParser(qstr, localParams, params, req, defaultField);
		
		//create boosting fields parsers
		String[] bf = solrParams.getParams(BF);
		if (bf == null)
			bf = new String[]{"author~5^0.6 journal~8^0.5"};
		List<FieldParams> queryFields = SolrPluginUtils.parseFieldBoostsAndSlop(bf, 2, 3);
		FieldTermsQParser[] parsers = new FieldTermsQParser[queryFields.size()];
		int i = 0;
		for (FieldParams f : queryFields) {
			parsers[i++] = new FieldTermsQParser(qstr, localParams, params, req, f.getField(), f.getSlop(), f.getBoost());
		}
		boostingParsers = parsers;
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


		BooleanQuery query = new BooleanQuery();
	    Query textQuery = textParser.parse();
	    if (textQuery != null) {
	    	query.clauses().addAll(((BooleanQuery)textQuery).clauses());
	    }
	    for (FieldTermsQParser boostingParser : boostingParsers) {
		    Query boostingQuery = boostingParser.parse();
		    if (boostingQuery != null) {
		    	query.clauses().addAll(((BooleanQuery)boostingQuery).clauses());
		    }
	    }
	    return query.clauses().isEmpty() ? null : query;
	}

}

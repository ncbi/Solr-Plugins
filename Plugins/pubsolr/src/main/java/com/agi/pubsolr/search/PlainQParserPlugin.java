/**
 * 
 */
package com.agi.pubsolr.search;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;

/**
 * @author Rafis
 */
public class PlainQParserPlugin extends QParserPlugin {

	@Override
	public void init(@SuppressWarnings("rawtypes") NamedList args) {
	}

	@Override
	public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
		return new PlainQParser(qstr, localParams, params, req);
	}

}

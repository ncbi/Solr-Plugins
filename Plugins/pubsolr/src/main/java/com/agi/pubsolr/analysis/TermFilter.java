/**
 * 
 */
package com.agi.pubsolr.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRefBuilder;

/**
 * {@link KeywordFilter} using index terms to recognize keywords.
 * @author Rafis
 */
public class TermFilter extends KeywordFilter {
	private final TermsEnum termsEnum;
	private final BytesRefBuilder builder = new BytesRefBuilder();
	
	public TermFilter(TokenStream input, TermsEnum termsEnum, int maxLenght) {
		super(input, maxLenght, false);
		this.termsEnum = termsEnum;
	}

	/* (non-Javadoc)
	 * @see com.agi.pubsolr.analysis.KeywordFilter#isKeyword()
	 */
	@Override
	protected boolean isKeyword() throws IOException {
		builder.copyChars(termAtt.buffer(), 0, termAtt.length());
		return termsEnum.seekExact(builder.get());
	}

}

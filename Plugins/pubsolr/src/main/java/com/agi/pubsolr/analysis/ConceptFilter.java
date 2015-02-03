/**
 * 
 */
package com.agi.pubsolr.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.Util;

/**
 * In addition to the input tokens this token filter emits the multi-word concepts -
 * all chains of the input tokens (joined by space) which present in provided {@link FST}.
 * All added tokens will be marked as keywords (set {@link KeywordAttribute} to {@code true}).
 * @author Rafis
 */
public final class ConceptFilter extends KeywordFilter {
	private final FST<Object> fst;
	private final BytesRefBuilder builder = new BytesRefBuilder();

	public ConceptFilter(TokenStream input, FST<Object> fst, int maxConceptLength) {
		super(input, maxConceptLength, true);
		this.fst = fst;
	}

	@Override
	protected boolean isKeyword() throws IOException {
		if (curLength == 1) //not multi-word
			return false;
		builder.copyChars(termAtt.buffer(), 0, termAtt.length());
		return Util.get(fst, builder.get()) != null;
	}
}

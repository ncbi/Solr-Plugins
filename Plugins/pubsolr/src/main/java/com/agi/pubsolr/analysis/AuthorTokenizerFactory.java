/**
 * 
 */
package com.agi.pubsolr.analysis;

import java.io.Reader;
import java.util.Map;

import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;

/**
 * @author Rafis
 *
 */
public class AuthorTokenizerFactory extends TokenizerFactory {

	/** Creates a new StandardTokenizerFactory */
	public AuthorTokenizerFactory(Map<String, String> args) {
		super(args);
		// maxTokenLength = getInt(args, "maxTokenLength", StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH);
		if (!args.isEmpty()) {
			throw new IllegalArgumentException("Unknown parameters: " + args);
		}
	}

	@Override
	public AuthorTokenizer create(AttributeFactory factory, Reader input) {
		AuthorTokenizer tokenizer = new AuthorTokenizer(factory, input);
		return tokenizer;
	}
}
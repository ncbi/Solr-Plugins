/**
 * 
 */
package com.agi.pubsolr.analysis;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import org.apache.lucene.store.InputStreamDataInput;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.NoOutputs;
import org.apache.lucene.util.fst.Outputs;

/**
 * Creates {@link ConceptFilter}. Accepts 2 arguments:<ul>
 * <li>{@code dictionary} (required) - filename of serialized FSA with concepts</li>
 * <li>{@code maxConceptLength} (optional) - maximum concept length in words, {@value #DEFAULT_MAX_CONCEPT_LENGTH} by default</li>
 * </ul>
 * @author Rafis
 */
public class ConceptFilterFactory extends TokenFilterFactory implements ResourceLoaderAware {
	private static final int DEFAULT_MAX_CONCEPT_LENGTH = 3;

	private final String dictionary;
	private final int maxConceptLength;
	private FST<Object> fst;

	public ConceptFilterFactory(Map<String, String> args) {
		super(args);
		dictionary = get(args, "dictionary");
		maxConceptLength = getInt(args, "maxConceptLength", DEFAULT_MAX_CONCEPT_LENGTH);
		if (!args.isEmpty()) {
			throw new IllegalArgumentException("Unknown parameters: " + args);
		}
		
	}

	@Override
	public void inform(ResourceLoader loader) throws IOException {
		final Outputs<Object> outputs = NoOutputs.getSingleton();
	    try (InputStream is = loader.openResource(dictionary)) {
	    	fst = new FST<>(new InputStreamDataInput(is), outputs);
	    }
	}

	@Override
	public TokenStream create(TokenStream input) {
		return new ConceptFilter(input, fst, maxConceptLength);
	}

}

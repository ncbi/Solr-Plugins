/**
 * 
 */
package com.agi.pubsolr.analysis;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.util.AttributeFactory;

import com.agi.pubsolr.model.Author;

/**
 * @author Rafis
 *
 */
public class AuthorTransformingTokenizerFactory extends StandardTokenizerFactory {
	private final String prefix = "l=";
	
	public AuthorTransformingTokenizerFactory(Map<String, String> args) {
		super(args);
		//prefix = get(args, "prefix");
		if (!args.isEmpty()) {
			throw new IllegalArgumentException("Unknown parameters: " + args);
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.analysis.util.TokenizerFactory#create(org.apache.lucene.util.AttributeFactory, java.io.Reader)
	 */
	@Override
	public StandardTokenizer create(AttributeFactory factory, Reader input) {
		Reader reader = transform(input);
		return super.create(factory, reader);
	}

	private Reader transform(Reader input) {
		int n = prefix.length();
		PushbackReader reader = new PushbackReader(input, n);
		try {
			char[] buf = new char[n];
			for (int i = 0; i < n; i++) {
				int c = reader.read();
				if (c < 0) {
					reader.unread(buf, 0, i);
					return reader;
				} else {
					buf[i] = (char)c;
					if (c != prefix.charAt(i)) {
						reader.unread(buf, 0, i+1);
						return reader;
					}
				}
			}
			reader.unread(buf);
			return createNewReader(reader);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Reader createNewReader(Reader input) throws IOException {
		Author author = AuthorTokenizer.parseAuthor(input);
		StringBuilder sb = new StringBuilder();
		sb.append(author.getLastName());
		if (author.getForeName() != null) {
			sb.append(' ').append(author.getForeName());
		}
		if (author.getInitials() != null) {
			sb.append(' ').append(author.getInitials());
		}
		return new StringReader(sb.toString());
	}
}

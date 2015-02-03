package com.agi.pubsolr.dataimport;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.AttributeFactory;

import com.agi.pubsolr.analysis.AuthorTokenizer;

public class AuthorToTextTransformer {
	public Object transformRow(Map<String, Object> row) {
		Object value = row.get("author");
		if (value instanceof List) {
			@SuppressWarnings("unchecked")
			List<String> list = (List<String>)value;
			List<String> result = new ArrayList<>();
			for (String author : list) {
				result.add(getAuthorWords(author));
			}
			row.put("author_text", result);
		}
		return row;
	}
	
	String getAuthorWords(String author) {
		try (AuthorTokenizer tokenizer = new AuthorTokenizer(AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY, new StringReader(author))) {
			Set<String> set = new HashSet<>();
			CharTermAttribute termAtt = tokenizer.addAttribute(CharTermAttribute.class);
			tokenizer.reset();
			while (tokenizer.incrementToken()) {
				String term = termAtt.toString();
				String[] words = StringUtils.split(term);
				for (String word : words) {
					set.add(word);
				}
			}
			tokenizer.end();
			return StringUtils.join(set, ' ');
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
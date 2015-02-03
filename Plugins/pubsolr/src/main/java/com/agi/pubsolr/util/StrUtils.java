package com.agi.pubsolr.util;

import java.io.IOException;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.Util;

public class StrUtils {
	private static final Pattern DEACRITIC_REMOVER = Pattern.compile("\\p{M}");
	private static final Pattern FIELD_SEP = Pattern.compile("&[a-z]=");

	/**
	 * Replace all white spaces and non-breaking spaces with space,
	 * remove leading and trailing spaces,
	 * replace each sequence of spaces with one space.
	 * <p>If there are no changes, return the argument.toString().
	 * <p>Examples:</p>
	 * <pre>
	 * normalizeSpaces(null)          = null
	 * normalizeSpaces("")            = ""
	 * normalizeSpaces("\t Some\u00A0\u2007text\r\n") = "Some text"
	 * </pre>
	 */
	public static String normalizeSpaces(CharSequence s) {
		if (s == null)
			return null;
		final int n = s.length();
		StringBuilder buf = new StringBuilder(n);
		boolean replaced = false;
		char prev = ' '; // ' ' means one of spaces, otherwise non-space char
		for (int i = 0; i < n; i++) {
			final char c = s.charAt(i);
			if (CharUtils.isSpace(c)) {
				if (prev != ' ') {
					buf.append(' ');
					prev = ' ';
					if (c != ' ') {
						replaced = true;
					}
				}
			} else {
				buf.append(c);
				prev = c;
			}
		}
		int len = buf.length();
		if (len > 0 && prev == ' ') { // last char is space
			buf.setLength(--len);
		}
		if (len == n && !replaced) {
			return s.toString();
		}
		return len == 0 ? "" : buf.toString();
	}

	public static boolean hasInput(FST<Object> fst, String input) throws IOException {
		return Util.get(fst, new BytesRef(input)) != null;
	}
	
	public static String removeDiacritics(String s) {
		s = Normalizer.normalize(s, Normalizer.Form.NFD);
		return DEACRITIC_REMOVER.matcher(s).replaceAll("");
	}
	
	public static Map<Character, String> parseFields(String s) {
		String[] fields = FIELD_SEP.split(s);
		Map<Character, String> map = new HashMap<>(fields.length * 2);
		int offset = 0;
		for (String field : fields) {
			String value = offset == 0 ? field.substring(2) : field;
			map.put(s.charAt(offset), value);
			offset += value.length() + 3;
		}
		return map;
	}
}

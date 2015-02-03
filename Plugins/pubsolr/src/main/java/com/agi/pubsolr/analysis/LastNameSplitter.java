/**
 * 
 */
package com.agi.pubsolr.analysis;

import org.apache.commons.lang.StringUtils;

/**
 * Splits Lastname of author into chunks.
 * <p>Reusable, not thread-safe.
 * @author rafis
 */
public class LastNameSplitter extends NameComponentSplitter {
	private final NamePrefixExtractor prefixExtractor;
	private final StringBuilder prefixBuffer = new StringBuilder();
	private String prefix;
	
	public LastNameSplitter(NamePrefixExtractor prefixExtractor) {
		super();
		this.prefixExtractor = prefixExtractor;
	}

	@Override
	public String[][] split(String s) {
		if (s == null)
			return null;
		String[] prefixAndBody = prefixExtractor.split(s);
		prefix = prefixAndBody == null ? null : prefixAndBody[0];
		int prefixLen = StringUtils.length(prefix);
		String body = prefixAndBody == null ? s : prefixAndBody[1];
		String[][] result = super.split(body);
		if (boundReceiver != null && prefixLen > 0)
			boundReceiver.shift(prefixLen);
		return result;
	}

	@Override
	protected String[] combineSubtokens() {
		String[] result = super.combineSubtokens();
		if (prefix != null) {
			convertPrefix();
			if (prefix.length() > 0) {
				boolean endsWithSpace = prefix.endsWith(" ");
				int n = result.length;
				String[] prepResult = new String[endsWithSpace ? n << 1 : n * 3];
				for (int i = 0; i < n; i++) {
					prepResult[i] = prefix + result[i];
					if (!endsWithSpace)
						prepResult[n + i] = prefix + ' ' + result[i];
				}
				System.arraycopy(result, 0, prepResult, prepResult.length - n, n);
				prefix = null;
				result = prepResult;
			}
		}
		return result;
	}
	
	private void convertPrefix() {
		if (prefix != null) {
			prefixBuffer.setLength(0);
			char prev = ' ';
			for (int i = 0, n = prefix.length(); i < n; i++) {
				char c = prefix.charAt(i);
				if (Character.isLetterOrDigit(c)) {
					prefixBuffer.append(c); //charConverter.append(prefixBuffer, c);
					prev = c;
				} else if (prev != ' ') {
					prefixBuffer.append(' ');
					prev = ' ';
				}
			}
			prefix = prefixBuffer.toString();
		}
	}
}

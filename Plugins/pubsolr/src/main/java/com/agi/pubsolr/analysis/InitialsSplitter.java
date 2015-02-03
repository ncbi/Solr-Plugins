/**
 * 
 */
package com.agi.pubsolr.analysis;

import com.agi.pubsolr.util.CharUtils;

/**
 * Splits initials of author into chunks.
 * <p>Reusable, not thread-safe.
 * @author rafis
 */
public class InitialsSplitter extends NameComponentSplitter {
	
	public InitialsSplitter() {
		super();
	}

	@Override
	public String[][] split(String s) {
		if (s == null)
			return null;
		
		init(s);
		
		skipTrash();
		if (dataPos == data.length)
			return null;
		
		int capitalLen = 0;
		while (dataPos < data.length) {
			final char c = data[dataPos++];
			if (Character.isUpperCase(c)) {
				if (buffer.length() > 0) {
					buffer2subtokens2results();
				}
				buffer.setLength(0);
				appendToBuffer(c);
				capitalLen = buffer.length();
//				buffer2subtokens2results();
//				skipTrash();
			} else if (c == '-') {
				String particle = buffer.substring(capitalLen);
				if (capitalLen > 0) {
					buffer.setLength(capitalLen);
					capitalLen = 0;
					buffer2subtokens2results();
				}
				subtokens.add(particle);
			} else if (CharUtils.isSpace(c)) {
				String particle = buffer.substring(capitalLen);
				if (capitalLen > 0) {
					buffer.setLength(capitalLen);
					capitalLen = 0;
					buffer2subtokens2results();
					buffer.append(particle);
				}
				buffer2subtokens2results();
			} else if (Character.isLetterOrDigit(c)) {
				appendToBuffer(c);
			}
		}
		buffer2subtokens2results();
		return result.toArray(new String[result.size()][]);
	}

}

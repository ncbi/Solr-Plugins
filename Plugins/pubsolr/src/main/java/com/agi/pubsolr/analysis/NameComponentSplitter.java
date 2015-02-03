/**
 * 
 */
package com.agi.pubsolr.analysis;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.agi.pubsolr.util.CharUtils;

/**
 * @author Rafis
 *
 */
public class NameComponentSplitter {
	protected char[] data;
	protected int dataPos;
	protected final List<String> subtokens = new ArrayList<>();
	protected final StringBuilder buffer = new StringBuilder();
	protected final List<String[]> result = new ArrayList<>();
	protected SplitterBoundReceiver boundReceiver;
	protected boolean registerNextBound;

	public NameComponentSplitter() {
	}

	public void setBoundReceiver(SplitterBoundReceiver boundReceiver) {
		this.boundReceiver = boundReceiver;
	}

	protected void init(String s) {
		data = s.toCharArray();
		dataPos = 0;
		buffer.setLength(0); //clear buffer
		subtokens.clear();
		result.clear();
		registerNextBound = boundReceiver != null;
	}
	
	public String[][] split(String s) {
		if (s == null)
			return null;
		
		init(s);
		
		skipTrash();
		if (dataPos == data.length)
			return null;
		
		char prev = ' ';
		while (dataPos < data.length) {
			final char c = data[dataPos++];
			if (CharUtils.isSpace(c) || c == '.') {
				buffer2subtokens2results();
				skipTrash();
				prev = ' ';
				continue;
			}
			if (Character.isLetterOrDigit(c)) {
				if (prev != ' ' && isNewTile(prev, c)) {
					moveBufferToSubtokens();
				}
				appendToBuffer(c);
			}
			prev = c;
		}
		buffer2subtokens2results();
		return result.toArray(new String[result.size()][]);
	}
	
	protected boolean isNewTile(char prev, char cur) {
		return !Character.isLetterOrDigit(prev) || // punctuation
				Character.isUpperCase(cur) && Character.isLowerCase(prev); // camel
	}
	
	/** Converts the argument to lower-case, then convert with {@code charConverter} and append to {@code buffer}*/
	protected void appendToBuffer(char c) {
		char lc = Character.toLowerCase(c);
		if (registerNextBound) {
			boundReceiver.addBound(dataPos - 1);
			registerNextBound = false;
		}
		buffer.append(lc); //charConverter.append(buffer, lc);
	}
	
	protected void skipTrash() {
		while (dataPos < data.length && !Character.isLetterOrDigit(data[dataPos]))
			dataPos++;
	}

	protected void moveBufferToSubtokens() {
		subtokens.add(buffer.toString());
		buffer.setLength(0);
	}
	
	protected String[] combineSubtokens() {
		int size = subtokens.size();
		if (size == 0)
			return null;
		String[] result;
		if (size == 1) {
			result = new String[] { subtokens.get(0) };
		} else {
			String[] a = subtokens.toArray(new String[size]);
			result = new String[] {
					StringUtils.join(a),
					StringUtils.join(a, ' ')
					};
		}
		return result;
	}
	
	protected void buffer2subtokens2results() {
		if (buffer.length() > 0)
			moveBufferToSubtokens();
		String[] combinedSubtokens = combineSubtokens();
		if (combinedSubtokens != null) {
			result.add(combinedSubtokens);
			subtokens.clear();
		}
		registerNextBound = boundReceiver != null;
	}
}

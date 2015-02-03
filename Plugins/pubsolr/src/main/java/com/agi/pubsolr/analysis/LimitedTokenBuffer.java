/**
 * 
 */
package com.agi.pubsolr.analysis;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

/**
 * @author Rafis
 *
 */
public class LimitedTokenBuffer {
	private static final char SEP = ' ';
	
	private final StringBuilder sb;
	private final int tokenLimit;
	private final int[] bounds;
	private final int[] tokenStarts;
	private final int[] tokenEnds;
	private int size;
	
	public LimitedTokenBuffer(int tokenLimit) {
		this(tokenLimit, 512);
	}
	
	public LimitedTokenBuffer(int tokenLimit, int initCharCapacity) {
		assert tokenLimit > 1;
		this.tokenLimit = tokenLimit;
		this.sb = new StringBuilder(initCharCapacity);
		this.bounds = new int[tokenLimit];
		this.tokenStarts = new int[tokenLimit];
		this.tokenEnds = new int[tokenLimit];
	}
	
	public void add(CharTermAttribute termAtt, OffsetAttribute offsetAtt) {
		add(termAtt.buffer(), 0, termAtt.length(), offsetAtt.startOffset(), offsetAtt.endOffset());
	}
	
	public void add(char[] src, int srcOffset, int srcLength, int tokenStart, int tokenEnd) {
		if (size == tokenLimit) {
			removeHead();
		}
		sb.append(src, srcOffset, srcLength);
		sb.append(SEP);
		bounds[size] = sb.length();
		tokenStarts[size] = tokenStart;
		tokenEnds[size] = tokenEnd;
		size++;
	}
	
	public int getSize() {
		return size;
	}
	
	public boolean isEmpty() {
		return size == 0;
	}
	
	public void clear() {
		size = 0;
		sb.setLength(0);
	}
	
	public void extract(int start, int end, StringBuilder out) {
		if (size == 0)
			return;
		int bufferStart = start == 0 ? 0 : bounds[start - 1];
		int bufferEnd = bounds[end - 1] - 1;
		out.setLength(0);
		out.append(sb, bufferStart, bufferEnd);
	}
	
	public void extract(int start, int end, CharTermAttribute out) {
		if (size == 0)
			return;
		int bufferStart = start == 0 ? 0 : bounds[start - 1];
		int bufferEnd = bounds[end - 1] - 1;
		out.setEmpty();
		out.append(sb, bufferStart, bufferEnd);
	}
	
	public void extract(int start, int end, OffsetAttribute out) {
		if (size == 0)
			return;
		out.setOffset(tokenStarts[start], tokenEnds[end - 1]);
	}
	
	public void removeHead() {
		if (size > 0) {
			size--;
			int len = bounds[0];
			sb.delete(0, len);
			for (int i = 0; i < size; i++) {
				bounds[i] = bounds[i + 1] - len;
			}
			System.arraycopy(tokenStarts, 1, tokenStarts, 0, size);
			System.arraycopy(tokenEnds, 1, tokenEnds, 0, size);
		}
	}
}

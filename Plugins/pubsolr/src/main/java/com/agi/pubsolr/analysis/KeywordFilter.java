/**
 * 
 */
package com.agi.pubsolr.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;

/**
 * Emits keywords (chains of {@code 1} to {@link #maxLenght} input tokens joined by space) recognized by {@link #isKeyword()} method.
 * Can additionally emit input tokens if {@code emitInputTokens} set to {@code true}.
 * @author Rafis
 */
public abstract class KeywordFilter extends TokenFilter {
	protected int curLength;
	protected boolean insideKeyword;
	protected final int maxLenght;
	protected final boolean emitInputTokens;
	protected final LimitedTokenBuffer buffer;
	protected final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	protected final PositionIncrementAttribute posIncAtt = addAttribute(PositionIncrementAttribute.class);
	protected final PositionLengthAttribute posLenAtt = addAttribute(PositionLengthAttribute.class);
	protected final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	protected final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);

	protected KeywordFilter(TokenStream input, int maxLenght, boolean emitInputTokens) {
		super(input);
		this.maxLenght = maxLenght;
		this.buffer = new LimitedTokenBuffer(maxLenght);
		this.emitInputTokens = emitInputTokens;
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		buffer.clear();
		curLength = 0;
	}

	@Override
	public boolean incrementToken() throws IOException {
		if (buffer.isEmpty()) {
			for (int i = 0; i < maxLenght && input.incrementToken(); i++) {
				buffer.add(termAtt, offsetAtt);
			}
			curLength = buffer.getSize();
		}
		while (!buffer.isEmpty()) {
			if (curLength > 0) {
				buffer.extract(0, curLength, termAtt);
				boolean keyword = isKeyword();
				if (keyword || emitInputTokens && curLength == 1) {
					buffer.extract(0, curLength, offsetAtt);
					keywordAtt.setKeyword(keyword); //was setKeyword(curLength > 1);
					if (insideKeyword)
						posIncAtt.setPositionIncrement(0);
					posLenAtt.setPositionLength(curLength);
					if (keyword)
						insideKeyword = true;
					curLength--;
					return true;
				}
				curLength--;
			} else {
				if (input.incrementToken()) {
					buffer.add(termAtt, offsetAtt);
				} else {
					buffer.removeHead();
				}
				curLength = buffer.getSize();
				insideKeyword = false;
			}
		}
		return false;
	}

	protected abstract boolean isKeyword() throws IOException;
}

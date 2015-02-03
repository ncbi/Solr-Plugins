package com.agi.pubsolr.util;

public class CharUtils extends org.apache.commons.lang.CharUtils {
	
	/** Return true, if {@code ch} is non-breaking space. */
	public static boolean isNonBreakingSpace(char ch) {
        switch (ch) {
            case '\u00A0':
            case '\u2007':
            case '\u202F':
                return true;
        }
        return false;
	}

	/** Return true, if {@code ch} is whitespace or non-breaking space or control char. */
	public static boolean isSpace(char ch) {
		return Character.isWhitespace( ch ) || isNonBreakingSpace( ch ) || ch < ' ';
	}

    /** Return true, if {@code ch} is line terminator character. */
    public static boolean isLineTerminator(char ch) {
        switch (ch) {
            case '\n':
            case '\r':
            case '\u0085':
            case '\u000b':
            case '\u000c':
            case '\u2028':
            case '\u2029':
                return true;
        }
        return false;
    }

}
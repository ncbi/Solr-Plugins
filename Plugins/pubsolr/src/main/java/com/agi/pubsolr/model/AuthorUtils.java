/**
 * 
 */
package com.agi.pubsolr.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.agi.pubsolr.util.CharUtils;

/**
 * @author Rafis
 */
public class AuthorUtils {
	private static final Set<String> PARTICLES = new HashSet<String>(Arrays.asList(
			"de", "do", "da", "du", "del", "dos", "le", "el"));
	
	/**
	 * <ul>
	 * <li>"Ann" -> "A"</li>
	 * <li>"Ivan Petrovich" -> "IP"</li>
	 * <li>"d'Isabelle" -> "I"</li>
	 * <li>"Maria del Costa" -> "Mdel C"</li>
	 * <li>"el-Shimaa M" -> "el-SM"</li>
	 * <li>"Osima El-Sayed" -> "Oel-S"</li>
	 * </ul>
	 * @param name
	 * @return non-empty initials or null
	 */
	public static String createInitials(String name) {
		if (name == null)
			return null;
		StringBuilder buffer = new StringBuilder(2);
		int upperCount = 0;
		boolean reqSpace = false;
		char prev = ' ';
		int wordPos = 0;
		for (int i = 0, n = name.length(); i < n; i++) {
			char c = name.charAt(i);
			if (prev == ' ')
				wordPos = i;
			if (CharUtils.isSpace(c)) {
				if (prev != ' ') { // space after word
					String word = name.substring(wordPos, i);
					if (upperCount > 0 && PARTICLES.contains(word)) {
						buffer.append(word);
						reqSpace = true;
					}
				}
				prev = ' ';
			} else {
				if (Character.isLetterOrDigit(c)) {
					if (Character.isUpperCase(c)) {
						if (prev == ' ') {
							if (!(c == 'E' && i < n-2 && Character.toLowerCase(name.charAt(i+1)) == 'l' && name.charAt(i+2) == '-')) {
								if (reqSpace)
									buffer.append(' ');
								buffer.append(c);
								upperCount++;
							}
						} else if (prev == '-') {
							if (i > 2 && Character.toLowerCase(name.charAt(i-3)) == 'e' && Character.toLowerCase(name.charAt(i-2)) == 'l')
								buffer.append("el-");
							buffer.append(c);
							upperCount++;
						} else if (!Character.isUpperCase(prev)) {
							buffer.append(c);
							upperCount++;
						}
					}
				}
				prev = c;
			}
		}
		return buffer.length() == 0 ? null : buffer.toString();
	}

	public static String getNormalizedSuffix(String lowerCaseSuffix) {
		char c = lowerCaseSuffix.charAt(0);
		if (c >= '0' && c <= '9') { // support only ASCII digits
			int num = prefixNumber(lowerCaseSuffix);
			switch (num) {
			case 1:
				return "1st";
			case 2:
				return "2nd";
			case 3:
				return "3rd";
			default:
				return num+"th";
			}
		} else if (c == 'i') {
			if ("i".equals(lowerCaseSuffix))
				return "1st";
			if ("ii".equals(lowerCaseSuffix))
				return "2nd";
			if ("iii".equals(lowerCaseSuffix))
				return "3rd";
			if ("iv".equals(lowerCaseSuffix))
				return "4th";
		}
		return null;
	}
	
	private static int prefixNumber(String s) {
		int n = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c < '0' || c > '9') // support only ASCII digits
				break;
			n = n * 10 + (c - '0');
		}
		return n;
	}
}

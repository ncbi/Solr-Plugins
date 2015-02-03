package com.agi.pubsolr.model;

import java.util.Map;

import com.agi.pubsolr.util.StrUtils;

public class Author {
    private String lastName;
    private String foreName; // first name + middle name
    private String initials;
    private String suffix;
	
    public Author(String lastName, String foreName, String initials) {
		this(lastName, foreName, initials, null);
	}

    public Author(String lastName, String foreName, String initials, String suffix) {
		this.lastName = lastName;
		this.foreName = foreName;
		this.initials = initials;
		this.suffix = suffix;
	}

	public String getLastName() {
		return lastName;
	}

	public String getForeName() {
		return foreName;
	}

	public String getInitials() {
		return initials;
	}

	public String getSuffix() {
		return suffix;
	}

	@Override
	public String toString() {
		return "Author ["
				+ (lastName != null ? "lastName=" + lastName + ", " : "")
				+ (foreName != null ? "foreName=" + foreName + ", " : "")
				+ (initials != null ? "initials=" + initials + ", " : "")
				+ (suffix != null ? "suffix=" + suffix : "") + "]";
	}

	public static Author parse(String s) {
    	if (s.startsWith("l=")) {
    		 Map<Character, String> map = StrUtils.parseFields(s);
    		return new Author(map.get('l'), map.get('f'), map.get('i'), map.get('s'));
    	} else {
    		return new Author(s, null, null, null);
    	}
    }
}

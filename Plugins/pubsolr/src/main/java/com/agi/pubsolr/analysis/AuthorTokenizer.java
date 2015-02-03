/**
 * 
 */
package com.agi.pubsolr.analysis;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.AttributeFactory;

import com.agi.pubsolr.model.Author;
import com.agi.pubsolr.model.AuthorUtils;
import com.agi.pubsolr.util.StrUtils;

/**
 * @author Rafis
 *
 */
public final class AuthorTokenizer extends Tokenizer {
	private static final int NAME_VARIANTS_LIMIT = 64;
	private static final int LASTNAME_VARIANTS_LIMIT = 64;
	private static final int VARIANTS_LIMIT = 512;
	private static final Log log = LogFactory.getLog(AuthorTokenizer.class);

	private Author author;
	private final NameComponentSplitter lastNameSplitter = new LastNameSplitter(new NamePrefixExtractor());
	private final NameComponentSplitter namePartSplitter = new NameComponentSplitter();
	private final NameComponentSplitter initialsSplitter = new InitialsSplitter();
	private final StringBuilder buffer = new StringBuilder(); //reusable buffer for name variants
	private final Collection<String> lastNameVariants = new ArrayList<String>(); // variants of lastName component
	private final Collection<String> nameVariants = new HashSet<>(32); // combinations of all components except lastName and suffix
	private final List<String> variants = new ArrayList<String>(); //all combinations of lastNameVariants, nameVariants and suffix
	private Iterator<String> iterator; //iterator over variants

	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	//private OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

	public AuthorTokenizer(AttributeFactory factory, Reader input) {
		super(factory, input);
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		iterator = null;
	}

	@Override
	public boolean incrementToken() throws IOException {
		if (iterator == null) {
			clearAttributes();
			String s = IOUtils.toString(input);
			s = StrUtils.removeDiacritics(s);
			author = Author.parse(s);
			fillLastNameVariants();
			fillNameVariants();
			fillVariants();
			iterator = variants.iterator();
		}
		if (iterator.hasNext()) {
			termAtt.setEmpty();
			termAtt.append(iterator.next());
			return true;
		}
		return false;
	}

	public static Author parseAuthor(Reader input) throws IOException {
		String s = IOUtils.toString(input);
		s = StrUtils.removeDiacritics(s);
		return Author.parse(s);
	}
	  
	public static Author parseAuthor(String s) {
		s = StrUtils.removeDiacritics(s);
		return Author.parse(s);
	}
	  
	private void fillLastNameVariants() {
		lastNameVariants.clear();
		String name = author.getLastName();
		if (name == null)
			return;
		String[][] chunks = lastNameSplitter.split(name);
		addVariants(lastNameVariants, chunks, true, LASTNAME_VARIANTS_LIMIT);
	}
	
	private void fillNameVariants() {
		nameVariants.clear();
		String name = author.getForeName();
		String init = author.getInitials();
		
		String generatedInitials = AuthorUtils.createInitials(name);
		if (StringUtils.equals(init, generatedInitials)) { //equal or both are null
			fillNameVariantsFromMedline(init);
		} else {
			if (init != null)
				fillNameVariantsFromMedline(init);
			if (generatedInitials != null)
				fillNameVariantsFromMedline(generatedInitials);
		}
	}
	
	private void fillNameVariantsFromMedline(String init) {
		String name = author.getForeName();
		String[][] nameChunks = namePartSplitter.split(name);
		String[][] initChunks = initialsSplitter.split(init);
		
		if (initChunks != null) {
			addVariants(nameVariants, initChunks, false, NAME_VARIANTS_LIMIT);
			if (initChunks.length > 1) {
				addVariants(nameVariants, initChunks[0]);
				addVariants(nameVariants, initChunks, true, NAME_VARIANTS_LIMIT);
				addSpecialInitialVariants(nameVariants, initChunks);
			}
		}
		if (nameChunks != null) {
			addVariants(nameVariants, nameChunks, true, NAME_VARIANTS_LIMIT);
			if (nameChunks.length > 1) {
				addVariants(nameVariants, nameChunks[0]);
			}
			if (correspond(nameChunks, initChunks)) {
				// first name + middle initial
				String[][] chunks = combine(nameChunks, initChunks);
				addVariants(nameVariants, chunks, true, NAME_VARIANTS_LIMIT);
				
				// first initial + middle name
				chunks = combine(initChunks, nameChunks);
				addVariants(nameVariants, chunks, true, NAME_VARIANTS_LIMIT);
			}
		}
	}
	
	public static boolean correspond(String[][] nameChunks, String[][] initChunks) {
		if (nameChunks != null && initChunks != null && initChunks.length <= nameChunks.length) { //test length
			for (int i = 0; i < initChunks.length; i++) {
				if (!nameChunks[i][0].startsWith(initChunks[i][0])) //name word does not start with initial
					return false;
			}
			return true; //all names starts with initials
		}
		return false;
	}
	
	private String[][] combine(String[][] firstChunks, String[][] secondChunks) {
		int n = secondChunks.length;
		String[][] result = new String[n][];
		result[0] = firstChunks[0];
		for (int i = 1; i < n; i++) {
			result[i] = secondChunks[i];
		}
		return result;
	}
	
	private void addVariants(Collection<String> result, String[] wordVariants) {
		result.addAll(Arrays.asList(wordVariants));
	}
	
	private void addVariants(Collection<String> result, String[][] chunks, boolean separated, int resultLimit) {
		if (chunks == null)
			return;
		final int size = chunks.length;
		if (size == 1) {
			// all variants in chunks[0], nothing to combine
			addVariants(result, chunks[0]);
		} else {
			// generate all variants from chunks
			int[] steps = new int[size];
			while (result.size() < resultLimit) {
				// generate variant
				buffer.setLength(0); // clear buffer
				for (int j = 0; j < size; j++) {
					int step = steps[j];
					String wordVariant = chunks[j][step];
					if (separated && j > 0)
						buffer.append(' ');
					buffer.append(wordVariant);
				}
				/*if (result.size() > 1000) {
					System.out.println(result.size());
				}
				if (buffer.length() > 2000) {
					System.out.println(this.author);
				}*/
				result.add(buffer.toString());
				// make next step
				int i = size - 1;
				while (i >= 0) {
					if (steps[i] < chunks[i].length - 1) {
						steps[i]++;
						break;
					} else {
						steps[i] = 0;
						i--;
					}
				}
				if (i < 0) { //no more steps
					return;
				}
			}
			log.debug("Variants limited for "+author);
		}
	}
	
	/** special handling for initials with particle, e.g. [[m], [del], [r]] -> "mdel r" */
	private void addSpecialInitialVariants(Collection<String> result, String[][] initChunks) {
		if (initChunks.length == 3
				&& initChunks[0].length == 1 && initChunks[0][0].length() == 1
				&& initChunks[1].length == 1 && initChunks[2].length == 1) {
			buffer.setLength(0);
			buffer.append(initChunks[0][0]).append(initChunks[1][0]);
			result.add(buffer.toString());
			buffer.append(' ').append(initChunks[2][0]);
			result.add(buffer.toString());
		}
	}
	
	/**
	 * lastNameVariants
	 * lastNameVariants foreNameVariants
	 * foreNameVariants lastNameVariants
	 */
	private void fillVariants() {
		variants.clear();
		
		if (lastNameVariants.isEmpty()) {
			log.warn(author.toString()+" is not indexed because lastName has no variants");
			return;
		}
		
		// lastNameVariants
		variants.addAll(lastNameVariants);
		
		if (!nameVariants.isEmpty()) {
			String[] lastNames = lastNameVariants.toArray(new String[lastNameVariants.size()]);
			String[] foreNames = nameVariants.toArray(new String[nameVariants.size()]);
			
			// lastNameVariants foreNameVariants
			String[][] chunks = new String[][] {lastNames, foreNames};
			addVariants(variants, chunks, true, VARIANTS_LIMIT);
			
			// foreNameVariants lastNameVariants
			chunks[0] = foreNames;
			chunks[1] = lastNames;
			addVariants(variants, chunks, true, VARIANTS_LIMIT);
		}
		
		if (author.getSuffix() != null) {
			String lowerCaseSuffix = author.getSuffix().toLowerCase();
			String normalizedSufix = AuthorUtils.getNormalizedSuffix(lowerCaseSuffix);
			if (normalizedSufix != null && !lowerCaseSuffix.equals(normalizedSufix)) {
				addSuffixVariants(lowerCaseSuffix, normalizedSufix);
			} else {
				addSuffixVariants(lowerCaseSuffix);
			}
		}
	}
	
	private void addSuffixVariants(String... suffixes) {
		int n = variants.size();
		for (int i = 0; i < n; i++) {
			String variant = variants.get(i);
			for (String suffix : suffixes) {
				variants.add(variant+' '+suffix);
			}
		}
	}
	
}

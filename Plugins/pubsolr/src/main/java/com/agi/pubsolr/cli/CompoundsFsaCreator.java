/**
 * 
 */
package com.agi.pubsolr.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.Builder;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.FST.INPUT_TYPE;
import org.apache.lucene.util.fst.NoOutputs;
import org.apache.lucene.util.fst.Outputs;
import org.apache.lucene.util.fst.Util;

import com.agi.pubsolr.util.StrUtils;

/**
 * 
 * @author Rafis
 */
public class CompoundsFsaCreator {
	private final Path textPath;
	private final Path fsaPath;
	private final int minLength;
	private final int maxLength;
	
	/**
	 * TODO use stop words
	 * @param textPath path to text file with phrases
	 * @param fsaPath path to resulting FSA file
	 * @param minLength Minimum phrase length (in words)
	 * @param maxLength Maximum phrase length (in words)
	 */
	public CompoundsFsaCreator(Path textPath, Path fsaPath, int minLength, int maxLength) {
		this.textPath = textPath;
		this.fsaPath = fsaPath;
		this.minLength = minLength;
		this.maxLength = maxLength;
	}
	
	public void create() throws IOException {
		final Outputs<Object> outputs = NoOutputs.getSingleton();
		final Object NO_OUTPUT = outputs.getNoOutput();
		Builder<Object> builder = new Builder<>(INPUT_TYPE.BYTE1, outputs);
		BytesRefBuilder scratchBytes = new BytesRefBuilder();
		IntsRefBuilder scratchInts = new IntsRefBuilder();
		Pattern splitter = Pattern.compile("\\W+");
		Pattern acceptor = Pattern.compile("[a-z]+");
		List<String> list = new ArrayList<>(24_000_000);
		try (BufferedReader reader = Files.newBufferedReader(textPath, StandardCharsets.ISO_8859_1)) {
			String line;
			int i = 0, acceptedCount = 0, maxLen = 0;
			String maxStr = null;
			while((line = reader.readLine()) != null) {
				String normLine = StrUtils.normalizeSpaces(line);
				String[] a = splitter.split(normLine);
				if (a.length >= minLength && a.length <= maxLength) {
					String s = StringUtils.join(a, ' ').trim();
					if (acceptor.matcher(s).find() && !s.endsWith(" the")) { //TODO check at least one word is non-stop-word
						list.add(s);
						/*
						scratchBytes.copyChars(s);
						scratch.copyUTF8Bytes(scratchBytes.get());
						builder.add(scratch.get(), NO_OUTPUT);
						*/
						acceptedCount++;
						if (s.length() > maxLen) {
							maxLen = s.length();
							maxStr = s;
						}
					}
				}
				i++;
				if (i % 1000000 == 0)
					System.out.format("lines: %,d\n", i);
			}
			System.out.format("Input lines - accepted: %,d, skipped: %,d, longest: \"%s\"\n", acceptedCount, i-acceptedCount, maxStr);
		}
		Collections.sort(list);
		for (String s : list) {
			scratchBytes.copyChars(s);
			builder.add(Util.toIntsRef(scratchBytes.get(), scratchInts), NO_OUTPUT);
		}
		FST<Object> fst = builder.finish();
		
		System.out.format("Created FSA - NodeCount: %,d, bytes: %,d\n", fst.getNodeCount(), fst.ramBytesUsed());
		fst.save(fsaPath.toFile());
	}
}

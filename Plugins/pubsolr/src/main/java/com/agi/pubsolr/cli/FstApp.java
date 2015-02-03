package com.agi.pubsolr.cli;

import java.io.IOException;

import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.Builder;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.FST.INPUT_TYPE;
import org.apache.lucene.util.fst.NoOutputs;
import org.apache.lucene.util.fst.Outputs;
import org.apache.lucene.util.fst.Util;

public class FstApp {

	public static void main(String[] args) throws IOException {
		// Input values (keys). These must be provided to Builder in Unicode  sorted order!
		String inputValues[] = { "cat", "dog", "dogs" };
		final Outputs<Object> outputs = NoOutputs.getSingleton();
		final Object NO_OUTPUT = outputs.getNoOutput();
		Builder<Object> builder = new Builder<>(INPUT_TYPE.BYTE1, outputs);
		BytesRefBuilder scratchBytes = new BytesRefBuilder();
		IntsRefBuilder scratchInts = new IntsRefBuilder();
		for (int i = 0; i < inputValues.length; i++) {
			scratchBytes.copyChars(inputValues[i]);
			builder.add(Util.toIntsRef(scratchBytes.get(), scratchInts), NO_OUTPUT);
		}
		FST<Object> fst = builder.finish();
		
		Object value = Util.get(fst, new BytesRef("dog"));
	    System.out.println(value); // 7
	}

}

/**
 * 
 */
package com.agi.pubsolr.cli;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.lucene.util.fst.BytesRefFSTEnum;
import org.apache.lucene.util.fst.BytesRefFSTEnum.InputOutput;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.NoOutputs;
import org.apache.lucene.util.fst.Outputs;

/**
 * @author Rafis
 *
 */
public class FsaPrinter {
	private final Path fsaPath;
	
	public FsaPrinter(Path fsaPath) {
		this.fsaPath = fsaPath;
	}

	public void print() throws IOException {
		final Outputs<Object> outputs = NoOutputs.getSingleton();
		FST<Object> fst = FST.read(fsaPath.toFile(), outputs);
		
		BytesRefFSTEnum<Object> iterator = new BytesRefFSTEnum<>(fst);
	    while (iterator.next() != null) {
	      InputOutput<Object> mapEntry = iterator.current();
	      System.out.println(mapEntry.input.utf8ToString());
	    }
	}

}

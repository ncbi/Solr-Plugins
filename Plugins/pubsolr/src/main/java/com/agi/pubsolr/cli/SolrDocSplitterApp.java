package com.agi.pubsolr.cli;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class SolrDocSplitterApp {
	private static final int SIZE_LIMIT = 500_000_000;

	public static void main(String[] args) throws IOException {
		Path dir = FileSystems.getDefault().getPath("T:/data/pubone");
		try (BufferedReader reader = Files.newBufferedReader(dir.resolve("pm06.solr.xml"), StandardCharsets.UTF_8)) {
			String line;
			int charCounter = 0, fileCounter = 1;
			BufferedWriter writer = null;
			while((line = reader.readLine()) != null) {
				if (writer == null) {
					String fname = "chunk6-"+fileCounter+".solr.xml";
					System.out.format("Creating file: %s\n", fname);
					writer = Files.newBufferedWriter(dir.resolve(fname), StandardCharsets.UTF_8);
					if (fileCounter > 1)
						writer.write("<add>");
					fileCounter++;
					charCounter = 0;
				}
				writer.write(line);
				writer.newLine();
				charCounter += line.length();
				if (charCounter > SIZE_LIMIT && line.startsWith( " </doc>")) {
					writer.write("</add>");
					writer.close();
					writer = null;
				}
					
			}
			if (writer != null) {
				writer.close();
			}
		}
	}

}

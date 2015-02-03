package com.agi.pubsolr.cli;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import com.agi.pubsolr.util.StrUtils;

public class QueryLogApp {

	public static void main(String[] args) throws IOException {
		Path dir = FileSystems.getDefault().getPath("T:/data/pubone");
		try (BufferedWriter writer = Files.newBufferedWriter(dir.resolve("query.log"), StandardCharsets.ISO_8859_1);) {
			try (BufferedReader reader = Files.newBufferedReader(dir.resolve("pubmed_query.log"), StandardCharsets.ISO_8859_1)) {
				String line;
				int i = 0;
				while((line = reader.readLine()) != null) {
					int p = line.indexOf('|');
					if (p < 0)
						continue;
					p = line.indexOf('|', p+1);
					if (p < 0)
						continue;
					String query = StrUtils.normalizeSpaces(line.substring(p+1));
					if (query.length() == 0)
						continue;
					writer.write(query);
					writer.newLine();
					i++;
					if (i % 1000000 == 0)
						System.out.format("lines: %,d\n", i);
				}
				System.out.format("Total: %,d\n", i);
			}
		}
	}

}

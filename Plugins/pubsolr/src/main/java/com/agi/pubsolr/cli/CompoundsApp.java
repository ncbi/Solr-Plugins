package com.agi.pubsolr.cli;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CompoundsApp {
//	private static final int PHRASE_LIMIT = 3;
	
	private static Options createOptions() {
		Options options = new Options();
		options.addOption("c", "create", false, "Create FSA file from text file");
		options.addOption("p", "print", false, "Print FSA file to standard output");
		options.addOption("t", "text", true, "Text file");
		options.addOption("f", "fsa", true, "FSA file");
		options.addOption("m", "max-length", true, "Maximum phrase length (in words); 3 by default");
		return options;
	}

	public static void main(String[] args) throws IOException {
		CommandLineParser parser = new GnuParser();
		Options options = createOptions();
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);
			boolean create = line.hasOption('c');
			boolean print = line.hasOption('p');
			if (create) {
				String input = line.getOptionValue('t');
				Path fsaPath = FileSystems.getDefault().getPath(line.getOptionValue('f'));
				Path inputPath = FileSystems.getDefault().getPath(input);
				int maxLength = Integer.parseInt(line.getOptionValue('m', "3"));
				CompoundsFsaCreator creator = new CompoundsFsaCreator(inputPath, fsaPath, 2, maxLength);
				creator.create();
			}
			if (print) {
				Path fsaPath = FileSystems.getDefault().getPath(line.getOptionValue('f'));
				FsaPrinter printer = new FsaPrinter(fsaPath);
				printer.print();
			}
			if (!create && !print) {
				new HelpFormatter().printHelp("java -jar pubsolr-cli-x.x.x", options);
				System.out.println("Example: java -jar pubsolr-cli-x.x.x.jar -c -t compounds.txt -f compounds.fsa");
			}
		} catch (ParseException exp) {
			System.out.println("Unexpected exception:" + exp.getMessage());
		}
	}

}

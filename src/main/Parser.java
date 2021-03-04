package main;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class Parser {
	
	public static void main(String args[]) {
		
		/* Set Parser flags */
		boolean verbose = false;
		
		if (args.length == 0) {
			System.out.println("ERROR: Parser requires at least one input argument (path to file to parse)");
			System.exit(1);
		}
		
		/* Parse all input files */
		ArrayList<String> filenameList = new ArrayList<String>();
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--verbose")) {
				verbose = true;
//				try {
//					FileOutputStream f = new FileOutputStream("debug.txt");
//					System.setOut(new PrintStream(f));
//				} catch (FileNotFoundException e) {
//					System.out.println(e.toString());
//				}
				continue;
			}
			else if (args[i].equals("--help")) {
				System.out.printf("Usage: java parse.jar <options> <source files>\n"
						+ "where possible options include: \n"
						+ "--verbose: enable tracing/debugging output\n");
			}
			else if (args[i].startsWith("-")) {
				System.out.printf("Unrecognized option %s\n", args[i]);
				System.exit(1);
			}
			filenameList.add(args[i]);
		}
		
		for (int i = 0; i < filenameList.size(); i++) {
			String filename = filenameList.get(i);
			try {
				parser.MiniJavaParser parserObject = new parser.MiniJavaParser(
						new FileInputStream(filename));
				
				new parser.ParseException(filename, 0);		
				
				/* First perform lexical scanning of input file */
//				Scan scanner = new Scan();
//				scanner.performScan(filename, parserObject, verbose);
//				parserObject.ReInit(new FileInputStream(filename));
	
				if (verbose) {
					parserObject.enable_tracing();
				}
				else {
					parserObject.disable_tracing();
				}
				try {
					parserObject.Program();
				} catch (parser.ParseException e) {
					System.err.println(e.toString());
				}
//				System.setOut(new PrintStream(System.out));
				System.out.printf("%s, errors=%d\n", filename,
						parser.ParseException.getCount());
				/* Error reporting */
	//			parser.ParseException.printLexicalErrors(args[0]);
				
			} catch (FileNotFoundException e) {
				System.err.println(e.toString());
				continue;
			}		
		}
	}

}

package main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class Compiler { 
	
	public static String filename;

	
	public static void main(String args[]) {
		run(args);
	}
	
	public static void run(String args[]) {
		
		
		/* Set Parser flags */
		boolean verbose = false;
		
		if (args.length == 0) {
			System.out.println("ERROR: Parser requires at least one input argument (path to file to parse)");
			return;
		}
		
		/* Parse all input files */
		ArrayList<String> filenameList = new ArrayList<String>();
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--verbose")) {
				verbose = true;
	//			try {
	//				FileOutputStream f = new FileOutputStream("debug.txt");
	//				System.setOut(new PrintStream(f));
	//			} catch (FileNotFoundException e) {
	//				System.out.println(e.toString());
	//			}
				continue;
			}
			else if (args[i].equals("--help")) {
				System.out.printf("Usage: java parse.jar <options> <source files>\n"
						+ "where possible options include: \n"
						+ "\t--verbose: enable tracing/debugging output\n");
				continue;
			}
			else if (args[i].startsWith("-")) {
				System.out.printf("Unrecognized option %s\n", args[i]);
				return;
			}
			filenameList.add(args[i]);
		}
		
		for (String f: filenameList) {
			int num_errors = compile(f, false);
		}
				
	}
		
	public static int compile(String f, boolean verbose) {
		
//		for (int i = 0; i < filenameList.size(); i++) {
		filename = f;
		syntax.Program program;
//		filename = filenameList.get(i);
		try {

			Lexer.LexicalScanner scanner = new Lexer.LexicalScanner(new FileInputStream(filename));
			
			if (verbose) {
				scanner.enable_tracing();
			}
			else {
				scanner.disable_tracing();
			}
			
			/* First perform lexical scanning of input file */	
			LexicalScan.performScan(scanner);
//				if (LexicalScan.num_lexical_errors > 0) {
//					System.exit(1);
//				}
			
			semanticAnalysis.MiniJavaSemanticAnalyzer semanticsObject = new semanticAnalysis.MiniJavaSemanticAnalyzer(
					new FileInputStream(filename));
							
			if (verbose) {
				semanticsObject.enable_tracing();
			}
			else {
				semanticsObject.disable_tracing();
			}
			
			/* Next try parsing */
			// Gotta initialzie this to zero for parsing error
			// count to be right at end
			int num_errors;
			try {
				program = semanticsObject.Start();
			} catch (semanticAnalysis.ParseException e) {
				System.err.printf(e.toString());
				num_errors = LexicalScan.num_lexical_errors;
				num_errors += semanticAnalysis.ParseException.getCount();
				return num_errors;
			}
			
			if ((num_errors = semanticAnalysis.ParseException.getCount()) > 0) {
				int total_errors = num_errors + LexicalScan.num_lexical_errors;
				System.out.printf("%s, errors=%d\n", filename, total_errors);
				return total_errors;
			}
			
			// With abstract syntax tree, visit nodes and identify semantics errors
//				syntax.PrettyPrint pp = new syntax.PrettyPrint();
//				pp.visit(program);
			
			// Set up symbol table
			SymbolTableVisitor sb = new SymbolTableVisitor();
			sb.visit(program);
//				sb.printTable();
			
			// Perform semantic analysis
			TypeCheckVisitor tv = new TypeCheckVisitor();
			tv.visit(program);
			
			num_errors = errors.TypeErrorMsg.getCount();
			System.out.printf("%s, errors=%d\n", filename, num_errors);
			
			return num_errors;
			
		} catch (FileNotFoundException e) {
			System.out.println("Error: file " + filename + " not found");
			return -1;
		}
		

	}
}

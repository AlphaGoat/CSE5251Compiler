package main;

import java.io.PrintWriter;
import java.io.File;

// TODO: Just add parent class fields to child when making symbol table.
// 		 Makes lookups easier, and there is functionally no difference


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import errors.ParseErrorMsg;
import errors.SemanticErrorMsg;
import visitors.LazyIRTreeVisitor;
import visitors.SymbolTableVisitor;
import visitors.TypeCheckVisitor;
import errors.LexicalErrorMsg;

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
			int num_errors = compile(f, verbose);
		}
				
	}
		
	public static int compile(String f, boolean verbose) {
		
		filename = f;
		
		// Reinitialize Error Handlers
		
		LexicalErrorMsg.reInit();
		ParseErrorMsg.reInit();
		SemanticErrorMsg.reInit();

		try {

			semanticAnalysis.MiniJavaSemanticAnalyzer grammarParser = new semanticAnalysis.MiniJavaSemanticAnalyzer(
					new FileInputStream(filename));
			
			if (verbose) {
				grammarParser.enable_tracing();
			}
			else {
				grammarParser.disable_tracing();
			}
			
			/* First perform lexical scanning of input file */	
			LexicalScan.performScan(grammarParser);
			
			grammarParser.ReInit(new FileInputStream(filename));

			/* Next try parsing */
			// Gotta initialize this to zero for parsing error
			// count to be right at end
			int num_errors;
			syntax.Program program;
			try {
				program = grammarParser.Start();
			} catch (semanticAnalysis.ParseException e) {
//				System.err.printf(e.toString());
				ParseErrorMsg.complain(e.getMessage());
				num_errors = LexicalScan.num_lexical_errors;
				num_errors += ParseErrorMsg.getCount();
//				num_errors += semanticAnalysis.ParseException.getCount();
				return num_errors;
			}
			
			// If syntax errors are reported, stop compiler and print number of errors
			int total_lex_syntax_errors = ParseErrorMsg.getCount() + LexicalErrorMsg.getCount();
			if ( total_lex_syntax_errors > 0 ) {
				System.out.printf("%s, errors=%d\n", filename, total_lex_syntax_errors);
				return total_lex_syntax_errors;
			}

			// Set up symbol table
			SymbolTableVisitor sb = new SymbolTableVisitor();
			sb.visit(program);
			sb.addFieldsToChild(); // Adds parent fields to child classes. Makes things easier later on
			
			// Perform semantic analysis
			TypeCheckVisitor tv = new TypeCheckVisitor();
			tv.visit(program);
			
			int total_semantic_errors = SemanticErrorMsg.getCount();
			if ( total_semantic_errors > 0 ) {
				System.out.printf("%s, errors=%d\n", filename, total_semantic_errors);
				return total_semantic_errors;
			}
			
			num_errors = errors.SemanticErrorMsg.getCount();
						
			// Construct method fragments
			LazyIRTreeVisitor lazy = new LazyIRTreeVisitor();
			lazy.visit(program);
			
			// Print to debug file
			PrintWriter writer; 
			File debugFile = new File("/home/alphagoat/eclipse-workspace/MiniJavaCompiler/logs/log.txt");
			if (verbose) {
				try {
					debugFile.createNewFile();				
					FileOutputStream debugFileStream = new FileOutputStream(debugFile);
					PrintStream printStream = new PrintStream(debugFileStream);
					writer = new PrintWriter(printStream);
					
	//				 print out function fragments in IR Tree
					for (visitors.MethodFragment fragment: lazy.methodFragments) {
						tree.Stm body = fragment.getBody();
						tree.TreePrint.print(writer, body);
					}
				} catch (IOException e) {
					;
				}
			}

			List<Sparc.SparcFrame> frameList = new ArrayList<Sparc.SparcFrame>();
			ArrayList<List<tree.Stm>> methodTraces = new ArrayList<List<tree.Stm>>();
			// Canonicalize method fragments
			for (visitors.MethodFragment fragment: lazy.methodFragments) {
				tree.Stm body = fragment.getBody();
				methodTraces.add(canon.Main.transform(body));
				frameList.add((Sparc.SparcFrame) fragment.getMethodFrame());
			}
			
//			if (verbose) {
//				try {
//					debugFile.createNewFile();				
//					FileOutputStream debugFileStream = new FileOutputStream(debugFile);
//					PrintStream printStream = new PrintStream(debugFileStream);
//					writer = new PrintWriter(printStream);
//					
//					// print out canonicalized function fragments
//					for (List<tree.Stm> trace: methodTraces) {
//						for (tree.Stm statement: trace) {
//							tree.TreePrint.print(writer, statement);
//						}
//					}
//				} catch (IOException e) {
//					;
//				}	
//			}
//			
			// print out assembly
			List <List<assem.Instruction>> fragmentInstructions = new ArrayList<List<assem.Instruction>>();
			List <Sparc.SparcFrame> newFrameList = new ArrayList<Sparc.SparcFrame>();
			for (int i = 0; i < methodTraces.size(); i++) {
				
				Sparc.CodeGen codeGenerator = new Sparc.CodeGen(methodTraces.get(i),
																frameList.get(i));
				fragmentInstructions.add(codeGenerator.getInstructionList());
				newFrameList.add(codeGenerator.getFrame());
			}
			frameList = new ArrayList<Sparc.SparcFrame>(newFrameList);
			newFrameList.clear();
			
			// For each of the fragments, perform register allocation
			List<List<assem.Instruction>> newFragmentInstructions = new ArrayList<List<assem.Instruction>>();
			for (int i = 0; i < fragmentInstructions.size(); i++) {
				
				Sparc.RegisterAllocation registerAllocater = new Sparc.RegisterAllocation(frameList.get(i),
																						  fragmentInstructions.get(i));
				newFragmentInstructions.add(registerAllocater.iterateThroughTemps());
				newFrameList.add(registerAllocater.getFrame());
			}
			fragmentInstructions = new ArrayList<List<assem.Instruction>>(newFragmentInstructions);
			newFragmentInstructions.clear();
//			newFragmentInstructions.clear();
			frameList = new ArrayList<Sparc.SparcFrame>(newFrameList);
			newFrameList.clear();
//			newFrameList.clear();
			
			// Add prelude and epilog to all instructions 
			for (int i = 0; i < fragmentInstructions.size(); i++) {
				if (i == 0) {
					/* first fragment is main, don't need to allocate stack frame for it */
					List<assem.Instruction> mainInstructions = new ArrayList<assem.Instruction>();
					mainInstructions = fragmentInstructions.get(i);
					assem.Instruction mainPrelude = new assem.LabelInstruction(
							frameList.get(0).name.label);
					
					List<assem.Instruction> mainEpilog = new Sparc.MainEpilogGen().epilog();
					mainInstructions.add(0, mainPrelude);
					
					// Remove weird jump instruction that is created during canonicalization
					mainInstructions.remove(mainInstructions.size()-1);
					
					mainInstructions.addAll(mainInstructions.size(), mainEpilog);
					newFragmentInstructions.add(mainInstructions);

				}
				
				else {
					List <assem.Instruction> methodInstructions = new ArrayList<assem.Instruction>();
					methodInstructions = fragmentInstructions.get(i);
					List<assem.Instruction> methodPrelude = new Sparc.PreludeGen(frameList.get(i)).prelude();
					List<assem.Instruction> methodEpilog = new Sparc.EpilogGen(frameList.get(i)).epilog();
					methodInstructions.addAll(0, methodPrelude);
					methodInstructions.addAll(methodEpilog);
					newFragmentInstructions.add(methodInstructions);
				}
			}
			fragmentInstructions = newFragmentInstructions;
//			newFragmentInstructions.clear();
			
			/* Generate final code output */
			StringBuilder codeOutput = new StringBuilder();
			for (int i = 0; i < fragmentInstructions.size(); i++) {
				Sparc.SparcFrame currFrame = frameList.get(i);
				for (assem.Instruction instr : fragmentInstructions.get(i)) {
					codeOutput.append(instr.format(currFrame.getMap()));
					codeOutput.append("\n");
				}
			}
			// Add Header to file
			codeOutput.insert(0,"start:\n");
			codeOutput.insert(0, ".align 4\n");
			codeOutput.insert(0, ".global start\n");
			codeOutput.insert(0, ".section \".text\"\n");
//			codeOutput.insert(0, ".set SYS_exit, 1\n"
//				               + ".set SP_TRAP_LINUX, 0x90\n"
//							   + ".macro exit_program\n"
//				               + "  set stdout, %o0\n"
//				               + "  call fflush\n"
//				               + "  nop\n"
//				               + "  clr %o0\n"
//				               + "  set SYS_exit, %g1\n"
//				               + "  ta SP_TRAP_LINX\n"
//				               + "  .endm\n");
//			
			writeToFile(codeOutput);
			
			System.out.printf("%s, errors=%d\n", filename, num_errors);
					
			return 0;
			
		} catch (FileNotFoundException e) {
			System.err.println("Error: file " + filename + " not found");
			return 0;
		}		
	}
	
	public static void writeToFile(StringBuilder codeContent) {
		// Writes contents of instructions to file	
		String fileToWrite;
//		if (filename.contains(".")) {
//			fileToWrite = filename.substring(
//					0, filename.lastIndexOf('.'));
//			fileToWrite = filename.concat(".s");
//		}
//		else {
//			fileToWrite = filename.concat(".s");
//		}
		fileToWrite = removeExtension(filename);
		fileToWrite = fileToWrite.concat(".s");	
		
		try {
			FileWriter instructionWriter = new FileWriter(fileToWrite);
			instructionWriter.append(codeContent);
			instructionWriter.close();
		}
		catch (IOException e) {
			System.err.println(e);
		}		
	}
	
//	public static String removeExtension(String fileName) {
//		// Looked up here:
//		// https://www.quickprogrammingtips.com/java/how-to-remove-extension-from-filename-in-java.html
//		if (fileName.indexOf(".") > 0) {
////			return fileName.substring(0, fileName.lastIndexOf("."));
//			return filename.replace("$(.+)\.\\w+", "\1");
//		}
//		else {
//			return fileName;
//		}
//	}
	
	   public static String removeExtension( String in )
	   // From here:
	   // https://stackoverflow.com/questions/3449218/remove-filename-extension-in-java
	   {
	       int p = in.lastIndexOf(".");
	       if ( p < 0 )
	           return in;

	       int d = in.lastIndexOf( File.separator );

	       if ( d < 0 && p == 0 )
	           return in;

	       if ( d >= 0 && d > p )
	           return in;

	       return in.substring( 0, p );
	   }
}

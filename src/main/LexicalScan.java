package main;
//import MiniJavaParserConstants.EOF;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class LexicalScan {
	
	static int num_lexical_errors;
	
//    public static void main(String args[]) {
//        String filename = args[0];
//        parser.MiniJavaParser lexer;
//        try {
//        	FileInputStream inputStream = new FileInputStream(filename);
//        	lexer = new parser.MiniJavaParser(inputStream);
//        	boolean verbose = false;
//            performScan(filename, lexer, verbose);
//			System.out.printf("%s, errors=%d\n", filename,
//					parser.TokenMgrError.getCount());
//       } catch (FileNotFoundException e) {
//        	System.err.println(e);
//       	System.exit(1);
//        }
//    }

    public static void performScan(Lexer.LexicalScanner lexer) {
    		
    		num_lexical_errors = 0;
            Lexer.Token t = lexer.getNextToken();

            // keep track of the number of errors, and
            // where they occurred in the file
            ArrayList<String> characters = new ArrayList<String>();
            ArrayList<Integer> line_numbers = new ArrayList<Integer>();
            ArrayList<Integer> col_numbers = new ArrayList<Integer>();

            while (t.kind != Lexer.LexerConstants.EOF) {
                try {
                    t = lexer.getNextToken();

                    if (t.kind == Lexer.LexerConstants.NOT_RECOGNIZED) {
                    	num_lexical_errors++;
                        characters.add(t.image);
                        line_numbers.add(t.beginLine);
                        col_numbers.add(t.beginColumn);
//                        System.err.printf("%s:%d.%d: Lexical Error -- illegal character '%s'\n",
//                                filename, t.beginLine,
//                                t.beginColumn, t.image);
                        System.err.println(Compiler.filename + ":" + t.beginLine + "." + t.beginColumn 
                        		+ ": Lexical Error -- illegal character " + t.image + "\n");
                    }

                }
                catch (Lexer.TokenMgrError e) {
                    System.err.println(e.getMessage());
                    continue;
                }
            }
    }
}
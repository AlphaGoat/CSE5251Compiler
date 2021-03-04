package main;
//import MiniJavaParserConstants.EOF;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class Scan {
	
    public static void main(String args[]) {
        String filename = args[0];
        parser.MiniJavaParser lexer;
        try {
        	FileInputStream inputStream = new FileInputStream(filename);
        	lexer = new parser.MiniJavaParser(inputStream);
        	boolean verbose = false;
            performScan(filename, lexer, verbose);
			System.out.printf("%s, errors=%d\n", filename,
					parser.TokenMgrError.getCount());
        } catch (FileNotFoundException e) {
        	System.err.println(e);
        	System.exit(1);
        }
    }

    public static void performScan(String filename, parser.MiniJavaParser lexer, boolean trace) {
 
    	
//            FileInputStream inputStream = new FileInputStream(filename);

//            lexer = new parser.MiniJavaParser(inputStream);
            if (trace) {
                lexer.enable_tracing();
            }
            else {
                lexer.disable_tracing(); // for parsing only
            }

            parser.Token t = lexer.getNextToken();

            // keep track of the number of errors, and
            // where they occurred in the file
            int num_errors = 0; 
            ArrayList<String> characters = new ArrayList<String>();
            ArrayList<Integer> line_numbers = new ArrayList<Integer>();
            ArrayList<Integer> col_numbers = new ArrayList<Integer>();

            while (t.kind != parser.MiniJavaParserConstants.EOF) {
                try {
                    t = lexer.getNextToken();

                    if (t.kind == parser.MiniJavaParserConstants.NOT_RECOGNIZED) {
                        characters.add(t.image);
                        line_numbers.add(t.beginLine);
                        col_numbers.add(t.beginColumn);
//                        System.err.printf("%s:%d.%d: Lexical Error -- illegal character '%s'\n",
//                                filename, t.beginLine,
//                                t.beginColumn, t.image);
                        throw new parser.TokenMgrError(filename + ":" + t.beginLine + "." + t.beginColumn 
                        		+ ": Lexical Error -- illegal character " + t.image + "\n", 0);
                    }

                }
                catch (parser.TokenMgrError e) {
                    System.err.println(e.getMessage());
                    continue;
                }
            }
    }
}
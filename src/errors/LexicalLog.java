package errors;

import java.util.ArrayList;

public class LexicalLog {
    // keep track of the number of errors, and
    // where they occurred in the file
	static String filename;
    static int num_lexical_errors = 0; 
    static ArrayList<String> characters = new ArrayList<String>();
    static ArrayList<Integer> line_numbers = new ArrayList<Integer>();
    static ArrayList<Integer> col_numbers = new ArrayList<Integer>();
    
    public LexicalLog(String filename) {
    	this.filename = filename;
    }

    public static void printLexicalErrors(String filename) {
        // Print out all errors to command line
        System.out.printf("filename=%s, errors=%d\n", filename, num_lexical_errors);
        for (int i = 0; i < num_lexical_errors; i++) {
            System.out.printf("%s:%d.%d: ERROR -- illegal character %s\n",
                    filename, line_numbers.get(i), col_numbers.get(i),
                    characters.get(i));
        }
    }
    
    public static void addLexicalError(String character, int line, int column) {
    	num_lexical_errors++;
    	characters.add(character);
    	line_numbers.add(line);
    	col_numbers.add(column);
    }

}

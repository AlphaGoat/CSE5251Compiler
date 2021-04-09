package errors;

import main.Compiler;

public class ParseErrorMsg implements ErrorMsg{
	static int errorCount = 0;
	final static String errorType = "Syntax Error";
	
	public static void complain(String msg) {
		errorCount++;
		System.err.println(msg);
	}
	
	public static void complain(int l, int c, String msg) {
		errorCount++;
		String line = String.valueOf(l);
		String col = String.valueOf(c);
		msg = Compiler.filename + ":" + line + "." + col + ":" + "Syntax Error -- " + msg;
		System.err.println(msg);
	}
	
	public static int getCount() {
		return errorCount;
	}
	
	public static void reInit() {
		errorCount = 0;
	}
}

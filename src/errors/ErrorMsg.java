package errors;

import main.Compiler;

public interface ErrorMsg {
	final static String errorType = "Error";
	
	public static void complain(String msg) {
		System.err.println(msg);
	}
	
	public static void complain(int l, int c, String msg) {
		String line = String.valueOf(l);
		String col = String.valueOf(c);
		msg = Compiler.filename + ":" + line + "." + col + ":" + errorType + msg;
		System.err.println(msg);
	}
}

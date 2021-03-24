package errors;

import main.Compiler;

public class TypeErrorMsg {
	static int errorCount = 0;
	public static void complain(String msg) {
		errorCount++;
		System.err.println(msg);
	}
	
	public static void complain(int l, int c, String msg) {
		errorCount++;
		String line = String.valueOf(l);
		String col = String.valueOf(c);
		msg = Compiler.filename + ":" + line + "." + col + ":" + "TypeError -- " + msg;
		System.err.println(msg);
	}
	
	public static int getCount() {
		return errorCount;
	}
}


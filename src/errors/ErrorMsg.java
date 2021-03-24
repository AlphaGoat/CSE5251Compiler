package errors;

import main.Compiler;

public class ErrorMsg {
	static int errorCount = 0;
	
	public void complain(String msg) {
		errorCount++;
		System.err.println(msg);
	}
	
	public void complain(int l, int c, String msg) {
		errorCount++;
		String line = String.valueOf(l);
		String col = String.valueOf(c);
		msg = Compiler.filename + ":" + line + "." + col + ":" + "ERROR -- " + msg;
		System.err.println(msg);
	}
	
	public int getCount() {
		return errorCount;
	}
}

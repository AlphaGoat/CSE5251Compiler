package errors;

import java.util.ArrayList;
import java.util.regex.Pattern;

import main.Compiler;

public class ParseErrorMsg implements ErrorMsg{
	static int errorCount = 0;
	final static String errorType = "Syntax Error";
	
	public static void complain(String msg) {
		errorCount++;
		/* Have to do some string manipulation to get 
		 * line number and column number
		 */
		String lineNumber = "";
		String colNumber = "";
		boolean lineFound = false, colFound = false;
		char[] tokens = msg.toCharArray();
		ArrayList<Character> newMsg = new ArrayList<Character>();
		for (int i = 0; i < tokens.length; i++) {
			char c = tokens[i];
			try {
				int n = Integer.parseUnsignedInt(String.valueOf(c));
				if (!(lineFound)) {
					lineNumber = String.valueOf(c);
					lineFound = true;
					if (i < tokens.length - 2) {
						int j = 1;
						char next = tokens[i+j];
						try {
							while(true) {
								Integer.parseUnsignedInt(String.valueOf(next));
								lineNumber += String.valueOf(next);
								j++;
								next = tokens[i+j];
							}
						} catch(Exception e) {
							i = i+j-1;
							continue;
						}
					}
				} else if (!(colFound)) {
					colNumber = String.valueOf(c);
					colFound = true;
					if (i < tokens.length - 2) {
						int j = 1;
						char next = tokens[i+j];
						try {
							while(true) {
								Integer.parseUnsignedInt(String.valueOf(next));
								colNumber += String.valueOf(next);
								j++;
								next = tokens[i + j];
							}
						} catch(Exception e) {
//							i = i + j;
							break;
						}
					}
				} else {
					newMsg.add(c);
				}
			} catch (Exception e) {
				newMsg.add(c);
				continue;
			}
		}
		StringBuilder sb = new StringBuilder();
		for (Character ch : newMsg) {
			sb.append(ch);
		}
		msg = sb.toString();
		msg = msg.replaceAll("[\n\r]", "");
		msg = msg.replace("at", "");
		msg = msg.replace("line", "");
		msg = msg.replace("column", "");
//		msg = msg.replace("\\d$", "");
//		msg = msg.replace("\\d$", ".");
		msg = msg.replace("  , ", "");
		msg = msg.replace(".", ". ");
		msg = Compiler.filename + ":" + lineNumber
				+ "." + colNumber + ":" + "Syntax Error -- " + msg;
		System.err.println(msg);
	}
	
	public static void complain(int l, int c, String msg) {
		errorCount++;
		String line = String.valueOf(l);
		String col = String.valueOf(c);
		msg = Compiler.filename + ":" + line + "." + col + ":" + "Syntax Error -- " + msg;
		System.err.println(msg);
	}
	
	public static void complain(String msg, boolean throwaway) {
		msg = Compiler.filename + ":Syntax Error -- " + msg;
		System.err.println(msg);
	}
	
	public static int getCount() {
		return errorCount;
	}
	
	public static void reInit() {
		errorCount = 0;
	}
}

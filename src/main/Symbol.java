package main;

import java.util.Dictionary;
import java.util.Hashtable;

import syntax.Identifier;

import java.util.Enumeration;
import java.util.ArrayList;

public class Symbol {
	
	private String name;
	private Symbol(String n) { name = n; }
	private static Dictionary<String, Symbol> dict = new Hashtable<String, Symbol>();
	
	public String toString() { return name; }
	public static Symbol symbol(String n) {
		String u = n.intern();
		Symbol s = (Symbol) dict.get(u);
		if (s == null) { s = new Symbol(u); dict.put(u, s); }
		return s;
	}

}


//class MainBinding {
//	String argName;
//	ArrayList<String> id;
//	
//	public MainBinding(String a, ArrayList<String> i) {
//		argName = a; id = i;
//	}
	
//}

//class ClassBinding {
//	String name;
//	Table fields;
//	Table methods;
	
//	public void addMethod(String name, MethodBinding b) {
//		Symbol key = Symbol.symbol(name);
//		methods.put(key, b);
//	}
	
//	public void addField(Identifier id, syntax.Type t) {
//		String s = id.toString();
//		Symbol key = Symbol.symbol(s);
//		fields.put(key, t);
//	}
	
//	public String getId() {
//		return name;
//	}
//}




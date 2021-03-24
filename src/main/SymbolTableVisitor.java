package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class SymbolTableVisitor implements syntax.SyntaxTreeVisitor<Void>{
	public static HashMap<Symbol, Binding> symbolTable;
	private static ClassBinding currClass = null;
	private static MethodBinding currMethod = null;
	private static int currParam = 0;
	
	public Void visit(syntax.Program n) {
		
		// Initialize symbol table
		symbolTable = new HashMap<Symbol, Binding>();
		
		if (n == null) {
			// TODO: Print some error (Handled in Parsing)
		}
		else if (n.m == null) {
			//TODO: print some error (NO MAIN CLASS!!! (Should be caught by parser though)
		}
		else {
			n.m.accept(this);
			for (syntax.ClassDecl c: n.cl) c.accept(this);
		}
		return null;
	}
	
	public Void visit(syntax.MainClass m) {
		
		String name = m.i1.toString();
		String argName = m.i2.toString();
		
		Symbol key = Symbol.symbol(m.i1.toString());
		
		
		MainBinding b = new MainBinding(key, argName);
		symbolTable.put(key, b);

		return null;	
	}
	
	public Void visit(syntax.SimpleClassDecl c) {
		String n = c.i.toString(); // Class name
		
		Symbol key = Symbol.symbol(n);
		ClassBinding cb = new ClassBinding(key);
		currClass = cb;
		
		for (int i = 0; i < c.fields.size(); i++) {
			c.fields.get(i).accept(this);
		}
		
		for (int j = 0; j < c.methods.size(); j++) {
			c.methods.get(j).accept(this);
		}
		
		// Add to symbol table
		symbolTable.put(key, cb);
		currClass = null;
		
		return null;
	}
	
	public Void visit(syntax.ExtendingClassDecl c) {
		String n = c.i.toString(); // Class name
		Symbol key = Symbol.symbol(n);
		
		/* Create class binding and fill it with fields and methods */
		String p = c.j.toString();
		Symbol parent = Symbol.symbol(p);
		ClassBinding cb = new ClassBinding(key, parent);
		currClass = cb;
		
		for (int i = 0; i < c.fields.size(); i++) {
			c.fields.get(i).accept(this);
		}
		
		for (int j = 0; j < c.methods.size(); j++) {
			c.methods.get(j).accept(this);
		}
		
		// Add to symbol table
		symbolTable.put(key, cb);
		currClass = null;
		
		return null;
	}
	

	public Void visit(syntax.MethodDecl m) {
		String n = m.i.toString(); // method name
		Symbol key = Symbol.symbol(n);
		
		// Check to see that this method has not been declared
		// in this class yet
		if (currClass.methods.get(key) != null) {
			int line = m.i.lineNumber;
			int col = m.i.columnNumber;
			errors.TypeErrorMsg.complain(line, col, n + " is already defined as a method in " + 
						currClass.getId().toString());
		}
		
		syntax.Type r = m.t;
			
		/* Create a method binding and fill it with params and locals */	
		MethodBinding mb = new MethodBinding(key, r);
		currMethod = mb;
		
		/* Formal arguments */
		for (int i = 0; i < m.fl.size(); i++) {
			m.fl.get(i).accept(this);
		}
		
		/* Local vars */
		for (int j = 0; j < m.locals.size(); j++) {
			m.locals.get(j).accept(this);
		}
		
		// Add to class binding
		currClass.addMethod(key, mb);
		currMethod = null;
		currParam = 0;
		
		return null;
	}
	
	public Void visit(syntax.FormalDecl formal) {
		String fName = formal.i.toString();
		Symbol key = Symbol.symbol(fName);
		
		ParamBinding t = new ParamBinding(key, formal.t, currParam);

		if (!currMethod.addParam(key, t)) {
			int line = formal.i.lineNumber;
			int col = formal.i.columnNumber;
			errors.TypeErrorMsg.complain(line, col, fName + " is already defined as argument in " +
					currMethod.getId().toString());
		}
		else {
			currParam++;
		}
				
		return null;
	}
	
	public Void visit(syntax.LocalDecl l) {
		String lName = l.i.toString();
		Symbol key = Symbol.symbol(lName);
		VarBinding t = new VarBinding(key, l.t);

		if (!currMethod.addLocal(key, t)) {
			int line = l.i.lineNumber;
			int col = l.i.columnNumber;
			errors.TypeErrorMsg.complain(line, col, lName + " is already defined in " + currMethod.getId());
		}
				
		return null;
	}
	
	public Void visit(syntax.FieldDecl f) {
		// Return type of variable
//		syntax.Type t = f.t.accept(this);
		String fname = f.i.toString();
		Symbol key = Symbol.symbol(fname);
		VarBinding t = new VarBinding(key,f.t);

		if (!currClass.addField(key, t)) {
			int line = f.i.lineNumber;
			int col = f.i.columnNumber;
			errors.TypeErrorMsg.complain(line, col, fname + " is already defined in " + currClass.getId());
		}
				
		return null;
	}
	
	// We don't need to visit these nodes to fill out the
	// symbol table. They're just included to complete the interface
	public Void visit(syntax.IdentifierType t) {
		
		return null;
	}
	
	public Void visit(syntax.IntArrayType t) {
		return null;
	}
	
	public Void visit(syntax.BooleanType t) {
		return null;
	}
	
	public Void visit(syntax.IntegerType t) {
		return null;
	}
	
	public Void visit(syntax.VoidType t) {
		return null;
	}
	
	public Void visit(syntax.Block s) {
		return null;
	}
	
	public Void visit(syntax.If s) {
		return null;
	}
	
	public Void visit(syntax.While s) {
		return null;
	}
	
	public Void visit(syntax.Print s) {
		return null;
	}
	
	public Void visit(syntax.Assign e) {
		return null;
	}
	
	public Void visit(syntax.ArrayAssign e) {
		return null;
	}
	
	public Void visit(syntax.And e) {
		return null;
	}
	
	public Void visit(syntax.LessThan e) {
		return null;
	}
	
	public Void visit(syntax.Plus e) {
		return null;
	}
	
	public Void visit(syntax.Minus e) {
		return null;
	}
	
	public Void visit(syntax.Times e) {
		return null;
	}
	
	public Void visit(syntax.ArrayLookup e) {
		return null;
	}
	
	public Void visit(syntax.ArrayLength e) {
		return null;
	}
	
	public Void visit(syntax.Call e) {
		return null;
	}
	
	public Void visit(syntax.IntegerLiteral e) {
		return null;
	}
	
	public Void visit(syntax.True e) {
		return null;
	}
	
	public Void visit(syntax.False e) {
		return null;
	}
	
	public Void visit(syntax.IdentifierExp e) {
		return null;
	}
	
	public Void visit(syntax.This e) {
		return null;
	}
	
	public Void visit(syntax.NewArray e) {
		return null;
	}
	
	public Void visit(syntax.NewObject e) {
		return null;
	}
	
	public Void visit(syntax.Not e) {
		return null;
	}
	
	public Void visit(syntax.Identifier i) {
		return null;
	}
	
	public void printTable() {
		new PrintSymbolTable();
	}
		
}

class Binding {
	Symbol name;
	int line;
	int col;
	
	public String getName() {
		return name.toString();
	}
}

class MainBinding extends Binding {
	String arg;
	
	public MainBinding(Symbol n, String a) { 
		name = n;
		arg = a; 
	}
}

class VarBinding extends Binding {
	syntax.Type varType;
	
	public VarBinding() {};
	public VarBinding(Symbol n, syntax.Type t) { name = n; varType = t; }
	
	public String toString() {
		if (varType instanceof syntax.IntegerType) {
			return "int";
		}
		else if ( varType instanceof syntax.IntArrayType ) {
			return "int[] ";
		}
		else if ( varType instanceof syntax.BooleanType ) {
			return "boolean";
		}
		else if ( varType instanceof syntax.IdentifierType ) {
			String id = ((syntax.IdentifierType) varType).s;
			return id;
		}
		else {
			return "void";
		}
	}
}

class ParamBinding extends VarBinding {
	final int index; // index of param in method call
	public ParamBinding(final Symbol n, final syntax.Type t, final int i) { 
		super(n, t);
		index = i;
	}
}

class ClassBinding extends Binding {
	Symbol parent = null;
	
	HashMap<Symbol, Binding> fields;
	HashMap<Symbol, Binding> methods;
	
	public ClassBinding(Symbol n) { 
		name = n;
		fields = new HashMap<Symbol, Binding>();
		methods = new HashMap<Symbol, Binding>();
		}
	
	public ClassBinding(Symbol n, Symbol p) { 
		name = n; 
		parent = p; 
		fields = new HashMap<Symbol, Binding>();
		methods = new HashMap<Symbol, Binding>();
	}
	
	public boolean addMethod(Symbol mName, MethodBinding b) {
//		Symbol key = Symbol.symbol(mname);
		
		// See if method name has already been added to the
		// symbol table
		if (methods.get(mName) != null) {
			return false;
		}
		else {
			methods.put(mName, b);
			return true;
		}
	}
	
	public boolean addField(Symbol fName, VarBinding t) {
//		Symbol key = Symbol.symbol(fname);
		
		if (fields.get(fName) != null) {
			return false;
		}
		else {
			fields.put(fName, t);
			return true;
		}
	}
	
	public Symbol getId() {
		return name;
	}
}

class MethodBinding extends Binding {
	syntax.Type rtype;
	ArrayList<Symbol> paramList;
	HashMap<Symbol, Binding> params;
	HashMap<Symbol, Binding> locals;
	
	public MethodBinding(Symbol n, syntax.Type r) { 
		name = n; 
		rtype = r;
		paramList = new ArrayList<Symbol>();
		params = new HashMap<Symbol, Binding>();
		locals = new HashMap<Symbol, Binding>();
	}		
	
	public boolean addParam(Symbol pName, VarBinding t) {
//		Symbol key = Symbol.symbol(pname);
		
		// Do a check to see if the parameter has already
		// been added to the symbol table
		if (params.get(pName) != null) {
			paramList.add(pName);
			return false;
		}
		
		else {
			params.put(pName, t);
			paramList.add(pName);
			return true;
		}
	}
	
	public boolean addLocal(Symbol lName, VarBinding t) {
//		Symbol key = Symbol.symbol(lname);
		
		// Check if symbol has already been added to the 
		// symbol table for method
		if ((params.get(lName) != null) || (locals.get(lName) != null)) {
			return false;
		}
		else {
			locals.put(lName, t);
			return true;
		}
	
	}
	
	public Symbol getId() {
		return name;
	}
}

class PrintSymbolTable {
	
	public PrintSymbolTable() {
		for (Map.Entry<Symbol, Binding> entry: 
			SymbolTableVisitor.symbolTable.entrySet() ) {
			
			String className = entry.getKey().toString();
			Binding cb = entry.getValue();
			
			if (cb instanceof MainBinding) {
				String arg = ((MainBinding) cb).arg;
				System.out.println("Class " + className + "(Arg: " + arg + ")\n");
			}
			
			else {			
				System.out.println("Class " + className + ":");			
				printClassBinding((ClassBinding) entry.getValue());
			}
		}
	}
	
	private void printClassBinding(ClassBinding b) {
		System.out.println("\tFields: ");
		for (Map.Entry<Symbol, Binding> entry: 
			b.fields.entrySet()) {
			String fname = entry.getKey().toString();
			String type = ((VarBinding) entry.getValue()).toString();
			System.out.println("\t\t" + type + " " + fname);
		}
		System.out.println("\tMethods: ");
		for (Map.Entry<Symbol, Binding> entry:
			b.methods.entrySet()) {
			String mname = entry.getKey().toString();
			MethodBinding mb = (MethodBinding) entry.getValue();
			String rtype = mb.rtype.toString();
			
			System.out.println("\t\t" + rtype + " " + mname + ":");
			
			printMethodBinding(mb);
		}
		
	}
	
	private void printMethodBinding(MethodBinding b) {
		System.out.println("\t\t\tParams: ");
		for (Map.Entry<Symbol, Binding> entry:
			b.params.entrySet()) {
			String pname = entry.getKey().toString();
			String type = ((VarBinding) entry.getValue()).toString();
			System.out.println("\t\t\t\t" + type + " " + pname);
		}
		System.out.println("\t\t\tLocals: ");
		for (Map.Entry<Symbol, Binding> entry:
			b.locals.entrySet()) {
			String lname = entry.getKey().toString();
			String type = ((VarBinding) entry.getValue()).toString();
			System.out.println("\t\t\t\t" + type + " " + lname);
		}
	}
	
}





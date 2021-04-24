package visitors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import errors.SemanticErrorMsg;
import main.Symbol;

public class TypeCheckVisitor implements syntax.SyntaxTreeVisitor<TypeSignature>{

	ClassBinding currClass = null;
	ClassBinding currParent = null;
	MethodBinding currMethod = null;
	ArrayList<Symbol> currInheritanceStack = new ArrayList<Symbol>();
	
	HashMap<Symbol, Boolean> currInitializationTable = new HashMap<Symbol, Boolean>();
	
//	private void buildInitializationStack(Symbol key) {
//		
//		
//	}
		
	private ArrayList<Symbol> buildInheritanceStack(Symbol key) {
		// Build the inheritance stack of a given class
		ArrayList<Symbol> inheritanceStack = new ArrayList<Symbol>();
		inheritanceStack.add(key);
		Binding b;
		ClassBinding cb = (ClassBinding) SymbolTableVisitor.symbolTable.get(key);
		int curr_index = 0;
		while ( cb.parent != null ) {
			
			if ( (b = SymbolTableVisitor.symbolTable.get(cb.parent)) == null ) {
				break;
			}
			
//			ClassBinding next = (ClassBinding) SymbolTableVisitor.symbolTable.get(cb.parent);
			ClassBinding next = (ClassBinding) b;
			if ( inheritanceStack.indexOf(next.name) < curr_index 
					&& inheritanceStack.indexOf(next.name) > -1) {
				break;
			}
			inheritanceStack.add(next.name);
			cb = next;
			curr_index++;
		}
		
		return inheritanceStack;		
	}
	
	private TypeSignature searchScope(Symbol key) {
		syntax.Type t;
		
//		ClassBinding parentClass;
		
//		if (key.toString().equals("r")) {
//			int r = 2;
//			System.out.println("inheritance stack: ");
//			for (int i = 0; i < inheritanceStack.size(); i++) {
//				System.out.println(inheritanceStack.get(i).toString());
//			}
//		}
		
		if ( currMethod != null ) {
			
			if ( currMethod.locals.get(key) == null &&
					currMethod.params.get(key) == null ) {
				
				if ( currClass.fields.get(key) != null ) {
					VarBinding var = (VarBinding) currClass.fields.get(key);
					t = var.varType;
					
					TypeSignature tw = t.accept(this);
					
					return tw;
				}
				
//				else {
//					
//					// Go through inheritance stack and see whether symbol is a field
//					for (int i = 0; i < currInheritanceStack.size(); i++ ) {
//						Symbol nextClassKey = currInheritanceStack.get(i);
//						ClassBinding nextClass = (ClassBinding) SymbolTableVisitor.symbolTable.get(nextClassKey);
//						
//						
//						if ( nextClass.fields.get(key) != null ) {
//							VarBinding var = (VarBinding) nextClass.fields.get(key);
//							t = var.varType;
//							
//							TypeSignature tw = t.accept(this);
//							return tw;
//						}
//					}
//					
//					// If none of classes in inheritance chain have field, return null
//					return new NullTypeSignature();
//					
//				}
				else {
					return new NullTypeSignature();
				}
				
			}
				
			else if (currMethod.locals.get(key) == null) {
				VarBinding var = (VarBinding) currMethod.params.get(key);
				t = var.varType;
				TypeSignature tw = t.accept(this);
				
				return tw;
			}
				
			else {
				VarBinding var = (VarBinding) currMethod.locals.get(key);
				t = var.varType;
				TypeSignature tw = t.accept(this);
				
				return tw;
			}	
		}
		
		else {
			if ( currClass == null) {
				return new NullTypeSignature();
			}
			
			else if ( currClass.fields.get(key) != null ) {
				VarBinding var = (VarBinding) currClass.fields.get(key);
				t = var.varType;
				TypeSignature tw = t.accept(this);
				
				return tw;
			}
			
			else {
				return new NullTypeSignature();
			}
			
//			else {
//				
//				// Go through inheritance stack and see whether symbol is a field
//				for (int i = 0; i < currInheritanceStack.size(); i++ ) {
//					Symbol nextClassKey = currInheritanceStack.get(i);
//					ClassBinding nextClass = (ClassBinding) SymbolTableVisitor.symbolTable.get(nextClassKey);
//					
//					if ( nextClass.fields.get(key) != null ) {
//						VarBinding var = (VarBinding) nextClass.fields.get(key);
//						t = var.varType;
//						TypeSignature tw = t.accept(this);
//						
//						return tw;
//					}
//				}
//				
//				// If none of classes in inheritance chain have field, return null
//				return new NullTypeSignature();
//				
//				// Check class and its parents 
//				
//			}
		}
		
	}
	
	public TypeSignature visit(syntax.Program n) {
		if (n == null) {
			// Some sort of error handling (should be handled in parse...)
		}
		else if (n.m == null) {
			// Some sort of error handling (should be handled in parse...)
		}
		else {
			n.m.accept(this);
			for (syntax.ClassDecl c: n.cl) c.accept(this);
		}
		
		return new NullTypeSignature();
	}
	
	public TypeSignature visit(syntax.MainClass m) {
		
		// Check statement
		m.s.accept(this);
		
		return new NullTypeSignature();
	}
	
	public TypeSignature visit(syntax.SimpleClassDecl c) {
		
		// Make this the current class, and the parent null
		String n = c.i.toString();
		Symbol key = Symbol.symbol(n);
		currClass = (ClassBinding) SymbolTableVisitor.symbolTable.get(key);
		
		// Initialize empty inheritance stack
		currInheritanceStack = new ArrayList<Symbol>();
		
	
		for (syntax.FieldDecl f: c.fields) {
			f.accept(this);
//			Symbol fieldKey = Symbol.symbol(f.i.toString());
//			currInitializationTable.put(fieldKey, false);
		}
		
		for (syntax.MethodDecl m: c.methods) {
			
			// Initialize ... initialization stack (keeps track of vars and
			// stores flag for their initialization status
			currInitializationTable = new HashMap<Symbol, Boolean>();
			for (syntax.FieldDecl f: c.fields) {
				Symbol fieldKey = Symbol.symbol(f.i.toString());
				currInitializationTable.put(fieldKey, true);
			}
			
			m.accept(this);

		}
		
		currClass = null;
		
		return new NullTypeSignature();
	}
	
	public TypeSignature visit(syntax.ExtendingClassDecl c) {
		// Make this the current class, and fetch parent
		String n = c.i.toString();
		Symbol currKey = Symbol.symbol(n);
		currClass = (ClassBinding) SymbolTableVisitor.symbolTable.get(currKey);
		
		Symbol parentKey = currClass.parent;
		currParent = (ClassBinding) SymbolTableVisitor.symbolTable.get(parentKey);
		
		/* Build stack of class inheritance and, while we are at it, check for cyclical references */		
		Binding b;
		ClassBinding cb = currClass;
		currInheritanceStack.add(cb.name);
		int curr_index = 0;
		while ( cb.parent != null ) {
			
			if ( (b = SymbolTableVisitor.symbolTable.get(cb.parent)) == null ) {
				int line = c.i.lineNumber;
				int col = c.i.columnNumber;
				String msg = "Detected non-existant class \"" + cb.parent.toString() + "\" in inheritance chain of " ;
				msg +=  n + "\". Recommend taking a look at chain of inheritance.";
				errors.SemanticErrorMsg.complain(line, col, msg);
				break;
			}
			
			ClassBinding next = (ClassBinding) b;
			if ( currInheritanceStack.indexOf(next.name) < curr_index 
					&& currInheritanceStack.indexOf(next.name) > -1) {
				int line = c.i.lineNumber;
				int col = c.i.columnNumber;
				String msg;
				if ( next.name.equals(cb.name) ) {
					msg = "Class \"" + cb.name + "\" inherits itself (Cyclical Inheritance).";
				}
				else {
					msg = "Detected cyclical pattern in inheritance chain from \"" + n + "\" to \"";
					msg +=  cb.name.toString() + "\". Recommend taking a look at chain of inheritance.";
				}
				errors.SemanticErrorMsg.complain(line, col, msg);
				break;
			}
			currInheritanceStack.add(next.name);
			cb = next;
			curr_index++;
		}
				

		for (syntax.FieldDecl f: c.fields) f.accept(this);
		
		for (syntax.MethodDecl m: c.methods) {
			
			// Initialize ... initialization stack (keeps track of initialization
			// status of variables)
			for (int i = currInheritanceStack.size() - 1; i >= 0; i--) {
				Symbol key = currInheritanceStack.get(i);
				ClassBinding next = (ClassBinding) SymbolTableVisitor.symbolTable.get(key);
				for (Map.Entry<Symbol, Binding> f: next.fields.entrySet()) {
					Symbol fKey = f.getKey();
					currInitializationTable.put(fKey, true);
				}
			}
			
			m.accept(this);
		}
		
		currClass = null;
		currParent = null;
		currInheritanceStack = new ArrayList<Symbol>();
		
		return new NullTypeSignature();
	}
	
	public TypeSignature visit(syntax.MethodDecl m) {
		
		String name = m.i.toString();
		Symbol key = Symbol.symbol(name);
		
		currMethod = (MethodBinding) currClass.methods.get(key);
		
		for (syntax.LocalDecl l: m.locals ) {
			l.accept(this);
		}
		
		for ( syntax.FormalDecl f: m.fl ) {

			f.accept(this);
		}		

		// Statements in method body
		for ( syntax.Statement s: m.sl ) s.accept(this);
		
		// Return expression
		m.e.accept(this);
		
		currMethod = null;
		
		return new NullTypeSignature();
	}
	
	public TypeSignature visit(syntax.LocalDecl n) {
		// Only really need to type check instance types to make
		// sure they correspond with a real object
		Symbol lKey = Symbol.symbol(n.i.toString());
		currInitializationTable.put(lKey, false);
		n.t.accept(this);
		
		return new NullTypeSignature();
	}
	
	public TypeSignature visit(syntax.FieldDecl n) {
		n.t.accept(this);
		
		return new NullTypeSignature();
	}
	
	public TypeSignature visit(syntax.FormalDecl n) {
		// Only really need to type check instance types to make
		// sure they correspond with a real object
		Symbol fKey = Symbol.symbol(n.i.toString());
		currInitializationTable.put(fKey, true);
		n.t.accept(this);
		
		return new NullTypeSignature();
	}
	
	public TypeSignature visit(syntax.IdentifierType t) {
		// See if identifier corresponds with an actual
		// object in table

		Symbol key = Symbol.symbol(t.s);
		int line = t.lineNumber;
		int col = t.columnNumber;

		if ( SymbolTableVisitor.symbolTable.get(key) == null) {
			String msg = "Object type \"" + t.s + "\" undeclared";
			errors.SemanticErrorMsg.complain(line, col, msg);
		}		
		
		return new IdentifierTypeSignature(key);		
	}
	
	public TypeSignature visit(syntax.IntArrayType t) {
		// Nothing to do here
		return new IntArrayTypeSignature();
	}
	
	public TypeSignature visit(syntax.BooleanType t) {
		// Nothing to do here
		return new BoolTypeSignature();
	}
	
	public TypeSignature visit(syntax.IntegerType t) {
		// Nothing to do here
		return new IntTypeSignature();
	}
	
	public TypeSignature visit(syntax.VoidType t) {
		// Nothing to do here
		return new VoidTypeSignature();
	}
	
	//-----------------------------------------------//
	//-------------BEGIN STATEMENTS------------------//
	//-----------------------------------------------//
	//------------(Return Nothing)------------------//
	
	public TypeSignature visit(syntax.Block b) {
		
		for ( syntax.Statement s: b.sl ) s.accept(this);
		
		return new NullTypeSignature();
	}

	public TypeSignature visit(syntax.If s) {
		
		// Expression should evaluate to boolean
//		syntax. t = s.e.accept(this);
		TypeSignature tw = s.e.accept(this);
		if ( !(tw instanceof BoolTypeSignature )) {
			int line = s.e.lineNumber;
			int col = s.e.columnNumber;
			String msg = "boolean expression expected";
			SemanticErrorMsg.complain(line, col, msg);
		}
		
//		else if (t instanceof syntax.IdentifierType) {
//			/* Lookup in symbol table */
//			String name = t.toString();
//			Symbol key = Symbol.symbol(name);
//			syntax.Type idType = searchScope(key);
//			
//			if ( !(idType instanceof syntax.BooleanType) ) {
//				int line = s.e.lineNumber;
//				int col = s.e.columnNumber;
//				String msg = "TypeError: boolean expression expected";
//				TypeErrorMsg.complain(line, col, msg);			
//			}
//		}
			
		s.s1.accept(this);
		
		s.s2.accept(this);
		
		return new NullTypeSignature();
	}
	
	public TypeSignature visit(syntax.While w) {
		
		// Expression should return boolean
		TypeSignature tw = w.e.accept(this);
		if (!( tw instanceof BoolTypeSignature)) {
			int line = w.e.lineNumber;
			int col = w.e.columnNumber;
			String msg = "boolean expression expected";
			SemanticErrorMsg.complain(line, col, msg);
		}
		
		w.s.accept(this);
		
		return new NullTypeSignature();
	}
	
	public TypeSignature visit(syntax.Print p) {
	
		p.e.accept(this);
		
		return new NullTypeSignature();
	}
	
	public TypeSignature visit(syntax.Assign a) {
		// First, make sure that we can find identifier in
		// symbol table
		String id = a.i.s;
		Symbol key = Symbol.symbol(id);
		TypeSignature idTW;
		if ((idTW = searchScope(key)) instanceof NullTypeSignature) {		
			int line = a.i.lineNumber;
			int col = a.i.columnNumber;
			String msg = "symbol \"" + id + "\" undeclared";
			SemanticErrorMsg.complain(line, col, msg);
			return new NullTypeSignature();
		}
		
		// If we can find entry in symbol table, add entry in initialization table to true
		currInitializationTable.replace(key, true);
		
		// Evaluate expression to see if it's the desired type
		TypeSignature expTW = a.e.accept(this);
		
		// If the expression does not evaluate into a recognized type, raise an error 
		if ( expTW instanceof NullTypeSignature ) {
			int line = a.e.lineNumber;
			int col = a.e.columnNumber;
			String msg = "right-hand side of assignment expression does not evaluate to recognized type. Should evaluate to type " + idTW.getName();
			SemanticErrorMsg.complain(line, col, msg);
			return new NullTypeSignature();
		}
		
		if ( !( expTW.getClass().equals(idTW.getClass()) )) {
			
			int line = a.e.lineNumber;
			int col = a.e.columnNumber;
			String msg = "right-hand side of assignment expression evaluates to \"" + expTW.getName();
			msg += "\". Should evaluate to type " + idTW.getName();
			SemanticErrorMsg.complain(line, col, msg);
			return new NullTypeSignature();
		}
		
		/* Make a check to see if they are not id types. If they are,
		 * lookup objects they reference
		 */
		if ( (expTW instanceof IdentifierTypeSignature) 
				&& (idTW instanceof IdentifierTypeSignature) ) {
			
			String expObjName = expTW.getName();
			String idObjName = idTW.getName();
			
			if ( !(expObjName.equals(idObjName)) ) {
				int line = a.e.lineNumber;
				int col = a.e.columnNumber;
				String msg = "type \"" + expObjName + "\" cannot be assigned to var \"" + a.i.s + "\" of type \"" + idObjName + "\"";
				SemanticErrorMsg.complain(line, col, msg);
			}
			
		}
		
		return new NullTypeSignature();
		
	}
	
	public TypeSignature visit(syntax.ArrayAssign a) {
		
		// See if we can look up array in symbol table
		TypeSignature idType;
		String name = a.nameOfArray.s;
		Symbol key = Symbol.symbol(name);
		if (( idType = searchScope(key) ) instanceof NullTypeSignature) {
			int line = a.lineNumber;
			int col = a.columnNumber;
			String msg = "cannot find symbol \"" + a.nameOfArray.s + "\"";
			SemanticErrorMsg.complain(line, col, msg);
			return new NullTypeSignature();
		}
		
		// If the id isn't of type array, we're done
		else if (!( idType instanceof IntArrayTypeSignature) ) {
			int line = a.lineNumber;
			int col = a.columnNumber;
			String msg = "cannot index into non-array type \"" + a.nameOfArray.s + "\"";
			SemanticErrorMsg.complain(line, col, msg);
			return null;
		}
		
		// If we can look up array in symbol table, and it evaluates to an array type,
		// add it to the initialization table
		currInitializationTable.replace(key, true);
		
		// Make sure index expression evaluates to integer
		TypeSignature indexType = a.indexInArray.accept(this);
		
		if (!( indexType instanceof IntTypeSignature )) {
			int line = a.indexInArray.lineNumber;
			int col = a.indexInArray.columnNumber;
			String msg = "index expression should evaluate to type \"int\"";
			SemanticErrorMsg.complain(line, col, msg);
		}
		
		// Make sure assigned expression evaluates to integer
		TypeSignature assignType = a.e.accept(this);
		
		if (!( assignType instanceof IntTypeSignature )) {
			int line = a.e.lineNumber;
			int col = a.e.columnNumber;
			String msg = "assignment expression should evaluate to type \"int\"";
			SemanticErrorMsg.complain(line, col, msg);
		}
		
		return new NullTypeSignature();
		
	}
	
	//-----------------------------------------------//
	//-------------END STATEMENTS------------------//
	//-----------------------------------------------//
	
	//----------------------------------------------//
	//------------BEGIN EXPRESSIONS-----------------//
	//----------------------------------------------//
	
	public TypeSignature visit(syntax.And e) {
		/* Both sides of expression should evaluate to booleans */
		TypeSignature e1Type = e.e1.accept(this);
		
		if (!( e1Type instanceof BoolTypeSignature)) {
			int line = e.e1.lineNumber;
			int col = e.e1.columnNumber;
			String msg = "left-hand side of \"and\" expression should evaluate to boolean type";
			SemanticErrorMsg.complain(line, col, msg);
		}
		
//		else if (e1Type instanceof syntax.IdentifierType) {
//			/* Lookup in symbol table */
//			String name = e1Type.toString();
//			Symbol key = Symbol.symbol(name);
//			syntax.Type idType = searchScope(key);
//			
//			if ( !(idType instanceof syntax.BooleanType) ) {
//				int line = e.lineNumber;
//				int col = e.columnNumber;
//				String msg = "TypeError: boolean expression expected";
//				TypeErrorMsg.complain(line, col, msg);			
//			}
//		}
			
		
		TypeSignature e2Type = e.e2.accept(this);
		
		if (!( e2Type instanceof BoolTypeSignature )) {
			int line = e.e2.lineNumber;
			int col = e.e2.columnNumber;
			String msg = "right-hand side of \"and\" expression should evaluate to boolean type";
			SemanticErrorMsg.complain(line, col, msg);
		}
		
		// Since the evaluated and expression is of type boolean, return that
//		syntax.Type evalType = syntax.Type.THE_BOOLEAN_TYPE;
		
		return new BoolTypeSignature();
	}
	
	public TypeSignature visit(syntax.LessThan e) {
		
		// both sides of the expression need to evaluate to integer types
		TypeSignature e1Type = e.e1.accept(this);
		
		if (!( e1Type instanceof IntTypeSignature )) {
			int line = e.e1.lineNumber;
			int col = e.e1.columnNumber;
			String msg = "left-hand side of \"<\" expression should evaluate to integer type";
			SemanticErrorMsg.complain(line, col, msg);
		}
		
		TypeSignature e2Type = e.e2.accept(this);
		
		if (!( e2Type instanceof IntTypeSignature )) {
			int line = e.e2.lineNumber;
			int col = e.e2.columnNumber;
			String msg = "right-hand side of \"<\" expression should evaluate to integer type";
			SemanticErrorMsg.complain(line, col, msg);
		}
		
		// Since the evaluated less than expression is of type boolean, return that
//		syntax.Type evalType = syntax.Type.THE_BOOLEAN_TYPE;
		
		return new BoolTypeSignature();
	}
	
	public TypeSignature visit(syntax.Plus e) {
		
		// Both sides of expression should evaluate to integers
		TypeSignature e1Type = e.e1.accept(this);
		
		if (!( e1Type instanceof IntTypeSignature )) {
			int line = e.e1.lineNumber;
			int col = e.e1.columnNumber;
			String msg = "left-hand side of \"+\" expression should evaluate to integer type";
			SemanticErrorMsg.complain(line, col, msg);
		}
		
		TypeSignature e2Type = e.e2.accept(this);
		
		if (!( e2Type instanceof IntTypeSignature )) {
			int line = e.e2.lineNumber;
			int col = e.e2.columnNumber;
			String msg = "right-hand side of \"+\" expression should evaluate to integer type";
			SemanticErrorMsg.complain(line, col, msg);
		}
		
		// Since the evaluated plus expression is of type integer, return that
//		syntax.Type evalType = syntax.Type.THE_INTEGER_TYPE;
		
		return new IntTypeSignature();		
	}
	
	public TypeSignature visit(syntax.Minus e) {
		// Both sides of expression should evaluate to integers
		TypeSignature e1Type = e.e1.accept(this);
		
		if (!( e1Type instanceof IntTypeSignature )) {
			int line = e.e1.lineNumber;
			int col = e.e1.columnNumber;
			String msg = "left-hand side of \"-\" expression should evaluate to integer type";
			SemanticErrorMsg.complain(line, col, msg);
		}
		
		TypeSignature e2Type = e.e2.accept(this);
		
		if (!( e2Type instanceof IntTypeSignature )) {
			int line = e.e2.lineNumber;
			int col = e.e2.columnNumber;
			String msg = "right-hand side of \"-\" expression should evaluate to integer type";
			SemanticErrorMsg.complain(line, col, msg);
		}
		
		// Since the evaluated plus expression is of type integer, return that
//		syntax.Type evalType = syntax.Type.THE_INTEGER_TYPE;
		
		return new IntTypeSignature();	
		
	}
	
	public IntTypeSignature visit(syntax.Times e) {
		// Both sides of expression should evaluate to integers
		TypeSignature e1Type = e.e1.accept(this);
		
		if (!( e1Type instanceof IntTypeSignature )) {
			int line = e.e1.lineNumber;
			int col = e.e1.columnNumber;
			String msg = "left-hand side of \"*\" expression should evaluate to integer type";
			SemanticErrorMsg.complain(line, col, msg);
		}
		
		TypeSignature e2Type = e.e2.accept(this);
		
		if (!( e2Type instanceof IntTypeSignature )) {
			int line = e.e2.lineNumber;
			int col = e.e2.columnNumber;
			String msg = "right-hand side of \"*\" expression should evaluate to integer type";
			SemanticErrorMsg.complain(line, col, msg);
		}
		
		// Since the evaluated plus expression is of type integer, return that
//		syntax.Type evalType = syntax.Type.THE_INTEGER_TYPE;
		
		return new IntTypeSignature();	
		
	}
	
	public TypeSignature visit(syntax.ArrayLookup e) {
		
		// Determine that the expression for array is an array type
		TypeSignature e1Type = e.expressionForArray.accept(this);
			
		if (!( e1Type instanceof IntArrayTypeSignature )) {
			int line = e.expressionForArray.lineNumber;
			int col = e.expressionForArray.columnNumber;
			String msg = "expression of type " + e1Type.toString() + " does not support indexing";
			SemanticErrorMsg.complain(line, col, msg);
			return new IntTypeSignature();
		}
		
		// Determine if the expression for the array index evaluates to integer
		TypeSignature e2Type = e.indexInArray.accept(this);
		
		if (!( e2Type instanceof IntTypeSignature )) {
			int line = e.indexInArray.lineNumber;
			int col = e.indexInArray.columnNumber;
			String msg = "index for array should be of type integer";
			SemanticErrorMsg.complain(line, col, msg);
		}
		
		// Since the evaluated array lookup expression is of type integer, return that
		
		return new IntTypeSignature();	
		
	}
	
	public TypeSignature visit(syntax.ArrayLength e) {
		
		// Determine that the expression for array is an array type
		TypeSignature e1Type = e.expressionForArray.accept(this);
			
		if (!( e1Type instanceof IntArrayTypeSignature )) {
			int line = e.expressionForArray.lineNumber;
			int col = e.expressionForArray.columnNumber;
			String msg = "non-array expression does not have field \"length\"";
			SemanticErrorMsg.complain(line, col, msg);
		}
		
		return new IntTypeSignature();
	}
	
	public TypeSignature visit(syntax.Call c) {
		
		/* See if the expression evaluates to an identifier */
		TypeSignature eType = c.e.accept(this);
		
		if (!( eType instanceof IdentifierTypeSignature )) {
			
			int line = c.e.lineNumber;
			int col = c.e.columnNumber;
			
			if ( eType instanceof NullTypeSignature ) {

				String msg = "instance performing call did not evaluate to a recognized type";
				SemanticErrorMsg.complain(line, col, msg);
				return new NullTypeSignature();
			}
			
			else {
				String msg = "instance of type " + eType.getName() + " does not perform method calls";
				SemanticErrorMsg.complain(line, col, msg);
				return new NullTypeSignature();
			}
		}

		String instanceName = eType.getName();
		Symbol key = Symbol.symbol(instanceName);
					
		/* Look up to see if symbol is in symbol table */
		Binding b;
		ClassBinding cb;
		if ( (b = SymbolTableVisitor.symbolTable.get(key)) == null ) {
			int line = c.e.lineNumber;
			int col = c.e.columnNumber;
			String msg = "object " + instanceName + " does not exist";
			SemanticErrorMsg.complain(line, col, msg);
			return new NullTypeSignature();
		}

		cb = (ClassBinding) b;
		
		/* Now see if method being invoked exists */
		String methodName = c.i.toString();
		Symbol mKey = Symbol.symbol(methodName);
		
		MethodBinding mb;
		
		// Check if method is in object being referenced. If it isn't,
		// check the object's inheritance stack
		if ( (b = cb.methods.get(mKey)) == null ) {
			
			if ( cb.parent == null ) {
				int line = c.i.lineNumber;
				int col = c.i.columnNumber;
				String msg = "method does not exist in class \"" + cb.getName() + "\"";
				SemanticErrorMsg.complain(line, col, msg);
				return new NullTypeSignature();		
			}
			
			else {
				
				// Get the inheritance stack for this class
				ArrayList<Symbol> inheritanceStack = buildInheritanceStack(cb.name);
			
				for ( int i = 0; i < inheritanceStack.size(); i++ ) {
					Symbol nextKey = inheritanceStack.get(i);
					cb = (ClassBinding) SymbolTableVisitor.symbolTable.get(nextKey);
					
					if ( !((b = cb.methods.get(mKey)) == null) ) {
						break;
					}
					
					else if ( i == inheritanceStack.size() - 1 ) {
						int line = c.i.lineNumber;
						int col = c.i.columnNumber;
						String msg = "method does not exist in inheritance stack";
						SemanticErrorMsg.complain(line, col, msg);
						return new NullTypeSignature();		
					}
				}					
			}
		}
		
		mb = (MethodBinding) b;
		TypeSignature rType = mb.rtype.accept(this);
		
		// Check to see we aren't going over the length of the 
		// methods parameter list
		if (c.el.size() > mb.paramList.size()) {
			int line = c.i.lineNumber;
			int col = c.i.columnNumber;
			String msg = String.valueOf(c.el.size()) + " args provided to method " + methodName + ".";
			int numParams;
			if ( (numParams = mb.paramList.size()) == 0 ) {
				msg += " Expected no args.";			
			}
			else {
				msg += " Expected " + String.valueOf(numParams) + " args.";
			}
			SemanticErrorMsg.complain(line, col, msg);
			return rType;
		}
		

		/* If the method being invoked does exist, ensure that all
		 * expressions in the call evaluate to the right parameter type
		 */
		for (int i = 0; i < c.el.size(); i++) {
			
			TypeSignature expType;
			
			if ((expType = c.el.get(i).accept(this)) instanceof NullTypeSignature) {
				int line = c.el.get(i).lineNumber;
				int col = c.el.get(i).columnNumber;
				String msg = "param did not evaluate to a recognized type";
				SemanticErrorMsg.complain(line, col, msg);
				return rType;
			}

			Symbol param = mb.paramList.get(i);
			ParamBinding vb = (ParamBinding)mb.params.get(param);
			TypeSignature paramType = vb.varType.accept(this);
			
			if ( !(paramType.getClass().equals(expType.getClass())) ) {
				int line = c.el.get(i).lineNumber;
				int col = c.el.get(i).columnNumber;
				String msg = "param did not match expected type";
				SemanticErrorMsg.complain(line, col, msg);
				return rType;
			}
			
			// If param is an object type, make sure that the expression returns
			// the same object type
			else if ( (paramType instanceof IdentifierTypeSignature )
					&& (expType instanceof IdentifierTypeSignature) ) {
				
				String paramObjName = paramType.getName();
				String expObjName = paramType.getName();
				if ( !(paramObjName.equals(expObjName)) ) {
					int line = c.el.get(i).lineNumber;
					int col = c.el.get(i).columnNumber;
					String msg = "param of type \"" + expObjName;
					msg += "\" did not match expected type of \"" + paramObjName + "\"";
					SemanticErrorMsg.complain(line, col, msg);
					return rType;				
				}
				
			}
			
		}
		
		return rType;
	}
	
	public TypeSignature visit(syntax.IntegerLiteral e) {
		/* This one's easy. Return integer type */
		return new IntTypeSignature();
	}
	
	public TypeSignature visit(syntax.True e) {
		return new BoolTypeSignature();
	}
	
	public TypeSignature visit(syntax.False e) {
		return new BoolTypeSignature();
	}
	
	public TypeSignature visit(syntax.IdentifierExp e) {
		/* See if the identifier is in scope. If it is, 
		 * return its type
		 */
		String name = e.s;
		Symbol key = Symbol.symbol(name);
		TypeSignature tw = searchScope(key);
		
		if ( tw instanceof NullTypeSignature ) {
			int line = e.lineNumber;
			int col = e.columnNumber;
			String msg = "symbol \"" + key.toString() + "\" undeclared";
			SemanticErrorMsg.complain(line, col, msg);
		}
		
		/* Check to see if identifier has been initialized */
		else if ( (currInitializationTable.get(key) == null) ) {
			if ( !( tw instanceof NullTypeSignature ) ) {
				/* Well...that ain't right */
				System.err.println("DEBUGGING MSG: shouldn't get here");
			}
			
		}
		
		else if ( currInitializationTable.get(key) == false ) {
			
			int l = e.lineNumber;
			int c = e.columnNumber;
			String msg = "Var \"" + name + "\" has not been initialized.";
			SemanticErrorMsg.complain(l, c, msg);			
		}
		
		/* See if the expression evaluates to a class object. If it
		 * does not, check scope for object instance
		 */
//		Binding b = SymbolTableVisitor.symbolTable.get(key);
		
//		if ( b != null ) {
//			return IdentifierTypeWrapper(name, e.lineNumber, e.columnNumber);
//		}
		
		return tw;
	}
	
	public TypeSignature visit(syntax.This e) {
		return new IdentifierTypeSignature(currClass.getId());
	}
	
	public TypeSignature visit(syntax.NewArray n) {
		/* make sure the index expression evaluates to integer */
		TypeSignature indexType = n.e.accept(this);
		
		if ( !(indexType instanceof IntTypeSignature) ) {
			int line = n.e.lineNumber;
			int col = n.e.columnNumber;
			String msg = "size for new array should be of type integer";
			SemanticErrorMsg.complain(line, col, msg);			
		}
		
		return new IntArrayTypeSignature();
	}
	
	public TypeSignature visit(syntax.NewObject n) {
		
		/* Make sure that this identifier corresponds to an actual object */
		String objectName = n.i.toString();
		Symbol key = Symbol.symbol(objectName);
		
		if (SymbolTableVisitor.symbolTable.get(key) == null) {
			int line = n.i.lineNumber;
			int col = n.i.columnNumber;
			String msg = "No object of type " + objectName + " exists";
			SemanticErrorMsg.complain(line, col, msg);	
		}
		
		return new IdentifierTypeSignature(key);
	}
	
	public TypeSignature visit(syntax.Not n) {
		/* Make sure that the expression evaluates to a boolean */
		TypeSignature expType = n.e.accept(this);
		
		if ( !(expType instanceof BoolTypeSignature)) {
			int line = n.e.lineNumber;
			int col = n.e.columnNumber;
			String msg = "evaluated expression should be of type boolean";
			SemanticErrorMsg.complain(line, col, msg);		
		}
		
		return new BoolTypeSignature();
	}
	
	public TypeSignature visit(syntax.Identifier i) {
		// Should never be called
		return new NullTypeSignature();
	}		
}

class TypeSignature {

	public TypeSignature() {}
	public String getName() {
		// should never get printed
		return "<TYPE>";
	}
	
}

class IntTypeSignature extends TypeSignature {
	public IntTypeSignature() {
	}
	
	public String getName() {
		return "int";
	}
}

class BoolTypeSignature extends TypeSignature {
	public BoolTypeSignature() {
	}
	
	public String getName() {
		return "boolean";
	}
}

class IntArrayTypeSignature extends TypeSignature {
	public IntArrayTypeSignature() {
	}
	
	public String getName() {
		return "int[]";
	}
}

class IdentifierTypeSignature extends TypeSignature {
	Symbol objectName;
//	int line;
//	int col;
	public IdentifierTypeSignature(Symbol n) {
		objectName = n;
//		line = l;
//		col = c;
	}
	
	public String getName() {
		return objectName.toString();
	}
	
//	public Symbol getKey() {
//		return objectName;
//	}
}



class NullTypeSignature extends TypeSignature {
	
	public NullTypeSignature() {}
	
	public String getName() {
		// should never get printed
		return "<NULL>";
	}
}

class VoidTypeSignature extends TypeSignature {
	// Should never be invoked. Just included for completion's
	// sake
	public VoidTypeSignature() {}
	
	public String getName() {
		return "void";
	}
}
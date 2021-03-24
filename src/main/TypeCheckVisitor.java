package main;

import java.util.ArrayList;

import java.util.Collections;
import java.util.Enumeration;

import errors.TypeErrorMsg;
import syntax.IdentifierType;

public class TypeCheckVisitor implements syntax.SyntaxTreeVisitor<syntax.Type>{

	ClassBinding currClass = null;
	ClassBinding currParent = null;
	
	ArrayList<Symbol> inheritanceStack = null;
	
	MethodBinding currMethod = null;
	
	private syntax.Type searchScope(Symbol key) {
		syntax.Type t;
		
//		ClassBinding parentClass;
		
		if ( currMethod != null ) {
			
			if ( currMethod.locals.get(key) == null &&
					currMethod.params.get(key) == null ) {
				
				if ( currClass.fields.get(key) != null ) {
					VarBinding var = (VarBinding) currClass.fields.get(key);
					t = var.varType;
					return t;
				}
				
				else {
					
					// Go through inheritance stack and see whether symbol is a field
					for (int i = 0; i < inheritanceStack.size(); i++ ) {
						Symbol nextClassKey = inheritanceStack.get(i);
						ClassBinding nextClass = (ClassBinding) SymbolTableVisitor.symbolTable.get(nextClassKey);
						
						if ( nextClass.fields.get(key) != null ) {
							VarBinding var = (VarBinding) nextClass.fields.get(key);
							t = var.varType;
							return t;
						}
					}
					
					// If none of classes in inheritance chain have field, return null
					return null;
					
				}
			}
				
			else if (currMethod.locals.get(key) == null) {
				VarBinding var = (VarBinding) currMethod.params.get(key);
				t = var.varType;
				return t;
			}
				
			else {
				VarBinding var = (VarBinding) currMethod.locals.get(key);
				t = var.varType;
				return t;
			}	
		}
		
		else {
			if ( currClass == null) {
				return null;
			}
			
			else if ( currClass.fields.get(key) != null ) {
				VarBinding var = (VarBinding) currClass.fields.get(key);
				t = var.varType;
				return t;
			}
			
			else {
				
				// Go through inheritance stack and see whether symbol is a field
				for (int i = 0; i < inheritanceStack.size(); i++ ) {
					Symbol nextClassKey = inheritanceStack.get(i);
					ClassBinding nextClass = (ClassBinding) SymbolTableVisitor.symbolTable.get(nextClassKey);
					
					if ( nextClass.fields.get(key) != null ) {
						VarBinding var = (VarBinding) nextClass.fields.get(key);
						t = var.varType;
						return t;
					}
				}
				
				// If none of classes in inheritance chain have field, return null
				return null;
				
				// Check class and its parents 
				
			}
		}
		
	}
	
//	private syntax.Type searchScope(Symbol key) {
//		// Search symbol table for variable and return its type		
//		syntax.Type t;
//		if ( currMethod != null) {
//			
//			if (currMethod.locals.get(key) == null && 
//				currMethod.params.get(key) == null) {
			
//				if ( currClass.fields.get(key) == null) {
//					if ( currParent == null || currParent.fields.get(key) == null) {
//						return null;
//					}
					
//					else {
//						VarBinding var = (VarBinding) currParent.fields.get(key);
//						t = var.varType;
//					}
//				}
					
//				else {
//					VarBinding var = (VarBinding) currClass.fields.get(key);
//					t = var.varType;
//				}
//			}
//			
//			else if (currMethod.locals.get(key) == null) {
//				VarBinding var = (VarBinding) currMethod.params.get(key);
//				t = var.varType;
//			}
			
//			else {
//				VarBinding var = (VarBinding) currMethod.locals.get(key);
//				t = var.varType;
//			}
//		}
		
//		else {
//			if ( currClass == null) {
//				return null;
//			}
			
//			else if ( currClass.fields.get(key) == null) {
//				if ( currParent == null || currParent.fields.get(key) == null) {
//					return null;
//				}
				
//				else {
//					VarBinding var = (VarBinding) currParent.fields.get(key);
//					t = var.varType;
//				}
//			}
				
//			else {
//				VarBinding var = (VarBinding) currClass.fields.get(key);
//				t = var.varType;
//			}
//		}
		
//		return t;
	
//	}
	
//	private boolean determineEquality(syntax.Type a, syntax.Type b) {
//		/* Determine if two types are equal */
//		
//		/* First we need to determine if one or both are id types.
//		 * If not, then comparison is trivial
//		 */
//		if ( (a instanceof syntax.IdentifierType) 
//				&& (b instanceof syntax.IdentifierType) ) {
//			String a_name = a.toString();
//			Symbol a_key = Symbol.symbol(a_name);
//			String a_objname;
//			syntax.Type aidType;
//			
//			String b_name = b.toString();
//			Symbol b_key = Symbol.symbol(b_name);
//			String b_objname;
//			syntax.Type bidType;
//			
//			boolean notInScope = false;
//			
//			// is 'a' instance a variable in scope?
//			if ( (aidType = searchScope(a_key)) == null) {
//				
//				notInScope = true;
//				
//				// If not, search class table
//				Binding ab;
//				if ( (ab = SymbolTableVisitor.symbolTable.get(a_key)) == null) {
//					// This should never happen
//					return true;
//				}
//				
//				a_objname = ab.getName();
//			}
//			
//			if ( (bidType = searchScope(b_key)) == null) {
//				
//				Binding bb;
//				if ( (bb = SymbolTableVisitor.symbolTable.get(b_key)) == null) {
//					return true;
//				}
//				
//				b_objname = bb.getName();
//				
//			}
//			
//			if (bidType == null && aidType == null) {
//				if ( !(b_objname.equals(a_objname)) ) {
//					return false;
//				}
//			}
//		}
//		
//	}

	
	public syntax.Type visit(syntax.Program n) {
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
		
		return null;
	}
	
	public syntax.Type visit(syntax.MainClass m) {
		
		// Check statement
		m.s.accept(this);
		
		return null;
	}
	
	public syntax.Type visit(syntax.SimpleClassDecl c) {
		
		// Make this the current class, and the parent null
		String n = c.i.toString();
		Symbol key = Symbol.symbol(n);
		currClass = (ClassBinding) SymbolTableVisitor.symbolTable.get(key);
		
		// Initialize empty inheritance stack
		inheritanceStack = new ArrayList<Symbol>();
		
		for (syntax.FieldDecl f: c.fields) f.accept(this);
		
		for (syntax.MethodDecl m: c.methods) m.accept(this);
		
		currClass = null;
		
		return null;
	}
	
	public syntax.Type visit(syntax.ExtendingClassDecl c) {
		// Make this the current class, and fetch parent
		String n = c.i.toString();
		Symbol currKey = Symbol.symbol(n);
		currClass = (ClassBinding) SymbolTableVisitor.symbolTable.get(currKey);
		
		Symbol parentKey = currClass.parent;
		currParent = (ClassBinding) SymbolTableVisitor.symbolTable.get(parentKey);
		
		/* Build stack of class inheritance and, while we are at it, check for cyclical references */
		inheritanceStack = new ArrayList<Symbol>();
		
		ClassBinding parentClass = (ClassBinding) SymbolTableVisitor.symbolTable.get(currClass.parent);
		int curr_index = 0;
		while ( parentClass.parent != null ) {
			inheritanceStack.add(parentClass.name);
			ClassBinding next = (ClassBinding) SymbolTableVisitor.symbolTable.get(parentClass.parent);
			curr_index++;
			if ( inheritanceStack.indexOf(next.name) < curr_index ) {
				int line = c.i.lineNumber;
				int col = c.i.columnNumber;
				String msg = "Detected cyclical pattern in inheritance chain from \"" + n + " to \"";
				msg +=  parentClass.name.toString() + ". Recommend taking a look at chain of inheritance.";
				errors.TypeErrorMsg.complain(line, col, msg);
				break;
			}
			
			parentClass = next;
		}
				
		/* Now go through stack and check for cyclical references */
//		for (int i = 0; i < classStack.size(); i++) {
//			Symbol key = classStack.get(i);
//			ClassBinding cb = (ClassBinding) SymbolTableVisitor.symbolTable.get(key);
			
//			if ( cb.parent != null && classStack.indexOf(cb.parent) < i ) {
//				int line = c.i.lineNumber;
//				int col = c.i.columnNumber;
//				String msg = "Detected cyclical pattern in inheritance chain from \"" + n + " to \"";
//				msg +=  cb.parent.toString() + ". Recommend taking a look at chain of inheritance.";
//				errors.TypeErrorMsg.complain(line, col, msg);
//			}
//		}
		
//		/* Check that class definitions aren't cyclical */
//		if (currParent.parent.equals(currKey)) {
//			int line = c.i.lineNumber;
//			int col = c.i.columnNumber;
//			String msg = "cyclical inheritance involving " + currClass.parent.toString();
//			TypeErrorMsg.complain(line, col, msg);
//		}
		
		for (syntax.FieldDecl f: c.fields) f.accept(this);
		
		for (syntax.MethodDecl m: c.methods) m.accept(this);
		
		currClass = null;
		currParent = null;
		
		return null;
	}
	
	public syntax.Type visit(syntax.MethodDecl m) {
		
		String name = m.i.toString();
		Symbol key = Symbol.symbol(name);
		
		currMethod = (MethodBinding) currClass.methods.get(key);
		
		// Statements in method body
		for ( syntax.Statement s: m.sl ) s.accept(this);
		
		// Return expression
		m.e.accept(this);
		
		currMethod = null;
		
		return null;
	}
	
	public syntax.Type visit(syntax.LocalDecl n) {
		// Only really need to type check instance types to make
		// sure they correspond with a real object
		n.t.accept(this);
		
		return null;
	}
	
	public syntax.Type visit(syntax.FieldDecl n) {
		n.t.accept(this);
		
		return null;
	}
	
	public syntax.Type visit(syntax.FormalDecl n) {
		// Only really need to type check instance types to make
		// sure they correspond with a real object
		n.t.accept(this);
		
		return null;
	}
	
	public syntax.Type visit(syntax.IdentifierType t) {
		// See if identifier corresponds with an actual
		// object in table

//		Symbol key = Symbol.symbol(t.s);
//		if ( SymbolTableVisitor.symbolTable.get(key) == null) {
//			int line = t.lineNumber;
//			int col = t.columnNumber;
//			String msg = "symbol \"" + t.s + "\" undeclared";
//			errors.TypeErrorMsg.complain(line, col, msg);
//		}		
		
		return t;		
	}
	
	public syntax.Type visit(syntax.IntArrayType t) {
		// Nothing to do here
		return null;
	}
	
	public syntax.Type visit(syntax.BooleanType t) {
		// Nothing to do here
		return null;
	}
	
	public syntax.Type visit(syntax.IntegerType t) {
		// Nothing to do here
		return null;
	}
	
	public syntax.Type visit(syntax.VoidType t) {
		// Nothing to do here
		return null;
	}
	
	public syntax.Type visit(syntax.Block b) {
		
		for ( syntax.Statement s: b.sl ) s.accept(this);
		
		return null;
	}

	public syntax.Type visit(syntax.If s) {
		
		// Expression should evaluate to boolean
		syntax.Type t = s.e.accept(this);
		if ( !(t instanceof syntax.BooleanType )) {
			int line = s.e.lineNumber;
			int col = s.e.columnNumber;
			String msg = "boolean expression expected";
			TypeErrorMsg.complain(line, col, msg);
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
		
		return null;
	}
	
	public syntax.Type visit(syntax.While w) {
		
		// Expression should return boolean
		syntax.Type t = w.e.accept(this);
		if (!( t instanceof syntax.BooleanType)) {
			int line = w.e.lineNumber;
			int col = w.e.columnNumber;
			String msg = "boolean expression expected";
			TypeErrorMsg.complain(line, col, msg);
		}
		
//		else if (t instanceof syntax.IdentifierType) {
//			/* Lookup in symbol table */
//			String name = t.toString();
//			Symbol key = Symbol.symbol(name);
//			syntax.Type idType = searchScope(key);
//			
//			if ( !(idType instanceof syntax.BooleanType) ) {
//				int line = w.e.lineNumber;
//				int col = w.e.columnNumber;
//				String msg = "TypeError: boolean expression expected";
//				TypeErrorMsg.complain(line, col, msg);			
//			}
//		}
//		
		w.s.accept(this);
		
		return null;
	}
	
	public syntax.Type visit(syntax.Print p) {
	
		p.e.accept(this);
		
		return null;
	}
	
	public syntax.Type visit(syntax.Assign a) {
		// First, make sure that we can find identifier in
		// symbol table
		String id = a.i.s;
		Symbol key = Symbol.symbol(id);
		syntax.Type idType;
		
		if ((idType = searchScope(key)) == null) {		
			int line = a.i.lineNumber;
			int col = a.i.columnNumber;
			String msg = "symbol \"" + id + "\" undeclared";
			TypeErrorMsg.complain(line, col, msg);
			return null;
		}
		
//		if ( (idType.getClass().equals(syntax.Type.THE_INT_ARRAY_TYPE.getClass())) ) {
//			int line = a.i.lineNumber;
//			int col = a.i.columnNumber;
//			String msg = "Array type does not support assignment";
//			TypeErrorMsg.complain(line, col, msg);
//			return null;
//		}

		// Evaluate expression to see if it's the desired type
		syntax.Type eType = a.e.accept(this);
		
		
		// If the expression is an Identifier Type, lookup object		
		if ( ( eType == null ) || !( eType.getClass().equals(idType.getClass()) )) {
			
			int line = a.e.lineNumber;
			int col = a.e.columnNumber;
			String msg = "right-hand side of assignment expression should evaluate to type " + idType.toString();
			TypeErrorMsg.complain(line, col, msg);
			return null;
		}
		
		/* Make a check to see if they are not id types. If they are,
		 * lookup objects they reference
		 */
		if ( (eType instanceof syntax.IdentifierType) 
				&& (idType instanceof syntax.IdentifierType) ) {
			
			String eName = eType.toString();
			String idName = idType.toString();
			
			if ( !(eName.equals(idName)) ) {
				int line = a.e.lineNumber;
				int col = a.e.columnNumber;
				String msg = "type " + eName + " cannot be assigned to var \"" + a.i.s + "\" of type " + idName;
				TypeErrorMsg.complain(line, col, msg);
			}
			
		}
		
		return null;
		
	}
	
	public syntax.Type visit(syntax.ArrayAssign a) {
		
		// See if we can look up array in symbol table
		syntax.Type idType;
		String name = a.nameOfArray.s;
		Symbol key = Symbol.symbol(name);
		if (( idType = searchScope(key) ) == null) {
			int line = a.lineNumber;
			int col = a.columnNumber;
			String msg = "cannot find symbol \"" + a.nameOfArray.s + "\"";
			TypeErrorMsg.complain(line, col, msg);
			return null;
		}
		
		// If the id isn't of type array, we're done
		else if (!( idType instanceof syntax.IntArrayType) ) {
			int line = a.lineNumber;
			int col = a.columnNumber;
			String msg = "cannot index into non-array type \"" + a.nameOfArray.s + "\"";
			TypeErrorMsg.complain(line, col, msg);
			return null;
		}
		
		// Make sure index expression evaluates to integer
		syntax.Type indexType = a.indexInArray.accept(this);
		
		if (!( indexType instanceof syntax.IntegerType )) {
			int line = a.indexInArray.lineNumber;
			int col = a.indexInArray.columnNumber;
			String msg = "index expression should evaluate to type \"int\"";
			TypeErrorMsg.complain(line, col, msg);
		}
		
		// Make sure assigned expression evaluates to integer
		syntax.Type assignType = a.e.accept(this);
		
		if (!( assignType instanceof syntax.IntegerType )) {
			int line = a.e.lineNumber;
			int col = a.e.columnNumber;
			String msg = "assignment expression should evaluate to type \"int\"";
			TypeErrorMsg.complain(line, col, msg);
		}
		
		return null;
		
	}
	
	public syntax.Type visit(syntax.And e) {
		/* Both sides of expression should evaluate to booleans */
		syntax.Type e1Type = e.e1.accept(this);
		
		if (!( e1Type instanceof syntax.BooleanType)) {
			int line = e.e1.lineNumber;
			int col = e.e1.columnNumber;
			String msg = "left-hand side of \"and\" expression should evaluate to boolean type";
			TypeErrorMsg.complain(line, col, msg);
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
			
		
		syntax.Type e2Type = e.e2.accept(this);
		
		if (!( e2Type instanceof syntax.BooleanType )) {
			int line = e.e2.lineNumber;
			int col = e.e2.columnNumber;
			String msg = "right-hand side of \"and\" expression should evaluate to boolean type";
			TypeErrorMsg.complain(line, col, msg);
		}
		
		// Since the evaluated and expression is of type boolean, return that
		syntax.Type evalType = syntax.Type.THE_BOOLEAN_TYPE;
		
		return evalType;
	}
	
	public syntax.Type visit(syntax.LessThan e) {
		
		// both sides of the expression need to evaluate to integer types
		syntax.Type e1Type = e.e1.accept(this);
		
		if (!( e1Type instanceof syntax.IntegerType )) {
			int line = e.e1.lineNumber;
			int col = e.e1.columnNumber;
			String msg = "left-hand side of \"<\" expression should evaluate to integer type";
			TypeErrorMsg.complain(line, col, msg);
		}
		
		syntax.Type e2Type = e.e2.accept(this);
		
		if (!( e2Type instanceof syntax.IntegerType )) {
			int line = e.e2.lineNumber;
			int col = e.e2.columnNumber;
			String msg = "right-hand side of \"<\" expression should evaluate to integer type";
			TypeErrorMsg.complain(line, col, msg);
		}
		
		// Since the evaluated less than expression is of type boolean, return that
		syntax.Type evalType = syntax.Type.THE_BOOLEAN_TYPE;
		
		return evalType;
	}
	
	public syntax.Type visit(syntax.Plus e) {
		
		// Both sides of expression should evaluate to integers
		syntax.Type e1Type = e.e1.accept(this);
		
		if (!( e1Type instanceof syntax.IntegerType )) {
			int line = e.e1.lineNumber;
			int col = e.e1.columnNumber;
			String msg = "left-hand side of \"+\" expression should evaluate to integer type";
			TypeErrorMsg.complain(line, col, msg);
		}
		
		syntax.Type e2Type = e.e2.accept(this);
		
		if (!( e2Type instanceof syntax.IntegerType )) {
			int line = e.e2.lineNumber;
			int col = e.e2.columnNumber;
			String msg = "right-hand side of \"+\" expression should evaluate to integer type";
			TypeErrorMsg.complain(line, col, msg);
		}
		
		// Since the evaluated plus expression is of type integer, return that
		syntax.Type evalType = syntax.Type.THE_INTEGER_TYPE;
		
		return evalType;		
	}
	
	public syntax.Type visit(syntax.Minus e) {
		// Both sides of expression should evaluate to integers
		syntax.Type e1Type = e.e1.accept(this);
		
		if (!( e1Type instanceof syntax.IntegerType )) {
			int line = e.e1.lineNumber;
			int col = e.e1.columnNumber;
			String msg = "left-hand side of \"-\" expression should evaluate to integer type";
			TypeErrorMsg.complain(line, col, msg);
		}
		
		syntax.Type e2Type = e.e2.accept(this);
		
		if (!( e2Type instanceof syntax.IntegerType )) {
			int line = e.e2.lineNumber;
			int col = e.e2.columnNumber;
			String msg = "right-hand side of \"-\" expression should evaluate to integer type";
			TypeErrorMsg.complain(line, col, msg);
		}
		
		// Since the evaluated plus expression is of type integer, return that
		syntax.Type evalType = syntax.Type.THE_INTEGER_TYPE;
		
		return evalType;	
		
	}
	
	public syntax.Type visit(syntax.Times e) {
		// Both sides of expression should evaluate to integers
		syntax.Type e1Type = e.e1.accept(this);
		
		if (!( e1Type instanceof syntax.IntegerType )) {
			int line = e.e1.lineNumber;
			int col = e.e1.columnNumber;
			String msg = "left-hand side of \"*\" expression should evaluate to integer type";
			TypeErrorMsg.complain(line, col, msg);
		}
		
		syntax.Type e2Type = e.e2.accept(this);
		
		if (!( e2Type instanceof syntax.IntegerType )) {
			int line = e.e2.lineNumber;
			int col = e.e2.columnNumber;
			String msg = "right-hand side of \"*\" expression should evaluate to integer type";
			TypeErrorMsg.complain(line, col, msg);
		}
		
		// Since the evaluated plus expression is of type integer, return that
		syntax.Type evalType = syntax.Type.THE_INTEGER_TYPE;
		
		return evalType;	
		
	}
	
	public syntax.Type visit(syntax.ArrayLookup e) {
		
		// Determine that the expression for array is an array type
		syntax.Type e1Type = e.expressionForArray.accept(this);
			
		if (!( e1Type instanceof syntax.IntArrayType )) {
			int line = e.expressionForArray.lineNumber;
			int col = e.expressionForArray.columnNumber;
			String msg = "expression of type " + e1Type.toString() + " does not support indexing";
			TypeErrorMsg.complain(line, col, msg);
			return syntax.Type.THE_INTEGER_TYPE;
		}
		
		syntax.Type e2Type = e.indexInArray.accept(this);
		
		if (!( e2Type instanceof syntax.IntegerType )) {
			int line = e.indexInArray.lineNumber;
			int col = e.indexInArray.columnNumber;
			String msg = "index for array should be of type integer";
			TypeErrorMsg.complain(line, col, msg);
		}
		
		// Since the evaluated plus expression is of type integer, return that
		
		return syntax.Type.THE_INTEGER_TYPE;	
		
	}
	
	public syntax.Type visit(syntax.ArrayLength e) {
		
		// Determine that the expression for array is an array type
		syntax.Type e1Type = e.expressionForArray.accept(this);
			
		if (!( e1Type instanceof syntax.IntArrayType )) {
			int line = e.expressionForArray.lineNumber;
			int col = e.expressionForArray.columnNumber;
			String msg = "non-array expression does not have field \"length\"";
			TypeErrorMsg.complain(line, col, msg);
		}
		
		return syntax.Type.THE_INTEGER_TYPE;
	}
	
	public syntax.Type visit(syntax.Call c) {
		
		/* See if the expression evaluates to an identifier */
		syntax.Type eType = c.e.accept(this);
		
		if (!( eType instanceof syntax.IdentifierType )) {
			int line = c.e.lineNumber;
			int col = c.e.columnNumber;
			String msg = "instance type " + eType.toString() + " does not perform method calls";
			TypeErrorMsg.complain(line, col, msg);
			return null;
		}
			
		String instanceName = ((syntax.IdentifierType) eType).toString();
		Symbol key = Symbol.symbol(instanceName);
		
		Binding b;
		ClassBinding cb;
			
		/* Look up to see if symbol is in scope */
		syntax.Type instance = searchScope(key);
		if ( instance == null ) {
			/* If not in scope, */
			/* Perform search on other objects */
			if ( (b = SymbolTableVisitor.symbolTable.get(key)) == null) {
				int line = c.e.lineNumber;
				int col = c.e.columnNumber;
				String msg = "object " + instanceName + " does not exist";
				TypeErrorMsg.complain(line, col, msg);
				return null;
			}
			
			cb = (ClassBinding) b;
		}
		
		else if ( !(instance instanceof syntax.IdentifierType) ) {
			int line = c.e.lineNumber;
			int col = c.e.columnNumber;
			String msg = "instance type does not perform method calls";
			TypeErrorMsg.complain(line, col, msg);
			return null;		
		}
		
		else {
			/* Perform search for class based on returned identifier */
			String className = ((syntax.IdentifierType) instance).toString();
			Symbol classKey = Symbol.symbol(className);
			
			if ( (b = SymbolTableVisitor.symbolTable.get(classKey)) == null) {
				int line = instance.lineNumber;
				int col = instance.columnNumber;
				String msg = "object " + instanceName + " does not exist";
				TypeErrorMsg.complain(line, col, msg);
				return null;
			}
			
			cb = (ClassBinding) b;
		}
		
		String className = cb.toString();
		
		/* Now see if method being invoked exists */
		String methodName = c.i.toString();
		Symbol mKey = Symbol.symbol(methodName);
		
		MethodBinding mb;
		
		if ((b = cb.methods.get(mKey)) == null) {
			
			ClassBinding parentBinding;
			// See if the method is in the parent class. If it isn't, 
			// display error
			if (cb.parent == null) {
			
				int line = c.i.lineNumber;
				int col = c.i.columnNumber;
				String msg = "method " + methodName + " in class " + className + " does not exist";
				TypeErrorMsg.complain(line, col, msg);
				return null;
			}
			
			else if ( (parentBinding = (ClassBinding) SymbolTableVisitor.symbolTable.get(cb.parent)) == null) {
				int line = c.i.lineNumber;
				int col = c.i.columnNumber;
				String msg = "method " + methodName + " in class " + className + " does not exist";
				TypeErrorMsg.complain(line, col, msg);
				return null;
			}
			
			else if ( (b = parentBinding.methods.get(mKey)) == null) {
				int line = c.i.lineNumber;
				int col = c.i.columnNumber;
				String msg = "method " + methodName + " in class " + className + " does not exist";
				TypeErrorMsg.complain(line, col, msg);
				return null;
			}
			
		}
		
		mb = (MethodBinding) b;
		syntax.Type rType = mb.rtype;
		
		// Check to see we aren't going over the length of the 
		// methods parameter list
		if (c.el.size() > mb.paramList.size()) {
			int line = c.i.lineNumber;
			int col = c.i.columnNumber;
			String msg = String.valueOf(c.el.size()) + " args provided to method " + methodName + ".";
			msg += " Expected only " + String.valueOf(mb.paramList.size()) + " args.";
			TypeErrorMsg.complain(line, col, msg);
			return rType;
		}
		

		/* If the method being invoked does exist, ensure that all
		 * expressions in the call evaluate to the right parameter type
		 */
		for (int i = 0; i < c.el.size(); i++) {
			
			syntax.Type expType;
			
			if ((expType = c.el.get(i).accept(this)) == null) {
				int line = c.el.get(i).lineNumber;
				int col = c.el.get(i).columnNumber;
				String msg = "expression did not evaluate to a recognized type";
				TypeErrorMsg.complain(line, col, msg);
				return rType;
			}

			Symbol param = mb.paramList.get(i);
			ParamBinding vb = (ParamBinding)mb.params.get(param);
			syntax.Type paramType = vb.varType;
			
			if ( !(paramType.getClass().equals(expType.getClass())) ) {
				int line = c.el.get(i).lineNumber;
				int col = c.el.get(i).columnNumber;
				String msg = "expression did not match expected param type";
				TypeErrorMsg.complain(line, col, msg);
				return rType;
			}
			
		}
		
		return rType;
	}
	
	public syntax.Type visit(syntax.IntegerLiteral e) {
		/* This one's easy. Return integer type */
		return syntax.Type.THE_INTEGER_TYPE;
	}
	
	public syntax.Type visit(syntax.True e) {
		return syntax.Type.THE_BOOLEAN_TYPE;
	}
	
	public syntax.Type visit(syntax.False e) {
		return syntax.Type.THE_BOOLEAN_TYPE;
	}
	
	public syntax.Type visit(syntax.IdentifierExp e) {
		/* See if the identifier is in scope. If it is, 
		 * return its type
		 */
		String name = e.s;
		Symbol key = Symbol.symbol(name);
		
		/* See if the expression evaluates to a class object. If it
		 * does not, check scope for object instance
		 */
		Binding b = SymbolTableVisitor.symbolTable.get(key);
		
//		if ( b != null ) {
//			return IdentifierTypeWrapper(name, e.lineNumber, e.columnNumber);
//		}
		
		return searchScope(key);
	}
	
	public syntax.Type visit(syntax.This e) {
		return new syntax.IdentifierType(0, 0, currClass.getName());
	}
	
	public syntax.Type visit(syntax.NewArray n) {
		/* make sure the index expression evaluates to integer */
		syntax.Type indexType = n.e.accept(this);
		
		if ( !(indexType instanceof syntax.IntegerType) ) {
			int line = n.e.lineNumber;
			int col = n.e.columnNumber;
			String msg = "size for new array should be of type integer";
			TypeErrorMsg.complain(line, col, msg);			
		}
		
		return syntax.Type.THE_INT_ARRAY_TYPE;
	}
	
	public syntax.Type visit(syntax.NewObject n) {
		
		/* Make sure that this identifier corresponds to an actual object */
		String objectName = n.i.toString();
		Symbol key = Symbol.symbol(objectName);
		
		if (SymbolTableVisitor.symbolTable.get(key) == null) {
			int line = n.i.lineNumber;
			int col = n.i.columnNumber;
			String msg = "No object of type " + objectName + " exists";
			TypeErrorMsg.complain(line, col, msg);	
		}
		
		return new syntax.IdentifierType(0, 0, n.i.toString());
	}
	
	public syntax.Type visit(syntax.Not n) {
		/* Make sure that the expression evaluates to a boolean */
		syntax.Type expType = n.e.accept(this);
		
		if ( !(expType instanceof syntax.BooleanType)) {
			int line = n.e.lineNumber;
			int col = n.e.columnNumber;
			String msg = "evaluated expression should be of type boolean";
			TypeErrorMsg.complain(line, col, msg);		
		}
		
		return syntax.Type.THE_BOOLEAN_TYPE;
	}
	
	public syntax.Type visit(syntax.Identifier i) {
		return null;
	}		
}

class TypeWrapper {
	int line;
	int col;
	public TypeWrapper() {}
	
	public TypeWrapper(int l, int c ) {
		line = l;
		col = c;
	}
}

class IntTypeWrapper extends TypeWrapper {
	public IntTypeWrapper(int l, int c) {
		super(l, c);
	}
}

class BoolTypeWrapper extends TypeWrapper {
	public BoolTypeWrapper(int l, int c) {
		super(l, c);
	}
}

class IntArrayTypeWrapper extends TypeWrapper {
	public IntArrayTypeWrapper(int l, int c) {
		super(l, c);
	}
}

class IdentifierTypeWrapper extends TypeWrapper {
	String name;
	public IdentifierTypeWrapper(String n, int l, int c) {
		super(l, c);
		name = n;
	}
}

class NullTypeWrapper extends TypeWrapper {
	public NullTypeWrapper(int l, int c) {
		super(l, c);
	}
}
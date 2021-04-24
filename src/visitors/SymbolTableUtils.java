package visitors;

import java.util.ArrayList;

import main.Symbol;

public class SymbolTableUtils {

	public SymbolTableUtils() {
		
	}
	
	// Given a class binding, build up inheritance stack
	public static ArrayList<Symbol> buildInheritanceStack(Symbol key) {
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
	
	public static Symbol searchForMethod(Symbol methodKey, ClassBinding cb) {
		// Given a method key, search for the class that contains it and return
		// it's key
		
		/* First perform search in current class */
		if (cb.methods.get(methodKey) != null) {
			return cb.name;
		}
		else {
			ArrayList<Symbol> inheritStack = buildInheritanceStack(cb.name);
			for (int i = 0; i < inheritStack.size(); i++) {
				ClassBinding currBind = (ClassBinding) SymbolTableVisitor.symbolTable.get(inheritStack.get(i));
				if (currBind.methods.get(methodKey) != null) {
					return currBind.name;
				}
			}
		}
		return null;
	}
	
	public static Binding searchScope(Symbol key, String currClassName,
			String currMethodName) {
		
		ClassBinding currClass = (ClassBinding) SymbolTableVisitor.symbolTable.get(
				Symbol.symbol(currClassName));
		
		MethodBinding currMethod = (MethodBinding) currClass.methods.get(
				Symbol.symbol(currMethodName));
		
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

					return var;
				}
				
				else {
					
					ClassBinding p = currClass;
					while ( p.parent != null ) {
						p = (ClassBinding) SymbolTableVisitor.symbolTable.get(p.parent);
						if ( p.fields.get(key) != null ) {
							break;
						}
					}
					
					VarBinding var = (VarBinding) p.fields.get(key);
					
					return var;
				}
					
					
			}
				
			else if (currMethod.locals.get(key) == null) {
				VarBinding var = (VarBinding) currMethod.params.get(key);
	
				return var;
			}
				
			else {
				VarBinding var = (VarBinding) currMethod.locals.get(key);
					
				return var;
			}	
		}
		
		else {
			if ( currClass == null) {
				return null;
			}
			
			else if ( currClass.fields.get(key) != null ) {
				VarBinding var = (VarBinding) currClass.fields.get(key);

				return var;
			}
			
			else {
				
				
				ClassBinding p = currClass;
				while ( p.parent != null ) {
					p = (ClassBinding) SymbolTableVisitor.symbolTable.get(p.parent);
					if ( p.fields.get(key) != null ) {
						break;
					}
				}
				
				// If none of classes in inheritance chain have field, return null
				VarBinding var = (VarBinding) p.fields.get(key);
				return var;
				
			}
		}
		
	}

}

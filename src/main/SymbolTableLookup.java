package main;

public class SymbolTableLookup {

	public SymbolTableLookup() {
		// TODO Auto-generated constructor stub
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

package visitors;

import java.util.Map;

import main.Symbol;

public class PrintSymbolTable {
	
	int a;
	
	public static void print() {
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
	
	private static void printClassBinding(ClassBinding b) {
//		System.out.println("\tFields: ");
//		for (Map.Entry<Symbol, Binding> entry: 
//			b.fields.entrySet()) {
//			String fname = entry.getKey().toString();
//			String type = ((VarBinding) entry.getValue()).toString();
//			System.out.println("\t\t" + type + " " + fname);
//		}
//		
		for (int i = 0; i < b.fieldList.size(); i++) {
			Symbol varKey = b.fieldList.get(i);
			String fname = varKey.toString();
			String type = ((VarBinding) b.fields.get(varKey)).toString();
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
	
	private static void printMethodBinding(MethodBinding b) {
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


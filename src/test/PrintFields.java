package test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class PrintFields {
	
	public static void main(String args[]) {
		
		String filename = args[0];
		try {
			semanticAnalysis.MiniJavaSemanticAnalyzer grammarParser = new semanticAnalysis.MiniJavaSemanticAnalyzer(
					new FileInputStream(filename));
			grammarParser.disable_tracing();
			try {
			syntax.Program program = grammarParser.Start();
			
			// construct symbol table
			visitors.SymbolTableVisitor stv = new visitors.SymbolTableVisitor();
			stv.visit(program);
			stv.addFieldsToChild();
			
			// Print out fields
			visitors.PrintSymbolTable.print();
			
			} catch (semanticAnalysis.ParseException e) {
				System.out.println(e.getMessage());
			}			
			
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		}
	}

}

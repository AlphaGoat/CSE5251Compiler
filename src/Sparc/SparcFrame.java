package Sparc;

import main.Symbol;
import main.SymbolTableVisitor.MethodBinding;
import frame.Frame;

public abstract class SparcFrame extends Frame {
	public final int wordSize = 4;
	
	public SparcFrame( MethodBinding m) {
		// Create temps for function parameters
		for (VarBinding v: m.paramList) {
			
			
		}
		
		
		// Pass parameters here
		for (int i = 0; i < numParams; i++) {
			InReg("")
		}
	}
}

class InFrame extends frame.Access {
	int offset;
	InFrame(int i) {
		offset = i;
	}
	
}

class InReg extends frame.Access {
	tree.TEMP temp;
	InReg(tree.TEMP t) {
		temp = t;
	}
} 
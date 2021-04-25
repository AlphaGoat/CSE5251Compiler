package Sparc;

import main.Symbol;
import tree.TEMP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

//import main.SymbolTableVisitor.MethodBinding;
import frame.Frame;

public class SparcFrame extends Frame {
	public final int wordSize = 4;
	private int maxArgs = 0; // Maximum number of arguments in function
							 // called by this method
	public int numTempsInFrame = 0; // number of temps that need to be written to stack frame
	Map<tree.NameOfTemp, Integer> tempsInFrame = new HashMap<tree.NameOfTemp, Integer>();
	
	Map<Integer, tree.TEMP> localRegistersMap;
	Map<Integer, tree.TEMP> globalRegistersMap;
	Map<Integer, Boolean> freeLocalRegisters;
	Map<Integer, Boolean> freeGlobalRegisters;
	
//	Map<Integer, tree.TEMP> outRegisters;
//	Map<Integer, tree.TEMP> inRegisters;
	
	// List of all registers
	public List<String> outRegisters; // %o1-%o5
	public List<String> inRegisters;  // %i0-%i5
	public List<String> specialRegisters; // Stack pointer, frame pointer, 
	public List<String> localRegisters; // %l0-%l7
	public List<String> globalRegisters; // %g0-%g7
	
	// Map of temps to registers
	Map<tree.NameOfTemp, String> tempToRegMap = new HashMap<tree.NameOfTemp, String>();
	
	// List of available registers
	Stack<String> availableRegisters = new Stack<String>();
	
	
	tree.TEMP framePointer;
	tree.TEMP stackPointer;
	tree.TEMP returnLocationRegister;
	tree.TEMP functionReturnRegister;
	tree.TEMP calleeReturnRegister;
	tree.TEMP globalZeroRegister;
	tree.TEMP globalOneRegister;
	tree.TEMP globalTwoRegister;
	tree.TEMP globalThreeRegister;
	
	public SparcFrame(String n, ArrayList<Symbol> fl) {
		name = new tree.LABEL(n);
		
		/*initialize regiseters */
		initializeRegisters();
		
		/* Initialize maps to hold variables */	
		for (int i = 0; i < fl.size(); i++) {
			
			if (i < 6) {
				
				// Assign formal to register and get 
				// a temp to represent that register
				String registerName = inRegisters.get(i);
				tree.TEMP inRegTemp = new tree.TEMP(registerName);
				tempToRegMap.put(inRegTemp.temp, registerName);	
				
				// Create an access for that variable
				frame.Access a = new inRegister(inRegTemp);
				Symbol varKey = fl.get(i);
				varMap.put(varKey, a);
				
				// Add arg to formals
				formals.add(a);
			}
			
			else {
				int offset = prevFrameFormals.size();
				frame.Access a = new inPrevFrame(offset, wordSize);
				varMap.put(fl.get(i), a);
				prevFrameFormals.add(a);
			}

		}
		
		// Allocate new frame pointer 
		tree.NameOfTemp fp_name = new tree.NameOfTemp("%fp");
		framePointer = new tree.TEMP(fp_name);
		tempToRegMap.put(framePointer.temp, specialRegisters.get(SPECIAL_REGISTERS.frame_pointer.ordinal()));
		
		// Allocate stack pointer
		tree.NameOfTemp sp_name = new tree.NameOfTemp("%sp");
		stackPointer = new tree.TEMP(sp_name);
		tempToRegMap.put(stackPointer.temp, specialRegisters.get(SPECIAL_REGISTERS.stack_pointer.ordinal()));
		
		// Have temporary hold return address 
//		returnLocationRegister = tree.TEMP.generateTEMP("`retAddress");
//		tempToRegMap.put(returnLocationRegister.temp, specialRegisters.get(SPECIAL_REGISTERS.return_address_i7.ordinal()));
		
		functionReturnRegister = new tree.TEMP(inRegisters.get(0));
		tempToRegMap.put(functionReturnRegister.temp, inRegisters.get(0));
		
		calleeReturnRegister = new tree.TEMP(outRegisters.get(0));
		tempToRegMap.put(calleeReturnRegister.temp, outRegisters.get(0));
		
		globalZeroRegister = new tree.TEMP(globalRegisters.get(0));
		tempToRegMap.put(globalZeroRegister.temp, globalRegisters.get(0));
		
		globalOneRegister = new tree.TEMP(globalRegisters.get(1));
		tempToRegMap.put(globalOneRegister.temp, globalRegisters.get(1));
		
		globalTwoRegister = new tree.TEMP(globalRegisters.get(2));
		tempToRegMap.put(globalTwoRegister.temp, globalRegisters.get(2));
		
		globalThreeRegister = new tree.TEMP(globalRegisters.get(3));
		tempToRegMap.put(globalThreeRegister.temp, globalRegisters.get(3));
	}

	public tree.TEMP FP() {
		// TODO Auto-generated method stub
		return framePointer;
	}
	
	public tree.TEMP SP() {
		return stackPointer;
	}
	
	public tree.TEMP RV() {
		return functionReturnRegister;
	}
	
	public tree.TEMP G0() {
		return globalZeroRegister;
	}
	
	public tree.TEMP G1() {
		return globalOneRegister;
	}
	
	public tree.TEMP G2() {
		return globalTwoRegister;
	}
	
	public tree.TEMP G3() {
		return globalThreeRegister;
	}
	
	public tree.TEMP getCalleeReturnRegister() {
		return calleeReturnRegister;
	}
	
	public int getWordSize() {
		// Should be 4
		return wordSize;
	}
	
	public int getMaxArgs() {
		return maxArgs;
	}
	
	public void setMaxArgs(int max) {
		maxArgs = max;
	}
	
	public void incrementTempsInFrame() {
		numTempsInFrame++;
	}
	
	public int getTempMemOffset(tree.NameOfTemp t) {
		// Get offset from frame pointer to next 
		// available patch of memory for temps in stack
		// frame
		int offset = 0;
		offset += locals.size();
		offset += 1; // Return address
		offset += tempsInFrame.get(t);
		offset += 1; // +1 because we're subtracting from frame pointer
		return offset;
	}
	
	public void initializeRegisters() {
		
		/* Initialize register tables */
		specialRegisters = new ArrayList<String>() {{
			add("%sp");		// Stack Pointer (%o6)
			add("%fp");		// Frame Pointer (%i6)
			add("%i7");		// return address
			add("%o7");		// return address
			add("%o0");     // return value
		}};
		
		outRegisters = new ArrayList<String>() {{
			add("%o0");
			add("%o1");
			add("%o2");
			add("%o3");
			add("%o4");
			add("%o5");
		}};
		
		localRegisters = new ArrayList<String>() {{
			add("%l0");
			add("%l1");
			add("%l2");
			add("%l3");
			add("%l4");
			add("%l5");
			add("%l6");
			add("%l7");		
		}};
		
		globalRegisters = new ArrayList<String>() {{
			add("%g0");
			add("%g1");
			add("%g2");
			add("%g3");
			add("%g4");
			add("%g5");
			add("%g6");
			add("%g7");
		}};
		
		inRegisters = new ArrayList<String>() {{
			add("%i0");
			add("%i1");
			add("%i2");
			add("%i3");
			add("%i4");
			add("%i5");
		}};		
		
		/* Initialize stack of available registers */
//		for (int i = 3; i < globalRegisters.size(); i++) 
//			availableRegisters.push(globalRegisters.get(i));
		
		/* push available input registers */
		for (int i = 5; i >= formals.size(); i--)
			availableRegisters.push(inRegisters.get(i));
			
		for (int i = 7; i >= 0; i--) 
			availableRegisters.push(localRegisters.get(i));
		
	}
	
	public boolean inRegisterMap(tree.NameOfTemp n) {
		/* Check if temp is in the register mapping alread */
		return tempToRegMap.containsKey(n);
	}
	
	public void addToMap(tree.NameOfTemp n, String s) {
		/* Add a mapping to the register map */
		tempToRegMap.put(n, s);
	}
	
	public boolean registerAvail() {
		/* Sees if there are any general-purpose registers
		 * available in current frame. If there are, call 
		 * findRegister immediately after
		 */
		if (availableRegisters.empty()) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public String popRegister() {
		/* Sees if there are any free general-purpose
		 * registers available and, if so, returns handle
		 * that can be used to map it to the appropriate string
		 */
		if (!(registerAvail())) {
			return null;
		}
		// pop available register
		String regStr = availableRegisters.pop();
		
		// Create a temporary linked to this register 
//		tree.NameOfTemp regName = new tree.NameOfTemp(regStr);
//		tempToRegMap.put(regName, regStr);
		return regStr;
	}
	
	public void addToAvailRegisterStack(String freeReg) {
		// When a register is free, add it back to the stack
//		String freeRegStr = tempToRegMap.get(freeReg);
		availableRegisters.push(freeReg);
	}

	public void allocLocal(Symbol l) {		
		
		int num_alloc = locals.size();
		int offset = num_alloc + 1;
		frame.Access memLocation = new inFrame(offset, wordSize);
		locals.add(memLocation);
		varMap.put(l, memLocation);
	}

	public ArrayList<tree.LABEL> incomingArgs() {
		// TODO Auto-generated method stub
		return null;
	}

	public tree.LABEL returnAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<tree.LABEL> outgoingArgs() {
		// TODO Auto-generated method stub
		return null;
	}
		
	public tree.Exp lookupVar(Symbol s) {
		return varMap.get(s).exp(framePointer);
	}
	
	public void mapTempToRegister(tree.TEMP t, String reg) {
		tempToRegMap.put(t.temp, reg);
	}
	
	public void mapNameOfTempToRegister(tree.NameOfTemp n, String reg) {
		tempToRegMap.put(n, reg);
	}
	
	public String getRegisterMapping(tree.NameOfTemp t) {
		String ret = tempToRegMap.get(t);
		if (ret != null) {
			return ret;
		}
		else {
			return null;
		}
	}
	
	public Map<tree.NameOfTemp, String> getMap() {
		return tempToRegMap;
	}
	
	public void addTempToFrame(tree.NameOfTemp t) {
		/* Adds temp to frame (put in tempInStack hashmap so that,
		 * when the list of instructions needs it, we can all a function
		 * that provides an offset to the frame pointer in place of 
		 * a move register instruction
		 */
		numTempsInFrame++;
		tempsInFrame.put(t, numTempsInFrame);
		
	}
} 


class inFrame extends frame.Access {
	int offset; //offset of variable in frame
	
	public inFrame(int o, int w) {
		offset = o * w;
	}
	
	public tree.Exp exp(tree.Exp framePtr) {
		return new tree.MEM(new tree.BINOP(tree.BINOP.MINUS, framePtr, 
				new tree.CONST((offset))));
	}
}

class inPrevFrame extends frame.Access {
	/* For the 7th and beyond formal args */
	int offset; // offset of variable from frame pointer
	
	public inPrevFrame(int o, int w) {
		offset = o * w;
	}
	
	public tree.Exp exp(tree.Exp framePtr) {
		return new tree.MEM(new tree.BINOP(tree.BINOP.PLUS, framePtr,
				new tree.CONST((offset))));
	}
	
	public frame.Access newPrevFrameAlloc(int o, int wordSize) {
		return new inPrevFrame(o, wordSize);
	}
 
}

class inRegister extends frame.Access {
	tree.TEMP register;
	inRegister(String n) {
		register = new tree.TEMP(n);
	}
	
	inRegister(tree.TEMP t) {
		register = t;
	}
	
	inRegister() {
		register = tree.TEMP.generateTEMP();
	}
	
	public tree.Exp exp(tree.Exp framePtr) {
		return register;
	}
	
}

enum SPECIAL_REGISTERS {
	stack_pointer,
	frame_pointer,
	return_address_i7,
	return_address_o7,
	function_return;
}





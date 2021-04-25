package Sparc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class SparcTemplates {

	public SparcTemplates() {
		// TODO Auto-generated constructor stub
	}
	
	///////////////// SPARC Instruction templates ///////////////
	/////////////////////////////////////////////////////////////

	private final static int
	DEST = 0,
	SRC = 1;

	
	//////////////////// LOAD TEMPLATE //////////////////////////
	static int dest_num = 0;
	static int src_num = 0;
	
	public static String LOAD(String src, String dest) {
		StringJoiner instrString = new StringJoiner(" ");
		instrString.add("LD")
				   .add("[" + src)
				   .add("],")
				   .add(dest);
//				   .add("\n");
		return instrString.toString();
	}
	
	public static String LOAD(int op, String src, String dest, String offset) {
		// Sparch instruction loading M
		/* Writes out a string for sparc instruction loading
		 * MEM 'src' into Register 'dest'
		 */
		Map<Integer, String> opMap = new HashMap<Integer, String>() {{
			put(tree.BINOP.PLUS, "+");
			put(tree.BINOP.MINUS, "-");
		}};
		String opStr = opMap.get(op);
		
//		return "LD M[" + src + " + " + offset + "], " + dest + "\n";

		StringJoiner instrString = new StringJoiner(" ");
		instrString.add("LD")
				   .add("[" + src)
				   .add(opStr)
				   .add(offset)
				   .add("],")
				   .add(dest);
//				   .add("\n");
		return instrString.toString();
	}
		
	

	//////////////////// BINOP TEMPLATE /////////////////////////////////
	public static String OPR(int op, String operand1, String operand2, String dest) {
		Map<Integer, String> opMap = new HashMap<Integer, String>() {{
			put(tree.BINOP.PLUS, "ADD");
			put(tree.BINOP.MINUS, "SUB");
			put(tree.BINOP.MUL, "SMUL");
			put(tree.BINOP.AND, "AND");
		}};
				
//		String opType = opMap.get(op);
//		StringJoiner joiner = new StringJoiner(" ");
//		joiner.add(opType)
//			  .add(operand1 + ",")
//			  .add(operand2 + ",")
//			  .add(dest + "\n");
		String opType = opMap.get(op);
		StringJoiner joiner = new StringJoiner(" ");
		joiner.add(opType)
			  .add(operand1)
			  .add(",")
			  .add(operand2)
			  .add(",")
			  .add(dest);
//			  .add("\n");
		
		return joiner.toString();
	}
		
	//////////////////// STORE TEMPLATE ///////////////////////////////////
	public static String STORE(String src, String dest) {
		StringJoiner joiner = new StringJoiner(" ");
		joiner.add("ST")
			  .add(src + ",")
			  .add("[" + dest + "]");
//			  .add("\n");
		return joiner.toString();
	}
	
	public static String STORE(int op, String src, String dest, String offset) {
		
		Map<Integer, String> opMap = new HashMap<Integer, String>() {{
			put(tree.BINOP.PLUS, "+");
			put(tree.BINOP.MINUS, "-");
		}};
		String opString = opMap.get(op);
		StringJoiner joiner = new StringJoiner(" ");
		joiner.add("ST")
			  .add(src + ",")
			  .add("[" + dest)
			  .add(opString)
			  .add(offset + "]");
//			  .add("\n");
		return joiner.toString();
	}
	
	public static String LABEL(String l) {
		return l + ":\n";
	}
	
	/////////////////////////////// BRANCH TEMPLATE ////////////////////////////////
	public static String BRANCH(int cond, String label) {
		
		Map<Integer, String> condMap = new HashMap<Integer, String>() {{
			put(tree.CJUMP.EQ, "BE");
			put(tree.CJUMP.NE, "BNE");
			put(tree.CJUMP.LT, "BL");
			put(tree.CJUMP.GT, "BG");
			put(tree.CJUMP.LE, "BLE");
			put(tree.CJUMP.GE, "BGE");
			put(tree.CJUMP.ULT, "BLU");
			put(tree.CJUMP.ULE, "BLEU");
			put(tree.CJUMP.UGT, "BGU");
			put(tree.CJUMP.UGE, "BGEU");
		}};
		
		StringJoiner joiner = new StringJoiner(" ");

		joiner.add(condMap.get(cond)) 
			  .add(label + "\n")
			  .add("nop");
		
		return joiner.toString();
	}
	
	///////////////////////////// BRANCH TEMPLATE ///////////////////////
	
	
	////////////////////////////// CALL TEMPLATE ///////////////////////
	public static String CALL(String label) {
		
		StringJoiner joiner = new StringJoiner(" ");
		joiner.add("CALL")
			  .add(label + "\n")
			  .add("nop");
		
		return joiner.toString();
	}
	
	///////////////////////////CALL TEMPLATE ////////////////////////
	
	////////////////////////// LABEL TEMPLATE ///////////////////////
	public static String JUMP(String label) {
		
		StringJoiner joiner = new StringJoiner(" ");
		joiner.add("BA")
			  .add(label + "\n")		  
			  .add("nop");
		
		return joiner.toString();
	}
	/////////////////////// LABEL TEMPLATE /////////////////////////////
	
	////////////////////////// CMP TEMPLATE //////////////////////////
	public static String CMP(String exp1, String exp2) {
		
		StringJoiner joiner = new StringJoiner(" ");
		joiner.add("CMP")
			  .add(exp1 + ",")
			  .add(exp2);
		
		return joiner.toString();
	}
	//////////////////////////// CMP TEMPLATE ////////////////////////
	
	////////////////// MOVE TEMPLATE //////////////////////////////////////
	public static String MOV(String src, String dest) {
		
		StringJoiner joiner = new StringJoiner(" ");
		joiner.add("MOV")
		      .add(src)
		      .add(",")
		      .add(dest);
//		      .add("\n");
		
		return joiner.toString();
	}

	/////////////////// OR TEMPLATE ///////////////////////////////////////
	public static String OR(String src1, String src2, String dest) {
		
		
		StringJoiner joiner = new StringJoiner(" ");
		joiner.add("OR")
			  .add(src1)
			  .add(",")
			  .add(src2);
//			  .add("\n");
		
		return joiner.toString();
			  
	}
	
	/////////////////// SET TEMPLATE ////////////////////////////////////////
	// Used in function prelude
	public static String SET(String varName, int num) {
		// variable name used in prelude, number to assign to it 
		StringJoiner joiner = new StringJoiner(" ");
		joiner.add(".set")
			  .add(varName + "," + Integer.toString(num));
		return joiner.toString();
	}
	
	////////////////// SAVE TEMPLATE /////////////////////////////////////////
	public static String SAVE(String src, List<String> vars, String dest) {
		StringJoiner joiner = new StringJoiner(" ");
		
		// Construct variable offset statement
		String offset = "-4*(";
		for (String v: vars) {
			offset += v + "+";
		}
		offset = offset.substring(0, offset.length() - 1);
		offset+= ")&-8";
		
		joiner.add("save")
			  .add(src + ",")
			  .add(offset + ",")
			  .add(dest);
		
		return joiner.toString();		
	}
	
	///////////////// CLR TEMPLATE //////////////////////////
	public static String CLR(String reg) {
		StringJoiner joiner = new StringJoiner(" ");
		joiner.add("clr")
			  .add(reg);
		return joiner.toString();
	}
	
	//////////////// SET TEMPLATE //////////////////////////////
	public static String SET(String constant, String register) {
		StringJoiner joiner = new StringJoiner(" ");
		joiner.add("SET")
			  .add(constant + ",")
			  .add(register);
		return joiner.toString();
	}
	
	////////////////// RET TEMPLATE ///////////////////////////////////////////
	public static String RET() {
		/* Honestly, don't really need this template. This is mainly 
		 * just for completion's sake
		 */
		return "ret";
	}
	
	//////////////////////// RESTORE TEMPLATE ////////////////////////////////
	public static String RESTORE() {
		/* Don't need this one either */
		return "restore";
	}
}

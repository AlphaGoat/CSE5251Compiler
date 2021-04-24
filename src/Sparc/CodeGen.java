package Sparc;

import java.util.List;
import java.util.StringJoiner;
import java.lang.StringBuilder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import assem.LabelInstruction;
import assem.MoveInstruction;
import assem.OperationInstruction;
import tree.CONST;
import tree.NameOfTemp;
import tree.TEMP;
import visitors.MethodFragment;

public class CodeGen {
	
	SparcFrame frame;

//	List<tree.TEMP> outgoingArgs = new ArrayList<tree.TEMP>();
	
	private final static int
		DEST = 0,
		SRC = 1;
	
	private static int temp_count = 0;
	
//	tree.TEMP stackPointer;
	
	private List<assem.Instruction> ilist = new ArrayList<assem.Instruction>(); 
	private assem.Instruction last = null;
	
	String fileToWrite;
	StringBuilder codeContents = new StringBuilder();

	private void emitComment(String s) {
		codeContents.append("!" + s + "\n");
	}
	
	private void emit(assem.Instruction inst) {
		/*Collect emitted instructions in list and write to file */
		ilist.add(inst);
		codeContents.append(inst.format());
		codeContents.append('\n');	
	}
	
	private String newTempGenerator(int type, int num) {
		if (type == DEST) {
//			tree.NameOfTemp temp = new tree.NameOfTemp("di" + Integer.toString(destNum));
			String dest_str = "`d" + Integer.toString(num);
			return dest_str;
		}
		
		else {
			String src_str = "`s" + Integer.toString(num);
			return src_str;
		}
		
	}
	
	
	private String tempMap(NameOfTemp t) {
		
		String regName = frame.getRegisterMapping(t);
		if (regName == null)
			return t.toString();
		
		return regName;
		
	}
	
//	public void writeToFile() {
//		// Writes contents of instructions to file
//		try {
//			FileWriter instructionWriter = new FileWriter(fileToWrite);
//			instructionWriter.append(codeContents);
//			instructionWriter.close();
//		}
//		catch (IOException e) {
//			System.err.println(e);
//		}
//		
//	}
	
	public StringBuilder getCodeContents() {
		return codeContents;
	}
	
	public CodeGen(List<tree.Stm> methodTrace, Sparc.SparcFrame f) {
//		frame = (Sparc.SparcFrame) mf.getMethodFrame();
		frame = f;
		
		// Start emitting code 
//		tree.Stm body = mf.getBody(); 
		for (tree.Stm t: methodTrace) {
			munchStm(t);
		}
	}


	
	/* MUNCH STMs */
	private void munchStm(tree.Stm s) {
		
		if (s instanceof tree.SEQ) {
			munchSeq((tree.SEQ) s);
		}
		else if (s instanceof tree.MOVE) {
			munchMove((tree.MOVE) s);
		}
		
		else if (s instanceof tree.JUMP) {
			//Munch args 
			
			tree.NAME jmpLabel = (tree.NAME) ((tree.JUMP)s).exp;
			tree.NameOfLabel jmpNameAsLabel= jmpLabel.label;
			String funcString = jmpNameAsLabel.toString();
			
			List<tree.NameOfLabel> labelNames = new ArrayList<tree.NameOfLabel>();
			labelNames.add(jmpNameAsLabel);
			
			// TODO: check that this is right
			assem.Instruction instr = new assem.OperationInstruction(
					SparcTemplates.JUMP(funcString), null,
					null, labelNames);
			
			emit(instr);
		}
		
		else if (s instanceof tree.CJUMP) {
			
			munchCJUMP((tree.CJUMP)s);	
		
		}
		
		else if (s instanceof tree.LABEL){
			munchLabel((tree.LABEL) s);
			
		}
		
		else if (s instanceof tree.EVAL) {
			munchExp( ((tree.EVAL) s).exp );
		}
		
		else {
			System.err.println("ERROR: WE HAVE AN UNCAUGHT EMIT STM");
			System.err.println(s.toString());
		}
	}
	
	/* MUNCH FOR TOP NODE STM */
	
	/* SEQ(stm1, stm2) */
	private void munchSeq(tree.SEQ s) {
		munchStm(s.left); munchStm(s.right);
	}
	
	private void munchCJUMP(tree.CJUMP s) {
		
		// CJUMP(CONST, CONST)
		if (s.right instanceof tree.CONST &&
			s.left instanceof tree.CONST) {
		
			int right = ((tree.CONST) s.right).value;
			int left = ((tree.CONST) s.left).value;
			
			String comment = "CJUMP(CONST, CONST) --munchCJUMP";
			comment.replaceAll("[\\n ]", "");
			
			assem.Instruction cmpInstr = new assem.OperationInstruction(
				SparcTemplates.CMP(Integer.toString(right), 
				Integer.toString(left)), comment);
			
			emit(cmpInstr);	

		}
		
		// CJUMP(EXP, CONST)
		else if (s.right instanceof tree.CONST) {
			int right = ((tree.CONST) s.right).value;
			tree.NameOfTemp leftReg = munchExp(s.left);
			
			String comment = String.format("CJUMP(%s, %d) --munchCJUMP",
					leftReg.toString(), right);
			comment.replaceAll("[\\n ]", "");

//			assem.Instruction cmpInstr = new assem.OperationInstruction(
//					SparcTemplates.CMP(Integer.toString(right),
//					newTempGenerator(DEST, 0)), comment, leftReg, null);
			assem.Instruction cmpInstr = new assem.OperationInstruction(
					SparcTemplates.CMP(Integer.toString(right),
					newTempGenerator(SRC, 0)), comment, null, leftReg);
			
			emit(cmpInstr);
			
		}
		
		// CJUMP(CONST, EXP)
		else if (s.left instanceof tree.CONST) {
			int left = ((tree.CONST) s.right).value;
			tree.NameOfTemp rightReg = munchExp(s.right);
			
			String comment = String.format("CJUMP(%s, %d) --munchCJUMP",
					left, rightReg.toString());
			comment.replaceAll("[\\n ]", "");

//			assem.Instruction cmpInstr = new assem.OperationInstruction(
//					SparcTemplates.CMP(Integer.toString(left),
//					newTempGenerator(DEST, 0)), comment, rightReg, null);
			assem.Instruction cmpInstr = new assem.OperationInstruction(
					SparcTemplates.CMP(Integer.toString(left),
					newTempGenerator(SRC, 0)), comment, null, rightReg);
			
			emit(cmpInstr);
		}
		
		else {
			tree.NameOfTemp leftReg = munchExp(s.left);
			tree.NameOfTemp rightReg = munchExp(s.right);
			String comment = "CMP " + leftReg.toString() + " -- " + rightReg.toString();
			comment.replaceAll("[\\n ]", "");

			assem.Instruction cmpInstr = new assem.OperationInstruction(
					SparcTemplates.CMP(newTempGenerator(SRC, 0),
					newTempGenerator(SRC, 1)), comment, null, rightReg, 
					leftReg);
			
			emit(cmpInstr);
		}
		
		String Comment = "BRANCH --CJUMP";
		Comment.replaceAll("[\\n ]", "");

		assem.Instruction branchInstr = new assem.OperationInstruction(
				SparcTemplates.BRANCH(s.relop, s.iftrue.toString()),
				Comment);
		
		emit(branchInstr);
		
	}
	
	
	private tree.NameOfTemp munchCall(tree.CALL e) {
		
		tree.NAME funcName = (tree.NAME) e.func;
		tree.NameOfLabel funcNameAsLabel= funcName.label;
		String funcString = funcNameAsLabel.toString();
		emitComment(String.format("Prepare to call %s", funcString));
		
		//Munch args 
		List<tree.NameOfTemp> tempList = munchArgs(e.args.toList());
		
		List<tree.NameOfLabel> labelNames = new ArrayList<tree.NameOfLabel>();
		labelNames.add(funcNameAsLabel);
		
//		List<tree.NameOfTemp> returnTemp = new ArrayList<tree.NameOfTemp>();
//		tree.NameOfTemp returnRegister = new tree.TEMP("%rv").temp;
//		tree.TEMP returnRegisterTemp = frame.RV();
//		tree.NameOfTemp returnName = returnRegisterTemp.temp;
//		returnTemp.add(returnName);
		
		// Add return register to map with mapping to function output register
//		frame.tempToRegMap(new tree.TEMP(returnRegister), "%o0");
		
		// TODO: check that this is right
//		assem.Instruction instr = new assem.OperationInstruction(
//				SparcTemplates.CALL(funcString), returnTemp,
//				tempList, labelNames);
		String comment = String.format("CALL %s -- munchCALL", funcString);
		comment.replaceAll("[\\n ]", "");

		assem.Instruction instr = new assem.OperationInstruction(
				SparcTemplates.CALL(funcString), comment);
		
		emit(instr);
		
		// Return value of expression in returnee call register (%o0)
		tree.TEMP calleeReturn = frame.getCalleeReturnRegister();
		emitComment(String.format("Exiting call %s\n", funcString));
		
		return calleeReturn.temp;	
	}
	
	private List<tree.NameOfTemp> munchArgs(LinkedList<tree.Exp> args) {
		/* Place first 7 args into registers. If there are more 
		 * than that, adjust off stack pointer
		 */
		List<tree.NameOfTemp> argList = new ArrayList<tree.NameOfTemp>();
		for (int i = 0; i < args.size(); i++) {
			/* Temp array list */
			
			if (i < 6) {
				
				/* Load args into outgoing registers */
				tree.NameOfTemp src = munchExp(args.get(i));
				String outRegister = frame.outRegisters.get(i);
				
				// Convert to inRegister 
				tree.NameOfTemp dest = new tree.TEMP(outRegister).temp;
				
				// Map this to register in frame
				frame.mapNameOfTempToRegister(dest, outRegister);
				
				/* Generate LOAD instruction to load into
				 * outgoing register
				 */
				String comment = "Move contents into outgoing register -- munchArgs";
				comment.replaceAll("[\\n ]", "");

				assem.Instruction loadInstr = new assem.OperationInstruction(
						SparcTemplates.MOV(newTempGenerator(SRC, 0), 
						newTempGenerator(DEST, 0)),
						comment, dest, src);
				emit(loadInstr);
				
				argList.add(dest);
			}
			
			// For args greater than 6, Move to stack memory
			else {
				// stack pointer (end of frame) should point to first past 6 arg, so offset
				// is the placement of the arg in the list subtracted by 6
				int offset = (i - 6) * frame.getWordSize();
				
				String comment = "Loading args > 6 at end of current frame -- munchArgs";
				comment.replaceAll("[\\n ]", "");

				tree.NameOfTemp src = munchExp(args.get(i));
				
				assem.Instruction moveInstr = new assem.MoveInstruction(
						SparcTemplates.STORE(tree.BINOP.PLUS, 
						newTempGenerator(SRC, 0), newTempGenerator(DEST, 0),
						Integer.toString(offset)), comment,
						frame.FP().temp, src);			
				emit(moveInstr);
			}
					
		}
		
		return argList;		
	}
	
	private void munchMove(tree.MOVE s) {
	
//		emitComment("In munchMove");
//		if (s.dst instanceof tree.TEMP &&
//				s.src instanceof tree.CALL) {
//			
//			tree.NameOfTemp returnRegister = munchCall((tree.CALL) s.src);
//			tree.NameOfTemp destRegister = ((tree.TEMP)s.dst).temp;
//			
//			String comment = "MOVE(TEMP, CALL) -- munchMove";
//			
//			assem.Instruction instr = new assem.MoveInstruction(
//					SparcTemplates.STORE(returnRegister.toString(), destRegister.toString()),
//					destRegister, returnRegister);
//			
//			emit(instr);
//		}
//		
		if (s.dst instanceof tree.MEM &&
			((tree.MEM)s.dst).exp instanceof tree.BINOP) {
			
		
			tree.BINOP dstExp = (tree.BINOP)((tree.MEM)s.dst).exp;
			
			// MOVE(MEM( +(e1, CONST(i))), e2)
			if (dstExp.right instanceof tree.CONST) {
				int op = dstExp.binop;
				int offset = ((tree.CONST)dstExp.right).value;
				tree.Exp src = s.src;
				tree.Exp dst = dstExp.left;
					
//				String comment = String.format("MOVE(MEM( +(%s, %d))) -- munchMove",
//						src.toString(), offset);
//				comment.replaceAll("[\\n]", "");

				
				List<tree.NameOfTemp> srcList = new ArrayList<tree.NameOfTemp>() {{
					add(munchExp(src));
					add(munchExp(dst));
				}};
					
				assem.Instruction instr = new assem.OperationInstruction(
						SparcTemplates.STORE(op, newTempGenerator(SRC, 0), 
						newTempGenerator(SRC, 1), Integer.toString(offset)),
						null, 
						null, 
						srcList);
					
				emit(instr);
			}
			
			// MOVE(MEM( +(CONST(i), e1)), e2)
			else if (dstExp.left instanceof tree.CONST ) {
				
//					String dest_str = newTempGenerator(DEST, 0);
//					String src_str = newTempGenerator(SRC, 0);
				
					int op = dstExp.binop;
					int offset = ((tree.CONST) dstExp.left).value;
					tree.Exp src = s.src;
					tree.Exp dest = dstExp.right;
					
					String comment = "MOVE(MEM( +(Const(i), dest), src)) -- munchMove";
					comment.replaceAll("[\\n]", "");

					
					List<tree.NameOfTemp> srcList = new ArrayList<tree.NameOfTemp>() {{
						add(munchExp(src));
						add(munchExp(dest));
					}};
					
//					assem.Instruction instr = new assem.MoveInstruction(
//							SparcTemplates.STORE(op, src_str, 
//							dest_str, Integer.toString(offset)),
//							comment, 
//							munchExp(dest), 
//							munchExp(src));
					
					assem.Instruction instr = new assem.OperationInstruction(
							SparcTemplates.STORE(op, newTempGenerator(SRC, 0), 
							newTempGenerator(SRC, 1), Integer.toString(offset)),
							comment, 
							null, 
							srcList);
					
					emit(instr);
					
				}	
			
			// MOVE (MEM( +(e1, e2)), e3)
			else {
				
				tree.Exp dest1 = dstExp.left;
				tree.Exp dest2 = dstExp.right;
				tree.Exp src = s.src;
				
//				List<tree.NameOfTemp> destList = new ArrayList<tree.NameOfTemp>() {{
//					add(munchExp(dest1));
//					add(munchExp(dest2));
//				}};
//				
//				List<tree.NameOfTemp> srcList = new ArrayList<tree.NameOfTemp>() {{
//					add(munchExp(src));
//				}};
				List<tree.NameOfTemp> srcList = new ArrayList<tree.NameOfTemp>() {{
					add(munchExp(src));
					add(munchExp(dest1));
					add(munchExp(dest2));
				}};
				
				String comment = "MOVE(MEM( +(e1, e2)), e3)";
				comment.replaceAll("[\\n ]", "");

//				assem.Instruction instr = new assem.OperationInstruction(
//						SparcTemplates.STORE(dstExp.binop, newTempGenerator(SRC, 0), 
//											 newTempGenerator(DEST, 1), newTempGenerator(DEST,2)),
//											 comment, destList, srcList);
				
				assem.Instruction instr = new assem.OperationInstruction(
						SparcTemplates.STORE(dstExp.binop, newTempGenerator(SRC, 0), 
											 newTempGenerator(SRC, 1), newTempGenerator(SRC,2)),
											 comment, null, srcList);
				
				emit(instr);
				
			}
				
		}
		// MOVE(MEM(e1), MEM(e2))
		else if (s.dst instanceof tree.MEM && s.src instanceof tree.MEM) {
			
			tree.Exp destExp = ((tree.MEM) s.dst).exp;
			tree.Exp srcExp = ((tree.MEM) s.src).exp;
						
			tree.NameOfTemp destMem = munchExp(destExp);
			tree.NameOfTemp srcMem = munchExp(srcExp);
			
			
			// INTERMEDIATE TEMP ////////
//			tree.NameOfTemp intermediateTemp = frame.globalOneRegister.temp;
			tree.NameOfTemp intermediateTemp = tree.TEMP.generateTEMP("%TEMP").temp;
					
			String intermediateComment = "Part of MOVE(MEM(e1), MEM(e2)).";
			intermediateComment += "Intermediate -- munchMove";
			intermediateComment.replaceAll("[\\n]", "");
			
			assem.Instruction intermediate = new assem.OperationInstruction(
					SparcTemplates.LOAD(newTempGenerator(SRC, 0), newTempGenerator(DEST, 0)),
					intermediateComment, intermediateTemp, srcMem);
			emit(intermediate);
			
			String comment = String.format("MOVE(MEM(%s), MEM(%s)) -- munchMove", 
								destMem.toString(), intermediateTemp.toString());
			comment.replaceAll("[\\n]", "");

			
			List<tree.NameOfTemp> srcList = new ArrayList<tree.NameOfTemp>() {{
				add(intermediateTemp);
				add(destMem);
			}};
			
//			assem.Instruction instr = new assem.MoveInstruction(
//					SparcTemplates.STORE(newTempGenerator(SRC, 0), newTempGenerator(DEST, 0)),
//					comment, destMem, intermediateTemp);
			
			assem.Instruction instr = new assem.OperationInstruction(
					SparcTemplates.STORE(newTempGenerator(SRC, 0), newTempGenerator(DEST, 0)),
					comment, null, srcList);
			
			emit(instr);
			
		}
		
		// MOVE(MEM(CONST(i)), e2)
		else if (s.dst instanceof tree.MEM &&
				((tree.MEM) s.dst).exp instanceof tree.CONST) {
			
			// Need local to load constant
			// Need to add from global to get constant
//			tree.TEMP l0 = localRegisters.get(0);
			tree.NameOfTemp imDest = tree.NameOfTemp.generateTemp("%TEMP" + 
							Integer.toString(temp_count));
			
			/////// TEMP /////////////////////
//	//		tree.NameOfTemp imDest = frame.popRegister();
			//////////////////////////////////
			
//			String g0_string = frame.globalRegisters.get(0);
			
//			tree.TEMP g0Temp = new tree.TEMP(g0_string);
//			tree.NameOfTemp g0_name = g0Temp.temp;
//			frame.mapNameOfTempToRegister(g0_name, g0_string);

			tree.TEMP g0_name = frame.globalZeroRegister;
					
			int constant = ((tree.CONST)((tree.MEM) s.dst).exp).value;
			
//			String intermediateComment = "Add Const to %l0.";
//			intermediateComment += " Part of MOVE(MEM(CONST(i)), e2) -- munchMOVE";
//			
//			assem.Instruction intermediate = new assem.OperationInstruction(
//					SparcTemplates.OPR(tree.BINOP.PLUS, newTempGenerator(SRC, 0), 
//					Integer.toString(constant), newTempGenerator(DEST, 0)), intermediateComment, 
//					imDest, 
//					g0_name.temp);
//			
			// EMIT intermediate
//			emit(intermediate);
			
			tree.Exp src = s.src;
			
			String comment = "MOVE(Const(i), e2) -- munchMOVE";
			
//			assem.Instruction instr = new assem.MoveInstruction(
//					SparcTemplates.STORE(munchExp(src).toString(), imDest.toString()),
//					comment, new tree.NameOfTemp(imDest.toString()), munchExp(src));
//			
//			assem.Instruction instr = new assem.MoveInstruction(
//					SparcTemplates.STORE(tree.BINOP.PLUS, newTempGenerator(SRC, 0),
//									newTempGenerator(DEST, 0), Integer.toString(constant)),
//									g0_name.temp, munchExp(src));
			
			assem.Instruction instr = new assem.OperationInstruction(
					SparcTemplates.STORE(tree.BINOP.PLUS, newTempGenerator(SRC, 0),
									Integer.toString(constant), Integer.toString(0)),
									comment, null, munchExp(src));
			emit(instr);
		}
		
		else if (s.dst instanceof tree.MEM) {
			
			tree.NameOfTemp src = munchExp(s.src);
			tree.NameOfTemp dest = munchExp(((tree.MEM) s.dst).exp);
			
			List<tree.NameOfTemp> srcList = new ArrayList<tree.NameOfTemp>() {{
				add(src);
				add(dest);
			}};
			
			String comment = "MOVE(MEM(e1), e2) -- muchMOVE";
//			assem.Instruction instr = new assem.MoveInstruction(
//					SparcTemplates.STORE(newTempGenerator(SRC, 0), newTempGenerator(DEST, 0)),
//					comment, dest, src);
//			
			assem.Instruction instr = new assem.OperationInstruction(
					SparcTemplates.STORE(newTempGenerator(SRC, 0), newTempGenerator(SRC, 1)),
					comment, null, srcList);
			
			emit(instr);
			
		}
		
		else if (s.dst instanceof tree.TEMP) {
			
			tree.NameOfTemp dest = ((tree.TEMP) s.dst).temp;
			tree.NameOfTemp src = munchExp(s.src);
									
			// GLOBAL REGISTER G0
//			tree.TEMP g0 = frame.globalZeroRegister;
//			tree.TEMP g0 = frame.G0();
//			String g0_string = frame.globalRegisters.get(0);			

			// GLOBAL REGISTER G0
				
//		String comment = "MOVE(" + dest.toString() + "," + src.toString() + ") -- munchMove";
//			comment.replaceAll("[\\n]", "");
//			assem.Instruction instr = new assem.OperationInstruction(
//					SparcTemplates.OPR(tree.BINOP.PLUS, newTempGenerator(SRC, 0),
//							newTempGenerator(SRC, 1), newTempGenerator(DEST, 0)),
//							comment, dest, src, g0.temp);
			
			assem.Instruction instr = new assem.OperationInstruction(
					SparcTemplates.MOV(newTempGenerator(SRC, 0), newTempGenerator(DEST, 0)),
					dest, src);
			
			emit(instr);
		}
		
	}
	

	void munchLabel(tree.LABEL s) {
		tree.NameOfLabel labelName = s.label;
		assem.Instruction instr = new assem.LabelInstruction(
				SparcTemplates.LABEL(labelName.toString()), labelName);	
		emit(instr);
	}
		
	/* TODO: Convert Temp expressions into Temp Lists (do
	 * we need to do that?)
	 */
	
	/* MUNCH EXPRESSIONS */
	private tree.NameOfTemp munchExp(tree.Exp e) {
		if (e instanceof tree.MEM) {
			return munchMem((tree.MEM) e);
		}
		else if (e instanceof tree.BINOP) {
			return munchBinop((tree.BINOP) e);
		}
		else if (e instanceof tree.CALL) {
			return munchCall((tree.CALL) e);			
			// Get return register 			
		}		
		else if (e instanceof tree.CONST) {
			return munchConst((tree.CONST) e);
		}
		else if (e instanceof tree.TEMP) {
			return munchTemp((tree.TEMP) e);
		}
		else {
			System.err.println("Unexpected Expression: "); 
			System.err.println(e.toString());
			return null;
		}
	}

	private tree.NameOfTemp munchMem(tree.MEM e) {
		
		// MEM( +(e1, CONST))
		if ((e.exp instanceof tree.BINOP) &&
			(((tree.BINOP) e.exp).right instanceof tree.CONST)) {
			
			int op = ((tree.BINOP) e.exp).binop;

//			// TEMPORARY -- TO BE CONVERTED TO REAL REGISTER LATER
			tree.NameOfTemp dest = tree.NameOfTemp.generateTemp("%TEMP");
			temp_count++;
//			
//			String dest_string = frame.getRegisterMapping(dest);
			// TEMPORARY -- TO BE CONVERTED TO REAL REGISTER LATER
			
//			tree.NameOfTemp dest = frame.popRegister();
			
			int offset = ((tree.CONST) ((tree.BINOP) e.exp).right).value;
			
			// Get src expression
			tree.Exp srcExp = ((tree.BINOP) e.exp).left;
			tree.NameOfTemp src = munchExp(srcExp);
			
			String comment = "MEM( +(" + src.toString() + "," + Integer.toString(offset) + ") --munchMem";
			
			// If a dest register was available, load source to dest register
			assem.Instruction instr = new assem.MoveInstruction(
										SparcTemplates.LOAD(op,
										newTempGenerator(SRC, 0), newTempGenerator(DEST, 0), 
									    Integer.toString(offset)), comment, dest,
										src);

			emit(instr);
			return dest;
		}
		
		// MEM( +(CONST, e1))
		else if ((e.exp instanceof tree.BINOP) &&
				(((tree.BINOP) e.exp).left instanceof tree.CONST)) {
			
			// TEMPORARY -- TO BE CONVERTED TO REAL REGISTER LATER
			tree.NameOfTemp dest = tree.NameOfTemp.generateTemp("%TEMP" + 
								temp_count);
			temp_count++;	
			
//			tree.NameOfTemp dest = frame.popRegister();
			
//			String dest_string = frame.getRegisterMapping(dest);
			// TEMPORARY -- TO BE CONVERTED TO REAL REGISTER LATER
					
			int op = ((tree.BINOP) e.exp).binop;
			int offset = ((tree.CONST)((tree.BINOP) e.exp).left).value;
			
			
			// Get src expression
			tree.Exp srcExp = ((tree.BINOP) e.exp).right;
			tree.NameOfTemp src = munchExp(srcExp);
			
			String comment = "MEM( +(" + Integer.toString(offset) + ", " + src.toString() + ") --munchMem";
			
			assem.Instruction instr = new assem.MoveInstruction(
					SparcTemplates.LOAD(op, newTempGenerator(SRC, 0), newTempGenerator(DEST, 0), 
							Integer.toString(offset)), comment, dest, src);
			
			emit(instr);
			
			return dest;
		}
		
		// MEM( CONST )
		else if (e.exp instanceof tree.CONST) {
			
//			 TEMPORARY -- TO BE CONVERTED TO REAL REGISTER LATER
			tree.NameOfTemp dest = tree.NameOfTemp.generateTemp("%TP" + 
							temp_count);
			temp_count++;
//			String dest_string = frame.getRegisterMapping(dest);
//			tree.NameOfTemp dest = frame.popRegister();
			// TEMPORARY -- TO BE CONVERTED TO REAL REGISTER LATER

			// GLOBAL REGISTER G0
//			String g0_string = "%g0";			
			tree.TEMP g0 = frame.globalZeroRegister;
			// GLOBAL REGISTER G0

			
			// Apply offset to register g0
			int offset = ((tree.CONST) e.exp).value;

						
			String comment = "%g0 + CONST -> " + dest.toString();
			
			assem.Instruction instr = new assem.MoveInstruction(
					SparcTemplates.LOAD(tree.BINOP.PLUS, newTempGenerator(SRC, 0), newTempGenerator(DEST, 0),
					Integer.toString(offset)), comment,
					dest, g0.temp);
			
			emit(instr);
			
			return dest;
			
		}
		
		// MEM( e )
		else {
			
			// TEMPORARY -- TO BE CONVERTED TO REAL REGISTER LATER		
			tree.NameOfTemp dest = tree.NameOfTemp.generateTemp("%TP" + 
						temp_count);
			temp_count++;
//			String dest_string = frame.getRegisterMapping(dest);
			// TEMPORARY -- TO BE CONVERTED TO REAL REGISTER LATER
			
//			tree.NameOfTemp dest = frame.popRegister();
			
			tree.Exp srcExp = e.exp;
			tree.NameOfTemp src = munchExp(srcExp);
			
			String comment = String.format("MEM(%s) -- munchMEM",
					src.toString());
			
			assem.Instruction instr = new assem.MoveInstruction(
					SparcTemplates.LOAD(newTempGenerator(SRC, 0), newTempGenerator(DEST, 0)),
					comment, dest, src);
			
			emit(instr);
			
			return dest;
		}
		
	}
	
	private tree.NameOfTemp munchBinop(tree.BINOP e) {
				
		// +( e1, CONST)
		if (e.right instanceof tree.CONST) {
			
			// TEMPORARY -- TO BE CONVERTED TO REAL REGISTER LATER		
			tree.NameOfTemp dest = tree.NameOfTemp.generateTemp("%TEMP" + 
						Integer.toString(temp_count));
			temp_count++;
			String dest_string = frame.getRegisterMapping(dest);
			// TEMPORARY -- TO BE CONVERTED TO REAL REGISTER LATER
		
			int op = e.binop;
			tree.Exp operand1 = e.left;
			int operand2 = ((tree.CONST) e.right).value;
			
			// NEEDS TO BE CONVERTED TO REAL REGISTER LATER
			tree.NameOfTemp src = munchExp(operand1);
			String src_string = frame.getRegisterMapping(src);
			
			String comment = "+( e1, CONST) --> " + dest.toString() + " (munchBINOP)";
			
			assem.Instruction instr = new assem.MoveInstruction(
					SparcTemplates.OPR(op, newTempGenerator(SRC, 0), 
									   Integer.toString(operand2),
									   newTempGenerator(DEST, 0)), comment, dest,
									   src);
			
			emit(instr);
			
			return dest;			
		}
		
		// +( CONST, e1)
		else if (e.left instanceof tree.CONST) {
		
			// TEMPORARY -- TO BE CONVERTED TO REAL REGISTER LATER
			tree.NameOfTemp dest = tree.NameOfTemp.generateTemp("%TP" 
					+ Integer.toString(temp_count));
			temp_count++;
			String dest_string = frame.getRegisterMapping(dest);
			// TEMPORARY -- TO BE CONVERTED TO REAL REGISTER LATER
			
			tree.Exp operand1 = e.right;
			tree.NameOfTemp src = munchExp(operand1);
			String src_string =frame.getRegisterMapping(src);
			
			int op = e.binop;
			int operand2 = ((tree.CONST) e.left).value;
			
			String comment = "+( CONST, e1) -- munchBinop";
			
			assem.Instruction instr = new assem.MoveInstruction(
					SparcTemplates.OPR(op, newTempGenerator(SRC, 0), 
									   Integer.toString(operand2),
									   newTempGenerator(DEST, 0)), comment, dest, 
									   src);
			
			emit(instr);
			
			return dest;
		}
		
		// +( e1, e2)
		else {
			
			int op = e.binop;
			tree.Exp operand1 = e.left;
			tree.Exp operand2 = e.right;
			
			// TEMPORARY -- TO BE CONVERTED TO REAL REGISTER LATER
			tree.NameOfTemp dest = tree.NameOfTemp.generateTemp("%TP"
					+ Integer.toString(temp_count));
			temp_count++;
			
			String dest_string = frame.getRegisterMapping(dest);
			// TEMPORARY -- TO BE CONVERTED TO REAL REGISTER LATER
			
			tree.NameOfTemp src1 = munchExp(operand1);
			String src_string1 =frame.getRegisterMapping(src1);
			
			tree.NameOfTemp src2 = munchExp(operand2);
			String src_string2 = frame.getRegisterMapping(src2);

			String comment = "+( e1, e2) -- munchBinop";
			
			assem.Instruction instr = new assem.OperationInstruction(
					SparcTemplates.OPR(op, newTempGenerator(SRC, 0), 
						newTempGenerator(SRC, 1), 
						newTempGenerator(DEST, 0)), comment, dest,
						src1, src2);
			
			emit(instr);
			
			return dest;
		}
		
	}
	
	// TOP NODE: CONST
	private tree.NameOfTemp munchConst(tree.CONST e) {
		
		// TEMPORARY -- TO BE CONVERTED TO REAL REGISTER LATER			
		tree.NameOfTemp dest = tree.NameOfTemp.generateTemp("%TEMP" +
					Integer.toString(temp_count));
		temp_count++;
		// TEMPORARY -- TO BE CONVERTED TO REAL REGISTER LATER
	
		// GLOBAL REGISTER G0
		tree.TEMP g0_temp = frame.globalZeroRegister;
		
		int constant = e.value;
		
		String comment = "CONST -- munchConst";
		
		assem.Instruction instr = new assem.OperationInstruction(
				SparcTemplates.MOV(Integer.toString(constant), 
				newTempGenerator(DEST, 0)), 
				comment, dest);
				
		emit(instr);
		
		return dest;
	}
	
	// TOP NODE: TEMP (Return register)
	private tree.NameOfTemp munchTemp(tree.TEMP e) {
		// No emitting done here, return TEMP to previous level of 
		// max munch tree
		return e.temp;
	}
	
	public Sparc.SparcFrame getFrame() {
		// get modified frame with new temp mappings
		return frame;
	}
	
	public List<assem.Instruction> getInstructionList() {
		// get list of instructions
		return ilist;
	}
 	

}

//class SparcTemplates {
//
//	///////////////// SPARC Instruction templates ///////////////
//	/////////////////////////////////////////////////////////////
//
//	private final static int
//	DEST = 0,
//	SRC = 1;
//
//	
//	//////////////////// LOAD TEMPLATE //////////////////////////
//	static int dest_num = 0;
//	static int src_num = 0;
//	
//	public static String LOAD(String src, String dest) {
//		StringJoiner instrString = new StringJoiner(" ");
//		instrString.add("LD")
//				   .add("[" + src)
//				   .add("],")
//				   .add(dest);
////				   .add("\n");
//		return instrString.toString();
//	}
//	
//	public static String LOAD(int op, String src, String dest, String offset) {
//		// Sparch instruction loading M
//		/* Writes out a string for sparc instruction loading
//		 * MEM 'src' into Register 'dest'
//		 */
//		Map<Integer, String> opMap = new HashMap<Integer, String>() {{
//			put(tree.BINOP.PLUS, "+");
//			put(tree.BINOP.MINUS, "-");
//		}};
//		String opStr = opMap.get(op);
//		
////		return "LD M[" + src + " + " + offset + "], " + dest + "\n";
//
//		StringJoiner instrString = new StringJoiner(" ");
//		instrString.add("LD")
//				   .add("[" + src)
//				   .add(opStr)
//				   .add(offset)
//				   .add("],")
//				   .add(dest);
////				   .add("\n");
//		return instrString.toString();
//	}
//		
//	
//
//	//////////////////// BINOP TEMPLATE /////////////////////////////////
//	public static String OPR(int op, String operand1, String operand2, String dest) {
//		Map<Integer, String> opMap = new HashMap<Integer, String>() {{
//			put(tree.BINOP.PLUS, "ADD");
//			put(tree.BINOP.MINUS, "SUB");
//			put(tree.BINOP.MUL, "SMUL");
//			put(tree.BINOP.AND, "AND");
//		}};
//				
////		String opType = opMap.get(op);
////		StringJoiner joiner = new StringJoiner(" ");
////		joiner.add(opType)
////			  .add(operand1 + ",")
////			  .add(operand2 + ",")
////			  .add(dest + "\n");
//		String opType = opMap.get(op);
//		StringJoiner joiner = new StringJoiner(" ");
//		joiner.add(opType)
//			  .add(operand1)
//			  .add(",")
//			  .add(operand2)
//			  .add(",")
//			  .add(dest);
////			  .add("\n");
//		
//		return joiner.toString();
//	}
//		
//	//////////////////// STORE TEMPLATE ///////////////////////////////////
//	public static String STORE(String src, String dest) {
//		StringJoiner joiner = new StringJoiner(" ");
//		joiner.add("ST")
//			  .add(src + ",")
//			  .add("[" + dest + "]");
////			  .add("\n");
//		return joiner.toString();
//	}
//	
//	public static String STORE(int op, String src, String dest, String offset) {
//		
//		Map<Integer, String> opMap = new HashMap<Integer, String>() {{
//			put(tree.BINOP.PLUS, "+");
//			put(tree.BINOP.MINUS, "-");
//		}};
//		String opString = opMap.get(op);
//		StringJoiner joiner = new StringJoiner(" ");
//		joiner.add("ST")
//			  .add(src + ",")
//			  .add("[" + dest)
//			  .add(opString)
//			  .add(offset + "]");
////			  .add("\n");
//		return joiner.toString();
//	}
//	
//	public static String LABEL(String l) {
//		return l + ":\n";
//	}
//	
//	/////////////////////////////// BRANCH TEMPLATE ////////////////////////////////
//	public static String BRANCH(int cond, String label) {
//		
//		Map<Integer, String> condMap = new HashMap<Integer, String>() {{
//			put(tree.CJUMP.EQ, "BE");
//			put(tree.CJUMP.NE, "BNE");
//			put(tree.CJUMP.LT, "BL");
//			put(tree.CJUMP.GT, "BG");
//			put(tree.CJUMP.LE, "BLE");
//			put(tree.CJUMP.GE, "BGE");
//			put(tree.CJUMP.ULT, "BLU");
//			put(tree.CJUMP.ULE, "BLEU");
//			put(tree.CJUMP.UGT, "BGU");
//			put(tree.CJUMP.UGE, "BGEU");
//		}};
//		
//		StringJoiner joiner = new StringJoiner(" ");
//
//		joiner.add(condMap.get(cond)) 
//			  .add(label + "\n")
//			  .add("nop");
//		
//		return joiner.toString();
//	}
//	
//	///////////////////////////// BRANCH TEMPLATE ///////////////////////
//	
//	
//	////////////////////////////// CALL TEMPLATE ///////////////////////
//	public static String CALL(String label) {
//		
//		StringJoiner joiner = new StringJoiner(" ");
//		joiner.add("CALL")
//			  .add(label + "\n")
//			  .add("nop");
//		
//		return joiner.toString();
//	}
//	
//	///////////////////////////CALL TEMPLATE ////////////////////////
//	
//	////////////////////////// LABEL TEMPLATE ///////////////////////
//	public static String JUMP(String label) {
//		
//		StringJoiner joiner = new StringJoiner(" ");
//		joiner.add("BA")
//			  .add(label)		  
//			  .add("nop");
//		
//		return joiner.toString();
//	}
//	/////////////////////// LABEL TEMPLATE /////////////////////////////
//	
//	////////////////////////// CMP TEMPLATE //////////////////////////
//	public static String CMP(String exp1, String exp2) {
//		
//		StringJoiner joiner = new StringJoiner(" ");
//		joiner.add("CMP")
//			  .add(exp1)
//			  .add(",")
//			  .add(exp2);
//	//		  .add("\n");
//		
//		return joiner.toString();
//	}
//	//////////////////////////// CMP TEMPLATE ////////////////////////
//	
//	////////////////// MOVE TEMPLATE //////////////////////////////////////
//	public static String MOV(String src, String dest) {
//		
//		StringJoiner joiner = new StringJoiner(" ");
//		joiner.add("MOV")
//		      .add(src)
//		      .add(",")
//		      .add(dest);
////		      .add("\n");
//		
//		return joiner.toString();
//	}
//
//	/////////////////// OR TEMPLATE ///////////////////////////////////////
//	public static String OR(String src1, String src2, String dest) {
//		
//		
//		StringJoiner joiner = new StringJoiner(" ");
//		joiner.add("OR")
//			  .add(src1)
//			  .add(",")
//			  .add(src2);
////			  .add("\n");
//		
//		return joiner.toString();
//			  
//	}
//	//////////////////// OR TEMPLATE /////////////////////////////////////
//}

package Sparc;

import java.util.ArrayList;
import java.util.List;

import assem.OperationInstruction;
import tree.NameOfTemp;

public class RegisterAllocation {
	
	private final static int
	DEST = 0,
	SRC = 1;

	SparcFrame currFrame;
	
	/* List of instructions that were generated after CodeGen step */
	List<assem.Instruction> ilist;
	
	/* List of new instructions generated after register allocation (
	 * should only need to inject steps for moving TEMPS to stack frame 
	 * as needed)
	 */
	
	public RegisterAllocation(SparcFrame frame, List<assem.Instruction> il) {
		currFrame = frame;
		ilist = il;
	}
	
	public List<assem.Instruction> iterateThroughTemps() {
		
		List<assem.Instruction> allocatedInstrList = new ArrayList<assem.Instruction>();
		
		for (int i = 0; i < ilist.size(); i++) {
			// Iterate through instruction list and map
			// registers that do not already have a mapping in the
			// frame table to available registers
			assem.Instruction instr = ilist.get(i);
			System.out.println(instr.format());
			if (instr instanceof assem.OperationInstruction ||
					instr instanceof assem.MoveInstruction) {
				// Go through sources and allocate registers if
				// needed
				
				/* List of temps we'll have to generate load from frame instructions for */
				List <tree.NameOfTemp> srcsToLoad = new ArrayList<tree.NameOfTemp>();
				List <tree.NameOfTemp> currSrcs;
				
				if (instr instanceof assem.OperationInstruction) { currSrcs = ((assem.OperationInstruction) instr).use(); }
				else { currSrcs = ((assem.MoveInstruction) instr).use(); }
//				System.out.println(instr.format());
				
				if (currSrcs != null) {
					for (tree.NameOfTemp src : currSrcs) {
						
						/* Check if the src register is in the current register map
						 * If so, we're good. If not, we'll have to inject an instruction
						 * to load the temp from the stack frame
						 */
						if ( !(currFrame.inRegisterMap(src)) ) {
								srcsToLoad.add(src);
						}								
					}
				}

				/* Keep list of temps we'll have to generate move to frame instructions for */
				List <tree.NameOfTemp> destsToStore = new ArrayList<tree.NameOfTemp>();
				List <tree.NameOfTemp> currDests;
				if (instr instanceof assem.OperationInstruction) { currDests = ((assem.OperationInstruction) instr).def(); }
				else { currDests = ((assem.MoveInstruction) instr).def(); }
				
				if (currDests != null) {
					for (tree.NameOfTemp dest : currDests) {
						if ( !(currFrame.inRegisterMap(dest))) {
							
							if ( !assignRegister(dest) ) {
								/* If we get here, we need to store dest in frame.
								 * Add to frame map
								 */							
								destsToStore.add(dest);
								currFrame.addTempToFrame(dest);
//								System.out.println(dest.toString() + " added to stack frame");
							}
						}
					}
				}
			
				/* If all srcs and dests have registers assigned to them, then we are good */
				if ( srcsToLoad.isEmpty() && destsToStore.isEmpty() ) {
					allocatedInstrList.add(instr);
				}
				
				/* Generate a new operation instruction to replace the one that we had (so that 
				 * we can add global registers to instruction to replace sources and dests 
				 */
				
				/* Hope that we don't have to replace more than two dests and sources at a time */
				else {
					
					int global_count = 0;
					String panicComment = "";
					List <tree.NameOfTemp> newSrcs = new ArrayList<tree.NameOfTemp>();
					if (currSrcs != null) {
						for (tree.NameOfTemp src : currSrcs) {
							if (!srcsToLoad.contains(src)) { newSrcs.add(src); }
							else {
								if (global_count == 0) {
									newSrcs.add(currFrame.globalOneRegister.temp);
									global_count++;
								}
								else if (global_count == 1) {
									newSrcs.add(currFrame.globalTwoRegister.temp);
									global_count++;
								}
								else if (global_count == 2) {
									newSrcs.add(currFrame.globalThreeRegister.temp);
									global_count++;
								}
								else {
									panicComment += "ERROR: Out of global registers for src!";
								}
							}
						}
					}
					
					/* Now for destinations */
					global_count = 0;
					panicComment += " -- ";
					List <tree.NameOfTemp> newDests = new ArrayList<tree.NameOfTemp>();
					if (currDests != null) {
						for (tree.NameOfTemp dest : currDests ) {
							if (!destsToStore.contains(dest)) { newDests.add(dest); }
							else {
								if (global_count == 0) {
									newDests.add(currFrame.globalOneRegister.temp);
									global_count++;
								}
								else if (global_count == 1) {
									newDests.add(currFrame.globalTwoRegister.temp);
									global_count++;
								}
								else if (global_count == 2) {
									newDests.add(currFrame.globalThreeRegister.temp);
									global_count++;
								}
								else {
									panicComment += "ERROR: out of global registers for dest!";
								}
							}
						}
					}
					
					/* Replace old instruction with new one with updated temp list */
					String assemblyCode = instr.assem;
					String comment = instr.comment;
					assem.Instruction updatedInstr = new assem.OperationInstruction(assemblyCode,
							comment + panicComment, newDests, newSrcs);
					
					/* Generate instructions to load srcs from stack frame */
					List <assem.Instruction> loadFromFrameInstrs = new ArrayList<assem.Instruction>();
					for (int k = 0; k < srcsToLoad.size(); k++) {
						System.out.println("srcsToLoad: " + srcsToLoad.toString());
						int offset = currFrame.getTempMemOffset(srcsToLoad.get(k)) * currFrame.getWordSize();
						tree.NameOfTemp globalDest;
						if (k == 0) {  globalDest = currFrame.globalOneRegister.temp; }
						else if (k == 1) { globalDest = currFrame.globalTwoRegister.temp; }
						else if (k == 2) { globalDest = currFrame.globalThreeRegister.temp; }
						else { break; } // If we get here, we're just plumb out of luck
						assem.Instruction load = new assem.OperationInstruction(
								SparcTemplates.LOAD(tree.BINOP.MINUS, newTempGenerator(SRC, 0), 
										newTempGenerator(DEST, 0), Integer.toString(offset)),
										"LOAD from frame", globalDest, currFrame.framePointer.temp);
						loadFromFrameInstrs.add(load);
					}
				
					
					/* Generate instructions to store dests in stack frame */
					List <assem.Instruction> storeInFrameInstrs = new ArrayList<assem.Instruction>();
					for (int k = 0; k < destsToStore.size(); k++) {
						int offset = currFrame.getTempMemOffset(destsToStore.get(k)) * currFrame.getWordSize();
						tree.NameOfTemp globalSrc;
						if (k == 0) { globalSrc = currFrame.globalOneRegister.temp; }
						else if (k == 1) {globalSrc = currFrame.globalTwoRegister.temp; }
						else if (k == 2) {globalSrc = currFrame.globalThreeRegister.temp; }
						else { break; } // ...same as above. Scream to God about this injustice?
						assem.Instruction store = new assem.OperationInstruction(
								SparcTemplates.STORE(tree.BINOP.MINUS, newTempGenerator(SRC, 0),
										newTempGenerator(DEST, 0), Integer.toString(offset)),
										"STORE to frame", currFrame.framePointer.temp, globalSrc);
						storeInFrameInstrs.add(store);
					}
					
					// AND finally, after this arduous journey of enabling register spilling, we close 
					// on our adventure. Construct new instruction list and add to current set
					List <assem.Instruction> finalInstructionSet = new ArrayList<assem.Instruction>();
					finalInstructionSet.addAll(loadFromFrameInstrs);
					finalInstructionSet.add(updatedInstr);
					finalInstructionSet.addAll(storeInFrameInstrs);
					allocatedInstrList.addAll(finalInstructionSet);
				}
			}
			
			/* If it's any other type of instruction, we don't need to perform register allocation */
			else {
				allocatedInstrList.add(instr);
			}
		}
		
		return allocatedInstrList;
	}
	
	
	
	/* NOTE: DISABLE THIS FUNCTION BEFORE TURNING IN COMPILER */
	public void panic(String s) {
		System.out.println(s);
	}
	
	public boolean assignRegister(tree.NameOfTemp n) {
		/* Assigns register if one is available. If there isn't,
		 * we'll have to generate a new instruction to move the 
		 * TEMP to global register g1 and then move to stack
		 */
		
		/* Assign free register to temp */
		String regStr = currFrame.popRegister();
		if (regStr != null) {
			currFrame.addToMap(n, regStr);
			return true;
		}
		
		// If there is no register available, we'll
		// need to place the temp in the stack frame.
		// This will require us to inject an instruction
		else {
			return false;
		}	
	}
	
	public List<assem.Instruction> moveToStackFrame(assem.OperationInstruction instr) {
		
		String assembly = instr.assem;
		String comment = instr.comment;
		
		/* List of source temps */
		List <tree.NameOfTemp> src = instr.use();
		List <tree.NameOfTemp> g1 = new ArrayList<tree.NameOfTemp>();
		g1.add(currFrame.globalOneRegister.temp);
		
		/* Add dest temp to frame */
		List<tree.NameOfTemp> dstTemp = instr.def();
		tree.NameOfTemp oldDst = dstTemp.get(0);
		currFrame.addTempToFrame(oldDst);
		
		/* Replace add g1 to original instruction as destination */
		assem.Instruction moveToGlobal = new assem.OperationInstruction(assembly, comment,
																		g1, src);	
		/* Generate instruction to move contents of g1 to stack frame */
		int offset = currFrame.getTempMemOffset(oldDst) * currFrame.getWordSize();
		List<tree.NameOfTemp> framePointer = new ArrayList<tree.NameOfTemp>();
		framePointer.add(currFrame.framePointer.temp);
		
		assem.Instruction storeInFrame = new assem.OperationInstruction(SparcTemplates.STORE(
				tree.BINOP.MINUS, newTempGenerator(SRC, 0), newTempGenerator(DEST, 0), Integer.toString(offset)),
				framePointer, g1);
		
		/* Compile old and new instruction in a list and send back to the register allocation function */
		List <assem.Instruction> instrList = new ArrayList<assem.Instruction>() {{
			add(moveToGlobal);
			add(storeInFrame);
		}};
		
		return instrList;	
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
	
	public SparcFrame getFrame() {
		/* Gets method frame with new temp mappings */
		return currFrame;
	}
}

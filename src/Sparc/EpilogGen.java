package Sparc;

import java.util.ArrayList;
import java.util.List;

import assem.LabelInstruction;
import assem.OperationInstruction;

public class EpilogGen {

	SparcFrame currFrame;
	public EpilogGen(SparcFrame f) {
		currFrame = f;
	}
	
	public List<assem.Instruction> epilog() {
		List<assem.Instruction> epilog = new ArrayList<assem.Instruction>();
		tree.NameOfLabel methodName = currFrame.name.label;
		
		epilog.add(new assem.LabelInstruction(new tree.NameOfLabel(methodName.toString(), "epilogBegin")));
		epilog.add(new assem.OperationInstruction(
				SparcTemplates.RET(), "return from " + methodName.toString()));
		epilog.add(new assem.OperationInstruction(
				SparcTemplates.RESTORE(), " (in the delay slot)"));		
		return epilog;
	}

}

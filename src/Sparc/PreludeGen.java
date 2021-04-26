package Sparc;

import java.util.ArrayList;
import java.util.List;

import assem.LabelInstruction;
import assem.OperationInstruction;

public class PreludeGen {

	SparcFrame currFrame;
	public PreludeGen(SparcFrame f) {
		currFrame = f;
	}
	
	/* Generate a function prelude */
	public List<assem.Instruction> prelude() {
		List <assem.Instruction> prelude = new ArrayList<assem.Instruction>();
		
		String nameComment = "procedure definition '" + currFrame.name.toString();
		String comment = "', formals=" + Integer.toString(currFrame.formals.size());
		comment += " (including 'this'), locals=" + Integer.toString(currFrame.locals.size());
		comment += " temps=" + Integer.toString(currFrame.numTempsInFrame);
		comment += " max args=" + Integer.toString(currFrame.getMaxArgs());
		
		String saveComment = "register save area=16 words; ";
		saveComment += "structure return area=1 word";
		
		prelude.add(new assem.Comment(nameComment));
		prelude.add(new assem.Comment(comment));
		prelude.add(new assem.Comment(saveComment));
		
		prelude.add(new assem.LabelInstruction(currFrame.name.label));
		prelude.add(new assem.OperationInstruction(
				SparcTemplates.SET("LOCALS", currFrame.locals.size()),
				"number of locals is now known"));
		prelude.add(new assem.OperationInstruction(
				SparcTemplates.SET("TEMPS", currFrame.numTempsInFrame),
				"number of temps is not known until late"));
		prelude.add(new assem.OperationInstruction(
				SparcTemplates.SET("ARGSB", currFrame.getMaxArgs()),
				"build area for arguments of all callees"));
		List<String> vars = new ArrayList<String>() {{
			add("LOCALS");
			add("TEMPS");
			add("ARGSB");
			add("1");
			add("16");
		}};
		String stackPointer = currFrame.getRegisterMapping(
				currFrame.stackPointer.temp);
		prelude.add(new assem.OperationInstruction(
				SparcTemplates.SAVE(stackPointer, vars, stackPointer),
				"Amount of memory to allocate for stack frame"));
		return prelude;
	}

}

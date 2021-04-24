package Sparc;

import java.util.List;
import java.util.ArrayList;

public class MainEpilogGen {
	public List<assem.Instruction> epilog() {
		List<assem.Instruction> epilog = new ArrayList<assem.Instruction>() {{
			add(new assem.OperationInstruction(SparcTemplates.CLR(
					"%o0"), "%o0 := 0; program status=0=success"));
			add(new assem.OperationInstruction(SparcTemplates.CALL("exit"),
					"flush and exit"));
		}};
		return epilog;
	}
}

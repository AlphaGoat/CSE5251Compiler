package frame;

import java.util.ArrayList;

public abstract class Frame {
	public int wordSize;
	public tree.LABEL name;
	public AccessList formals;
	abstract public tree.TEMP FP(); // frame point register
	abstract public int wordSize();
	abstract public Frame newFrame(tree.LABEL name, Util.BoolList formals);
	abstract public Access allocLocal(boolean escape);
	abstract public ArrayList<tree.LABEL> incomingArgs();
	abstract public tree.LABEL returnAddress();
	abstract public ArrayList<tree.LABEL> outgoingArgs();
}


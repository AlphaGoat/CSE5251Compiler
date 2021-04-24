package frame;

import java.util.ArrayList;
import java.util.HashMap;

import main.Symbol;

public abstract class Frame {
	public int wordSize;
	private int maxArgs;
	
	public tree.TEMP framePointer;
	public tree.TEMP retLocation;
	public tree.LABEL name;
	public ArrayList<frame.Access> locals = new ArrayList<frame.Access>();
	public ArrayList<frame.Access> formals = new ArrayList<frame.Access>();
	public ArrayList<frame.Access> outgoingArgs = new ArrayList<frame.Access>();
	
	// Technically not part of this frame, but useful to
	// keep track of so that we know offsets 
	public ArrayList<frame.Access> prevFrameFormals = new ArrayList<frame.Access>();
	
	public HashMap<Symbol, frame.Access> varMap = new HashMap<Symbol, frame.Access>(); // For translation phase (lookup where temps are saved 

	abstract public tree.TEMP FP(); // frame point register
	abstract public tree.TEMP RV(); // return value location
	abstract public tree.TEMP getCalleeReturnRegister();
	abstract public int getWordSize();
	abstract public void allocLocal(Symbol localKey);
	abstract public ArrayList<tree.LABEL> incomingArgs();
	abstract public tree.LABEL returnAddress();
	abstract public ArrayList<tree.LABEL> outgoingArgs();
	
	abstract public int getMaxArgs();
	abstract public void setMaxArgs(int max);
	
	// Functions for translation phase
	abstract public tree.Exp lookupVar(Symbol varKey);
}


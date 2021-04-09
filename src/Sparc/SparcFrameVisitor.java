package Sparc;

import syntax.SyntaxTreeVisitor;

public class SparcFrameVisitor implements SyntaxTreeVisitor<Void> {

	public SparcFrameVisitor() {
	}
	
	public Void visit(syntax.Program p) {
		
		// Visit main class (entry point to execution of program)
		p.m.accept(this);
		
		return null;
	}
	
	public Void visit(syntax.MainClass m) {
		// Visit statements. Whenever a method is called, implement
		// new stack frame
		m.s.accept(this);
	}
	
	public Void visit(syntax.)

}

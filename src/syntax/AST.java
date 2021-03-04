package syntax;

public abstract class AST {
	/*
    For the reporting of semantic errors, we keep track of the position of
    all the constructs of the syntax.
	*/
  public final int lineNumber, columnNumber;

  protected AST (int l, int c) {
     lineNumber=l;
     columnNumber=c;
  }

  /*
    This method "accept" must be abstract; its code can't be inherited.
    Because the code:
       v.visit (this)
    is a call to the *overloaded* method "visit" and it is statically
    dispatched by the compiler based on the static type of "this". Hence,
    the "this" must statically be in the class and not inherited by the
    superclass.

    One could use reflection to get around this problem, but this adds
    considerable overhead in time. (Cf. Palsberg and Jay, 1997.)
  
  */
  public abstract <T> T accept (final SyntaxTreeVisitor <T> v);

  public String toString () {
     return String.format ("%s:%03d:%03d", super.toString(), lineNumber, columnNumber);
  }

}

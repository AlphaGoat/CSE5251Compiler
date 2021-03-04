package syntax;

public class SyntaxTreeVisitor <T> {
	   T visit (Program n);
	   T visit (MainClass n);
	   T visit (SimpleClassDecl n);
	   T visit (ExtendingClassDecl n);
	   T visit (VarDecl n);
	   T visit (MethodDecl n);
	   T visit (Formal n);
	   T visit (IdentifierType n);
	   T visit (IntArrayType n);
	   T visit (BooleanType n);
	   T visit (IntegerType n);
	   T visit (VoidType n);
	   T visit (Block n);
	   T visit (If n);
	   T visit (While n);
	   T visit (Print n);
	   T visit (Assign n);
	   T visit (ArrayAssign n);
	   T visit (And n);
	   T visit (LessThan n);
	   T visit (Plus n);
	   T visit (Minus n);
	   T visit (Times n);
	   T visit (ArrayLookup n);
	   T visit (ArrayLength n);
	   T visit (Call n);
	   T visit (IntegerLiteral n);
	   T visit (True n);
	   T visit (False n);
	   T visit (IdentifierExp n);
	   T visit (This n);
	   T visit (NewArray n);
	   T visit (NewObject n);
	   T visit (Not n);
	   T visit (Identifier n);
}

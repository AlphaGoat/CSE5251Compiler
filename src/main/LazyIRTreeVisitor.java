package main;

import java.util.ArrayList;
import java.util.List;

import tree.MEM;


// TODO: implement checks for empty statements
//		 (perfectly valid code, so we can't just exception it away)


public class LazyIRTreeVisitor implements syntax.SyntaxTreeVisitor<LazyIRTree> {

//	ArrayList<String> CurrLabels = new ArrayList<String>();
	
//	int wordSize;
	final int wordSize; 
	
	String currClass;
	String currMethod;
//	Pair <String, String> methodString; // Pair containing method name and class where 
										// it resides
	
	/* Label for end of code fragments */
	tree.NameOfLabel epilog;
	
	// Array to hold method fragments
	public static ArrayList<tree.Stm> methodFragments = new ArrayList<tree.Stm>();
		
	public LazyIRTreeVisitor(int sz) {
		// TODO Auto-generated constructor stub
		wordSize = sz;
	}
	
	public int getWordSize() {
		return wordSize;
	}
	
	private tree.Exp lookupVar(Symbol varKey) {
		// lookup variable in current method to see if 
		// it is a parameter or local. If none of those
		// things, it must be a field in the class
		
		// Get bindings for class and method
		Symbol methodKey = Symbol.symbol(currMethod);
		Symbol classKey = Symbol.symbol(currClass);
		
		ClassBinding cb = (ClassBinding) SymbolTableVisitor.symbolTable.get(classKey);
		MethodBinding mb = (MethodBinding) cb.methods.get(methodKey);

		// Lookup in method's locals
		if ( mb.locals.get(varKey) != null ) {
			
			// get offset of local
			int offset = mb.localList.indexOf(varKey);
			
			// Return local's location in memory
			return new tree.MEM(new tree.BINOP(tree.BINOP.PLUS,
								  new tree.TEMP("fp"),
								  new tree.CONST((offset+1) * wordSize)));
			
		}
		
		// If it's not in method's locals, check params
		else if ( mb.params.get(varKey) != null ) {
			
			// get offset of param
			int offset = mb.paramList.indexOf(varKey);
			
			// Return TEMP storing param's register
			return new tree.TEMP("%i" + Integer.toString(offset + 1));
			
		}
		
		// otherwise, the variable must be one of the class's fields
		else {
			
			// Get field in class. If it's not in the current class,
			// check its parents
			if ( cb.fields.get(varKey) == null ) {
				
				// Traverse class's parents to get field
				int offset = cb.fieldList.size();
				ClassBinding p = cb;
				while ( p.parent != null ) {
					p = (ClassBinding) SymbolTableVisitor.symbolTable.get(p.parent);
					if ( p.fields.get(varKey) != null ) {
						break;
					}
					
					offset += p.fieldList.size();
				}
				
				offset = p.fieldList.indexOf(varKey);
				
				// Return location in memory pointed to by "this" ptr (plus offset)
				return new tree.MEM( new tree.BINOP(tree.BINOP.PLUS,
									  				new tree.TEMP("%i0"),
									  				new tree.CONST((offset + 1) * 
									  				wordSize)));
				
			}
			
			else {
				int offset = cb.fieldList.indexOf(varKey);
				
				return new tree.MEM(new tree.BINOP(tree.BINOP.PLUS,
						  			new tree.TEMP("%i0"),
						  			new tree.CONST((offset + 1) * wordSize)));
			}
			
		}	
	}

	public tree.Stm constructArrayIndexCheck(tree.Exp arrayMemLocation,
											 tree.Exp indexExp) {
		tree.Exp length = new tree.MEM(new tree.BINOP(tree.BINOP.PLUS, 
			 	  arrayMemLocation,
			 	  new tree.BINOP(tree.BINOP.MUL, 
			 			  		 new tree.CONST(1), 
			 			  		 new tree.CONST(wordSize))
			 ));
		
//		tree.NameOfLabel negIndex = new tree.NameOfLabel("array", 
//				"negative", "index");
		
		tree.NameOfLabel nextCheck = new tree.NameOfLabel("array", "check", 
				"less", "length");
		tree.NameOfLabel indexError = new tree.NameOfLabel("array", "index", 
				"OutOfBounds", "error");
//		
//		tree.CALL negErrorCall = new tree.CALL(new tree.NAME(
//				"print_err_neg_index"));
//		
		/* First construct check to see if index is greater than 0 */
		tree.CJUMP geZero = new tree.CJUMP(tree.CJUMP.GE, 
										   indexExp,
										   tree.CONST.ZERO,
										   nextCheck,
										   indexError);
		
		/* Now check that the index we are trying to assign to is less
		 * than the length of the array */
		tree.NameOfLabel contAssign = new tree.NameOfLabel("array", "assign");
		
		tree.CALL indexOutOfBoundsCall = new tree.CALL(new tree.NAME(
				"print_err_out_of_bounds"));
		
		tree.CJUMP ltLength = new tree.CJUMP(tree.CJUMP.LT,
											 indexExp,
											 length,
											 contAssign,
											 indexError);
	
		tree.Stm seq = tree.SEQ.fromList(
						geZero,
						new tree.LABEL(nextCheck),
						ltLength,
						new tree.LABEL(indexError),
						new tree.EVAL(indexOutOfBoundsCall),
						new tree.JUMP(epilog),
						new tree.LABEL(contAssign)
						);
				
		return seq;
	}

	
	private int getObjectSize(Symbol key) {
		// Get the number of fields in object, as well as in parents
		// TODO: maybe just add parent's fields to child, so we don't
		// 		 have to go through all of this hassle
		
		int numFields = 0;
		ClassBinding cb = (ClassBinding) SymbolTableVisitor.symbolTable.get(key);
		numFields += cb.fieldList.size();
		
		ClassBinding parentClass = cb;
		while (parentClass.parent != null) {
			parentClass = (ClassBinding) SymbolTableVisitor.symbolTable.get(
					parentClass.parent);
			numFields += parentClass.fieldList.size();
		}
		
		return numFields;
	}


	@Override
	public LazyIRTree visit(syntax.Program n) {

		n.m.accept(this);
		for (syntax.ClassDecl c: n.cl) c.accept(this);
		
		return null;
	}

	@Override
	public LazyIRTree visit(syntax.MainClass n) {
		
		/* Add name of main class to labels */
		currClass = n.i1.toString();
		currMethod = "main";

//		String methodPrefix = currClass + "$" + currMethod;
		tree.NameOfLabel prelude = new tree.NameOfLabel(currClass, currMethod, 
				"preludeEnd");
//		tree.NameOfLabel epilog = new tree.NameOfLabel(currClass, currMethod,
//				"epilogBegin");
		epilog = new tree.NameOfLabel(currClass, currMethod,
				"epilogBegin");
		tree.Stm pre = new tree.LABEL(prelude);
		tree.Stm post = new tree.JUMP(epilog);
		
		tree.Stm s = n.s.accept(this).asStm();

		tree.Stm seq = new tree.SEQ(pre, new tree.SEQ(s, post));
		
		methodFragments.add(seq);
		
		/* Don't need to do this, just for completion's sake */
		currMethod = null;
		currClass = null;
		
		return null;
	}

	@Override
	public LazyIRTree visit(syntax.SimpleClassDecl n) {
		// Need to go through here to get to methods
		// Generate new fragments from methods
		
		currClass = n.i.toString();
		
		for (syntax.MethodDecl m: n.methods) m.accept(this);
		
		currClass = null;
		
		return null;
	}

	@Override
	public LazyIRTree visit(syntax.ExtendingClassDecl n) {
		// Need to go through here to get methods
		// Generate new fragments from methods
		
		currClass = n.i.toString();
		
		for (syntax.MethodDecl m: n.methods) m.accept(this);
		
		currClass = null;
		
		return null;
	}

	@Override
	public LazyIRTree visit(syntax.MethodDecl n) {
		// Generate new method fragment from statements
		currMethod = n.i.toString();
		
		String methodPrefix = currClass + "$" + currMethod;
		tree.Stm pre = new tree.LABEL(methodPrefix + "$preludeEnd");
		tree.Stm post = new tree.JUMP(methodPrefix + "$epilogBegin");
		
		// Get the number of statements in method 
		final int l = n.sl.size();
		
		// if there are no statements, just generate label and 
		// jump statement
		if (l == 0) {			
			tree.Stm seq = new tree.SEQ(pre, post);
			methodFragments.add(seq);
		}
		
		// If there is only one statement, dive in to get it, then
		// add post statement at end
		else if ( l == 1 ) {
			tree.Stm s = n.sl.get(0).accept(this).asStm();
			tree.Stm seq = new tree.SEQ(pre, new tree.SEQ(s, post));
			methodFragments.add(seq);
		}
		
		// If there are more then one statements, that's where
		// things get interesting
		else {
			
			tree.Stm s1 = n.sl.get(l-2).accept(this).asStm();
			tree.Stm s2 = n.sl.get(l-1).accept(this).asStm();
			
			tree.Stm seq = new tree.SEQ(s1, s2);
			
			for (int i = l-3; i >=0; i--) {
				tree.Stm s = n.sl.get(i).accept(this).asStm();
				seq = new tree.SEQ(s, seq);
			}
			
			tree.Stm endSeq = new tree.SEQ(pre, new tree.SEQ(seq, post));
			methodFragments.add(endSeq);
		} 
		
		currMethod = null;
		
		return null;
	}

	@Override
	public LazyIRTree visit(syntax.LocalDecl n) {
		// Nothing to do here
		return null;
	}

	@Override
	public LazyIRTree visit(syntax.FieldDecl n) {
		// Nothing to do here
		return null;
	}

	@Override
	public LazyIRTree visit(syntax.FormalDecl n) {
		// Nothing to do here
		return null;
	}

	@Override
	public LazyIRTree visit(syntax.IdentifierType n) {
		// Nothing to do here
		return null;
	}

	@Override
	public LazyIRTree visit(syntax.IntArrayType n) {
		// Nothing to do here
		return null;
	}

	@Override
	public LazyIRTree visit(syntax.BooleanType n) {
		// Nothing to do here
		return null;
	}

	@Override
	public LazyIRTree visit(syntax.IntegerType n) {
		// Nothing to do here
		return null;
	}

	@Override
	public LazyIRTree visit(syntax.VoidType n) {
		// Nothing to do here
		return null;
	}

	@Override
	public LazyIRTree visit(syntax.Block n) {
		// Get the number of statements in block
		final int l = n.sl.size();
		
		// if there are no statements, nothing to be returned
		if (l == 0) {
			
			// Generate a nonsense statement that has no effect
			// TODO: come up with better solution later
			tree.Stm dumbStm = new tree.LABEL("Empty", "Statement");
			
			return new StmIRTree(dumbStm);
		}
		
		// If there is only one statement, return that
		
		else if ( l == 1 ) {
			tree.Stm s = n.sl.get(0).accept(this).asStm();
			return new StmIRTree(s);
		}
		
		// If there are more then one statements, that's where
		// things get interesting
		else {
			tree.Stm s1 = n.sl.get(l-2).accept(this).asStm();
			tree.Stm s2 = n.sl.get(l-1).accept(this).asStm();
			
			tree.Stm seq = new tree.SEQ(s1, s2);
			
			for (int i = l-3; i >=0; i--) {
				tree.Stm s = n.sl.get(i).accept(this).asStm();
				seq = new tree.SEQ(s, seq);
			}
			return new StmIRTree(seq);
		}
	}

	@Override
	public LazyIRTree visit(syntax.If n) {
		
		return new IfThenElseExp(n.e.accept(this), n.s1.accept(this), n.s2.accept(this));
	}

	@Override
	public LazyIRTree visit(syntax.While n) {
		
		// Generate expression for condition
//		tree.LABEL test = new tree.LABEL("test");
//		tree.LABEL body = new tree.LABEL("body");
//		tree.LABEL done = new tree.LABEL("done");
		
		tree.NameOfLabel test = new tree.NameOfLabel("while", "test");
		tree.NameOfLabel body = new tree.NameOfLabel("while", "body");
		tree.NameOfLabel done = new tree.NameOfLabel("while", "done");
		
		LazyIRTree cond = n.e.accept(this);
		
		// Generate statement for loop
		tree.Stm seq = tree.SEQ.fromList(
					new tree.LABEL(test),
					cond.asCond(body, done),
					new tree.LABEL(body),
					n.s.accept(this).asStm(),
					new tree.JUMP(test),
					new tree.LABEL(done)
				);
		
		return new StmIRTree(seq);
	}

	@Override
	public LazyIRTree visit(syntax.Print n) {
		// Perform method call
		tree.NAME name = new tree.NAME("print_int");
		return new ExpIRTree(new tree.CALL(name, n.e.accept(this).asExp()));
	}

	@Override
	public LazyIRTree visit(syntax.Assign n) {
		
		// Lookup variable in symbol table
		Symbol varKey = Symbol.symbol(n.i.toString());
		
		// Get expression for memory/register holding variable
		tree.Exp varLocation = lookupVar(varKey);
		
		// Get expression on right hand side of assignment operator
		tree.Exp assignExp = n.e.accept(this).asExp();
		
		// Move value of evaluated expression to memory/register holding variable		
		return new StmIRTree(new tree.MOVE(varLocation, assignExp));
	}

	@Override
	public LazyIRTree visit(syntax.ArrayAssign n) {
		// Assign value to memory location pointed to
		// by array pointer and offset given by index

		
		Symbol nameOfArray = Symbol.symbol(n.nameOfArray.toString());

		
		// Retrieve memory location storing pointer
		tree.Exp arrayPtr = lookupVar(nameOfArray);
		
		// Get actual memory address of array
		tree.Exp arrayMemLocation = new tree.MEM(arrayPtr);
		
		// Get the index of array
		tree.Exp index = n.indexInArray.accept(this).asExp();
		
		// Get expression on right hand side of assignment operator
		tree.Exp assignExp = n.e.accept(this).asExp();
		

		
		// memory location to be assigned to (arrayPtr plus offset given by index)
		tree.MEM memToAssign = new tree.MEM(
									new tree.BINOP(
									tree.BINOP.PLUS,
									arrayPtr,
									new tree.BINOP(tree.BINOP.MUL,
									new tree.BINOP(tree.BINOP.PLUS,
											index,
											tree.CONST.ONE),
									new tree.CONST(wordSize)
									))
									);
		
		// TODO: Perform index out of bounds check
		// Access length of array 	
		tree.Stm indexCheckSeq = constructArrayIndexCheck(arrayMemLocation,
														  index);
		
		// Finally, assign result of expression to memory being pointed
		// to by array pointer (with offset given by index)
		tree.Stm memAssignStm = new tree.MOVE(memToAssign, assignExp);
		
		// Put it all together
		tree.NameOfLabel contAssign = new tree.NameOfLabel("array", "assign");
		tree.Stm assignStm = tree.SEQ.fromList(indexCheckSeq,
				new tree.LABEL(contAssign), memAssignStm);
		
		return new StmIRTree(assignStm);
	}

	@Override
	public LazyIRTree visit(syntax.And n) {
		// And-Than can evaluate to a conditional or boolean expression.
		// Handled in special LazyIRTree subclass
		return new AndExp(n.e1.accept(this), n.e2.accept(this));
	}

	@Override
	public LazyIRTree visit(syntax.LessThan n) {
		// Less-Than can evaluate to a conditional or boolean expression.
		// Handled in special LazyIRTree subclass
		return new LessThanExp(n.e1.accept(this), n.e2.accept(this));
	}

	@Override
	public LazyIRTree visit(syntax.Plus n) {
		return new ExpIRTree(new tree.BINOP(tree.BINOP.PLUS,
											n.e1.accept(this).asExp(),
											n.e2.accept(this).asExp()));
	}

	@Override
	public LazyIRTree visit(syntax.Minus n) {
		return new ExpIRTree(new tree.BINOP(tree.BINOP.MINUS,
											n.e1.accept(this).asExp(),
											n.e2.accept(this).asExp()));
	}

	@Override
	public LazyIRTree visit(syntax.Times n) {		
		return new ExpIRTree(new tree.BINOP(tree.BINOP.MUL,
											n.e1.accept(this).asExp(),
											n.e2.accept(this).asExp()));
	}

	@Override
	public LazyIRTree visit(syntax.ArrayLookup n) {
		
		// Get reference to array object
		tree.Exp arrayPtr = n.expressionForArray.accept(this).asExp();
		
		// evaluate index in array
		tree.Exp index = n.indexInArray.accept(this).asExp();
		
		// Get value stored in memory given by array pointer + index offset
		// +(arrayPtr, *( +(index, 1), wordSize) )
		return new ExpIRTree(
				new tree.MEM(new tree.BINOP(tree.BINOP.PLUS, 
						arrayPtr,
						new tree.BINOP(tree.BINOP.MUL,
						new tree.BINOP(tree.BINOP.PLUS, index, tree.CONST.ONE),
						new tree.CONST(wordSize))
					)
				));
	}

	@Override
	public LazyIRTree visit(syntax.ArrayLength n) {
		
		// Get ptr to array object. The length of the array
		// should be stored at the memory pointed to by the array pointer
		// (no offset needed)
		
//		tree.Exp arrayPtr = new tree.MEM(n.expressionForArray.accept(this).asExp());
		tree.Exp arrayPtr = n.expressionForArray.accept(this).asExp();
		return new ExpIRTree(new tree.MEM(arrayPtr));
	}

	@Override
	public LazyIRTree visit(syntax.Call n) {
	
		// Get name label for method
		String name = n.i.toString();
		
		// get name label for object calling method
		// (should be passed up with LazyIRTree reference)
		LazyIRTree objExp = n.e.accept(this);
		String className = objExp.getName();
				
		tree.NAME func = new tree.NAME(className + "$" + name);
		
		// Initialize list of expressions to be passed to the call
		// as arguments
		List<tree.Exp> l = new ArrayList<tree.Exp>();
		
		// First argument is a reference to the calling object
		l.add(objExp.asExp());
		
		// Get all other args
		for (syntax.Expression e: n.el) {
			l.add(e.accept(this).asExp());
		}
		
		// Initialize call expression and pass it up the tree
		tree.CALL call = new tree.CALL(func, l);
		
		return new ExpIRTree(call);
	}

	@Override
	public LazyIRTree visit(syntax.IntegerLiteral n) {
		
		return new ExpIRTree(new tree.CONST(n.i));
	}

	@Override
	public LazyIRTree visit(syntax.True n) {
		// TODO Auto-generated method stub
		return new BoolExp(tree.CONST.TRUE);
	}

	@Override
	public LazyIRTree visit(syntax.False n) {
		// TODO Auto-generated method stub
		return new BoolExp(tree.CONST.FALSE);
	}

	@Override
	public LazyIRTree visit(syntax.IdentifierExp n) {
		// TODO Auto-generated method stub
		Symbol key = Symbol.symbol(n.toString());
//		String objName = n.toString();
		
		// Lookup object "Type" in symbol table
		VarBinding idBinding = (VarBinding) SymbolTableLookup.searchScope(key,
				currClass, currMethod);
		String objName = idBinding.toString();
		
		return new ExpIRTree(lookupVar(key), objName);
	}

	@Override
	public LazyIRTree visit(syntax.This n) {
		// Return reference to first register
		// (holds reference to current class object)
		String objName = currClass;
		return new ExpIRTree(new tree.TEMP("%i0"), objName);
	}

	@Override
	public LazyIRTree visit(syntax.NewArray n) {
		// Return reference to array
		
		// call malloc to allocate a new array
		tree.Exp name = new tree.NAME("alloc_array");
		
		// get size of array
		tree.Exp arraySize = n.e.accept(this).asExp();
		
		// Add one to for length, times word size
		tree.Exp bytesToAlloc = new tree.BINOP(
				tree.BINOP.MUL,
				new tree.BINOP(tree.BINOP.PLUS, arraySize, tree.CONST.ONE),
				new tree.CONST(wordSize));
				
		return new ExpIRTree(new tree.CALL(name, bytesToAlloc));
	}

	@Override
	public LazyIRTree visit(syntax.NewObject n) {
		// Return reference to object
		
		// call malloc to allocate a new object
		String objName = n.i.toString();

		tree.Exp name = new tree.NAME("alloc_object");
		
		// Get size of object...a little more involved than with the array
		int size = getObjectSize(Symbol.symbol(n.i.toString()));
		
		tree.Exp bytesToAlloc = new tree.BINOP(
				tree.BINOP.MUL,
				new tree.CONST(size),
				new tree.CONST(wordSize));
		
		return new ExpIRTree(new tree.CALL(name, bytesToAlloc), objName);
	}

	@Override
	public LazyIRTree visit(syntax.Not n) {
		// TODO Auto-generated method stub
		return new NotExp(n.e.accept(this));
	}

	@Override
	public LazyIRTree visit(syntax.Identifier n) {
		// Nothing to do here
		
		
		return null;
	}

}

abstract class LazyIRTree {
	
	abstract tree.Exp asExp();	// ESEQ (asStm(), CONST(0))
	abstract tree.Stm asStm();	// EVAL (asExp())
	abstract tree.Stm asCond(tree.NameOfLabel t, tree.NameOfLabel f); // CJUMP (=, asExp(), CONST(0), t, f)	
	abstract public void passName(String n);
	abstract public String getName();
}

class ExpIRTree extends LazyIRTree {
	private String objectName;
	private final tree.Exp exp;
	ExpIRTree (tree.Exp e) { exp = e; }
	ExpIRTree (tree.Exp e, String name) { exp = e; objectName = name; }
	tree.Exp asExp() { return exp; }
	tree.Stm asStm() { return new tree.EVAL(exp); }
	tree.Stm asCond(tree.NameOfLabel truthLabel, tree.NameOfLabel falseLabel) { 
		// Because '==' will make every val that isn't '1' false if we
		// do a direct comparison with '1', which isn't the behavior we
		// want, we'll do a '!=' comparison with zero instead
		return new tree.CJUMP(tree.CJUMP.EQ,
							  exp,
							  tree.CONST.TRUE,
							  truthLabel,
							  falseLabel);
		
	}
	
	public void passName(String n) {
		objectName = n;
	}
	
	public String getName() {
		return objectName;
	}
	
}

class StmIRTree extends LazyIRTree {
	private final tree.Stm stm;
	StmIRTree (tree.Stm s) { stm = s; }
	tree.Stm asStm() { return stm; }
	@Override
	tree.Exp asExp() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	tree.Stm asCond(tree.NameOfLabel t, tree.NameOfLabel f) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void passName(String n) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
}

class BoolExp extends LazyIRTree {
	private final tree.Exp bool; // CONST exp with either '1' or '0' value
	BoolExp (tree.Exp b) { bool = b; }
	public String getName() { return null; }
	tree.Exp asExp() { return bool; }
	tree.Stm asStm() { return new tree.EVAL(bool); }
	tree.Stm asCond(tree.NameOfLabel truthLabel, tree.NameOfLabel falseLabel) {
		return new tree.CJUMP(tree.CJUMP.EQ,
						  bool,
						  tree.CONST.TRUE,
						  truthLabel,
						  falseLabel);
	}
	@Override
	public void passName(String n) {
		// TODO Auto-generated method stub
		
	}
}


class IfThenElseExp extends LazyIRTree {
	private final LazyIRTree cond, e2, e3;
//	final tree.LABEL t = new tree.LABEL("if", "then");
//	final tree.LABEL f = new tree.LABEL("if", "else");
//	final tree.LABEL join = new tree.LABEL("if", "end");
	tree.NameOfLabel truthLabel = new tree.NameOfLabel("if", "then");
	tree.NameOfLabel falseLabel = new tree.NameOfLabel("if", "else");
	tree.NameOfLabel joinLabel = new tree.NameOfLabel("if", "end");		
	
	IfThenElseExp (final LazyIRTree c, final LazyIRTree thenClause, final LazyIRTree elseClause) {
//		assert cond != null; assert e2 != null;
		cond = c; e2 = thenClause; e3 = elseClause;
	}
	
	// NO if-then-else expression (_?_:_) in MiniJava
	public tree.Exp asExp() {
		//throw new UnsupportedException("ERROR: IF should not be used as exp");
		return null;
	}
	
	// if-then-else statement as a statement
	public tree.Stm asStm() {
		final tree.Stm seq;
		if (e3 == null) {
			seq = tree.SEQ.fromList(		
					cond.asCond(truthLabel, falseLabel),		
					new tree.LABEL(truthLabel),		// T:
					e2.asStm(),				//    then Stm
					new tree.LABEL(falseLabel)		// F:
					);
		} else {
			seq = tree.SEQ.fromList(		
					cond.asCond(truthLabel, falseLabel),
					new tree.LABEL(truthLabel),		// T:
					e2.asStm(),				// then Stm
					new tree.JUMP(joinLabel),			// goto join
					new tree.LABEL(falseLabel),		// F:
					e3.asStm(),				// 		else stm
					new tree.LABEL(joinLabel)	// join:
					);
		}
		return seq;
	}	
	
	public tree.Stm asCond(tree.NameOfLabel tt, tree.NameOfLabel ff) { 
		final tree.Stm seq;
		if (e3 == null) {
			seq = tree.SEQ.fromList(
					cond.asCond(truthLabel, falseLabel),
					new tree.LABEL(truthLabel),	// T:
					e2.asCond(tt, ff),	// then cond (jump to tt if true, jump to ff if false)
//					new tree.JUMP(join),		// goto join (should be handled in e2 cond statement)
					new tree.LABEL(falseLabel),	// F: 
					new tree.JUMP(ff)	// If there is no expression to evaluate, jump to ff
					);
		} else {
			seq = tree.SEQ.fromList(
					cond.asCond(tt, ff),
					new tree.LABEL(truthLabel),	// T:
					e2.asCond(tt, ff),	// then cond (jump to tt if true, jump to ff if false)
//					new tree.JUMP(join) // goto join (redundant by jump in e2 cond statement)
					new tree.LABEL(falseLabel),	// F:
					e3.asCond(tt, ff)  // else other cond (jump to tt if true, jump to ff if false)
					);
		}
		
		return seq;
				
	}

	@Override
	public void passName(String n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
	
}

class LessThanExp extends LazyIRTree {
	
	private final LazyIRTree exp1, exp2;
	
	LessThanExp(LazyIRTree e1, LazyIRTree e2) {
		exp1 = e1; exp2 = e2;
	}
	
	public tree.Exp asExp() {
		// TODO: verify that this approach works (and that the register
		// 		 we store to is safe)! (currently choosing local register 7)
		
		// Since there is no way to directly evaluate a conditional
		// expression, we're going to have to be a little tricky. We'll
		// generate a CJUMP to a label point that returns the constant
		// value we want ('1' or '0')
//		tree.LABEL l = new tree.LABEL("Get_ONE");
//		tree.LABEL ge = new tree.LABEL("Get_ZERO");
//		tree.LABEL join = new tree.LABEL("join");
//		
		/* Create temporary for register we are going to store value in */
		tree.TEMP storeRegister = new tree.TEMP("local7");
		
		tree.Stm seq = tree.SEQ.fromList(
				new tree.CJUMP(tree.CJUMP.LT, 
							   exp1.asExp(),
							   exp2.asExp(),
							   new tree.NameOfLabel("Get_ONE"),
							   new tree.NameOfLabel("Get_ZERO")),
				new tree.LABEL("Get_ONE"),
				new tree.MOVE(storeRegister, tree.CONST.ONE),
				new tree.JUMP("join"),
				new tree.LABEL("Get_ZERO"),
				new tree.MOVE(storeRegister, tree.CONST.ZERO),
				new tree.LABEL("join")
				);
		
		return new tree.RET(seq, storeRegister);
	}
	
	public tree.Stm asStm() {

		return tree.SEQ.fromList(
				new tree.CJUMP(tree.CJUMP.LT, 
							   exp1.asExp(),
							   exp2.asExp(),
							   new tree.NameOfLabel("Get_ONE"),
							   new tree.NameOfLabel("Get_ZERO")),
				new tree.LABEL("Get_ONE"),
				new tree.JUMP("join"),
				new tree.LABEL("Get_ZERO"),
				new tree.LABEL("join")
				);
	}
	
	public tree.Stm asCond(tree.NameOfLabel truthLabel, tree.NameOfLabel falseLabel) {
		return new tree.CJUMP(tree.CJUMP.LT,
				  			  exp1.asExp(),
				  			  exp2.asExp(),
				  			  truthLabel,
				  			  falseLabel);	
	}

	@Override
	public void passName(String n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}

class AndExp extends LazyIRTree {
	final LazyIRTree e1, e2;
	
	public AndExp(LazyIRTree exp1, LazyIRTree exp2) {
		e1 = exp1; e2 = exp2;
	}
	
	public tree.Exp asExp() {
		return new tree.BINOP(tree.BINOP.AND, e1.asExp(), e2.asExp());
	}
	
	public tree.Stm asStm() {
		return new tree.EVAL(new tree.BINOP(tree.BINOP.AND, e1.asExp(), e2.asExp()));
	}
	
	public tree.Stm asCond(tree.NameOfLabel truthLabel, tree.NameOfLabel falseLabel) {
		return new tree.CJUMP(tree.CJUMP.EQ,
							  e1.asExp(),
							  e2.asExp(),
							  truthLabel,
							  falseLabel
							  );
	}

	@Override
	public void passName(String n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
}


class NotExp extends LazyIRTree {
	final LazyIRTree e;
	
	public NotExp(LazyIRTree exp) {
		e = exp;
	}
	
	public tree.Exp asExp() {
		return new tree.BINOP(tree.BINOP.AND, e.asExp(), tree.CONST.FALSE);
	}
	
	public tree.Stm asStm() {
		return new tree.EVAL(new tree.BINOP(tree.BINOP.AND, e.asExp(), tree.CONST.FALSE));
	}
	
	public tree.Stm asCond(tree.NameOfLabel truthLabel, tree.NameOfLabel falseLabel) {
		tree.Stm cjump;
		cjump = new tree.CJUMP(tree.CJUMP.NE, 
							   e.asExp(),
							   tree.CONST.TRUE,
							   truthLabel,
							   falseLabel
						   	   );
		return cjump;
	}

//	@Override
//	Stm asCond(NameOfLabel t, NameOfLabel f) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public void passName(String n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
}

class UnsupportedException extends Exception {
	UnsupportedException(String s) {
		super(s);
	}
}



//class BinopExp extends LazyIRTree {
//	final int b;
//	final tree.Exp l, r;
//	public BinopExp(int binop, LazyIRTree left, LazyIRTree right) {
//		
//		b = binop;
//		// We know that we need to evaluate these expressions,
//		// so there is now point in waiting to evaluate them
//		l = left.asExp();
//		r = right.asExp();
//	}
//	
//	public tree.Exp asExp() {
//		return new tree.BINOP(b, l, r);
//	}
//	
//	// Should not evaluate to a Stm, throw exception
//	public tree.Stm asStm() {
//		throw new UnsupportedException();
//	}
//	
//	public tree.Stm asCond(tree.LABEL t, tree.LABEL f) {
//		return tree.CJUMP(tree.CJUMP.EQ,
//						  tree.BINOP(b, l, r),
//						  tree.CONST(0),
//						  new tree.Label(t),
//						  new tree.LABEL(f)				
//						  );
//	}
//}


//
//class CallExp extends LazyIRTree {
//	private final tree.NAME func;
//	public final tree.ExpList args;
//	public final List<tree.Exp> UNUSEDargs;
//	
//	CallExp(final tree.NAME f, final ArrayList<LazyIRTree> a, 
//			final ArrayList<LazyIRTree> x) {
//		func = f;
//		args = 
//	}
//	
//	public tree.Exp asExp() {
//		tree.Exp call;
//		
//		
//		call = tree.CALL(func, args, );
//		return call;
//	}
//	
//	// Function call as cond
//	public tree.Stm asCond(tree.LABEL t, tree.LABEL f) {
//		tree.Stm cjump;
//		cjump = tree.CJUMP(EQ, 
//				tree.CALL(func, args, UNUSEDargs),
//				tree.CONST(0),
//				new tree.LABEL(t),
//				new tree.LABEL(f)
//				);
//		return cjump;
//	}	
//}

//class WhileExp extends LazyIRTree {
//	private final LazyIRTree cond, e;
//	final tree.NameOfLabel test = new tree.LABEL("while", "test");
//	final tree.NameOfLabel body = new tree.LABEL("while", "body");
//	final tree.NameOfLabel done = new tree.LABEL("while", "done");
//	
//	WhileExp(final LazyIRTree c, final LazyIRTree body) {
//		assert cond != null; assert body != null;
//		cond = c; e = body;
//	}
//	
//	// NO while expression in MiniJava
//	public tree.Exp asExp() {
//		throw new UnsupportedException();
//	}
//	
//	// while statement as statement
//	public tree.Stm asStm() {
//		final tree.Stm seq;
//		seq = tree.SEQ.fromList(
//				new tree.LABEL(test),
//				cond.asCond(body, done),
//				new tree.LABEL(body),
//				e.asStm(),
//				new tree.JUMP(test),
//				new tree.LABEL(done)
//				);
//	}
//	
//	// NO while expression as cond
//	public tree.Stm asCond(tree.LABEL tt, tree.LABEL ff) {
//		throw new UnsupportedException();
//	}	
//}

//class NewObjExp extends LazyIRTree {
//	public final tree.NAME object;
//	
//	
//}




package visitors;

public class MethodFragment {

	tree.NameOfLabel methodName;
	frame.Frame methodFrame;
	tree.Stm body;
	
	public MethodFragment(tree.NameOfLabel n, frame.Frame f, tree.Stm b) {
		methodName = n;
		methodFrame = f;
		body = b;
	}
	
	public tree.Stm getBody() {
		return body;
	}
	
	public tree.NameOfLabel getMethodName() {
		return methodName;
	}
	
	public frame.Frame getMethodFrame() {
		return methodFrame;
	}
}

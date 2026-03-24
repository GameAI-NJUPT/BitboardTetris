package search;
import rl.ValueFunction;

public abstract class TreeNode {

	TreeNode[] children;
	public abstract boolean expand();
    public abstract double expectimax(int searchDepth);
    ValueFunction vf;
    double value;
    double discount;
	//boolean isAfterState;
	public int arity() {
        return children == null ? 0 : children.length;
    }
}

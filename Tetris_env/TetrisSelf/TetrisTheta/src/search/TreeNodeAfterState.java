package search;

import rl.ValueFunction;

public class TreeNodeAfterState extends TreeNode{

	public int[] afterstate;
	double reward;
	public int action; 
	double evaluationValue;
	double searchValue;
	public TreeNodeAfterState(ValueFunction vf,int[] gas,int action,double reward,double discount)
	{
		this.vf = vf;
		afterstate = gas;
		this.action = action;
		this.reward = reward;
		this.discount = discount;
	}
	public boolean expand() {
		
		this.children = new TreeNodeState[7];
		for(int i=0;i<7;i++)
		{
			int[] state = new int[afterstate.length];
			for(int j=0;j<afterstate.length;j++)
			{
				state[j] = afterstate[j];
			}
			state[11] += state[10];
			state[12] = i;
			children[i] = new TreeNodeState(vf,state,discount);
		}
		return true;
	}

	@Override
	public double expectimax(int searchDepth) {
		this.expand();
		int size = this.children.length;
		if(size==0)return 0;
		for(int i=0;i<this.children.length;i++)
		{
			this.children[i].value=this.children[i].expectimax(searchDepth-1);
		}
		//average
		{
			double sum = 0;
			for(int i=0;i<size;i++)
			{
				sum += this.children[i].value;
			}
			this.value = sum / size;
			return this.value;
		}
		
	}
}

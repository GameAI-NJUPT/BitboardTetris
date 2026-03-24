package search;

import rl.ValueFunction;
import settings.Parameters;

public class PlayerExpectimax {

    public double discount;
    public TreeNodeState tns;
    public ValueFunction vf;

    public PlayerExpectimax(ValueFunction vf, double discount) {
        this.vf = vf;
        this.discount = discount;
    }

    //mal 可选动作数量     ms current[]
    public TreeNodeState getChoice(int[] current, long mal) {
        tns = new TreeNodeState(vf, current, discount);
        if (mal == 0) {
            tns.greedyAction = -1;
            return tns;
        }
        tns.expectimax(Parameters.expectimaxSearchDepth);
        return tns;
    }
}

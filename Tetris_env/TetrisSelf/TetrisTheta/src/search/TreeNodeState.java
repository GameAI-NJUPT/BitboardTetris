package search;

import tetris.TetrisEnvironment;
import rl.ValueFunction;
import settings.Parameters;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class TreeNodeState extends TreeNode {

    public int[] state;
    int greedyAction = -1;
    public double[] nodeValue = new double[34];
    public int[] nodeMask = new int[34];
    public double[] features = new double[306];
    public boolean[] dones = new boolean[34];
    //可行动作数量
    public int validAction = 0;
    public int[] actions = new int[34];
    public double[] rewards = new double[34];

    public TreeNodeState(ValueFunction vf, int[] state, double discount) {
        //this.isAfterState = false;
        this.discount = discount;
        this.vf = vf;
        this.state = state;
    }

    @Override
    public boolean expand() {
        long al = TetrisEnvironment.getActionList(state);
        int size = TetrisEnvironment.actionSize(al);
        //如果节点不能继续往下搜索就返回false
        if (size == 0)
            return false;
        this.children = new TreeNodeAfterState[size];
        int index = -1;
        int start = 0;
        for (int i = 0; i < 34; i++) {
            actions[i] = -1;
            dones[i] = true;
        }
        for (int i = 0; i < TetrisEnvironment.blocks.squares[state[12]].actionSize; i++) {
            if (TetrisEnvironment.actionValid(i, al)) {
                index++;
                ++validAction;
                int[] gas = TetrisEnvironment.afterState(state, i);
                double reward = TetrisEnvironment.getReward(state, i, gas);
                this.children[index] = new TreeNodeAfterState(vf, gas, i, reward, this.discount);
                double[] nextFeature = vf.getFeature(state, i);
                for (double x : nextFeature) {
                    features[start++] = x;
                }
                int[] next = TetrisEnvironment.successorState(state, i);
                rewards[i] = next[10];
                if (!TetrisEnvironment.isFinal(next)) {
                    dones[i] = false;
                }
                actions[i] = 0;
            } else {
                double[] nextFeature = vf.getFeature(state, i);
                for (double x : nextFeature) {
                    features[start++] = x;
                }
            }
        }
        return true;
    }

    public int getGreedyAction() {
        return greedyAction;
    }

    /**
     * public int greedyAction()
     * {
     * //this.value =
     * this.expectimax(Parameters.expectimaxSearchDepth);
     * return this.greedyAction;
     * <p>
     * TreeNodeAfterState selected = null;
     * double bestValue = Double.NEGATIVE_INFINITY ;
     * for (TreeNode tn : children) {
     * TreeNodeAfterState tnas = (TreeNodeAfterState)tn;
     * double value = //tnas.reward+
     * Parameters.gamma*tnas.value;
     * if (value > bestValue) {
     * selected = tnas;
     * bestValue = value;
     * }
     * }
     * //System.out.println("Actually section action: "+selected.action+" with value: "+bestValue);
     * return selected.action;
     * }
     */

    public double expectimax(int searchDepth) {
        boolean success = this.expand();
        Arrays.fill(nodeValue, Double.NEGATIVE_INFINITY);
        if (!success)
            return 0;
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < this.children.length; i++) {
            TreeNodeAfterState tnas = (TreeNodeAfterState) this.children[i];
            //特征线性求和
            tnas.evaluationValue = vf.getValue(this.state, tnas.action);
            if (searchDepth == 0) {
                tnas.searchValue = 0;
                tnas.value = tnas.evaluationValue;
            } else {
                tnas.searchValue = this.children[i].expectimax(searchDepth);
                tnas.value = (1 - discount) * tnas.evaluationValue + discount * tnas.searchValue;
            }

            double value = tnas.reward + Parameters.gamma * tnas.value;
            this.nodeMask[tnas.action] = 1;
            this.nodeValue[tnas.action] = value;
            if (value > max) {
                max = value;
                greedyAction = tnas.action;
            }
        }
        //System.out.println("Tree section action: "+greedyAction+" with value: "+max);
        this.value = max;
        return max;
    }
}

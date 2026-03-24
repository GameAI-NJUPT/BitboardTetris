package utility;

import player.Player;
import rl.ValueFunction;
import tetris.TetrisDTFeature;
import tetris.TetrisEnvironment;

import java.util.Arrays;

public class validate {
    public static void main(String[] args) {
        TetrisDTFeature current = new TetrisDTFeature();
        TetrisDTFeature next = new TetrisDTFeature();
        ValueFunction vf = new ValueFunction(current, next, current.getNumberoffeature());
        vf.readTheta("D:\\graduate\\TetrisTheta\\TetrisTheta\\theta\\", "CBMPI20unsigned.theta");
        Player player = new Player(vf);
        int[] currentState = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0};
        System.out.println("当前十五维状态:" + Arrays.toString(currentState));
//        System.out.println("当前状态特征:" + Arrays.toString(vf.get));
        long al = TetrisEnvironment.getActionList(currentState);
        int a = player.getGreedyChoice(currentState, al);
        for (int i = 0; i < TetrisEnvironment.blocks.squares[currentState[12]].actionSize; i++) {
            if (TetrisEnvironment.actionValid(i, al)) {
                double[] nextFeature = vf.getFeature(currentState, i);
                System.out.println(i + ":" + Arrays.toString(nextFeature));
                System.out.println(vf.getValue(currentState, i));
            } else {
                double[] nextFeature = vf.getFeature(currentState, i);
                System.out.println(i + ":" + Arrays.toString(nextFeature));
                System.out.println("-inf");
            }
        }
        System.out.println("动作:" + a);
        double[] nextFeature = vf.getFeature(currentState, a);
//        vf.getValue()
        System.out.println("下一个状态的特征:" + Arrays.toString(nextFeature));

    }
}

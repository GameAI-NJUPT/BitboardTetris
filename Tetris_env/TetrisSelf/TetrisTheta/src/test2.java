import player.Player;
import rl.ParallelismTest;
import rl.ValueFunction;
import tetris.TetrisDTFeature;
import tetris.TetrisEnvironment;

public class test2 {
    public static void main(String[] args) {
        TetrisDTFeature current = new TetrisDTFeature();
        TetrisDTFeature next = new TetrisDTFeature();
        ValueFunction vf = new ValueFunction(current, next, current.getNumberoffeature());
        vf.readTheta("theta\\", "2900.theta");
        int[] currentState = new int[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        Player player = new Player(vf);
        double[] tpWeight = new double[261];
        tpWeight[0] = 100;
        player.setWeight(tpWeight);
        ParallelismTest test = new ParallelismTest(player);
        test.go();
        System.out.println(test.getNum());

//        double[] temp2 = vf.get9featureAndMask_2order(currentState);
//        int[] temp = TetrisEnvironment.successorState(currentState, 0);
//
//        boolean test = TetrisEnvironment.isFinal(temp);
//        System.out.println(test);
//        int[] currentState = new int[]{0, 54, 1023, 287, 944, 996, 511, 205, 61, 48, 0, 0, 6, 4, 0};
//        long startTime = System.currentTimeMillis();
//
//        // 要测试的代码块
//        for (int i = 0; i < 100000; i++) {
//            vf.get9featureAndMask(currentState);
//        }
//
//        // 记录结束时间
//        long endTime = System.currentTimeMillis();
//
//        // 计算执行时间
//        long executionTime = endTime - startTime;
//        System.out.println("代码执行时间: " + executionTime + " 毫秒");

//        long al1 = TetrisEnvironment.getActionList(currentState);
//        int[] s = currentState;
//        double maxValue = Double.NEGATIVE_INFINITY;
//        int action = -1;
//        for (int i = 0; i < TetrisEnvironment.blocks.squares[s[12]].actionSize; i++) {
//            if (TetrisEnvironment.actionValid(i, al1)) {
//                //System.out.print(i+", ");
//                //index++;
//                //int[] as = TetrisEnvironment.afterState(s, i);
//                double value = vf.getValue(s, i) - 1.7527;
//                double[] feature = vf.getFeature(s, i);
//                for (int j = 0; j < 9; j++) {
//                    System.out.print(feature[j] + " ");
//                }
//                System.out.println("===========" + i + "===========");
//                System.out.println(value);
//                if (maxValue < value) {
//                    maxValue = value;
//                    action = i;
//                }
//            }
//        }
//        System.out.println(action);
//        int[] nextState = TetrisEnvironment.successorState(currentState, 18);
//        System.out.println(Arrays.toString(nextState));
//        if (TetrisEnvironment.isFinal(nextState))
//            System.out.println("结束");
    }
}

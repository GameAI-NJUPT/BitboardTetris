import player.Player;
import rl.ValueFunction;
import tetris.TetrisDTFeature;
import tetris.TetrisEnvironment;

public class testTime {
    public static void main(String[] args) throws InterruptedException {
        TetrisDTFeature current = new TetrisDTFeature();
        TetrisDTFeature next = new TetrisDTFeature();
        ValueFunction vf = new ValueFunction(current, next, current.getNumberoffeature());
        vf.readTheta("D:\\Desktop\\graduate\\TetrisSelf\\TetrisTheta\\theta\\", "CBMPI10.theta");
        Player player = new Player(vf);
        int[] currentState = TetrisEnvironment.defaultInitialState();
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            long al0 = TetrisEnvironment.getActionList(currentState);
            int a1 = player.getChoice(currentState, al0);
            int[] nextState = TetrisEnvironment.successorState(currentState, a1);
            if (TetrisEnvironment.isFinal(nextState)) {
                break;
            }
            currentState = TetrisEnvironment.copy(nextState);
        }
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime; // 单位为毫秒
        System.out.println("代码运行时间: " + duration + " 毫秒");
    }
}
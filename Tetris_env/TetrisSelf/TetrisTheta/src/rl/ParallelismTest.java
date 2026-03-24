package rl;

import player.Player;
import settings.Parameters;
import tetris.TetrisDTFeature;
import tetris.TetrisEnvironment;
import utility.Record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelismTest {

    final Record record;
    Player player;
    double num;

    public ParallelismTest(Player player) {
        this.player = player;
        record = new Record(Parameters.testEpisodeNum);
    }

    public double getNum() {
        return num;
    }

    public void setNum(double num) {
        this.num = num;
    }

    public void go() {
        GoTest[] gotest = new GoTest[Parameters.lockFreeTestNum];
        for (int i = 0; i < Parameters.lockFreeTestNum; i++) {
            gotest[i] = new GoTest(i, Parameters.testEpisodeNum / Parameters.lockFreeTestNum, this.player.copy());
            gotest[i].start();
        }
        for (int i = 0; i < Parameters.lockFreeTestNum; i++) {
            try {
                gotest[i].join();
            } catch (InterruptedException e) {
                System.out.print(e.getMessage());
            }
        }
        synchronized (record) {
            num = record.averaged();
//            System.out.println(record);
        }
    }

    AtomicInteger nums = new AtomicInteger(0);

    class GoTest extends Thread {
        int ID;
        int size;
        Player p;

        public GoTest(int id, int size, Player player) {
            this.size = size;
            ID = id;
            this.p = player;
        }

        public void run() {
            for (int i = 0; i < size; i++) {
                double lines = episode(p);
                int index = i + ID * size;
                record.setResult(lines, index);
                nums.incrementAndGet();
//                if (nums.get() % (Parameters.lockFreeTestNum * 10) == 0) {
//                    System.out.println(nums.get() + "/" + Parameters.testEpisodeNum);
//                }
//                System.out.println(lines);
            }

        }
    }

    public double episode(Player player) {
        int[] current = TetrisEnvironment.defaultInitialState();
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            long al0 = TetrisEnvironment.getActionList(current);
            int a1 = player.getChoice(current, al0);
//            正确的俄罗斯方块游戏
            int[] next = TetrisEnvironment.successorState(current, a1);
            //double reward = TetrisEnvironment.getReward(current, a1, next);
            if (TetrisEnvironment.isFinal(next)) {
                break;
            }
            current = TetrisEnvironment.copy(next);
        }
        return current[11];
    }

    // 生成随机方块顺序
    private static int[] generateRandomBlockOrder() {
        int[] blockOrder = new int[]{0, 1, 2, 3, 4, 5, 6}; // 初始化数组
        List<Integer> list = new ArrayList<>();
        for (int num : blockOrder) {
            list.add(num);
        }
        // 使用Collections.shuffle()对List进行随机排序
        Collections.shuffle(list);
        // 将List转换回数组
        for (int i = 0; i < blockOrder.length; i++) {
            blockOrder[i] = list.get(i);
        }
        return blockOrder;
    }

    public double episode7Bag(Player player) {
        int[] current = TetrisEnvironment.defaultInitialState();
        int[] blockOrder = generateRandomBlockOrder();
        int index = 0;
        current[12] = blockOrder[index++];
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            long al0 = TetrisEnvironment.getActionList(current);
            int a1 = player.getChoice(current, al0);
            if (index >= 7) {
                blockOrder = generateRandomBlockOrder();
                index = 0;
            }
            int[] next = TetrisEnvironment.successorState(current, a1);
            next[12] = blockOrder[index++];
            //double reward = TetrisEnvironment.getReward(current, a1, next);
            if (TetrisEnvironment.isFinal(next)) {
                break;
            }
            current = TetrisEnvironment.copy(next);
        }
        return current[11];
    }

    public static void main(String[] args) {
//        //评估正常的dt-9的weight
//        TetrisDTFeature current = new TetrisDTFeature();
//        TetrisDTFeature next = new TetrisDTFeature();
//        ValueFunction vf = new ValueFunction(current, next, current.getNumberoffeature());
//        vf.readTheta("D:\\Desktop\\graduate\\code\\Tetris_env\\TetrisSelf\\TetrisTheta\\theta\\", "5268.theta");
//        Player player = new Player(vf);
//        ParallelismTest p = new ParallelismTest(player);
//        p.go();
//        System.out.println(p.getNum());

        //评估as的weight 修改parameters中typechoice为231  修改TetrisDTFeature中的特征
//        TetrisDTFeature current = new TetrisDTFeature();
//        TetrisDTFeature next = new TetrisDTFeature();
//        ValueFunction vf = new ValueFunction(current, next, current.getNumberoffeature());
//        Player player = new Player(vf);
//        player.readWeight("D:\\Desktop\\graduate\\code\\Tetris_env\\TetrisSelf\\TetrisTheta\\theta\\as");
//        ParallelismTest p = new ParallelismTest(player);
//        p.go();
//        System.out.println(p.getNum());

        //评估as-s的weight 修改parameters中typechoice为205 修改TetrisDTFeature中的特征
        TetrisDTFeature current = new TetrisDTFeature();
        TetrisDTFeature next = new TetrisDTFeature();
        ValueFunction vf = new ValueFunction(current, next, current.getNumberoffeature());
        Player player = new Player(vf);
        player.readDiffWeight("D:\\Desktop\\graduate\\code\\Tetris_env\\TetrisSelf\\TetrisTheta\\theta\\as-s");
        ParallelismTest p = new ParallelismTest(player);
        p.go();
        System.out.println(p.getNum());
    }
}

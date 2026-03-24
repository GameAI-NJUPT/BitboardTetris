package dataset;

import rl.ValueFunction;
import search.PlayerExpectimax;
import search.TreeNodeState;
import tetris.Blocks;
import tetris.TetrisDTFeature;
import tetris.TetrisEnvironment;


public class buffer {
    private final int bufferLength = 100000;
    public ValueFunction vf;
    public double[][] features = new double[bufferLength][306];
    public double[][] nfeatures = new double[bufferLength][306];
    // 实际选择动作
    public int[] raction = new int[bufferLength];
    public double[] rewards = new double[bufferLength];
    public int[] dones = new int[bufferLength];
    public double[][] rfeatures = new double[bufferLength][9];
    public double[][] vfeatures = new double[bufferLength][7];
    public double[][] allvfeatures = new double[bufferLength][238];
    public double[][] ln_a = new double[bufferLength][34];
    public int[][] mask = new int[bufferLength][34];
    public int reward;

    //有参构造
    public buffer(ValueFunction vf) {
        this.vf = vf;
    }

    public void setVf(ValueFunction vf) {
        this.vf = vf;
    }


    public static void main(String[] args) {
        TetrisDTFeature current = new TetrisDTFeature();
        TetrisDTFeature next = new TetrisDTFeature();
        ValueFunction vf = new ValueFunction(current, next, current.getNumberoffeature());
        vf.readTheta("D:\\graduate\\TetrisSelf\\TetrisTheta\\theta\\", "CBMPI20unsigned.theta");
        buffer b = new buffer(vf);
        int length = b.generateAllBuffer(3000, 0.1);
        System.out.println(length);

        //获得用vf函数得到的数据集
//        int length = b.generateAllBuffer(10000);
//        System.out.println(length);
//        for(int i=0;i<5;i++) {
//            System.out.println(b.generateBetterBuffer(pe)+" "+b.reward);
//        }
    }

    public double[] state2SearchDis(int[] state, PlayerExpectimax pe) {
        long al0 = TetrisEnvironment.getActionList(state);
        TreeNodeState tns = pe.getChoice(state, al0);
        double[] probabilities = softmax(tns.nodeValue, tns.validAction);
        double[] log_a = new double[34];
        for (int i = 0; i < probabilities.length; i++) {
            log_a[i] = Math.log(probabilities[i]);
        }
        return log_a;
    }

    public void arrayCopy(double[] obj, double[] src) {
        obj[0] = src[0];
        obj[1] = src[1];
        obj[2] = src[2];
        obj[3] = src[4];
        obj[4] = src[5];
        obj[5] = src[7];
        obj[6] = src[8];
    }

    public void arrayCopyAll(double[] obj, double[] src) {
        for (int i = 0; i < 34; ++i) {
            obj[7 * i] = src[9 * i];
            obj[7 * i + 1] = src[9 * i + 1];
            obj[7 * i + 2] = src[9 * i + 2];
            obj[7 * i + 3] = src[9 * i + 4];
            obj[7 * i + 4] = src[9 * i + 5];
            obj[7 * i + 5] = src[9 * i + 7];
            obj[7 * i + 6] = src[9 * i + 8];
        }
    }

//    //输入需要生成的数据集长度
//    public int generateSearchBuffer(int length) {
//        int step = 0;
//        int i = 0;
//        PlayerExpectimax pe = new PlayerExpectimax(vf.copy(), 0.15);
//        while (step < length) {
//            int[] current = TetrisEnvironment.defaultInitialState();
//            double[] rfeature;
//            for (; i < Integer.MAX_VALUE; i++) {
////                if (i % 1000 == 0)
////                    System.out.println("generate:" + i + "/" + length);
//                System.arraycopy(current, 0, states[i], 0, 15);
//                long al0 = TetrisEnvironment.getActionList(current);
//                TreeNodeState tns = pe.getChoice(current, al0);
//                int a = tns.getGreedyAction();
//                System.arraycopy(tns.features, 0, features[i], 0, 306);
//                System.arraycopy(tns.nodeMask, 0, mask[i], 0, 34);
//                double[] probabilities = softmax(tns.nodeValue, tns.validAction);
//                for (int j = 0; j < probabilities.length; ++j) {
//                    ln_a[i][j] = Math.log(probabilities[j]);
//                }
//                System.arraycopy(tns.dones, 0, dones[i], 0, 34);
//                System.arraycopy(tns.rewards, 0, rewards[i], 0, 34);
//                if (a == -1) {
//                    reward = current[11];
//                    break;
//                }
//                raction[i] = a;
//                double[] temp = vf.getFeature(current, a);
//                rfeature = temp;
//                System.arraycopy(rfeature, 0, rfeatures[i], 0, 9);
//                arrayCopy(vfeatures[i], rfeature);
//                arrayCopyAll(allvfeatures[i], features[i]);
//                if (i > 0) {
//                    System.arraycopy(features[i - 1], 0, nfeatures[i], 0, 306);
//                }
//                int[] next = TetrisEnvironment.successorState(current, a);
//                if (dones[i][a]) {
//                    reward = Math.max(reward, next[11]);
//                    break;
//                }
//                ++step;
//                System.arraycopy(states[i], 0, current, 0, 15);
//                if (step == length)
//                    break;
//                current = TetrisEnvironment.copy(next);
//            }
//        }
//        return step;
//    }

//    public int getGreedyBuffer(int[] s, long al, int index) {
//        //先全部置为不可执行
//        for (int i = 0; i < 34; i++) {
//            dones[index][i] = true;
//        }
//        if (al == 0) {
//            return -1;
//        }
//        double[] value = new double[34];
//        double maxValue = Double.NEGATIVE_INFINITY;
//        int a = -1;
//        int start = 0;
//        int i = 0;
//        for (; i < TetrisEnvironment.blocks.squares[s[12]].actionSize; i++) {
//            if (TetrisEnvironment.actionValid(i, al)) {
//                int[] next = TetrisEnvironment.successorState(s, i);
//                rewards[index][i] = next[10];
//                mask[index][i] = 1;
//                value[i] = vf.getValue(s, i);
//                double[] nextFeature = vf.getFeature(s, i);
//                for (double x : nextFeature) {
//                    features[index][start++] = x;
//                }
//                if (!TetrisEnvironment.isFinal(next)) {
//                    dones[index][i] = false;
//                }
//                if (maxValue < value[i]) {
//                    maxValue = value[i];
//                    a = i;
//                }
//            } else {
//                double[] nextFeature = vf.getFeature(s, i);
//                for (double x : nextFeature) {
//                    features[index][start++] = x;
//                }
//                value[i] = Double.NEGATIVE_INFINITY;
//            }
//        }
//        while (i < 34) {
//            value[i++] = Double.NEGATIVE_INFINITY;
//        }
//        if (i == 35)
//            --i;
//        double[] probabilities = softmax(value, i);
//        for (int j = 0; j < probabilities.length; ++j) {
//            ln_a[index][j] = Math.log(probabilities[j]);
//        }
//        return a;
//    }


    //输入需要生成的数据集长度
    public int generateAllBuffer(int length, double bias) {
        int step = 0;
        int i = 0;
        while (step < length) {
            int[] current = TetrisEnvironment.defaultInitialState();
            boolean done = false;
            double[] feature = new double[306];
            double[] rfeature = new double[9];
            while (!done) {
                System.arraycopy(feature, 0, features[i], 0, 306);
                System.arraycopy(rfeature, 0, rfeatures[i], 0, 9);
                arrayCopy(vfeatures[i], rfeature);
                arrayCopyAll(allvfeatures[i], features[i]);
                long al0 = TetrisEnvironment.getActionList(current);
                int a = choose_from_softmax(current, al0, i, bias);
                if (a == -1) {
                    reward = current[11];
                    break;
                }
                raction[i] = a;
                //下个状态的实际特征
                rfeature = vf.getFeature(current, a);
                System.arraycopy(nfeatures[i], 0, feature, 0, 306);
                int[] next = TetrisEnvironment.successorState(current, a);
                rewards[i] = next[10];
                if (TetrisEnvironment.isFinal(next)) {
                    done = true;
                    dones[i] = 1;
                    reward = Math.max(reward, next[11]);
                }
                current = TetrisEnvironment.copy(next);
                ++i;
                ++step;
                if (step == length) {
                    break;
                }
            }
        }
        return step;
    }

    //原版留档
//    //输入需要生成的数据集长度和偏差
//    public int generateAllBuffer(int length, double bias) {
//        int step = 0;
//        int i = 0;
//        while (step < length) {
//            int[] current = TetrisEnvironment.defaultInitialState();
//            double[] rfeature;
//            for (; i < Integer.MAX_VALUE; i++) {
//                System.arraycopy(current, 0, states[i], 0, 15);
//                long al0 = TetrisEnvironment.getActionList(current);
////                int a = getGreedyBuffer(current, al0, i);
//                int a = choose_from_softmax(current, al0, i, bias);
//                if (a == -1) {
//                    reward = current[11];
//                    break;
//                }
//                actions[i][a] = 1;
//                raction[i] = a;
//                double[] temp = vf.getFeature(current, a);
//                rfeature = temp;
//                System.arraycopy(rfeature, 0, rfeatures[i], 0, 9);
//                arrayCopy(vfeatures[i], rfeature);
//                arrayCopyAll(allvfeatures[i], features[i]);
//                if (i > 0) {
//                    System.arraycopy(features[i - 1], 0, nfeatures[i], 0, 306);
//                }
//                int[] next = TetrisEnvironment.successorState(current, a);
//                if (dones[i][a]) {
//                    reward = Math.max(reward, next[11]);
//                    break;
//                }
//                ++step;
//                System.arraycopy(states[i], 0, current, 0, 15);
//                if (step == length)
//                    break;
//                current = TetrisEnvironment.copy(next);
//            }
//        }
//        return step;
//    }

    public static double[] softmax(double[] logits, int j) {
        double[] probabilities = new double[j];
        System.arraycopy(logits, 0, probabilities, 0, j);
        double sum = 0.0;
        for (int i = 0; i < j; i++) {
            probabilities[i] = Math.exp(logits[i]);
            sum += probabilities[i];
        }

        for (int i = 0; i < j; i++) {
            probabilities[i] /= sum;
        }
        return probabilities;
    }

    public static int selectAction(double[] probabilities) {
        double randomValue = Math.random();
        double cumulativeProbability = 0.0;
        for (int i = 0; i < probabilities.length; i++) {
            cumulativeProbability += probabilities[i];
            if (randomValue <= cumulativeProbability) {
                return i;
            }
        }
        // 如果随机数落在最后一个动作之后，默认选择最后一个动作
        return probabilities.length - 1;
    }

    public int choose_from_softmax(int[] s, long al, int i, double bias) {
        if (al == 0)
            return -1;
        int index = 0;
        int j = 0;
        double[] value = new double[34];
        for (; j < Blocks.squares[s[12]].actionSize; j++) {
            if (TetrisEnvironment.actionValid(j, al)) {
                value[j] = vf.getValue(s, j) + bias;
                double[] temp = vf.getFeature(s, j);
                for (double x : temp) {
                    nfeatures[i][index++] = x;
                }
                mask[i][j] = 1;
            } else {
                double[] temp = vf.getFeature(s, j);
                for (double x : temp) {
                    nfeatures[i][index++] = x;
                }
                value[j] = Double.NEGATIVE_INFINITY;
            }
        }
        while (j < 34) {
            value[j++] = Double.NEGATIVE_INFINITY;
        }
        if (j == 35)
            j--;
        // 计算 softmax
        double[] probabilities = softmax(value, j);
        for (int k = 0; k < probabilities.length; ++k) {
            ln_a[i][k] = Math.log(probabilities[k]);
        }
        return selectAction(probabilities);
    }

    public int getChoice(int[] s, long al, int i) {
        if (al == 0)
            return -1;
        double maxValue = Double.NEGATIVE_INFINITY;
        int action = -1;
        int index = 0;
        for (int j = 0; j < Blocks.squares[s[12]].actionSize; j++) {
            if (TetrisEnvironment.actionValid(j, al)) {
                double value = vf.getValue(s, j);
                double[] temp = vf.getFeature(s, j);
                for (double x : temp) {
                    features[i][index++] = x;
                }
                if (maxValue < value) {
                    maxValue = value;
                    action = j;
                }
            }
        }
        return action;
    }
}

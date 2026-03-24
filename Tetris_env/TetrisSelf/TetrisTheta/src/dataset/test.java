package dataset;

import java.util.Random;

public class test {
    public static void main(String[] args) {
        // 输入值
        double[] logits = {2.0, 1.0, 0.1};

        // 计算 softmax
        double[] probabilities = softmax(logits);

        // 输出概率
        System.out.println("Softmax Probabilities:");
        for (int i = 0; i < probabilities.length; i++) {
            System.out.println("Action " + i + ": " + probabilities[i]);
        }

        // 根据概率选择动作
        int selectedAction = selectAction(probabilities);
        System.out.println("Selected Action: " + selectedAction);
    }

    // 计算 softmax 函数
    public static double[] softmax(double[] logits) {
        double[] probabilities = new double[logits.length];
        double sum = 0.0;

        for (int i = 0; i < logits.length; i++) {
            probabilities[i] = Math.exp(logits[i]);
            sum += probabilities[i];
        }

        for (int i = 0; i < probabilities.length; i++) {
            probabilities[i] /= sum;
        }

        return probabilities;
    }

    // 根据概率选择动作
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
}

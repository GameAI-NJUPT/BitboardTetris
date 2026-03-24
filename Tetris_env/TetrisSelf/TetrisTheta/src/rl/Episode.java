package rl;

import tetris.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import player.Player;
import utility.Record;
import settings.Parameters;

public class Episode {

    public double episodeTrainingExperience(Player player) {
        player.newEpisode();
        int[] current = TetrisEnvironment.defaultInitialState();
        long al1 = TetrisEnvironment.getActionList(current);
        double[] a1 = player.getLearningChoice(current, al1);
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            //System.out.println("steps: "+i+", action: "+a1[0]);
            //player.vf.printTheta();
            int[] next = TetrisEnvironment.successorState(current, (int) a1[0]);
            double reward = TetrisEnvironment.getReward(current, (int) a1[0], next);
            long al2 = TetrisEnvironment.getActionList(next);
            double[] a2 = player.getLearningChoice(next, al2);

            Experience e = new Experience(current, (int) a1[0], reward, next, (int) a2[0], a2[1], a2[2], a2[3], a2[4], 0);
            current = TetrisEnvironment.copy(next);
            a1 = TetrisEnvironment.copy(a2);
            if (TetrisEnvironment.isFinal(current)) {
                e.setDone(1);
                player.learn(e);
                break;
            } else {
                player.learn(e);
            }
        }
        return current[11];
    }

    public void training(Player player, Log log) {
        log.info("training for learning algorithm: " + player.typeLearning);
        Record rlResultAll = new Record(Parameters.episodeNum * Parameters.independentRuns);
        Record rlResultMean = new Record(Parameters.episodeNum);
        Record rlResultStd = new Record(Parameters.episodeNum);
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < Parameters.independentRuns; i++) {
            player.vf.init();
            for (int j = 0; j < Parameters.episodeNum; j++) {
                double record = -1;
                record = episodeTrainingExperience(player);
                //player.vf.printQ();
                if ((j + 1) % 100 == 0)
                    log.info("Run: " + i + ", episode: " + j + ", lines: " + record);
                rlResultAll.setResult(record, j + Parameters.episodeNum * i);
                if (max < record) {
                    max = record;
                    if (max > 1000) {
                        player.vf.writeTheta("theta//Tetris_" + Parameters.RowNum + "_" + 10 + "_learningType_" + player.typeLearning + "_" + max + ".theta");
                    }
                }
            }
        }
        for (int i = 0; i < Parameters.episodeNum; i++) {
            double sum = 0;
            for (int j = 0; j < Parameters.independentRuns; j++) {
                sum += rlResultAll.getResult(i + j * Parameters.episodeNum);
            }
            rlResultMean.setResult(sum / Parameters.independentRuns, i);

            sum = 0;
            for (int j = 0; j < Parameters.independentRuns; j++) {
                double x = rlResultAll.getResult(i + j * Parameters.episodeNum);
                double average = rlResultMean.getResult(i);
                sum += (x - average) * (x - average);
            }
            rlResultStd.setResult(Math.sqrt(sum / Parameters.independentRuns), i);
        }
        String fileName = "record//" + TetrisEnvironment.getName() + "_" + Parameters.RowNum + "_" + 10 + "_learningType_" + player.typeLearning;
        rlResultMean.writeResultToUTF(fileName + ".Mean");
        rlResultStd.writeResultToUTF(fileName + ".Std");
        rlResultAll.writeResultToUTF(fileName + ".All");
        log.info("write record to " + fileName);
    }

    class GoTest extends Thread {
        Player p;

        public GoTest(Player player) {
            this.p = player;
        }

        public void run() {
            //System.out.println("learning type: "+p.typeLearning);
            Log log = LogFactory.getLog("Training algorithms: " + p.typeLearning);
            training(p, log);
        }
    }

    public void go() {
        //int[] learningTypes = {0,1,2,3,4,5,6,7,8};
        //int[] learningTypes = {0,1,2,3,4,5,6,7};
        int[] learningTypes = {3};
        GoTest[] gotest = new GoTest[learningTypes.length];
        for (int i = 0; i < learningTypes.length; i++) {
            TetrisDTFeature current = new TetrisDTFeature();
            TetrisDTFeature next = new TetrisDTFeature();
            ValueFunction vf = new ValueFunction(current, next, current.getNumberoffeature());
            Player player = new Player(vf);
            player.typeLearning = learningTypes[i];
            gotest[i] = new GoTest(player);
            gotest[i].start();
            //log.info("Lock-free testing: "+i+" start");
        }
    }

    public double episodeTesting(Player player) {
        int[] current = TetrisEnvironment.defaultInitialState();
        long al1 = TetrisEnvironment.getActionList(current);
        int a1 = player.getChoice(current, al1);
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            int[] next = TetrisEnvironment.successorState(current, a1);
            double reward = TetrisEnvironment.getReward(current, a1, next);
            long al2 = TetrisEnvironment.getActionList(next);
            int a2 = player.getChoice(next, al2);
            //player.learn(current, a1, reward, next, a2);
            current = TetrisEnvironment.copy(next);
            a1 = a2;
            if (TetrisEnvironment.isFinal(current)) {
                break;
            }
        }
        return current[11];
    }

    public void test(Player player) {
        Record rlResultAll = new Record(Parameters.episodeNum * Parameters.independentRuns);
        Record rlResultMean = new Record(Parameters.episodeNum);
        Record rlResultStd = new Record(Parameters.episodeNum);
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < Parameters.independentRuns; i++) {
            player.vf.init();
            for (int j = 0; j < Parameters.episodeNum; j++) {
                double record = episodeTesting(player);
                //player.vf.printQ();
                System.out.println("Run: " + i + ", episode: " + j + ", lines: " + record);
                rlResultAll.setResult(record, j + Parameters.episodeNum * i);
                if (max < record) {
                    max = record;
                    if (max > 1000) {
                        player.vf.writeTheta("theta\\featureIndex_" + Parameters.RowNum + "_" + 10 + "_" + max + ".theta");
                    }
                }
            }
        }
        for (int i = 0; i < Parameters.episodeNum; i++) {
            double sum = 0;
            for (int j = 0; j < Parameters.independentRuns; j++) {
                sum += rlResultAll.getResult(i + j * Parameters.episodeNum);
            }
            rlResultMean.setResult(sum / Parameters.independentRuns, i);

            sum = 0;
            for (int j = 0; j < Parameters.independentRuns; j++) {
                double x = rlResultAll.getResult(i + j * Parameters.episodeNum);
                double average = rlResultMean.getResult(i);
                sum += (x - average) * (x - average);
            }
            rlResultStd.setResult(Math.sqrt(sum / Parameters.independentRuns), i);
        }
        String fileName = "record\\" + TetrisEnvironment.getName();
        rlResultMean.writeResultToUTF(fileName + ".Mean");
        rlResultStd.writeResultToUTF(fileName + ".Std");
        rlResultAll.writeResultToUTF(fileName + ".All");
    }

    public static void main(String[] args) {
        Episode e = new Episode();
        e.go();
    }
}

package rl;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import tetris.Blocks;
import tetris.TetrisDTFeature;
import tetris.TetrisEnvironment;
import utility.ArrfFileWriter;
import utility.DominanceFilter;

public class Dataset {

    ValueFunction vf;
    TetrisDTFeature tf;
    int RecordLength = 100000;
    int sampleLength = 10;
    int[][] data = new int[RecordLength][sampleLength];
    int recordStart = 0;
    DominanceFilter df;
    public static Log log = LogFactory.getLog("Tetris Dataset generation");

    public Dataset() {
        tf = new TetrisDTFeature();
        TetrisDTFeature current = new TetrisDTFeature();
        TetrisDTFeature next = new TetrisDTFeature();
        vf = new ValueFunction(current, next, current.getNumberoffeature());
        vf.readTheta("theta//", "CBMPI10unsigned.theta");
        df = new DominanceFilter();
    }

    public int greedyChoice(int[] s, long al) {
        if (al == 0) return -1;
        int size = TetrisEnvironment.actionSize(al);
        //System.out.print("size in all: "+size);
        double maxValue = Double.NEGATIVE_INFINITY;
        int[][] feature = new int[size][sampleLength];
        double[] actionValue = new double[size];
        int index = -1;
        int action = -1;
        for (int i = 0; i < Blocks.squares[s[12]].actionSize; i++) {
            if (TetrisEnvironment.actionValid(i, al)) {
                //System.out.print(i+", ");
                index++;
                int[] as = TetrisEnvironment.afterState(s, i);
                double reward = TetrisEnvironment.getReward(s, i, as);
                tf.setFeature(s, i);
                actionValue[index] = vf.getValue(s, i);

                for (int j = 0; j < tf.featureNum; j++) {
                    feature[index][j] = (int) tf.getFeature(j);
                }
                //feature[index][9]=(int)reward;
                feature[index][9] = 0;
                if (maxValue < actionValue[index]) {
                    maxValue = actionValue[index];
                    action = i;
                }
            }
        }
        //first step is feature filtering
        //second step is labeling
        //we only use non-cumulative dominance filter
        boolean[] dom = df.dominate(feature)[1];
        int selectNum = 0;
        for (boolean b : dom) {
            if (b) selectNum++;
        }
        //System.out.println(", after select: "+size);
        int count = 0;
        for (int i = 0; i < size; i++) {
            if (maxValue - actionValue[i] < 0.000001) {
                feature[i][9] = 1;
                count++;
                //System.out.println("action: "+i+" has max value!");
            }
        }
        //System.out.println("number of actions that have max value is: "+count);

        //there is no means to add a sample with only one action
        if (selectNum > 1) {
            if (selectNum + recordStart > RecordLength - 1) {
                return -2;
            }
            int i = 0;
            for (index = 0; index < feature.length; index++) {
                if (dom[index]) {
                    //System.out.println("select: "+index);
                    for (int j = 0; j < sampleLength; j++) {
                        this.data[recordStart + i][j] = feature[index][j];
                    }
                    i++;
                }
            }
            recordStart += selectNum;
        }
        //System.out.println("Take action: "+action);
        //System.out.println("size: "+size+", select action index: "+action);
        return action;
    }

    public int getGreedyChoice(int[] s, long al) {
        if (al == 0) return -1;
        int size = TetrisEnvironment.actionSize(al);
        if (size + recordStart > RecordLength - 1) {
            return -2;
        }
        //System.out.println("***********************************************");
        double maxValue = Double.NEGATIVE_INFINITY;
        double[] actionValue = new double[size];
        int index = -1;
        int action = -1;
        for (int i = 0; i < TetrisEnvironment.blocks.squares[s[12]].actionSize; i++) {
            if (TetrisEnvironment.actionValid(i, al)) {
                //System.out.print(i+", ");
                index++;
                int[] as = TetrisEnvironment.afterState(s, i);
                double reward = TetrisEnvironment.getReward(s, i, as);
                tf.setFeature(s, i);
                actionValue[index] = //reward+
                        vf.getValue(s, i);
                for (int j = 0; j < tf.featureNum; j++) {
                    if (j == 6) {
                        this.data[recordStart + index][j] = (int) (tf.getFeature(j) * 2);
                    } else {
                        this.data[recordStart + index][j] = (int) tf.getFeature(j);
                    }
                }
                this.data[recordStart + index][9] = (int) reward;
                this.data[recordStart + index][10] = 0;
                if (maxValue < actionValue[index]) {
                    maxValue = actionValue[index];
                    action = i;
                }
            }
        }
        index = -1;
        for (int i = 0; i < TetrisEnvironment.blocks.squares[s[12]].actionSize; i++) {
            if (TetrisEnvironment.actionValid(i, al)) {
                index++;
                //label all actions with the maximum value
                if (maxValue == actionValue[index]) {
                    this.data[recordStart + index][10] = 1;
                }
            }
        }
        recordStart += size;
        //System.out.println();
        //System.out.println("size: "+size+", select action index: "+action);
        return action;
    }

    public int[] episode() {
        int[] current = TetrisEnvironment.defaultInitialState();
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            long al0 = TetrisEnvironment.getActionList(current);
            int a1 = greedyChoice(current, al0);
            if (a1 == -2) {
                return new int[]{current[11], -2};
            }
            int[] next = TetrisEnvironment.successorState(current, a1);
            if (TetrisEnvironment.isFinal(next)) {
                break;
            }
            current = TetrisEnvironment.copy(next);
        }
        return new int[]{current[11], 1};
    }

    public void generateDataset(int episodeNum) {
        long startTime = System.currentTimeMillis();
        //如果dataset塞满了，则测试结束，记录当前进行的局数，不如statistic统计有误
        int episodeCount = 0;
        double averagedLines = 0;
        for (int i = 0; i < episodeNum; i++) {
            System.out.print("Episode: " + i + ", ");
            int[] back = episode();
            int terminal = back[1];
            int lines = back[0];
            averagedLines += lines;
            System.out.println(lines);
            if (terminal == -2) {
                episodeCount = i;
                averagedLines = averagedLines / episodeCount;
                log.info("Averaged removed lines in " + episodeCount + " games are " + averagedLines);
                break;
            }
            episodeCount++;
        }

        this.writeDataset("C:\\Users\\Luke\\Desktop\\TetrisTheta\\TetrisTheta\\src\\dataset\\dataset.arff");
        long endTime = System.currentTimeMillis();
        long seconds = (endTime - startTime) / 1000;
        log.info("The program takes " + seconds + " seconds!");
    }

    public void writeDataset(String s) {
        FileOutputStream fos = null;
        DataOutputStream bw = null;
        try {
            fos = new FileOutputStream(s);
            bw = new DataOutputStream(fos);
            ArrfFileWriter afw = new ArrfFileWriter();
            String thetaString = afw.fileHead(tf);
            bw.write(thetaString.getBytes(StandardCharsets.UTF_8));
            bw.flush();
            int segment = 100000;
            for (int i = 0; i < segment + 1; i++) {
                thetaString = "";
                int range = this.RecordLength;
                for (int j = 0; j < range / segment; j++) {
                    int index = i * (range / segment) + j;
                    if (index >= range) {
                        break;
                    }
                    //thetaString += theta[stage][index][i*(range/segment)+j]+",";
                    for (int k = 0; k < sampleLength; k++) {
                        if (k == sampleLength - 1) {
                            thetaString += this.data[index][k] + "\n";
                        } else {
                            thetaString += this.data[index][k] + ",";
                        }
                    }
                }
                bw.write(thetaString.getBytes(StandardCharsets.UTF_8));
                bw.flush();
            }

        } catch (FileNotFoundException ex) {
            System.out.println("file not found: " + s);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            try {
                bw.close();
                fos.close();
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
    }

    public static void main(String[] args) {
        Dataset ds = new Dataset();
        ds.generateDataset(100);
    }
}

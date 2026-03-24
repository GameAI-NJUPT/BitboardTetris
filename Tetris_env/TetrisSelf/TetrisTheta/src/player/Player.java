package player;

import rl.Experience;
import rl.ValueFunction;
import settings.Parameters;
import tetris.Blocks;
import tetris.TetrisEnvironment;

import java.io.*;

public class Player {
    public ValueFunction vf;
    double epsilon = 0;
    public int typeLearning = 0;
    public int steps;
    //用于保存as231特征
    public double[] weight = new double[231];
    //用于保存as-s205特征
    public double[] weightDiff = new double[205];

    public void setWeight(double[] weight) {
        System.arraycopy(weight, 0, this.weight, 0, 231);
    }

    public void readWeight(String thetaFile) {
        FileInputStream fis = null;
        InputStreamReader isr;
        try {
            fis = new FileInputStream(thetaFile);
            isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String line;
            for (int i = 0; i < this.weight.length; i++) {
                line = br.readLine();
                this.weight[i] = Double.parseDouble(line);
            }
            System.out.println("load weight:" + thetaFile);
        } catch (FileNotFoundException ex) {
            System.out.println("file not found :" + thetaFile);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            try {
                if (fis != null) fis.close();
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public void readDiffWeight(String thetaFile) {
        FileInputStream fis = null;
        InputStreamReader isr;
        try {
            fis = new FileInputStream(thetaFile);
            isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String line;
            for (int i = 0; i < this.weightDiff.length; i++) {
                line = br.readLine();
                this.weightDiff[i] = Double.parseDouble(line);
            }
            System.out.println("load weight:" + thetaFile);
        } catch (FileNotFoundException ex) {
            System.out.println("file not found :" + thetaFile);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            try {
                if (fis != null) fis.close();
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }


    public void setWeightDiff(double[] weight) {
        System.arraycopy(weight, 0, this.weightDiff, 0, 205);
    }

    public Player(ValueFunction vf) {
        this.vf = vf;
    }

    public void setVf(ValueFunction vf) {
        this.vf = vf;
    }

    public void init() {
        vf.init();
        //epsilon = 1.0;
        epsilon = 0.01;

    }

    public void newEpisode() {
        epsilon *= 0.9992;
        if (epsilon <= 0.0001) epsilon = 0.0001;
    }

    //{action,rho,c,hybridX, x}
    public double[] getLearningChoice(int[] s, long al) {
        int greedyAction = this.getGreedyChoice(s, al);
        int size = TetrisEnvironment.actionSize(al);
        //rho = pi(s,a)/mu(s,a)
        //c = min(1,rho(s,a))
        //hybridX = greedy?1:x
        //x = min_a (mu(s,a)/pi(s,a))
        /*
         * in epsilon-greedy
         * greedy action: rho=1/mu(s,greedyAction),
         *                c=1,
         *                x= muGreedy
         *                hybridX=1
         * non-greedy action: rho=c= 0, hybridX=x=muGreedy
         */
        double rho = 0, c = 0, hybridX, x = 0;
        double muGreedy = 1.0 - epsilon + epsilon / size;
        //System.out.println("size: "+size);
        if (Parameters.typeChoice == 2) {
            if (Math.random() < epsilon) {
                int selection = this.getRandomChoice(s, al);
                if (greedyAction == selection) {
                    rho = 1.0 / muGreedy;
                    c = 1;
                    hybridX = 1;
                    x = muGreedy;
                    return new double[]{selection, rho, c, hybridX, x};
                } else {
                    rho = 0;
                    c = 0;
                    hybridX = muGreedy;
                    x = muGreedy;
                    return new double[]{selection, rho, c, hybridX, x};
                }
            } else {
                rho = 1.0 / muGreedy;
                c = 1;
                hybridX = 1;
                x = muGreedy;
                return new double[]{greedyAction, rho, c, hybridX, x};
            }
        } else return null;

    }

    public int getChoice(int[] s, long al) {
        if (Parameters.typeChoice == 0) return this.getRandomChoice(s, al);
        else if (Parameters.typeChoice == 1) return this.getGreedyChoice(s, al);
        else if (Parameters.typeChoice == 2) return this.getEpsilonGreedyChoice(s, al);
        else if (Parameters.typeChoice == 3) return this.getCriticChoice(s, al);
        else if (Parameters.typeChoice == 231) return this.getHandCodedChoice(s, al);
        else if (Parameters.typeChoice == 205) return this.getDiffHandCodedChoice(s, al);
        else return -1;
    }

    private int getEpsilonGreedyChoice(int[] s, long al) {
        if (al == 0) return -1;
        if (Math.random() < epsilon) {
            return this.getRandomChoice(s, al);
        } else {
            return this.getGreedyChoice(s, al);
        }
    }

    public int getRandomChoice(int[] s, long al) {
        if (al == 0) return -1;
        int size = TetrisEnvironment.actionSize(al);
        int actionIndex = (int) (Math.random() * size);
        int action = -1;
        for (int i = 0; i < TetrisEnvironment.blocks.squares[s[12]].actionSize; i++) {
            if (TetrisEnvironment.actionValid(i, al)) {
                action++;
                if (actionIndex == action) {
                    return i;
                }
            }
        }
        return -1;
    }

    public int getGreedyChoice(int[] s, long al) {
        if (al == 0)
            return -1;
        double maxValue = Double.NEGATIVE_INFINITY;
        int action = -1;
        for (int i = 0; i < TetrisEnvironment.blocks.squares[s[12]].actionSize; i++) {
            if (TetrisEnvironment.actionValid(i, al)) {
                double value = vf.getValue(s, i);
                if (maxValue < value) {
                    maxValue = value;
                    action = i;
                }
            }
        }
        return action;
    }

    public int getHandCodedChoice(int[] s, long al) {
        if (al == 0)
            return -1;
        double maxValue = Double.NEGATIVE_INFINITY;
        int action = -1;
        for (int i = 0; i < Blocks.squares[s[12]].actionSize; i++) {
            if (TetrisEnvironment.actionValid(i, al)) {
                double[] temp = vf.get231Feature(s, i);
                double value = 0;
                for (int j = 0; j < temp.length; j++) {
                    value = value + temp[j] * weight[j];
                }
                if (maxValue < value) {
                    maxValue = value;
                    action = i;
                }
            }
        }
        return action;
    }

    public int getDiffHandCodedChoice(int[] s, long al) {
        if (al == 0)
            return -1;
        double maxValue = Double.NEGATIVE_INFINITY;
        int action = -1;
        for (int i = 0; i < Blocks.squares[s[12]].actionSize; i++) {
            if (TetrisEnvironment.actionValid(i, al)) {
                double[] temp = vf.get205Feature(s, i);
                double value = 0;
                for (int j = 0; j < temp.length; j++) {
                    value = value + temp[j] * weightDiff[j];
                }
                if (maxValue < value) {
                    maxValue = value;
                    action = i;
                }
            }
        }
        return action;
    }

    public int getCriticChoice(int[] s, long al) {
        if (al == 0)
            return -1;
        double maxValue = Double.NEGATIVE_INFINITY;
        int action = -1;
        for (int i = 0; i < Blocks.squares[s[12]].actionSize; i++) {
            if (TetrisEnvironment.actionValid(i, al)) {
                double value = vf.getValue(s, i);
                double[] feature = vf.getFeature(s, i);
                double v2 = feature[0] * 1.9555e-04 + feature[1] * 2.2956e-05 + feature[2] * 2.1259e-05 +
                        feature[3] * 9.4617e-06 + feature[4] * (-2.6915e-05) + feature[5] * 1.5398e-04 +
                        feature[6] * 4.9989e-06 + feature[7] * (-1.3477e-04) + feature[8] * 2.6464e-05;
                value = value + Parameters.searchParameter * v2;
                if (maxValue < value) {
                    maxValue = value;
                    action = i;
                }
            }
        }
        return action;
    }

    public void learn(Experience e) {
        if (Parameters.learning == true) {
            //rho = Math.max(rho, 1);
            switch (typeLearning) {
                case 0:
                    this.vf.updateQLearning(e);
                    break;
                case 1:
                    this.vf.updateDoubleQLearning(e);
                    break;
                case 2:
                    this.vf.updateMRetrace(e);
                    break;
                default:
                    break;
            }
        }
    }

    public Player copy() {
        Player p = new Player(this.vf.copy());
        p.epsilon = this.epsilon;
        p.typeLearning = this.typeLearning;
        p.steps = this.steps;
        p.vf = this.vf.copy();
        p.weight = this.weight.clone();
        p.weightDiff = this.weightDiff.clone();
        return p;
    }


}

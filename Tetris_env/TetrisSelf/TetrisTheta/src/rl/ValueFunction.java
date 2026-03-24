package rl;

import settings.Parameters;
import tetris.Blocks;
import tetris.TetrisDTFeature;
import tetris.TetrisEnvironment;

import java.io.*;

public class ValueFunction {

    public String fileName = "";
    protected TetrisDTFeature current, next;
    protected double[] theta, omega;
    static double[] two_order = new double[54 * 34 + 34];
    final int[] dt_9_dim = new int[]{10, 17, 62, 19, 15, 26, 17, 5, 51};
    final int[] dt_9_dim_diff = new int[]{14, 8, 71, 19, 9, 31, 17, 8, 19};


    public ValueFunction(TetrisDTFeature current, TetrisDTFeature next, int size) {
        this.current = current;
        this.next = next;
        this.theta = new double[size];
        this.omega = new double[size];
        this.init();
    }

    public void init() {
        if (this.theta != null) {
            for (int i = 0; i < this.theta.length; i++) {
                this.theta[i] = 0;
                this.omega[i] = 0;
                //this.theta[i]=(Math.random()-0.5)*0.01;
            }
        }
    }

    public double getValue(int[] s, int a) {
        if (TetrisEnvironment.isFinal(s))
            return 0;
        current.setFeature(s, a);
        return this.getValue(current);
    }

    public double getValue(int[] s, int a, double[] theta) {
        if (TetrisEnvironment.isFinal(s))
            return 0;
        current.setFeature(s, a);
        return this.getValue(current, theta);
    }

    public double getValue(TetrisDTFeature f) {
        double x = 0;
        for (int i = 0; i < this.theta.length; i++) {
            if (i == 0) {
                x += this.theta[i] * f.getFeature(i);///2;
            } else {
                x += this.theta[i] * f.getFeature(i);
            }

        }
        return x;
    }

    public double getValue(TetrisDTFeature f, double[] theta) {
        double x = 0;
        for (int i = 0; i < theta.length; i++) {
            x += theta[i] * f.getFeature(i);
        }
        return x;
    }

    public double[] getMaxValue(int[] s) {
        long al = TetrisEnvironment.getActionList(s);
        if (al == 0) return new double[]{0, -1};
        double maxValue = Double.NEGATIVE_INFINITY;
        int action = -1;
        for (int i = 0; i < TetrisEnvironment.blocks.squares[s[12]].actionSize; i++) {
            if (TetrisEnvironment.actionValid(i, al)) {
                double value = this.getValue(s, i);
                if (maxValue < value) {
                    maxValue = value;
                    action = i;
                }
            }
        }
        return new double[]{maxValue, action};
    }

    public double[] getMaxValue(int[] s, double[] theta) {
        long al = TetrisEnvironment.getActionList(s);
        if (al == 0) return new double[]{0, -1};
        double maxValue = Double.NEGATIVE_INFINITY;
        int action = -1;
        for (int i = 0; i < TetrisEnvironment.blocks.squares[s[12]].actionSize; i++) {
            if (TetrisEnvironment.actionValid(i, al)) {
                double value = this.getValue(s, i, theta);
                if (maxValue < value) {
                    maxValue = value;
                    action = i;
                }
            }
        }
        return new double[]{maxValue, action};
    }


    public double getTheta(int index) {
        if (index > -1 && index < this.theta.length) return this.theta[index];
        else return -100;
    }

    public void setTheta(int index, double x) {
        if (index > -1 && index < this.theta.length) this.theta[index] = x;
    }

    public void setTheta(double[] x) {
        for (int i = 0; i < this.theta.length; i++) {
            this.theta[i] = x[i];
        }
    }

    public void readTheta(String filePath, String fileName) {
        this.fileName = fileName;
        FileInputStream fis = null;
        InputStreamReader isr;
        try {
            fis = new FileInputStream(filePath + fileName);
            isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String line;
            for (int i = 0; i < this.theta.length; i++) {
                line = br.readLine();
                this.theta[i] = Double.parseDouble(line);
            }
            System.out.println("load weight:" + fileName);
        } catch (FileNotFoundException ex) {
            System.out.println("file not found :" + fileName);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            try {
                if (fis != null) fis.close();
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
    }

    public void readNormalizedTheta(String fileName) {
        this.fileName = fileName;
        FileInputStream fis = null;
        InputStreamReader isr;
        try {
            fis = new FileInputStream(fileName);
            isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            for (int i = 0; i < this.theta.length; i++) {
                line = br.readLine();
                this.theta[i] = Double.parseDouble(line);
            }
            theta[0] = theta[0] * (Parameters.RowNum * 1.0);
            theta[1] = theta[1] * (16.0);
            theta[2] = theta[2] * (Parameters.RowNum * 10.0);
            theta[3] = theta[3] * ((Parameters.RowNum + 1) * 10.0);
            theta[4] = theta[4] * ((Parameters.RowNum - 1) * 10.0);
            theta[5] = theta[5] * ((Parameters.RowNum + 1) * Parameters.RowNum / 2.0 * 5.0);
            theta[6] = theta[6] * ((Parameters.RowNum - 1) * 10.0);
            theta[7] = theta[7] * (Parameters.RowNum * 1.0);
            theta[8] = theta[8] * (5.0);
            theta[9] = theta[9] * ((Parameters.RowNum + 1) * Parameters.RowNum / 2.0 * 5.0);
            theta[10] = theta[10] * (Parameters.RowNum * Parameters.RowNum / 4.0);
            System.out.println("load weight:" + fileName);
        } catch (FileNotFoundException ex) {
            System.out.println("file not found :" + fileName);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            try {
                if (fis != null) fis.close();
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }

    }

    public void writeTheta(String fileName) {
        FileOutputStream fws = null;
        OutputStreamWriter out = null;
        try {
            fws = new FileOutputStream(fileName);
            out = new OutputStreamWriter(fws, "UTF8");
            String content = "";
            for (int i = 0; i < this.theta.length; i++) {
                content += theta[i] + "\n";
            }
            out.write(content);
            out.flush();
        } catch (FileNotFoundException ex) {
            System.out.println("file not found :" + fileName);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            try {
                if (fws != null) fws.close();
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }

    }

    public ValueFunction copy() {
        ValueFunction vf = new ValueFunction(new TetrisDTFeature(), new TetrisDTFeature(), theta.length);
        for (int i = 0; i < this.theta.length; i++) {
            vf.theta[i] = this.theta[i];
            vf.omega[i] = this.omega[i];
        }
        vf.fileName = this.fileName;
        return vf;
    }

    public void updateQLearning(Experience e) {
        int[] s1 = e.s1;
        int a1 = e.a1;
        double reward = e.reward;
        int[] s2 = e.s2;
        double[] maxValueAndAction = this.getMaxValue(s2);
        double delta = reward + Parameters.gamma * maxValueAndAction[0]
                - this.getValue(s1, a1);
        current.setFeature(s1, a1);
        for (int i = 0; i < this.theta.length; i++) {
            this.theta[i] += Parameters.alpha * delta * current.getFeature(i);
        }
    }

    public void updateDoubleQLearning(Experience e) {
        int[] s1 = e.s1;
        int a1 = e.a1;
        double reward = e.reward;
        int[] s2 = e.s2;

        double random = Math.random();
        if (random < 0.5)//update theta
        {
            double[] maxValueAndAction = this.getMaxValue(s2, theta);
            double maxValue = this.getValue(s2, (int) maxValueAndAction[1], omega);
            double delta = reward + Parameters.gamma * maxValue
                    - this.getValue(s1, a1, theta);
            current.setFeature(s1, a1);
            for (int i = 0; i < this.theta.length; i++) {
                this.theta[i] += Parameters.alpha * delta * current.getFeature(i);
            }
        } else//update omega
        {
            double[] maxValueAndAction = this.getMaxValue(s2, omega);
            double maxValue = this.getValue(s2, (int) maxValueAndAction[1], theta);
            double delta = reward + Parameters.gamma * maxValue
                    - this.getValue(s1, a1, omega);
            current.setFeature(s1, a1);
            for (int i = 0; i < this.omega.length; i++) {
                this.omega[i] += Parameters.alpha * delta * current.getFeature(i);
            }
        }
    }

    public void updateMRetrace(Experience e) {
        int[] s1 = e.s1;
        int a1 = e.a1;
        double reward = e.reward;
        int[] s2 = e.s2;
        int a2 = e.a2;
        double rho = e.rho;
        double x = e.x;
        double[] maxValueAndAction = this.getMaxValue(s2);
        double delta = reward - this.getValue(s1, a1);
        if (e.done != 1) {
            delta += Parameters.gamma * x * maxValueAndAction[0];
        }
        current.setFeature(s1, a1);
        for (int i = 0; i < this.theta.length; i++) {
            this.theta[i] += Parameters.alpha * rho * delta * current.getFeature(i);
            //System.out.println("x: "+x+" maxValueNext: "+maxValueAndAction[0]+", delta: "+delta+", rho: "+rho+", phi "+i+":"+current.getFeature(i));
        }

        //this.printTheta();
    }

    public double[] getFeature(int[] s, int a) {
        double[] feature = new double[this.theta.length];
        current.setFeature(s, a);
        for (int i = 0; i < this.theta.length; i++) {
            feature[i] = this.current.getFeature(i);
        }
        return feature;
    }

    public double[] get9featureAndMask(int[] s) {
        double[] featureAndMask = new double[306 + 34];
        int index = 0;
        long al = TetrisEnvironment.getActionList(s);
        for (int j = 0; j < Blocks.squares[s[12]].actionSize; j++) {
            double[] temp = this.getFeature(s, j);
            if (TetrisEnvironment.actionValid(j, al)) {
                for (double x : temp) {
                    featureAndMask[index++] = x;
                }
                featureAndMask[306 + j] = 1;
            } else {
                for (double x : temp) {
                    featureAndMask[index++] = x;
                }
            }
        }
        return featureAndMask;
    }

    public double[] get9featureAndMask_2order(int[] s) {
        long al = TetrisEnvironment.getActionList(s);
        //每次需要对mask重新初始化
        for (int i = 0; i < 34; i++) {
            two_order[54 * 34 + i] = 0;
        }
        for (int j = 0; j < Blocks.squares[s[12]].actionSize; j++) {
            double[] temp = getTwoOrderFeature(s, j);
            int index;
            if (TetrisEnvironment.actionValid(j, al)) {
                index = j * 54;
                for (int i = 0; i < 54; i++) {
                    two_order[index++] = temp[i];
                }
                two_order[54 * 34 + j] = 1;
            }
        }
        return two_order;
    }

    //DT-9+DT-9的一维编码
    public double[] get231featureAndMask(int[] s) {
        double[] featureAndMask = new double[306 + 7548 + 34];
        int index = 0;
        long al = TetrisEnvironment.getActionList(s);
        for (int j = 0; j < Blocks.squares[s[12]].actionSize; j++) {
            double[] temp = this.get231Feature(s, j);
            if (TetrisEnvironment.actionValid(j, al)) {
                for (double x : temp) {
                    featureAndMask[index++] = x;
                }
                featureAndMask[306 + 7548 + j] = 1;
            } else {
                for (double x : temp) {
                    featureAndMask[index++] = x;
                }
            }
        }
        return featureAndMask;
    }

    //DT-9+DT-9的一维编码 as-s 版本 9+196
    public double[] get205featureAndMask(int[] s) {
        double[] featureAndMask = new double[306 + 6664 + 34];
        int index = 0;
        long al = TetrisEnvironment.getActionList(s);
        for (int j = 0; j < Blocks.squares[s[12]].actionSize; j++) {
            double[] temp = this.get205Feature(s, j);
            if (TetrisEnvironment.actionValid(j, al)) {
                for (double x : temp) {
                    featureAndMask[index++] = x;
                }
                featureAndMask[306 + 6664 + j] = 1;
            } else {
                for (double x : temp) {
                    featureAndMask[index++] = x;
                }
            }
        }
        return featureAndMask;
    }

    public double[] get48featureAndMask(int[] s) {
        double[] featureAndMask = new double[1632 + 34];
        int index = 0;
        long al = TetrisEnvironment.getActionList(s);
        for (int j = 0; j < Blocks.squares[s[12]].actionSize; j++) {
            double[] temp = this.get48Feature(s, j);
            if (TetrisEnvironment.actionValid(j, al)) {
                for (double x : temp) {
                    featureAndMask[index++] = x;
                }
                featureAndMask[1632 + j] = 1;
            } else {
                for (double x : temp) {
                    featureAndMask[index++] = x;
                }
            }
        }
        return featureAndMask;
    }

    public double[] get48Feature(int[] s, int a) {
        double[] feature = new double[48];
        double[] originFeature = getFeature(s, a);
        // 0-6 七个不包含动作信息的特征
        feature[0] = originFeature[0];
        feature[1] = originFeature[1];
        feature[2] = originFeature[2];
        feature[3] = originFeature[4];
        feature[4] = originFeature[5];
        feature[5] = originFeature[7];
        feature[6] = originFeature[8];
        // 7-13 方块信息
        feature[7 + s[12]] = 1;
        // 14-47 动作信息
        feature[14 + a] = 1;
        return feature;
    }

    public double[] get260featureAndMask(int[] s) {
        double[] featureAndMask = new double[8840 + 34];
        long al = TetrisEnvironment.getActionList(s);
        int index = 0;
        for (int i = 0; i < Blocks.squares[s[12]].actionSize; i++) {
            int[] tempFeature = this.get260Feature(s, i);
            if (TetrisEnvironment.actionValid(i, al)) {
                for (int x : tempFeature) {
                    featureAndMask[index++] = x;
                }
                featureAndMask[8840 + i] = 1;
            } else {
                for (double x : tempFeature) {
                    featureAndMask[index++] = x;
                }
            }
        }
        return featureAndMask;
    }


    //DT-9的一维编码
    public double[] get222featureAndMask(int[] s) {
        double[] featureAndMask = new double[7548 + 34];
        long al = TetrisEnvironment.getActionList(s);
        int index = 0;
        for (int i = 0; i < Blocks.squares[s[12]].actionSize; i++) {
            int[] tempFeature = this.get222Feature(s, i);
            if (TetrisEnvironment.actionValid(i, al)) {
                for (int x : tempFeature) {
                    featureAndMask[index++] = x;
                }
                featureAndMask[7548 + i] = 1;
            } else {
                for (double x : tempFeature) {
                    featureAndMask[index++] = x;
                }
            }
        }
        return featureAndMask;
    }


    public int[] get222Feature(int[] s, int a) {
        int[] feature = new int[222];
        double[] originFeature = getFeature(s, a);
        int[] mappedFeature = getMappedFeature(originFeature);
        int offset = 0;
        for (int i = 0; i < 9; i++) {
            feature[offset + mappedFeature[i]] = 1;
            offset += dt_9_dim[i];
        }
        return feature;
    }

    public double[] get231Feature(int[] s, int a) {
        double[] feature = new double[231];
        double[] originFeature = getFeature(s, a);
        // DT-9特征
        System.arraycopy(originFeature, 0, feature, 0, 9);
        int[] mappedFeature = getMappedFeature(originFeature);
        int offset = 9;
        for (int i = 0; i < 9; i++) {
            feature[offset + mappedFeature[i]] = 1;
            offset += dt_9_dim[i];
        }
        return feature;
    }

    public double[] get205Feature(int[] s, int a) {
        double[] feature = new double[205];
        double[] originFeature = getFeature(s, a);
        // DT-9特征
        System.arraycopy(originFeature, 0, feature, 0, 9);
        int[] mappedFeatureDiff = getMappedFeatureDiff(originFeature);
        int offset = 9;
        for (int i = 0; i < 9; i++) {
            feature[offset + mappedFeatureDiff[i]] = 1;
            offset += dt_9_dim_diff[i];
        }
        return feature;
    }

    public double[] getTwoOrderFeature(int[] s, int a) {
        double[] feature = new double[54];
        double[] originFeature = getFeature(s, a);
        // 0维 直接记录值
        System.arraycopy(originFeature, 0, feature, 0, 9);

        // 1维 记录值的位置
        int[] mappedFeature = getMappedFeature(originFeature);
        int offset = 0;

        for (int i = 9; i < 18; i++) {
            feature[i] = offset + mappedFeature[i - 9];
            offset += dt_9_dim[i - 9];
        }

        // 2维
        int start = 18;
        for (int i = 0; i < 8; i++) {
            for (int j = i + 1; j < 9; j++) {
                feature[start++] = offset + mappedFeature[i] * dt_9_dim[j] + mappedFeature[j];
                offset += dt_9_dim[i] * dt_9_dim[j];
            }
        }
        return feature;
    }

    //取得映射的222编码特征
    private int[] getMappedFeature(double[] originFeature) {
        int[] mappedFeature = new int[9];
        int f0 = (int) originFeature[0];
        if (f0 < 0) {
            f0 = 0;
        } else if (f0 > 9) {
            f0 = 9;
        }
        mappedFeature[0] = f0;


        int f1 = (int) ((originFeature[1] - 10) / 2);
        if (f1 < 0) {
            f1 = 0;
        } else if (f1 > 16) {
            f1 = 16;
        }
        mappedFeature[1] = f1;

        int f2 = (int) originFeature[2];
        if (f2 < 0) {
            f2 = 0;
        } else if (f2 > 61) {
            f2 = 61;
        }
        mappedFeature[2] = f2;

        int f3 = (int) (originFeature[3] * 2);
        if (f3 < 0) {
            f3 = 0;
        } else if (f3 > 18) {
            f3 = 18;
        }
        mappedFeature[3] = f3;

        int f4 = (int) ((originFeature[4] - 20) / 2);
        if (f4 < 0) {
            f4 = 0;
        } else if (f4 > 14) {
            f4 = 14;
        }
        mappedFeature[4] = f4;

        int f5 = (int) originFeature[5];
        if (f5 < 0) {
            f5 = 0;
        } else if (f5 > 25) {
            f5 = 25;
        }
        mappedFeature[5] = f5;

        int f6 = (int) originFeature[6];
        if (f6 < 0) {
            f6 = 0;
        } else if (f6 > 16) {
            f6 = 16;
        }
        mappedFeature[6] = f6;

        int f7 = (int) originFeature[7];
        if (f7 < 0) {
            f7 = 0;
        } else if (f7 > 4) {
            f7 = 4;
        }
        mappedFeature[7] = f7;

        int f8 = (int) originFeature[8];
        if (f8 < 0) {
            f8 = 0;
        } else if (f8 > 50) {
            f8 = 50;
        }
        mappedFeature[8] = f8;
        return mappedFeature;
    }

    private int[] getMappedFeatureDiff(double[] originFeature) {
        int[] mappedFeature = new int[9];
        int f0 = (int) (originFeature[0] + 4);
        if (f0 < 0) {
            f0 = 0;
        } else if (f0 > 13) {
            f0 = 13;
        }
        mappedFeature[0] = f0;


        int f1 = (int) ((originFeature[1] + 8) / 2);
        if (f1 < 0) {
            f1 = 0;
        } else if (f1 > 7) {
            f1 = 7;
        }
        mappedFeature[1] = f1;

        int f2 = (int) (originFeature[2] + 26);
        if (f2 < 0) {
            f2 = 0;
        } else if (f2 > 70) {
            f2 = 70;
        }
        mappedFeature[2] = f2;

        int f3 = (int) (originFeature[3] * 2);
        if (f3 < 0) {
            f3 = 0;
        } else if (f3 > 18) {
            f3 = 18;
        }
        mappedFeature[3] = f3;

        int f4 = (int) ((originFeature[4] + 8) / 2);
        if (f4 < 0) {
            f4 = 0;
        } else if (f4 > 8) {
            f4 = 8;
        }
        mappedFeature[4] = f4;

        int f5 = (int) (originFeature[5] + 5);
        if (f5 < 0) {
            f5 = 0;
        } else if (f5 > 30) {
            f5 = 30;
        }
        mappedFeature[5] = f5;

        int f6 = (int) originFeature[6];
        if (f6 < 0) {
            f6 = 0;
        } else if (f6 > 16) {
            f6 = 16;
        }
        mappedFeature[6] = f6;

        int f7 = (int) (originFeature[7] + 4);
        if (f7 < 0) {
            f7 = 0;
        } else if (f7 > 7) {
            f7 = 7;
        }
        mappedFeature[7] = f7;

        int f8 = (int) (originFeature[8] + 14);
        if (f8 < 0) {
            f8 = 0;
        } else if (f8 > 18) {
            f8 = 18;
        }
        mappedFeature[8] = f8;
        return mappedFeature;
    }

    public int[] get260Feature(int[] s, int a) {
        int[] feature = new int[260];
        int[] as = TetrisEnvironment.afterState(s, a);
        int holes = TetrisDTFeature.getHoles(as);
        int[] height = new int[10];
        for (int i = 0; i < 10; i++) {
            height[i] = getHighestBitPosition(as[i]);
            if (height[i] >= 10)
                height[i] = 10;
        }

        for (int i = 0; i <= 9; i++) {
            //10列的高度 10*11 占用0-109 110位
            //0-10 11-21 22-32 33-43 44-54 55-65 66-76 77-87 88-98 99-109
            int index = i * 11;
            feature[index + (10 - height[i])] = 1;

            //10列间的相对高度差 9*11   +-5范围  占用 110-208 99位
            if (i == 9)
                break;
            int num = height[i + 1] - height[i];
            if (num > 5) {
                num = 5;
            } else if (num < -5) {
                num = -5;
            }
            index = i * 11 + 115;
            feature[index + num] = 1;
        }


        //洞的个数   0-50 占用 209-259 51位
        if (holes > 50) {
            holes = 50;
        }
        feature[259 - holes] = 1;
        return feature;
    }


    public double[] get269Feature(int[] s, int a) {
        double[] feature = new double[269];
        int[] as = TetrisEnvironment.afterState(s, a);
        double[] originFeature = getFeature(s, a);
        System.arraycopy(originFeature, 0, feature, 260, 9);
        int holes = TetrisDTFeature.getHoles(as);
        int[] height = new int[10];
        for (int i = 0; i < 10; i++) {
            height[i] = getHighestBitPosition(as[i]);
            if (height[i] >= 10)
                height[i] = 10;
        }

        for (int i = 0; i <= 9; i++) {
            //10列的高度 10*11 占用0-109 110位
            //0-10 11-21 22-32 33-43 44-54 55-65 66-76 77-87 88-98 99-109
            int index = i * 11;
            feature[index + (10 - height[i])] = 1;

            //10列间的相对高度差 9*11   +-5范围  占用 110-208 99位
            if (i == 9)
                break;
            int num = height[i + 1] - height[i];
            if (num > 5) {
                num = 5;
            } else if (num < -5) {
                num = -5;
            }
            index = i * 11 + 115;
            feature[index + num] = 1;
        }


        //洞的个数   0-50 占用 209-259 51位
        if (holes > 50) {
            holes = 50;
        }
        feature[259 - holes] = 1;
        return feature;
    }

    private int getHighestBitPosition(int num) {
        return 32 - Integer.numberOfLeadingZeros(num);
    }

    public double[] getTheta() {
        return this.theta;
    }

}

package tetris;

import settings.Parameters;

public class TetrisDTFeature {

    public int featureNum = 9;
    public double[] feature = new double[featureNum];
    public int[] directionCBMPI = {-1, -1, -1, -1, -1, 1, 1, 1, -1};

    public TetrisDTFeature() {
        for (int i = 0; i < this.featureNum; i++) {
            this.feature[i] = -1;
        }

    }

    public static int getPieceCells(int[] as, Blocks.Block block) {
        if (as[10] == 0)
            return 0;
        int num = 0;
        int temp;
        //deleteLine>>> dropHeight
        int deleteLine = as[14] >>> as[13];
        for (int j = 0; j < block.width; j++) {
            temp = deleteLine & block.block[j];
            while (temp != 0) {
                temp &= (temp - 1);
                num++;
            }
        }
        return num * as[10];
    }

    public static int getRowTransitions(int[] as) {
        int num = 0;
        int y = as[0];
        if (Parameters.RowNum == 20) {
            y = y ^ (0x000fffff);
        } else if (Parameters.RowNum == 10) {
            y = y ^ (0x000003ff);
        }
        while (y != 0) {
            y &= (y - 1);
            num++;
        }
        y = as[9];
        if (Parameters.RowNum == 20) {
            y = y ^ (0x000fffff);
        } else if (Parameters.RowNum == 10) {
            y = y ^ (0x000003ff);
        }
        while (y != 0) {
            y &= (y - 1);
            num++;
        }
        for (int i = 0; i < 9; i++) {
            y = as[i] ^ as[i + 1];
            while (y != 0) {
                y &= (y - 1);
                num++;
            }
        }
        return num;
    }

    public static int getColumnTransitions(int[] as) {
        int num = 0;
        int y;
        for (int i = 0; i < 10; i++) {
            y = as[i] ^ ((as[i] << 1) + 1);    //+1等价于把棋盘外满的那一行移上来
            while (y != 0) {
                y &= (y - 1);
                num++;
            }
        }
        return num;
    }

    public static int getHoles(int[] as) {
        int num = 0;
        int y;
        for (int i = 0; i < 10; i++) {
            if (as[i] > 1) {
                y = as[i];
                y |= (y >>> 1);
                y |= (y >>> 2);
                y |= (y >>> 4);
                y |= (y >>> 8);
                y |= (y >>> 16);
                y ^= as[i];
                while (y != 0) {
                    y &= (y - 1);
                    num++;
                }
            }

        }
        return num;
    }

    public static int getCumulativeWells(int[] as) {
        int num = 0;
        int y = 0;
        int[] exState = new int[12];
        if (Parameters.RowNum == 20) {
            exState[0] = 0x000fffff;
            exState[11] = 0x000fffff;
        } else if (Parameters.RowNum == 10) {
            exState[0] = 0x000003ff;
            exState[11] = 0x000003ff;
        }
        System.arraycopy(as, 0, exState, 1, 10);
        for (int i = 1; i < 11; i++) {
            for (int j = 0; j < Parameters.RowNum; j++) {
                if (((exState[i] >> j) & 1) == 0)               //中间为0
                {
                    y++;
                    if ((((exState[i - 1] >> j) & 1) == 1) && (((exState[i + 1] >> j) & 1) == 1))     //两边为1
                        num += y;
                } else
                    y = 0;
            }
            y = 0;
        }
        return num;
    }

    public static int getHoleDepth(int[] as) {
        int num = 0;
        int y;
        for (int i = 0; i < 10; i++) {
            y = ~(as[i] ^ (as[i] + 1));
            y = as[i] & y;
            while (y != 0) {
                y &= (y - 1);
                num++;
            }
        }
        return num;
    }

    public static int getHoleDepthLichtenberg(int[] as) {
        int num = 0;
        int y = 0;
        int count;
        for (int i = 0; i < 10; i++) {
            count = 0;
            if (as[i] > 1) {
                y = as[i];
                while (y != 0) {
                    y &= (y - 1);
                    count++;
                }
                for (int j = 0; j < TetrisEnvironment.searchHeight(as[i]); j++) {
                    y = as[i];
                    if (((y >> j) & 1) == 0)
                        num += count;
                    else
                        count--;
                }
            }
        }
        return num;
    }

    public static void main(String[] args) {
        int[] as = new int[]{5, 7, 5, 0, 0, 0, 0, 0, 0, 0, 0};
        getRowHoles(as);
    }

    public static int getRowHoles(int[] as) {
        int num = 0;
        int y1;
        int y2 = 0;
        for (int i = 0; i < 10; i++) {
            if (as[i] > 1) {
                y1 = as[i];
                y1 |= (y1 >>> 1);
                y1 |= (y1 >>> 2);
                y1 |= (y1 >>> 4);
                y1 |= (y1 >>> 8);
                y1 |= (y1 >>> 16);
                y1 ^= as[i];
                y2 |= y1;
            }
        }
        while (y2 != 0) {
            y2 &= (y2 - 1);
            num++;
        }
        return num;
    }

    public static int getPatternDiversity(int[] as) {
        int num = 0;
        int h1;
        int h2;
        int h;
        int[] count = {0, 0, 0, 0, 0};
        int[] p = {-2, -1, 0, 1, 2};
        for (int i = 0; i < 9; i++) {
            if (as[i] != 0)
                h1 = TetrisEnvironment.searchHeight(as[i]);
            else
                h1 = -1;
            if (as[i + 1] != 0)
                h2 = TetrisEnvironment.searchHeight(as[i + 1]);
            else
                h2 = -1;
            h = h1 - h2;
            for (int j = 0; j < 5; j++) {
                if (h == p[j])
                    count[j]++;
            }
        }
        for (int i = 0; i < 5; i++)
            if (count[i] != 0)
                num++;
        return num;
    }

    public static int getBoardWells(int[] as) {
        int num = 0;
        int y;
        int count = 0;
        int[] exState = new int[12];
        exState[0] = 0x000003ff;
        exState[11] = 0x000003ff;
        System.arraycopy(as, 0, exState, 1, 10);
        for (int i = 1; i < 11; i++) {
            y = (~exState[i]) & exState[i - 1] & exState[i + 1];
            for (int j = 0; j < Parameters.RowNum; j++)
                if (((y >> j) & 1) == 1) {
                    count++;
                    num += count;
                } else
                    count = 0;
        }
        return num;
    }

    public double getLandingHeight(int[] as, Blocks.Block block) {
        double num;
        //dropHeight+blockHeight/2
        num = as[13] + (block.height - 1) / 2.0;
        //num = as[13]+(block.height)/2.0;
        //num = Parameters.RowNum - num;
        return num;
    }

    public void setFeature(int[] s, int a) {
        int[] as = TetrisEnvironment.afterState(s, a);
        int[] actionCombined = TetrisEnvironment.blocks.getAction(s[12], a);
        int rotation = actionCombined[0];
        //int shift = actionCombined[1];
        /*
         * int index = 0,rotation,shift=0; flag: for (rotation = 0; rotation <
         * TetrisEnvironment.blocks.squares[s[12]].rotationNum; rotation++) for (shift =
         * 0; shift < (11 -
         * TetrisEnvironment.blocks.squares[s[12]].blocks[rotation].width); shift++) {
         * if((a&(1<<index))!=0) { break flag; } index++; }
         */
        Blocks.Block block = TetrisEnvironment.blocks.squares[s[12]].blocks[rotation];
        //0~RowNum
//        feature[0] = getRowHoles(as);
//        //(RowNum+1)*10
//        feature[1] = getColumnTransitions(as);
//        //((RowNum+1)*RowNum/2.0*5.0)  board wells
//        feature[2] = getCumulativeWells(as);
//        //RowNum
//        feature[3] = getLandingHeight(as, block);
//        //10*RowNum
//        feature[4] = getRowTransitions(as);
//        //(RowNum-1)*10.0  holes
//        feature[5] = getHoles(as);
//        //0-16   eroded piece cells
//        feature[6] = getPieceCells(as, block);
//        //0,1,2,3,4,5
//        feature[7] = getPatternDiversity(as);
//        //(RowNum-1)*10
//        feature[8] = getHoleDepth(as);

        //RowNum*RowNum/4.0
        //feature[10] = getHoleDepthLichtenberg(as);
//        for (int i = 0; i < feature.length; i++) {
//            this.feature[i] *= this.directionCBMPI[i];
//        }

        // as - s 版本
        //0~RowNum
        feature[0] = getRowHoles(as) - getRowHoles(s);
        //(RowNum+1)*10
        feature[1] = getColumnTransitions(as) - getColumnTransitions(s);
        //((RowNum+1)*RowNum/2.0*5.0)
        feature[2] = getCumulativeWells(as) - getCumulativeWells(s);
        //RowNum
        feature[3] = getLandingHeight(as, block);
        //10*RowNum
        feature[4] = getRowTransitions(as) - getRowTransitions(s);
        //(RowNum-1)*10.0
        feature[5] = getHoles(as) - getHoles(s);
        //16
        //multiply 2 to ensure to be an integer
        feature[6] = getPieceCells(as, block);

        //0,1,2,3,4,5
        feature[7] = getPatternDiversity(as) - getPatternDiversity(s);
        //(RowNum-1)*10
        feature[8] = getHoleDepth(as) - getHoleDepth(s);
    }


    public double getFeature(int index) {
        // TODO Auto-generated method stub
        if (index > -1 && index < this.getNumberoffeature())
            return this.feature[index];
        else return -100;
    }

    public int getNumberoffeature() {
        // TODO Auto-generated method stub
        return this.feature.length;
    }

    public String[] getFeatureNames() {
        String[] s = new String[this.feature.length];
        s[0] = "Rows with Holes";
        s[1] = "Column Transitions";
        s[2] = "Cumulative Wells";
        s[3] = "Landing Height";
        s[4] = "Row Transitions";
        s[5] = "Holes";
        s[6] = "Eroded Piece Cells";
        s[7] = "Diversity";
        s[8] = "Hole Depth";
        //s[9] = "Board Wells";
        //s[10] = "Hole Depth Lichtenberg";
        return s;
    }

    public void terminalFeature() {
        for (int i = 0; i < this.feature.length; i++) {
            this.feature[i] = 0;
        }
    }

}

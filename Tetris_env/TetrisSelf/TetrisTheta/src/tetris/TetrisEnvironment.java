package tetris;

import settings.Parameters;
import tetris.Blocks.Block;

import java.util.Arrays;
import java.util.Collections;

public class TetrisEnvironment {

    //afterState:
    //10 columns   0-9
    //+ reward  10
    //state:
    //+ score   11
    //+ randomBlock  12
    //feature
    //+ dropHeight   13
    //+ deleteLine   14
    //public int[] afterState=new int[15];

    //7 blocks
    public static Blocks blocks = new Blocks();


    public static int[][] getBoard(int[] as) {
        int[][] board = new int[Parameters.RowNum][10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < Parameters.RowNum; j++) {
                board[j][i] = (as[i] & (1 << (Parameters.RowNum - 1 - j))) != 0 ? 1 : 0;
            }
        }
        return board;
    }

    public static void printAll(int[] as) {
        int[][] board = getBoard(as);
        for (int i = 0; i < Parameters.RowNum; i++) {
            for (int j = 0; j < 10; j++) {
                if (j < 9) {
                    System.out.print(board[i][j] + ", ");
                } else {
                    System.out.print(board[i][j]);
                }
            }
            System.out.println();
        }
        System.out.println("reward: " + as[10]);
        System.out.println("score: " + as[11]);
        System.out.println("blockID: " + as[12]);
        System.out.println("dropHeight: " + as[13]);
        System.out.println("deleteLine: " + as[14]);
    }

    //找10列中的最高列
    public static int seachHighestColumn(int[] afterstate) {
        int max = 0, cur_h;
        for (int i = 0; i < 10; i++) {
            if (afterstate[i] != 0)
                cur_h = searchHeight(afterstate[i]);
            else
                cur_h = -1;
            if (cur_h > max)
                max = cur_h;
        }
        return max;
    }

    public static int searchHeight(int d) {
        int r = 0;
        if ((d & 0xffff0000) != 0) {
            d >>>= 16;
            r += 16;
        }
        if ((d & 0xff00) != 0) {
            d >>>= 8;
            r += 8;
        }
        if ((d & 0xf0) != 0) {
            d >>>= 4;
            r += 4;
        }
        if ((d & 0x0c) != 0) {
            d >>>= 2;
            r += 2;
        }
        if ((d & 0x02) != 0) {
            r += 1;
        }
        return r;
    }

    public static int getHighest(int[] afterstate, Block block, int shiftAction) {
        int max = 0, cur_h;
        for (int i = 0; i < block.width; i++) {
            if (afterstate[shiftAction + i] != 0)
                cur_h = searchHeight(afterstate[shiftAction + i]);
            else
                cur_h = -1;
            if (cur_h > max)
                max = cur_h;
        }
        return max;
    }

    public static boolean valid(int[] afterstate) {
        int h = (1 << Parameters.RowNum);
        for (int i = 0; i < 10; i++)
            if ((h & afterstate[i]) != 0)
                return false;
        return true;

    }

    public static boolean valid(int[] as, Block block, int dropPosition) {
        //copy for safety
        int[] afterstate = copy(as);
        int h = getHighest(afterstate, block, dropPosition);                  //��߸߶�
        int dropHigh = 0;
        int blockOver = 0;
        for (int i = 0; i < 3; i++)               //�����λ�ã�h-i
        {
            for (int j = 0; j < block.width; j++) {
                blockOver += (block.block[j] << (h - i)) & afterstate[dropPosition + j];
            }
            if ((blockOver != 0) || (h - i) == -1) {
                dropHigh = h - i + 1;
                break;
            }
        }
        for (int i = 0; i < block.width; i++)
            afterstate[dropPosition + i] |= (block.block[i] << dropHigh);          //����״̬
        return valid(afterstate);
    }

    public static int[] defaultInitialState() {
        int[] state = new int[15];
        for (int i = 0; i < 15; i++) {
            state[i] = 0;
        }
        state[12] = Parameters.random.nextInt(7);
        return state;
    }

    public static long getActionList(int[] state) {
        //System.out.println("___________________________________________________");
        long actions = 0;
        long x = 1;
        //valid if the height is less than RowNum-block's maxHeight
        for (int i = 0; i < blocks.squares[state[12]].actionSize; i++) {
            int[] actionCombined = blocks.getAction(state[12], i);
            int rotation = actionCombined[0];
            int shift = actionCombined[1];
            if (valid(state, blocks.squares[state[12]].blocks[rotation], shift)) {
                actions |= (x << i);
                //System.out.print(i+", ");
            }
        }
        return actions;
    }

    public static boolean actionValid(int i, long actionList) {
        long x = 1;
        if ((actionList & (x << i)) != 0) return true;
        return false;
    }

    public static int actionSize(long actionList) {
        //System.out.println("actionList: "+Long.toBinaryString(actionList));
        int size = 0;
        for (int i = 0; i < 34; i++) {
            if (actionValid(i, actionList)) {
                size++;
                //System.out.println(1);
            } else {
                //System.out.println(0);
            }
        }
        return size;
    }

    public static int[] copy(int[] state) {
        int[] afterstate = new int[state.length];
        for (int i = 0; i < state.length; i++) {
            afterstate[i] = state[i];
        }
        return afterstate;
    }

    public static double[] copy(double[] s) {
        double[] as = new double[s.length];
        System.arraycopy(s, 0, as, 0, s.length);
        return as;
    }

    public static int[] afterState(int[] state, int action) {
        if (action == -1)
            System.err.println("action = -1");
        int[] afterstate = copy(state);
        int[] actionCombined = Blocks.getAction(state[12], action);
        int rotation = actionCombined[0];
        int shift = actionCombined[1];
        Block block = Blocks.squares[state[12]].blocks[rotation];
        int h = getHighest(afterstate, block, shift);
        int blockOver = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < block.width; j++) {
                blockOver += (block.block[j] << (h - i)) & afterstate[shift + j];
            }
            //如果blockOver != 0 说明方块发生碰撞， h - i == 0 说明方块触底
            if ((blockOver != 0) || (h - i) == -1) {
                afterstate[13] = h - i + 1;
                break;
            }
        }
        for (int i = 0; i < block.width; i++)
            afterstate[shift + i] |= (block.block[i] << afterstate[13]);


        int above;
        int below;
        int delete_line;
        int reward = 0;

        afterstate[14] = afterstate[0];
        for (int i = 1; i < 10; i++)
            afterstate[14] &= afterstate[i];

        while (true) {
            delete_line = afterstate[0];
            for (int i = 1; i < 10; i++)
                delete_line &= afterstate[i];
            if (delete_line != 0) {
                delete_line |= (delete_line >>> 1);
                delete_line |= (delete_line >>> 2);
                delete_line |= (delete_line >>> 4);
                delete_line |= (delete_line >>> 8);
                delete_line |= (delete_line >>> 16);
                //��������λ֮��ȫȡ1
                for (int i = 0; i < 10; i++) {
                    above = (afterstate[i] & (~delete_line)) >>> 1;      //������ȥ�����������������һλ
                    below = (delete_line >>> 1) & afterstate[i];        //������ȥ���������
                    afterstate[i] = (above | below);                    //�ϲ�
                }
                //完成一次消行，reward+1
                reward++;
            } else
                break;
        }
        afterstate[10] = reward;
        return afterstate;
    }

    public static int[] successorState(int[] state, int action) {
        try {
            int[] newState = afterState(state, action);
            //score
            newState[11] += newState[10];
            //new block
            newState[12] = Parameters.random.nextInt(7);
            return newState;
        } catch (ArrayIndexOutOfBoundsException e) {
            return new int[]{-1};
        }
    }

    public static double getReward(int[] s1, int a, int[] s2) {
        return s2[10];
    }

    public static boolean isFinal(int[] state) {
        long list = getActionList(state);
        return list == 0;
    }

    public static String getName() {
        // TODO Auto-generated method stub
        return "Tetris";
    }

    public static void main(String[] args) {
        int[] state = TetrisEnvironment.defaultInitialState();
        state[0] = 0x000003ff;
        state[1] = 0x000003ff;
        boolean f = TetrisEnvironment.valid(state, TetrisEnvironment.blocks.squares[0].blocks[0], 0);
        System.out.println(f);

        f = TetrisEnvironment.actionValid(32, 0x1ff);
        System.out.println(f);
    }
}

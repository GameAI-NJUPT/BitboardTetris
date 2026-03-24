package tetris;

public class Blocks {

    public static Sq[] squares = new Sq[8];

    public static int[] getAction(int blockID, int actionIndex) {
        int sum = 0;
        for (int i = 0; i < squares[blockID].rotationNum; i++) {
            int start = sum;
            sum += 11 - squares[blockID].blocks[i].width;
            if (actionIndex < sum) {
                return new int[]{i, actionIndex - start};
            }
        }
        return new int[]{};

    }

    public Blocks() {
        int[] O_block1 = {3, 3};          //O
        Block[] block1 = new Block[1];
        block1[0] = new Block(O_block1, 2, 2);
        squares[0] = new Sq(block1, 1, 9, 2);

        int[] I_block1 = {15};           //I
        int[] I_block2 = {1, 1, 1, 1};
        Block[] block2 = new Block[2];
        block2[0] = new Block(I_block1, 1, 4);
        block2[1] = new Block(I_block2, 4, 1);
        squares[1] = new Sq(block2, 2, 17, 4);

        int[] S_block1 = {6, 3};          //S
        int[] S_block2 = {1, 3, 2};
        Block[] block3 = new Block[2];
        block3[0] = new Block(S_block1, 2, 3);
        block3[1] = new Block(S_block2, 3, 2);
        squares[2] = new Sq(block3, 2, 17, 3);

        int[] OS_block1 = {3, 6};         //Z
        int[] OS_block2 = {2, 3, 1};
        Block[] block4 = new Block[2];
        block4[0] = new Block(OS_block1, 2, 3);
        block4[1] = new Block(OS_block2, 3, 2);
        squares[3] = new Sq(block4, 2, 17, 3);

        int[] L_block1 = {7, 1};          //L
        int[] L_block2 = {3, 2, 2};
        int[] L_block3 = {4, 7};
        int[] L_block4 = {1, 1, 3};
        Block[] block5 = new Block[4];
        block5[0] = new Block(L_block1, 2, 3);
        block5[1] = new Block(L_block2, 3, 2);
        block5[2] = new Block(L_block3, 2, 3);
        block5[3] = new Block(L_block4, 3, 2);
        squares[4] = new Sq(block5, 4, 34, 3);

        int[] OL_block1 = {1, 7};            //J
        int[] OL_block2 = {3, 1, 1};
        int[] OL_block3 = {7, 4};
        int[] OL_block4 = {2, 2, 3};
        Block[] block6 = new Block[4];
        block6[0] = new Block(OL_block1, 2, 3);
        block6[1] = new Block(OL_block2, 3, 2);
        block6[2] = new Block(OL_block3, 2, 3);
        block6[3] = new Block(OL_block4, 3, 2);
        squares[5] = new Sq(block6, 4, 34, 3);

        int[] T_block1 = {1, 3, 1};          //T
        int[] T_block2 = {7, 2};
        int[] T_block3 = {2, 3, 2};
        int[] T_block4 = {2, 7};
        Block[] block7 = new Block[4];
        block7[0] = new Block(T_block1, 3, 2);
        block7[1] = new Block(T_block2, 2, 3);
        block7[2] = new Block(T_block3, 3, 2);
        block7[3] = new Block(T_block4, 2, 3);
        squares[6] = new Sq(block7, 4, 34, 3);
        //System.out.println("new guole");

    }

    public static class Block {
        public int[] block; //同一个方块的不同形式
        public int width;   //方块的宽度
        public int height;  //方块的高度

        public Block(int[] b, int w, int h) {
            block = b;
            width = w;
            height = h;
        }
    }

    public static class Sq {
        public Block[] blocks;
        public int rotationNum;  //方块的可旋转数
        public int actionSize;   //方块的最大可选动作
        public int maxHeight;    //方块不同形式中的最大高度

        public Sq(Block[] b, int s, int size, int height) {
            blocks = b;
            rotationNum = s;
            actionSize = size;
            maxHeight = height;
        }
    }
}

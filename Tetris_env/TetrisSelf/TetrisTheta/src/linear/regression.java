package linear;

import rl.ValueFunction;
import tetris.Blocks;
import tetris.TetrisDTFeature;
import tetris.TetrisEnvironment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;


//用于检测线性回归后动作选择分布更加接近
public class regression {
    public static void  readtxt(String filePath) throws IOException {
        FileInputStream fin = new FileInputStream(filePath);
        InputStreamReader reader = new InputStreamReader(fin);
        BufferedReader br = new BufferedReader(reader);
        String [] s;
        for (int i=0;i<length;i++){
            s=br.readLine().split(",");
            for (int j=0;j<15;j++){
                state[i][j]=Integer.parseInt(s[j]);
            }
            action[i]=Integer.parseInt(s[15]);
            y[i][0]=Double.parseDouble(s[16]);
        }
        System.out.println("end");
        br.close();
    }
    //17列   1-15 state 16搜索后的动作 17搜索后的v值
    //数据集行数
    static int length=50000;
    static int[][] state=new int[length][15];
    static double[][] feature=new double[length][9];
    static int[] action=new int[length];
    static int[] actionGreedy=new int[length];
    static double[][] y=new double[length][1];

    public static int greedyChoice(int[] s,ValueFunction vf){
        long al = TetrisEnvironment.getActionList(s);
        if(al==0)
            return -1;
        int size = TetrisEnvironment.actionSize(al);
        double maxValue = Double.NEGATIVE_INFINITY;
        int action = -1;
        for(int i = 0; i< Blocks.squares[s[12]].actionSize; i++)
        {
            if(TetrisEnvironment.actionValid(i, al))
            {
                double value  = vf.getValue(s, i);
                if(maxValue<value)
                {
                    maxValue = value;
                    action = i;
                }
            }
        }
        return action;
    }
    public static void main(String[] args) throws IOException{
        readtxt("C:\\Users\\Luke\\Desktop\\TetrisTheta\\TetrisTheta\\src\\dataset\\dataset.txt");
        TetrisDTFeature current = new TetrisDTFeature();
        TetrisDTFeature next = new TetrisDTFeature();
        ValueFunction vf = new ValueFunction(current, next,current.getNumberoffeature());
        vf.readTheta("theta//","CBMPI10unsigned.theta");
        int[] s=new int[15];
        for (int j=0;j<length;j++){
            System.arraycopy(state[j], 0, s, 0, 15);
            actionGreedy[j]=greedyChoice(s,vf);
        }
        int num=0;
        for (int i=0;i<length;i++){
            if (actionGreedy[i]!=action[i])
                ++num;
        }
        System.out.println(num);

    }
}

package settings;

import java.util.Random;

public class Parameters {

    //棋盘的行数
    public static int RowNum = 10;//20;
    public static Random random = new Random(System.currentTimeMillis());
    public static int independentRuns = 10;
    public static int episodeNum = 10;
    //测试局数
    public static int testEpisodeNum = 10000;
    //testEpisodeNum要能被lockFreeTestNum整除
    public static int lockFreeTestNum = 10;
    //0:random;1:greedy;2:epsilon-greedy;3:search;231:dt-9+dt-9 as hand-coded;205 as-s
    public static int typeChoice = 205;
    public static double gamma = 1.0;
    public static double alpha = 0.01;
    public static double beta = 0.01;
    public static boolean learning = true;
    public static int expectimaxSearchDepth = 0;
    public static double searchParameter;
}


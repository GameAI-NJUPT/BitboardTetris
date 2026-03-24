package search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import rl.ValueFunction;
import settings.Parameters;
import tetris.TetrisDTFeature;
import tetris.TetrisEnvironment;
import utility.Record;


public class ExpectimaxTest {

    public static Log log = LogFactory.getLog("Tetris Parallelism Test for searching");

    ValueFunction vf;
    Record record;
    int index;

    public ExpectimaxTest(ValueFunction vf, int index) {
        this.vf = vf;
        this.index = index;
        record = new Record(index);
    }

    public void go(double discount) throws InterruptedException {
        log.info("test weight: " + vf.fileName + "_Expectimax_" + Parameters.expectimaxSearchDepth
                + "_Lambda_" + discount);
        for (int i = 0; i < this.index; i++) {
            GoTest gotest = new GoTest(new PlayerExpectimax(vf.copy(), discount));
            gotest.run(i);
        }
        double averaged = record.averaged();
        log.info("averaged in all: " + averaged);
    }

    class GoTest {

        PlayerExpectimax pe;

        public GoTest(PlayerExpectimax pe) {
            this.pe = pe;
        }

        public void run(int i) throws InterruptedException {
            int lines = episode(pe);
            System.out.println(i);
            record.setResult(lines, i);
            log.info("index: " + i + ", removed lines: " + lines);
        }
    }

    public int episode(PlayerExpectimax pe) {
        int[] current = TetrisEnvironment.defaultInitialState();
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            long al0 = TetrisEnvironment.getActionList(current);
            TreeNodeState tns = pe.getChoice(current, al0);
            int a1 = tns.greedyAction;
            /*
			 int a2 = player.getChoice(current, al0);
			 if(a1!=a2)
			 {
			 System.out.println("Step: "+i+", expectimax select: "+a1+", while greedy select: "+a2);
			 }
			 */
            int[] next = TetrisEnvironment.successorState(current, a1);
            if (TetrisEnvironment.isFinal(next)) {
                break;
            }
            current = TetrisEnvironment.copy(next);
        }
        return current[11];
    }


    public static void main(String[] args) throws InterruptedException {

        TetrisDTFeature current = new TetrisDTFeature();
        TetrisDTFeature next = new TetrisDTFeature();
        ValueFunction vf = new ValueFunction(current, next, current.getNumberoffeature());

        vf.readTheta("D:\\graduate\\TetrisSelf\\TetrisTheta\\theta\\", "CBMPI20unsigned.theta");

//		double[] discountSearch = {0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0};
        double[] discountSearch = {0.15};
        ExpectimaxTest test = new ExpectimaxTest(vf, 1000);
        test.go(discountSearch[0]);
    }

}

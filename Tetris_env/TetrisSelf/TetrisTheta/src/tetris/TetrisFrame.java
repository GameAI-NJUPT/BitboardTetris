package tetris;


import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import player.*;
import rl.*;
import settings.Parameters;

import java.awt.event.*;
import java.awt.*;
import java.text.DecimalFormat;


public class TetrisFrame extends JFrame implements ActionListener {
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    int sleepTime;
    boolean gameover = true;
    boolean stop = false;
    boolean isPause = false;
    boolean isClickNextstep = false;
    int RowNum;
    int ColumnNum;
    String lines = "Lines:";
    boolean showStepFinal = false;


    JMenuBar jmb;


    JMenu jmTheta;
    JMenuItem[] jmiTheta = new JMenuItem[5];

    MyJPanel[][] jlBlank;
    JSlider jsSpeed;
    JLabel jlCount;
    JButton jbBegin;
    JButton jbNextStep;
    JButton jbpause;

    JTextField[][] jlWeight;
    JPanel jp_west;
    JPanel jp__north;
    JPanel jp__center;
    JPanel jp_center;
    JPanel jp_south;

    JTextArea jtaDebug;
    JPanel jpDebug;


    KeyboardJPanel jpanel;
    Color red = new Color(40, 53, 210);
    Color blue = new Color(140, 53, 10);
    Color yellow = new Color(40, 153, 20);
    Color green = new Color(40, 93, 80);

    TetrisDTFeature tf;
    Player player;


    public TetrisFrame(ValueFunction vf) {

        player = new Player(vf);
        tf = new TetrisDTFeature();
        this.RowNum = Parameters.RowNum;
        this.ColumnNum = 10;

        /*
         * Menu
         */
        jmb = new JMenuBar();


        jmTheta = new JMenu("θ");
        for (int i = 0; i < 5; i++) {
            jmiTheta[i] = new JMenuItem();
            jmiTheta[i].addActionListener(this);
            jmTheta.add(jmiTheta[i]);
        }
        jmiTheta[0].setText("Load θ");
        jmiTheta[1].setText("Load normalized θ暂不支持");
        jmiTheta[2].setText("Save θ");
        jmiTheta[3].setText("Test θ");
        jmiTheta[4].setText("training");

        jmb.add(jmTheta);


        this.setJMenuBar(jmb);


        jpanel = new KeyboardJPanel();
        jpanel.setLayout(new BorderLayout());
        JPanel jpBlank = new JPanel();
        jpBlank.setLayout(new GridLayout(RowNum, ColumnNum));
        jlBlank = new MyJPanel[RowNum][ColumnNum];
        for (int i = 0; i < RowNum; i++)
            for (int j = 0; j < ColumnNum; j++) {
                jlBlank[i][j] = new MyJPanel();
                jlBlank[i][j].setBounds(0, 0, 10, 10);
                jpBlank.add(jlBlank[i][j]);
                jlBlank[i][j].setBorder(BorderFactory.createLineBorder(new Color(148, 171, 112)));
            }
        jpanel.add(jpBlank, BorderLayout.CENTER);

        JPanel jpbutton = new JPanel();
        jpbutton.setLayout(new GridLayout(1, 6));

        jbBegin = new JButton("Begin");
        jbNextStep = new JButton("Next Step");
        jbpause = new JButton("Pause");
        jbBegin.addActionListener(this);
        jbNextStep.addActionListener(this);
        jbpause.addActionListener(this);

        jlCount = new JLabel();
        jlCount.setText("Count: " + 0);

        jpbutton.add(jbBegin);
        jpbutton.add(jbpause);
        jpbutton.add(jbNextStep);
        jpbutton.add(jlCount);

        jbpause.setVisible(false);
        jbNextStep.setVisible(false);


        jpanel.add(jpbutton, BorderLayout.SOUTH);


        JPanel jpSpeed = new JPanel();
        jpSpeed.setLayout(new BorderLayout());
        JLabel jlSpeed = new JLabel("Speed: ");
        jpSpeed.add(jlSpeed, BorderLayout.WEST);
        jsSpeed = new JSlider(0, 1000, 500);
        sleepTime = 1001 - jsSpeed.getValue();
        jpSpeed.add(jsSpeed, BorderLayout.CENTER);
        jsSpeed.addChangeListener(new ChangeListener() {


            @Override
            public void stateChanged(ChangeEvent e) {
                /**
                 * ���ڵ��ٶȣ�ͨ��JSlider��ȡ����Ӧ��[10,110]
                 * 110-value��valueԽ����˯��ʱ��Խ�̣�Ҳ����Խ��
                 */
                //sleepTime = (int)(1000*Math.exp(jsSpeed.getValue()));
                sleepTime = 1001 - jsSpeed.getValue();
                if (jsSpeed.getValue() == 1000) {
                    showStepFinal = true;
                } else {
                    showStepFinal = false;
                }
            }
        });

        jpanel.add(jpSpeed, BorderLayout.NORTH);


        jp_west = new JPanel();


        jtaDebug = new JTextArea();
        jtaDebug.setText(this.lines);
        jp_south = new JPanel();
        jp_south.add(jtaDebug);

        jpDebug = new JPanel();
        jpDebug.setLayout(new BorderLayout());
        jpDebug.add(jp_west, BorderLayout.CENTER);
        jpDebug.add(jp_south, BorderLayout.SOUTH);
        this.initDebug();

        this.setBordinit();

        this.getContentPane().setLayout(new GridLayout(1, 2));
        this.getContentPane().add(jpanel);
        this.getContentPane().add(jpDebug);

        jpanel.requestFocus();


        this.setTitle("Tetris");
        if (Parameters.RowNum == 20) {
            this.setSize(550, 600);
        }
        if (Parameters.RowNum == 10) {
            this.setSize(550, 350);
        }
        this.setResizable(true);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setCenter();
        this.setVisible(true);
        //this.showTetris();
        this.showDebug();
        jpanel.requestFocus();
    }

    public void setCenter() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int a = screenSize.width;
        int b = screenSize.height;
        Dimension eluosiSize = this.getSize();
        int x = (a - eluosiSize.width) / 2;
        int y = (b - eluosiSize.height) / 2;
        this.setLocation(x, y);
    }

    public void settoFront() {
        this.setVisible(true);
        this.toFront();
    }

    public void sleepy() {
        try {
            if (this.isPause == false) {
                Thread.sleep(sleepTime);
            } else {
                while (this.isPause == true && isClickNextstep == false) {
                    Thread.sleep(50);
                }
            }
        } catch (InterruptedException e) {
            System.out.println(e);
        }
    }

    public void initDebug() {
        jp_west = new JPanel();
        jlWeight = new JTextField[tf.getNumberoffeature() + 1][5];
        jp_west.setLayout(new GridLayout(tf.getNumberoffeature() + 1, 5));
        for (int i = 0; i < tf.getNumberoffeature() + 1; i++)
            for (int j = 0; j < 3; j++) {
                jlWeight[i][j] = new JTextField("");
                jlWeight[i][j].setBounds(0, 0, 10, 5);
                if (j < 2 || i < 1) {
                    jlWeight[i][j].setEnabled(false);
                } else {
                    jlWeight[i][j].setEnabled(true);
                    jlWeight[i][j].addActionListener(this);
                }
                jlWeight[i][j].setFont(new Font("Arial", Font.PLAIN, 12));
                jp_west.add(jlWeight[i][j]);
            }
        jpDebug.add(jp_west, BorderLayout.CENTER);
    }

    public void showDebug() {
        if (jlWeight != null) {
            jlWeight[0][0].setText("Feature");
            jlWeight[0][1].setText("φ(s)");
            jlWeight[0][2].setText("θ");
            DecimalFormat nf = new DecimalFormat("####0.00");
            if (tf.getNumberoffeature() > 0) {
                for (int i = 1; i < tf.getNumberoffeature() + 1; i++) {
                    jlWeight[i][2].setText("" + nf.format(player.vf.getTheta(i - 1)));
                }

                for (int i = 1; i < tf.getNumberoffeature() + 1; i++) {
                    jlWeight[i][0].setText(tf.getFeatureNames()[i - 1]);
                    jlWeight[i][1].setText("" + tf.getFeature(i - 1));
                }
            }
        }

    }

    public void showTetris(int[] current) {
        jbBegin.setText("Begin");
        jbNextStep.setText("Next Step");
        jbpause.setText("Pause");

        int[][] board = TetrisEnvironment.getBoard(current);
        for (int i = 0; i < RowNum; i++)
            for (int j = 0; j < ColumnNum; j++) {
                jlBlank[i][j].setbackground(board[i][j], "", 0, 0);
            }
        //tf.countFeature(ts.tas);
        this.showDebug();
        jlCount.setText("" + current[11]);

        this.sleepy();
    }

    public void setBordinit() {
        for (int i = 0; i < RowNum; i++)
            for (int j = 0; j < ColumnNum; j++) {
                jlBlank[i][j].setbackground(0, "", 0, 0);
            }
    }


    class KeyboardJPanel extends JPanel implements KeyListener {
        /**
         * Comment for <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = 1L;

        public KeyboardJPanel() {
            addKeyListener(this);
        }

        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_LEFT && gameover == false) {
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT && gameover == false) {
                //rightPress();
            }
            if (e.getKeyCode() == KeyEvent.VK_UP && gameover == false) {
                //upPress();
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN && gameover == false) {
                //downPress();
            }

            if (e.getKeyCode() == KeyEvent.VK_SPACE && gameover == false) {
                //downPress();
            }

        }

        public void keyReleased(KeyEvent e) {

        }

        public void keyTyped(KeyEvent e) {

        }

    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == jbBegin) {
            if (gameover == true) {
                jbpause.setVisible(true);
                jbNextStep.setVisible(false);
                Go g = new Go();
                g.start();
                jbBegin.setVisible(false);
            }

            jpanel.requestFocus();
        }
        if (e.getSource() == jbNextStep) {
            if (gameover == false) {
                isClickNextstep = true;
                jpanel.requestFocus();
                //System.out.println("man pressed");
            }

        }
        if (e.getSource() == jbpause) {
            if (this.isPause == true) {
                this.isPause = false;
                jbNextStep.setVisible(false);
                this.jbpause.setText("Pause");
            } else {
                this.isPause = true;
                this.jbpause.setText("Contin");
                jbNextStep.setVisible(true);
            }
        }


        if (e.getSource() == jmiTheta[0]) {
            FileDialog f = new FileDialog(this, "Load Theta");
            f.setVisible(true);
            String s = f.getFile();
            if (s != null) {
                this.player.vf.readTheta(f.getDirectory(), s);
                this.showDebug();
            }
        }
        if (e.getSource() == jmiTheta[1]) {
            FileDialog f = new FileDialog(this, "Load Normalized Theta");
            f.setVisible(true);
            String s = f.getFile();
            if (s != null) {
                this.player.vf.readNormalizedTheta(f.getDirectory() + s);
                this.showDebug();
            }
        }
        if (e.getSource() == jmiTheta[2]) {
            FileDialog f = new FileDialog(this, "Save Theta");
            f.setVisible(true);
            String s = f.getFile();
            this.player.vf.writeTheta(s);
        }
        if (e.getSource() == jmiTheta[3]) {
            ParallelismTest p = new ParallelismTest(player);
            p.go();
        }
        if (e.getSource() == jmiTheta[4]) {
            Episode ep = new Episode();
            ep.test(player);
        }

        for (int i = 1; i < tf.getNumberoffeature() + 1; i++)
            for (int j = 2; j < 5; j++) {
                if (jlWeight != null)
                    if (e.getSource() == jlWeight[i][j]) {
                        double x = Double.valueOf(jlWeight[i][j].getText());
                        System.out.print("Change " + (i - 1) + "th weight to " + x + "\n");
                        player.vf.setTheta(i - 1, x);
                        this.showDebug();
                    }
            }
    }


    class Go extends Thread {
        public void run() {
            episode();
        }
    }


    //
    public void episode() {
        this.gameover = false;
        int[] current = TetrisEnvironment.defaultInitialState();
        long al1 = TetrisEnvironment.getActionList(current);
        int a1 = player.getChoice(current, al1);
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            jlCount.setText("" + current[11]);
            tf.setFeature(current, a1);
            System.out.println(a1);
            int[] next = TetrisEnvironment.successorState(current, a1);
            long al2 = TetrisEnvironment.getActionList(next);
            int a2 = player.getChoice(next, al2);

            //player.learn(current, a1, reward, next, a2);
            current = TetrisEnvironment.copy(next);
            a1 = a2;
            this.showTetris(current);
            isClickNextstep = false;
            if (TetrisEnvironment.isFinal(current)) {
                break;
            }
        }
        this.showTetris(current);
        this.gameover = true;

        jpanel.requestFocus();
        jbBegin.setVisible(true);
        jbNextStep.setVisible(false);
        jbpause.setVisible(false);
        this.lines += " " + current[11];
        if (this.lines.length() % 25 == 0 || this.lines.length() % 24 == 0) this.lines += "\n";
        jtaDebug.setText(this.lines);
    }

    public static void main(String[] args) {
        TetrisDTFeature current = new TetrisDTFeature();
        TetrisDTFeature next = new TetrisDTFeature();
        ValueFunction vf = new ValueFunction(current, next, current.getNumberoffeature());
        vf.readTheta("D:\\Desktop\\graduate\\TetrisSelf\\TetrisTheta\\theta\\", "CBMPI20.theta");
        TetrisFrame t = new TetrisFrame(vf);
    }
}

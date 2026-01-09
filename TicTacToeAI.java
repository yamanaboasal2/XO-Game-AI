import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Locale;

public class TicTacToeAI extends JFrame implements ActionListener {

    private char[][] board = {{' ',' ',' '},{' ',' ',' '},{' ',' ',' '}};
    private JButton[][] buttons = new JButton[3][3];
    private JLabel statusLabel;

    private char player = 'X', ai = 'O';
    private boolean playerTurn = true;

    private String difficulty = "Hard";
    private String evalMode = "ML";

    private final MLModel mlModel = new MLModel();

    public TicTacToeAI() {
        setTitle("Tic-Tac-Toe AI");
        setSize(900,700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(15,15,26));

        add(createHeader(), BorderLayout.NORTH);
        add(createBoard(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);

        showSettings();
    }



    private JPanel createHeader(){
        JPanel p = new JPanel(new GridLayout(2,1));
        p.setBackground(new Color(15,15,26));
        p.setBorder(BorderFactory.createEmptyBorder(30,0,20,0));

        JLabel title = new JLabel("TIC-TAC-TOE AI", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 48));
        title.setForeground(new Color(162,155,254));

        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 22));
        statusLabel.setForeground(Color.WHITE);

        p.add(title);
        p.add(statusLabel);
        return p;
    }

    private JPanel createBoard(){
        JPanel p = new JPanel(new GridLayout(3,3,15,15));
        p.setBackground(new Color(22,33,62));
        p.setBorder(BorderFactory.createEmptyBorder(30,50,30,50));

        for(int i=0;i<3;i++)
            for(int j=0;j<3;j++){
                JButton b = new JButton("");
                b.setFont(new Font("Arial", Font.BOLD, 80));
                b.setBackground(new Color(15,52,96));
                b.setFocusPainted(false);
                b.addActionListener(this);
                buttons[i][j] = b;
                p.add(b);
            }
        return p;
    }

    private JPanel createFooter(){
        JPanel p = new JPanel();
        p.setBackground(new Color(15,15,26));

        JButton newGame = new JButton("New Game");
        JButton settings = new JButton("Settings");
        JButton eval = new JButton("Evaluation");

        newGame.addActionListener(e -> resetGame());
        settings.addActionListener(e -> showSettings());
        eval.addActionListener(e -> showEvaluationBestForAI());

        JButton[] arr = {newGame, settings, eval};
        Color[] c = {new Color(0,210,255), new Color(255,107,107), new Color(162,155,254)};

        for(int i=0;i<3;i++){
            arr[i].setBackground(c[i]);
            arr[i].setForeground(Color.WHITE);
            arr[i].setFont(new Font("Arial", Font.BOLD, 16));
            arr[i].setPreferredSize(new Dimension(200,55));
            p.add(arr[i]);
        }

        return p;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        for(int i=0;i<3;i++)
            for(int j=0;j<3;j++)
                if(buttons[i][j]==e.getSource()
                        && board[i][j]==' '
                        && playerTurn
                        && !isGameOver()){

                    board[i][j]=player;
                    buttons[i][j].setText(String.valueOf(player));
                    buttons[i][j].setForeground(Color.PINK);

                    playerTurn = false;
                    updateStatus();


                    if(!isGameOver()){
                        SwingUtilities.invokeLater(this::aiMove);
                    }

                }
    }
//end gui
    private void aiMove(){
        int[] move = findBestMove();
        if(move!=null){
            board[move[0]][move[1]] = ai;
            buttons[move[0]][move[1]].setText(String.valueOf(ai));
            buttons[move[0]][move[1]].setForeground(Color.CYAN);
        }
        playerTurn=true;
        updateStatus();
    }

    private void resetGame(){
        board = new char[][]{{' ',' ',' '},{' ',' ',' '},{' ',' ',' '}};
        for(int i=0;i<3;i++)
            for(int j=0;j<3;j++)
                buttons[i][j].setText("");
        playerTurn = (player=='X');
        updateStatus();
        if(!playerTurn) SwingUtilities.invokeLater(this::aiMove);
    }

    private void updateStatus(){
        if(checkWin(ai)) statusLabel.setText("AI Wins!");
        else if(checkWin(player)) statusLabel.setText("You Win!");
        else if(isFull()) statusLabel.setText("Draw!");
        else statusLabel.setText("Your Turn ("+player+")");
    }

    private boolean isGameOver(){ return checkWin(player)||checkWin(ai)||isFull(); }

    private boolean isFull(){
        for(char[] r:board)
            for(char c:r)
                if(c==' ') return false;
        return true;
    }

    private boolean checkWin(char p){
        for(int i=0;i<3;i++){
            if(board[i][0]==p && board[i][1]==p && board[i][2]==p) return true;
            if(board[0][i]==p && board[1][i]==p && board[2][i]==p) return true;
        }
        return (board[0][0]==p && board[1][1]==p && board[2][2]==p)
                || (board[0][2]==p && board[1][1]==p && board[2][0]==p);
    }

    private int[] findBestMove(){

        if(difficulty.equals("Easy") && Math.random() < 0.3){
            return randomMove();
        }

        int depth = difficulty.equals("Easy") ? 2 :
                difficulty.equals("Normal") ? 6 : 9;

        double best = Double.NEGATIVE_INFINITY;
        int[] bestMove = null;

        for(int i=0;i<3;i++)
            for(int j=0;j<3;j++)
                if(board[i][j]==' '){
                    board[i][j]=ai;
                    double val = alphaBeta(depth-1,
                            Double.NEGATIVE_INFINITY,
                            Double.POSITIVE_INFINITY,
                            false);
                    board[i][j]=' ';
                    if(val>best){
                        best=val;
                        bestMove=new int[]{i,j};
                    }
                }
        return bestMove;
    }
    private int[] randomMove(){
        java.util.List<int[]> moves = new java.util.ArrayList<>();
        for(int i=0;i<3;i++)
            for(int j=0;j<3;j++)
                if(board[i][j]==' ')
                    moves.add(new int[]{i,j});
        return moves.get((int)(Math.random()*moves.size()));
    }


    private double alphaBeta(int d, double a, double b, boolean max){
        if(checkWin(ai)) return 100+d;
        if(checkWin(player)) return -100-d;
        if(isFull()||d==0) return evaluateLeaf();
        if(max){
            double v=Double.NEGATIVE_INFINITY;
            for(int i=0;i<3;i++)
                for(int j=0;j<3;j++)
                    if(board[i][j]==' '){
                        board[i][j]=ai;
                        v=Math.max(v, alphaBeta(d-1,a,b,false));
                        board[i][j]=' ';
                        a=Math.max(a,v);
                        if(b<=a) return v;
                    }
            return v;
        } else {
            double v=Double.POSITIVE_INFINITY;
            for(int i=0;i<3;i++)
                for(int j=0;j<3;j++)
                    if(board[i][j]==' '){
                        board[i][j]=player;
                        v=Math.min(v, alphaBeta(d-1,a,b,true));
                        board[i][j]=' ';
                        b=Math.min(b,v);
                        if(b<=a) return v;
                    }
            return v;
        }
    }


    private double evaluateLeaf(){
        if(evalMode.equals("ML")){
            double score = MLModel.predictScore(extractFeatures());
            return (ai=='X') ? score : -score;

        } else {
            return classicalEval();
        }
    }


    private int classicalEval(){
        int score = 0;

        int[][] lines = {
                {0,0,0,1,0,2},{1,0,1,1,1,2},{2,0,2,1,2,2},
                {0,0,1,0,2,0},{0,1,1,1,2,1},{0,2,1,2,2,2},
                {0,0,1,1,2,2},{0,2,1,1,2,0}
        };

        for(int[] l:lines){
            int x=0,o=0;
            for(int i=0;i<3;i++){
                char c=board[l[i*2]][l[i*2+1]];
                if(c==ai) o++;
                else if(c==player) x++;
            }
            if(o==2 && x==0) score+=10;
            if(x==2 && o==0) score-=10;
        }

        if(board[1][1]==ai) score+=3;
        if(board[1][1]==player) score-=3;

        return score;
    }

    private double[] extractFeatures(){
        int numX=0,numO=0,Xnear=0,Onear=0,centerX=0,cornerX=0;

        for(int i=0;i<3;i++)
            for(int j=0;j<3;j++){
                if(board[i][j]=='X') numX++;
                else if(board[i][j]=='O') numO++;
            }

        int[][] lines = {
                {0,0,0,1,0,2},{1,0,1,1,1,2},{2,0,2,1,2,2},
                {0,0,1,0,2,0},{0,1,1,1,2,1},{0,2,1,2,2,2},
                {0,0,1,1,2,2},{0,2,1,1,2,0}
        };

        for(int[] l:lines){
            int x=0,o=0;
            for(int i=0;i<3;i++){
                char c=board[l[i*2]][l[i*2+1]];
                if(c=='X') x++;
                else if(c=='O') o++;
            }
            if(x==2 && o==0) Xnear++;
            if(o==2 && x==0) Onear++;
        }

        if(board[1][1]=='X') centerX=1;
        int[][] corners={{0,0},{0,2},{2,0},{2,2}};
        for(int[] c:corners)
            if(board[c[0]][c[1]]=='X') cornerX++;

        return new double[]{numX,numO,Xnear,Onear,centerX,cornerX};
    }




    private void showEvaluationBestForAI(){
        if(isGameOver()){
            JOptionPane.showMessageDialog(this,"Game Over! No moves available.");
            return;
        }

        int depthForDisplay = difficulty.equals("Easy") ? 2 :
                difficulty.equals("Normal") ? 6 : 9;

        class MoveScore{
            int r,c;
            double raw;
            MoveScore(int r,int c,double raw){
                this.r=r; this.c=c; this.raw=raw;
            }
        }

        java.util.List<MoveScore> list = new java.util.ArrayList<>();


        for(int i=0;i<3;i++){
            for(int j=0;j<3;j++){
                if(board[i][j]==' '){
                    board[i][j] = ai;

                    double raw = alphaBeta(
                            depthForDisplay-1,
                            Double.NEGATIVE_INFINITY,
                            Double.POSITIVE_INFINITY,
                            false
                    );

                    board[i][j] = ' ';
                    list.add(new MoveScore(i,j,raw));
                }
            }
        }


        list.sort((a,b)->Double.compare(b.raw,a.raw));

        StringBuilder sb = new StringBuilder("possible moves to Ai:\n\n");

        for(MoveScore m : list){
            if(evalMode.equals("Classical")){
                sb.append(String.format(Locale.US,
                        "Move (%d,%d) -> %d\n",
                        m.r+1, m.c+1, (int)Math.round(m.raw)));
            } else {
                sb.append(String.format(Locale.US,
                        "Move (%d,%d) -> %.3f\n",
                        m.r+1, m.c+1, m.raw));
            }
        }

        MoveScore best = list.get(0);
        sb.append("\n Best AI Move: (")
                .append(best.r+1).append(",").append(best.c+1).append(")");

        JOptionPane.showMessageDialog(this, sb.toString());
    }
    private void showSettings(){
        Object[] sym={"X (Start)","O (Second)"};
        Object[] dif={"Easy","Normal","Hard"};
        Object[] ev={"Classical","Machine Learning"};

        int s=JOptionPane.showOptionDialog(this,"Choose symbol","Settings",0,1,null,sym,sym[0]);
        int d=JOptionPane.showOptionDialog(this,"Choose difficulty","Settings",0,1,null,dif,dif[2]);
        int e=JOptionPane.showOptionDialog(this,"Choose evaluation","Settings",0,1,null,ev,ev[1]);

        player = (s==1)?'O':'X';
        ai = (player=='X')?'O':'X';
        difficulty = dif[d<0?2:d].toString();
        evalMode = (e==0)?"Classical":"ML";

        resetGame();
    }
    public static void main(String[] args){
        SwingUtilities.invokeLater(TicTacToeAI::new);
    }
}
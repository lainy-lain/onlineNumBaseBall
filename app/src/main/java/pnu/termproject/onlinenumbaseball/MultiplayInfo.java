package pnu.termproject.onlinenumbaseball;

public class MultiplayInfo {
    private String player1 = null; // 방장 uid
    private String player2 = null; // guest uid
    private String whosTurn = null; // player uid로 구별
    private boolean isEnd = false; // 한사람이 숫자 맞추면 게임 종료
    // 플레이 정보
    private String p1_inputNum = null;
    private int p1_time = 0;
    private int p1_turn = 0;
    private String p1_status = null; // "1B 1S 이런 정보를 담음"
    private String p2_inputNum = null;
    private int p2_time = 0;
    private int p2_turn = 0;
    private String p2_status = null;

    public MultiplayInfo() {

    }

    /*************** GETTER AND SETTER **************/
    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public String getWhosTurn() {
        return whosTurn;
    }

    public void setWhosTurn(String whosTurn) {
        this.whosTurn = whosTurn;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean end) {
        isEnd = end;
    }

    public String getP1_inputNum() {
        return p1_inputNum;
    }

    public void setP1_inputNum(String p1_inputNum) {
        this.p1_inputNum = p1_inputNum;
    }

    public int getP1_time() {
        return p1_time;
    }

    public void setP1_time(int p1_time) {
        this.p1_time = p1_time;
    }

    public int getP1_turn() {
        return p1_turn;
    }

    public void setP1_turn(int p1_turn) {
        this.p1_turn = p1_turn;
    }

    public String getP2_inputNum() {
        return p2_inputNum;
    }

    public void setP2_inputNum(String p2_inputNum) {
        this.p2_inputNum = p2_inputNum;
    }

    public int getP2_time() {
        return p2_time;
    }

    public void setP2_time(int p2_time) {
        this.p2_time = p2_time;
    }

    public int getP2_turn() {
        return p2_turn;
    }

    public void setP2_turn(int p2_turn) {
        this.p2_turn = p2_turn;
    }

    public String getP1_status() {
        return p1_status;
    }

    public void setP1_status(String p1_status) {
        this.p1_status = p1_status;
    }

    public String getP2_status() {
        return p2_status;
    }

    public void setP2_status(String p2_status) {
        this.p2_status = p2_status;
    }
/***********************************************/
}

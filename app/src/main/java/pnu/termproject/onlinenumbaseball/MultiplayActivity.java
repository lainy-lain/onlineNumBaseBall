package pnu.termproject.onlinenumbaseball;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MultiplayActivity extends AppCompatActivity{
    //변수 선언
    private RadioGroup rg_number;
    private RadioButton rb_select;
    private RadioButton[] radio_btn = new RadioButton[5];
    private Button[] btn_num = new Button[10];
    private Button btn_result, btn_cancel, btn_memo;
    private TextView tv_info;
    private Random random = new Random();
    private int strike, ball, turn;
    private ListView result_list;
    private Button[] memo_color = new Button[6];
    private Button btn_back;
    private Button btn_clear;
    private TextView guess;

    int[] ans; //맞춰야 할 정답
    int[] input_num; //사용자가 선택한 정답

    // 이전 activity로부터 전달받을 것들.
    private String p1_nickname, p2_nickname;
    private String p1_profileUrl, p2_profileUrl;
    private String p1_id, p2_id;
    private int ball_number;
    // 게임 진행에 필요한 정보들
    private int whosTurn; // 1이면 방장, 2면 게스트의 차례
//    private boolean isEnd = false;
//    private String p1_solNum, p2_solNum;
//    private String p1_inputNum, p2_inputNum;
//    private String p1_status, p2_status;
    // 필요하지만 DB에 R/W하지는 않는 변수들
    private String opponentInputNum; // 상대가 입력한 숫자
    private boolean isAlreadySubmit = false; // 내 턴을 소모했는가?
    private boolean am_i_p1; // 내가 플레이어 1인지 아닌지를 나타내는 변수

    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference DB_game = FirebaseDatabase.getInstance().getReference("GAME");

    private long backKeyPressedTime = 0;
    private Toast toast;

    ArrayList<Point> points = new ArrayList<Point>();
    LinearLayout drawLinear, resultLinear, drawBtnLinear;
    TableLayout inputTable;
    int color;
    ArrayList<Integer> lastDraw = new ArrayList<Integer>();

    // 설정(색깔) 관련한 변수 전역변수로 뺐습니다.
    SharedPreferences sp = getSharedPreferences("setting", MODE_PRIVATE);


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_play);

        //game.xml 의 값을 변수에 대입
        int[] radio_Id = {R.id.radioButton1, R.id.radioButton2, R.id.radioButton3, R.id.radioButton4, R.id.radioButton5};
        int[] btn_Id = {R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4, R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9};
        for(int i = 0; i < 5; i++)
            radio_btn[i] = findViewById(radio_Id[i]);
        for(int i = 0; i < 10; i++)
            btn_num[i] = findViewById(btn_Id[i]);
        rg_number = findViewById(R.id.rg_number);
        btn_result = findViewById(R.id.btn_result);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_memo = findViewById(R.id.btn_memo);
        btn_clear = findViewById(R.id.btn_clear);
        // tv_turn = findViewById(R.id.turn_text);
        tv_info = findViewById(R.id.text_info);
        result_list = findViewById(R.id.result_ListView);
        int[] memo_color_Id = {R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6};
        for (int i = 0; i < 6; i++) {
            memo_color[i] = findViewById(memo_color_Id[i]);
        }
        btn_back = findViewById(R.id.back);
        guess = findViewById(R.id.guess);
        rb_select = findViewById(R.id.radioButton1);
        radio_btn[0].setChecked(true);

        // 데이터 초기화 해야함
        if (Objects.equals(currentUser.getUid(), p1_id)){
            am_i_p1 = true;
        }
        else{
            am_i_p1 = false;
        }
        // 공 갯수, 플레이어 2명의 정보를 이전 activity로부터 받아오면 됨
        Intent intent = getIntent();
        ball_number = intent.getExtras().getInt("ballNumber");
        ans       = new int[ball_number]; //맞춰야 할 정답
        input_num = new int[ball_number]; //사용자가 선택한 정답

        // DB에 게임 방 생성 (방장(p1)이 생성)
        if (am_i_p1){
            // 게임이 진행되기 전에, 이 child에 대한 listener가 설정돼야 하므로
            // 여기서 미리 초기화시켜두는 것이다.
            DB_game.child(p1_id).child("p1_inputNum").setValue("init");
            DB_game.child(p1_id).child("p2_inputNum").setValue("init");
            DB_game.child(p1_id).child("p1_status").setValue("init");
            DB_game.child(p1_id).child("p2_status").setValue("init");
            DB_game.child(p1_id).child("isEnd").setValue(false);
            DB_game.child(p1_id).child("whosTurn").setValue(0);
            DB_game.child(p1_id).child("isOneExited").setValue(false);
        }


        // 플레이어 2명의 정보를 화면에 띄워야 함


        // 설정
        setting();

        //결과를 나타내는 리스트들을 위한 코드
        // recyclerView로 바꾸자...
        List<String> data = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = view.findViewById(android.R.id.text1);
                tv.setTextColor(sp.getInt("btnbgtx", 0xFF000000));
                return view;
            }
        };
        result_list.setAdapter(adapter);



        // 라디오 버튼 갯수 설정
        for(int i = ball_number; i < 5; i++)
            radio_btn[i].setVisibility(View.INVISIBLE);
        turn = 1;

        //라디오 버튼(몇번째 공을 선택했는지 구별)을 눌렀을때의 동작을 구현하는 코드
        rg_number.setOnCheckedChangeListener((radioGroup, i) -> rb_select = findViewById(i));

        //숫자 버튼을 눌렀을때의 동작을 구현하는 코드
        for(int i = 0; i < 10; i++)
        {
            int tmp = i;
            btn_num[tmp].setOnClickListener(view -> {
                rb_select.setText(String.valueOf(tmp));
                for(int i1 = 0; i1 < ball_number; i1++) {
                    if(rb_select == radio_btn[i1]) {
                        input_num[i1] = tmp;
                        if (i1 < ball_number - 1) {
                            rb_select = radio_btn[i1 + 1];
                            rb_select.setChecked(true);
                            break;
                        }
                    }
                }
                for(int i1 = 0; i1 < 10; i1++)
                    btn_num[i1].setEnabled(true);
                for(int i1 = 0; i1 < ball_number; i1++)
                    if(input_num[i1] != -1)
                        btn_num[input_num[i1]].setEnabled(false);
            });
        }


        // 초기 숫자 결정
        // 10초동안 초기 숫자를 선택가능
        // 10초 후에 체크박스에 입력되어있는 숫자가 해답이 됨.
        // 만약 숫자가 중복된다면, 중복되지 않게 숫자를 바꿈.
        Timer init_timer = new Timer();
        TimerTask init_task = new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                // 입력된 초기숫자를 받아서, 중복이 있으면 없도록 처리해줌
                Integer[] solNumArr = Arrays.stream(input_num).boxed().toArray(Integer[]::new);
                Set<Integer> solNumSet = new HashSet<>(Arrays.asList(solNumArr));
                boolean is_dup = false;

                while (solNumSet.size() < ball_number){ // 중복이 없어질때까지
                    is_dup = true;
                    int randomNum = random.nextInt(10);
                    solNumSet.add(randomNum);
                }

                String solNum_str = "";
                if (is_dup){
                    Iterator<Integer> it = solNumSet.iterator();
                    while (it.hasNext()){
                        solNum_str += String.valueOf(it.next());
                    }
                }
                else{
                    for (int i : input_num){
                        solNum_str += String.valueOf(i);
                    }
                }
                // 중복처리 완료

                // DB에 입력한 초기 값 쓰기
                if (am_i_p1){
                    DB_game.child(p1_id).child("p1_solNum").setValue(solNum_str);
                }
                else{
                    DB_game.child(p1_id).child("p2_solNum").setValue(solNum_str);
                }

                // input_num 초기화시켜야함.
                resetButton();

                playMultiGame();
            }
        };
        btn_result.setVisibility(View.INVISIBLE); // 확인(제출) 버튼 사용불가
        init_timer.schedule(init_task, 10000); // 10초 후에 이 타이머 스레드가 실행된다.
        tv_info.setText("당신의 숫자를 입력하세요");


        // 이 아래는, 그냥 listener 설정만 해주는 것이다.
        //확인(제출) 버튼을 눌렀을때의 동작을 구현하는 코드
        btn_result.setOnClickListener(view -> {
            boolean trigger = true;

            if (!isMyTurn()){
                trigger = false;
            }
            else{
                for(int i = 0; i < ball_number; i++){
                    if(input_num[i] == -1 ){
                        trigger = false;
                        break;
                    }
                }
            }

            if (trigger) {
                isAlreadySubmit = true;
                getResultAndUpdate();
            }
        });

        // 취소 버튼 눌렀을 때
        btn_cancel.setOnClickListener(v -> {
            resetButton();
        });

        // 게임 강제종료에 대한 이벤트 리스너 만들어야 함
        // 상대방이 뒤로가기 버튼을 눌러 나간 경우 실행되는 리스너
        DB_game.child(p1_id).child("isOneExited").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // 상대방이 나가서 게임이 종료됐습니다. 승리 처리됩니다.
                // 상대방의 해답은 xxx였습니다.
                // 방 청소
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        setMemoFunc();

    } // end of onCreate()



    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void playMultiGame(){
        // DB에서 상대방이 입력한 해답을 받아온다
        DB_game.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    if (Objects.equals(ds.child("p1_id").getValue(), p1_id)){ // 이 snapshot이 현재 게임에 대한 data라면
                        if (currentUser.getUid().equals(p1_id)) { // 이 코드를 수행하는 것이 방장(p1) 이라면
                            opponentInputNum = (String) ds.child("p2_solNum").getValue();
                        }
                        else{ // 방장이 아니라면
                            opponentInputNum = (String) ds.child("p1_solNum").getValue();
                        }
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // 상대방이 입력한 숫자(해답)을 배열에 저장
        for(int i = 0; i < ball_number; i++) {
            ans[i] = Integer.parseInt(opponentInputNum.substring(i, i+1));
            input_num[i] = -1;
        }

        btn_result.setVisibility(View.VISIBLE);

        // 상대방의 입력값/상태 를 얻어오기 위한 변수
        String whosInput, whosStatus;
        if (am_i_p1) {
            whosInput = "p2_inputNum";
            whosStatus = "p2_status";
        }
        else{
            whosInput = "p1_inputNum";
            whosStatus = "p1_status";
        }

        // 상대방의 입력값이 갱신되면 자동 감지
        DB_game.child(p1_id).child(whosInput).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (Objects.equals(whosInput, "p2_inputNum")){ // 방장이 실행할 코드
                    String p2_inputNum = (String) snapshot.getValue();
                    // 이걸 표시해줘야함 이제
                }
                else{ // guest가 실행할 코드
                    String p1_inputNum = (String) snapshot.getValue();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        // 상대방의 상태가 갱신되면 자동 감지
        DB_game.child(p1_id).child(whosStatus).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (Objects.equals(whosStatus, "p2_status")){ // 방장이 실행할 코드
                    String p2_status = (String) snapshot.getValue();
                }
                else{ // guest가 실행할 코드
                    String p1_status = (String) snapshot.getValue();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        // 게임이 종료되면 실시간으로 감지. 패자만이 이 코드를 실행하게 된다.
        DB_game.child(p1_id).child("isEnd").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                int opponentClearTurn;
                if (am_i_p1){
                    opponentClearTurn = turn;
                }
                else{
                    opponentClearTurn = turn + 1;
                }
                // 상대방이 opponentClearTurn 만에 클리어하셨습니다.
                // 상대방의 해답은 xxx였습니다.
                // 방 청소
                // 룸으로 돌아오게.
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        // 턴 정보가 변경되면 실시간으로 감지해서 가져온다.
        DB_game.child(p1_id).child("whosTurn").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                whosTurn = (int) snapshot.getValue();
                displayTurnInfo(); // 턴 정보를 화면에 표시

                if (isMyTurn()){ // && !isEnd 조건도 넣자. => 게임 끝나면 턴 정보 바꾸지 않는걸로 하면 필요없다.
                    Timer play_timer = new Timer();
                    TimerTask play_task = new TimerTask() {
                        @Override
                        public void run() {
                            if (isAlreadySubmit){ // 시간이 경과하기 전에 이미 제출(확인) 버튼을 누른 경우
                                isAlreadySubmit = false; // 값 reset
                                // do nothing
                            }
                            else{ // 시간이 경과할 동안 제출 버튼을 누르지 않은 경우, 자동 제출
                                getResultAndUpdate();
                                isAlreadySubmit = false; // concurrency problem(race condition) 방지
                            }
                        }
                    };

                    play_timer.schedule(play_task, 20000); // 제한 시간은 20초
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        // 방장이 선공을 결정한다
        if (currentUser.getUid().equals(p1_id)) { // 이 코드를 수행하는 것이 방장(p1) 이라면
            int randomNum = random.nextInt(2) + 1;
            switch (randomNum){
                case 1:
                    whosTurn = 1;
                    break;
                case 2:
                    whosTurn = 2;
                    break;
                default:
                    break;
            }

            DB_game.child(p1_id).child("whosTurn").setValue(whosTurn); // DB에 선공 데이터 갱신
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private boolean isMyTurn(){
        if (am_i_p1){ // 방장이라면
            return whosTurn == 1;
        }
        else{
            return whosTurn == 2;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void getResultAndUpdate(){
        // 이 밑은 '함수'로 만들어야 할듯
        // 왜냐? 버튼을 누르거나, 시간 제한이 다되거나 했을때 둘다 작동해야 하므로!

        // 소모 turn 1회 증가.
        turn++;

        // 결과를 계산하는 코드
        strike = 0;
        ball = 0;
        for (int i = 0; i < ball_number; i++) {
            if (ans[i] == input_num[i])
                strike++;
            else {
                for (int j = 0; j < ball_number; j++) {
                    if (ans[i] == input_num[j])
                        ball++;
                }
            }
        }

        //진행상황을 기록하는 코드
        String myResult = "";
        for (int i = 0; i < ball_number; i++){
            myResult += String.valueOf(input_num[i]);
            myResult += " ";
        }
        String inputForDB = myResult;
        String resultForDB = strike + "S" + " " + ball + "B";

        // inputForDB, resultForDB를 DB에 갱신해야함
        String my_inputNum, my_status;
        if (am_i_p1){
            my_inputNum = "p1_inputNum";
            my_status = "p1_status";
        }
        else{
            my_inputNum = "p2_inputNum";
            my_status = "p2_status";
        }
        DB_game.child(p1_id).child(my_inputNum).setValue(inputForDB);
        DB_game.child(p1_id).child(my_status).setValue(resultForDB);


        // 화면에 "내 결과" 표시하는 부분임 => recyclerview 이용하자.
        myResult += "\n" + resultForDB;
//        data.add(myResult);
//        adapter.notifyDataSetChanged();

        // turn 교체
        if (whosTurn == 1)
            whosTurn = 2;
        else
            whosTurn = 1;


        if (ball_number == strike) { // 게임 종료. 승자만이 이 코드를 실행하게 된다.
            DB_game.child(p1_id).child("isEnd").setValue(true); // DB에 값 갱신
            // 승리하셨습니다! x turn 소모 표시하기.
            // 승/패수 갱신 후 승률 갱신하기.
            // 방으로 나가기.

        }
        else{
            resetButton();
            // turn 정보를 DB에 갱신해야함 => isEnd 갱신 후에 갱신하자.
            // 게임이 끝나지 않은 경우에만 턴 갱신. 게임 끝나면 턴을 갱신할 필요가 없다.
            DB_game.child(p1_id).child("whosTurn").setValue(whosTurn);
        }
    } // end of getResultAndUpdate()

    private void resetButton(){
        rb_select = radio_btn[0];
        rb_select.setChecked(true);
        for (int i = 0; i < ball_number; i++) {
            input_num[i] = -1;
            radio_btn[i].setText("");
        }
        for(int i = 0; i < 10; i++){
            btn_num[i].setEnabled(true);
        }
    }

    private void displayTurnInfo(){
        String info;
        switch (whosTurn){
            case 1:
                info = "Turn:" + p1_nickname + "(P1)";
                break;
            case 2:
                info = "Turn:" + p2_nickname + "(P2)";
                break;
            default:
                info = "error";
                break;
        }
        tv_info.setText(info);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setting(){
        // 설정 적용
        ColorStateList[] colors = {ColorStateList.valueOf(sp.getInt("btn1bg", 0xFFFFEB3B)),
                ColorStateList.valueOf(sp.getInt("btn2bg", 0xFFCDDC39)),
                ColorStateList.valueOf(sp.getInt("btn3bg", 0xFF8BC34A)),
                ColorStateList.valueOf(sp.getInt("btn4bg", 0xFF00BCD4)),
                ColorStateList.valueOf(sp.getInt("btn5bg", 0xFF03A9F4)),
                ColorStateList.valueOf(sp.getInt("btnbgbg", 0xFFFFFFFF)),
                ColorStateList.valueOf(sp.getInt("btn1tx", 0xFF000000)),
                ColorStateList.valueOf(sp.getInt("btn2tx", 0xFF000000)),
                ColorStateList.valueOf(sp.getInt("btn3tx", 0xFFFFFFFF)),
                ColorStateList.valueOf(sp.getInt("btn4tx", 0xFFFFFFFF)),
                ColorStateList.valueOf(sp.getInt("btn5tx", 0xFFFFFFFF)),
                ColorStateList.valueOf(sp.getInt("btnbgtx", 0xFF000000))
        };
        int radiusChecked = sp.getInt("radius", 0);
        int cornerRadius = (radiusChecked + 1) * 8;
//        tv_turn.setTextColor(colors[11]);
//        tv_turn.getRootView().setBackgroundTintList(colors[5]);
        for (int i = 0; i < 5; i++) {
            radio_btn[i].setTextColor(colors[6]);
        }
        rg_number.setBackgroundColor(sp.getInt("btn1bg", 0xFFFFEB3B));
        for (int i = 0; i < 10; i++) {
            btn_num[i].setTextColor(colors[7]);
            btn_num[i].setBackgroundTintList(colors[1]);
            ((MaterialButton)btn_num[i]).setCornerRadius(cornerRadius);
        }
        btn_result.setTextColor(colors[8]);
        btn_result.setBackgroundTintList(colors[2]);
        ((MaterialButton)btn_result).setCornerRadius(cornerRadius);
        btn_cancel.setTextColor(colors[9]);
        btn_cancel.setBackgroundTintList(colors[3]);
        ((MaterialButton)btn_cancel).setCornerRadius(cornerRadius);
        btn_memo.setTextColor(colors[10]);
        btn_memo.setBackgroundTintList(colors[4]);
        ((MaterialButton)btn_memo).setCornerRadius(cornerRadius);
        btn_clear.setTextColor(colors[11]);
        btn_clear.setBackgroundTintList(colors[5]);
        ((MaterialButton)btn_clear).setCornerRadius(cornerRadius);
        for (int i = 0; i < 6; i++) {
            if (i == 5) {
                memo_color[i].setBackgroundTintList(colors[11]);
            }
            else {
                memo_color[i].setBackgroundTintList(colors[i]);
            }
            ((MaterialButton)memo_color[i]).setCornerRadius(cornerRadius);
        }
        btn_back.setBackgroundTintList(colors[5]);
        btn_back.setTextColor(colors[11]);
        ((MaterialButton)btn_back).setCornerRadius(cornerRadius);
        guess.setTextColor(colors[11]);
        color = colors[11].getDefaultColor();
    }


    //메모 기능을 위한 클래스 2개
    class Point{
        float x;
        float y;
        boolean check;
        int color;

        public Point(float x, float y, boolean check, int color) {
            this.x = x;
            this.y = y;
            this.check = check;
            this.color = color;
        }
    }

    class MyView extends View{
        public MyView(Context context) {super(context);}
        @Override
        protected void onDraw(Canvas canvas) {
            Paint p = new Paint();
            p.setStrokeWidth(5);
            for(int i=1; i<points.size(); i++){
                p.setColor(points.get(i).color);
                if(!points.get(i).check)
                    continue;
                canvas.drawLine(points.get(i-1).x,points.get(i-1).y,points.get(i).x,points.get(i).y,p);
            }
        }
        @Override
        public boolean onTouchEvent(MotionEvent event){
            float x = event.getX();
            float y = event.getY();

            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    points.add(new Point(x, y, false, color));
                    break;
                case MotionEvent.ACTION_MOVE:
                    points.add(new Point(x, y, true, color));
                    break;
                case MotionEvent.ACTION_UP:
                    if (lastDraw.isEmpty()) {
                        lastDraw.add(points.size());
                    }
                    else {
                        int lastPointNum = 0;
                        for (int i = 0; i < lastDraw.size(); i++) {
                            lastPointNum = lastPointNum + lastDraw.get(i);
                        }
                        lastDraw.add(points.size() - lastPointNum);
                    }
                    break;
            }
            invalidate();
            return true;
        }
    }

    //아래로는 메모기능을 위한 코드임
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setMemoFunc(){
        final MyView m = new MyView(this);
        for (int i = 0; i < 6; i++) {
            memo_color[i].setOnClickListener(v -> color = v.getBackgroundTintList().getDefaultColor());
        }
        btn_back.setOnClickListener(v -> {
            if (!lastDraw.isEmpty()) {
                for (int i = 0; i < lastDraw.get(lastDraw.size() - 1); i++) {
                    if (points.isEmpty()) {
                        break;
                    }
                    points.remove(points.size() - 1);
                }
                lastDraw.remove(lastDraw.size() - 1);
                m.invalidate();
            }
        });

        drawLinear = findViewById(R.id.draw_linear);
        drawLinear.setVisibility(View.INVISIBLE);
        drawBtnLinear = findViewById(R.id.memo_colors);
        drawBtnLinear.setVisibility(View.INVISIBLE);
        inputTable = findViewById(R.id.input_Table);

        resultLinear = findViewById(R.id.layout_result);
        final boolean[] memoStatus = {false};

        btn_memo.setOnClickListener(v -> {
            if(!memoStatus[0]) {
                btn_memo.setText("게임");
                guess.setVisibility(View.VISIBLE);
                inputTable.setVisibility(View.INVISIBLE);
                drawBtnLinear.setVisibility(View.VISIBLE);
                drawLinear.setVisibility(View.VISIBLE);
                memoStatus[0] = true;
            }
            else {
                btn_memo.setText("메모");
                inputTable.setVisibility(View.VISIBLE);
                drawBtnLinear.setVisibility(View.INVISIBLE);
                memoStatus[0] = false;
            }
        });
        btn_clear.setOnClickListener(v -> {
            points.clear();
            m.invalidate();
        });
        drawLinear.addView(m);
    }

    //뒤로가기 버튼 관련 설정
    @Override
    public void onBackPressed(){

        if (System.currentTimeMillis() > backKeyPressedTime + 2500) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "뒤로 가기 버튼을 한 번 더 누르시면 처음화면으로 돌아갑니다.", Toast.LENGTH_LONG);
            toast.show();
            return;
        }
        // 마지막으로 뒤로 가기 버튼을 눌렀던 시간에 2.5초를 더해 현재 시간과 비교 후
        // 마지막으로 뒤로 가기 버튼을 눌렀던 시간이 2.5초가 지나지 않았으면 배경화면으로 돌아감
        if (System.currentTimeMillis() <= backKeyPressedTime + 2500) {
            // DB에 플레이어 한명이 나갔다는 값을 true로 만들어야 함.
            DB_game.child(p1_id).child("isOneExited").setValue(true);
            super.onBackPressed();
        }
    }

}



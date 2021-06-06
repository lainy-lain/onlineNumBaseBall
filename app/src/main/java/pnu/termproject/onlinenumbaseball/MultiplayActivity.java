package pnu.termproject.onlinenumbaseball;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Thread.sleep;

public class MultiplayActivity extends AppCompatActivity{
    // 버튼 관련 전역변수
    private RadioGroup rg_number;
    private RadioButton rb_select;
    private RadioButton[] radio_btn = new RadioButton[5];
    private Button[] btn_num = new Button[10];
    private Button btn_result, btn_cancel, btn_memo;
    private Random random = new Random();
    private int strike, ball, turn;
    private ListView result_list;
    private Button[] memo_color = new Button[6];
    private Button btn_back;
    private Button btn_clear;
    private TextView guess;

    // 이전 activity로부터 전달받을 정보들
    private String p1_nickname, p2_nickname;
    private String p1_photoUrl, p2_photoUrl;
    private String p1_id, p2_id;
    private int ball_number;

    // 플레이어 2명의 정보를 나타내기 위한 View들
    private ImageView iv_photo1, iv_photo2;
    private TextView tv_nickname1, tv_nickname2;
    private TextView tv_winRate1, tv_winRate2;

    // RecyclerView관련 전역변수
    private RecyclerView recyclerView_result1, recyclerView_result2;
    private RecyclerView.Adapter adapter_result1, adapter_result2;
    private RecyclerView.LayoutManager layoutManager_result1, layoutManager_result2;
    private ArrayList<InputAndResult> arrayList_result1, arrayList_result2;
    private InputAndResult ir = new InputAndResult();

    // 게임 진행에 필요한 정보들
    private TextView tv_info;
    private int[] ans; //맞춰야 할 정답
    private int[] input_num; //사용자가 선택한 정답
    private int whosTurn; // 1이면 방장, 2면 게스트의 차례
    private String opponentInputNum; // 상대가 입력한 숫자
    private boolean am_i_p1; // 내가 플레이어 1인지 아닌지를 나타내는 변수
    Queue<Boolean> submit_queue = new LinkedList<>();

    private String whosInput, whosAns;
    private ArrayList<String> inputAndResult = new ArrayList<>();

    // Firebase DB 관련 전역변수
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference DB_game;

    // 메모 관련 변수
    ArrayList<Point> points = new ArrayList<Point>();
    LinearLayout drawLinear, resultLinear, drawBtnLinear;
    TableLayout inputTable;
    int color;
    ArrayList<Integer> lastDraw = new ArrayList<Integer>();

    private long backKeyPressedTime = 0;
    private Handler handler;


    @SuppressLint("HandlerLeak")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_play);

        // 이전 activity로부터 정보 받아오기
        Intent intent = getIntent();
        ball_number = intent.getExtras().getInt("ballNumber");
        p1_nickname = intent.getExtras().getString("p1_nickname");
        p2_nickname = intent.getExtras().getString("p2_nickname");
        p1_photoUrl = intent.getExtras().getString("p1_photoUrl");
        p2_photoUrl = intent.getExtras().getString("p2_photoUrl");
        p1_id = intent.getExtras().getString("p1_id");
        p2_id = intent.getExtras().getString("p2_id");

        // 데이터 초기화, findViewById
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

        // 설정 적용
        SharedPreferences sp = getSharedPreferences("setting", MODE_PRIVATE);
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
        tv_info.setTextColor(colors[11]);
        tv_info.getRootView().setBackgroundTintList(colors[5]);
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

        //
        ans       = new int[ball_number]; // 맞춰야 할 정답
        input_num = new int[ball_number]; // 사용자가 선택한 정답
        if (Objects.equals(currentUser.getUid(), p1_id)){
            am_i_p1 = true;
        }
        else{
            am_i_p1 = false;
        }
        recyclerView_result1 = findViewById(R.id.recyclerView_result1);
        recyclerView_result2 = findViewById(R.id.recyclerView_result2);
        recyclerView_result1.setHasFixedSize(true);
        recyclerView_result2.setHasFixedSize(true);
        layoutManager_result1 = new LinearLayoutManager(this);
        layoutManager_result2 = new LinearLayoutManager(this);
        recyclerView_result1.setLayoutManager(layoutManager_result1);
        recyclerView_result2.setLayoutManager(layoutManager_result2);
        arrayList_result1 = new ArrayList<>();
        arrayList_result2 = new ArrayList<>();
        ColorStateList tx = ColorStateList.valueOf(sp.getInt("btnbgtx", 0xFF000000));
        // 어댑터 설정
        adapter_result1 = new ResultAdapter(arrayList_result1, this, tx);
        adapter_result2 = new ResultAdapter(arrayList_result2, this, tx);
        recyclerView_result1.setAdapter(adapter_result1);
        recyclerView_result2.setAdapter(adapter_result2);

        // reset button handler 설정
        handler = new Handler(){
            public void handleMessage(Message msg){
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
        };


        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        // DB에 게임 방 생성 (방장(p1)이 생성)
        if (am_i_p1){
            // 게임이 진행되기 전에, 이 child에 대한 listener가 설정돼야 하므로
            // 여기서 미리 초기화시켜두는 것이다.
            mDatabase.child("multiPlay").child(p1_id).setValue(null); // 혹시라도 유령방이 남아있으면 clear
            mDatabase.child("multiPlay").child(p1_id).child("p1_inputNum").setValue("init");
            mDatabase.child("multiPlay").child(p1_id).child("p2_inputNum").setValue("init");
            mDatabase.child("multiPlay").child(p1_id).child("p1_status").setValue("init");
            mDatabase.child("multiPlay").child(p1_id).child("p2_status").setValue("init");
            mDatabase.child("multiPlay").child(p1_id).child("p1_solNum").setValue("init");
            mDatabase.child("multiPlay").child(p1_id).child("p2_solNum").setValue("init");
            mDatabase.child("multiPlay").child(p1_id).child("isEnd").setValue(false);
            mDatabase.child("multiPlay").child(p1_id).child("whosTurn").setValue(0);
            mDatabase.child("multiPlay").child(p1_id).child("isOneExited").setValue(false);
        }
        DB_game = mDatabase.child("multiPlay");

        // 플레이어 2명의 정보를 화면에 띄워야 함
        // winRate DB에서 가져와서 저장하기. 그리고 화면에 띄워야함
        // winRate는 Room에서 DB에 접근하는걸로 하고, 여기서는 getExtras로 받는걸로 하자.
        iv_photo1 = findViewById(R.id.iv_profile1);
        iv_photo2 = findViewById(R.id.iv_profile2);
        Glide.with(this).load(p1_photoUrl).into(iv_photo1);
        Glide.with(this).load(p2_photoUrl).into(iv_photo2);
        tv_nickname1 = findViewById(R.id.tv_nickname1);
        tv_nickname2 = findViewById(R.id.tv_nickname2);
        tv_nickname1.setText(p1_nickname);
        tv_nickname2.setText(p2_nickname);

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

        if (am_i_p1) {
            whosInput = "p2_inputNum";
            whosAns = "p2_solNum";
        }
        else{
            whosInput = "p1_inputNum";
            whosAns = "p1_solNum";
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
                // 숫자 3개 중 입력되지 않은 값(-1) 이 있으면 처리해줘야 한다.
                setInputNumValid();

                String solNum_str = "";
                for (int i : input_num){
                    solNum_str += String.valueOf(i);
                }

                // DB에 입력한 초기 값 쓰기
                if (am_i_p1){
                    DB_game.child(p1_id).child("p1_solNum").setValue(solNum_str);
                }
                else{
                    DB_game.child(p1_id).child("p2_solNum").setValue(solNum_str);
                }

                // input_num 초기화시켜야함.
                resetButton();

                determineFirstAttack();
            }
        };
        init_timer.schedule(init_task, 15000); // 10초 후에 이 타이머 스레드가 실행된다.
        tv_info.setText("당신의 숫자를 입력하세요");

        // 취소 버튼 눌렀을 때
        btn_cancel.setOnClickListener(v -> {
            resetButton();
        });

        DB_game.child(p1_id).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // 여기서의 previousChildName이란... DB에서 '자신 위에 있는' Child의 이름을 말한다...

                // 상대방의 해답이 입력된 경우
                // p_solnum위에 p_inputNum이 있으므로 whosAns 대신 whosInput과 비교
                if (Objects.equals(previousChildName, whosInput)){
                    opponentInputNum = (String) snapshot.getValue();
                    // 상대방이 입력한 숫자(해답)을 배열에 저장
                    for(int i = 0; i < ball_number; i++) {
                        ans[i] = Integer.parseInt(opponentInputNum.substring(i, i+1));
                        input_num[i] = -1;
                    }
                }

                // 상대방이 뒤로가기 버튼을 눌러 게임을 나간 경우
                else if (Objects.equals(previousChildName, "isEnd")){ // isOneExited 위에 isEnd가 있으므로
                    String victoryMsg = "상대방이 입력한 숫자는 " + opponentInputNum + " 였습니다.";
                    // 확인 메시지 창을 띄움.
                    AlertDialog.Builder msgBuilder = new AlertDialog.Builder(MultiplayActivity.this)
                            .setTitle("상대방이 방을 나가서 게임이 종료됐습니다.")
                            .setMessage(victoryMsg)
                            .setPositiveButton("확인", (dialog, which) -> {
                                // DB에 승률 갱신

                                DB_game.child(p1_id).setValue(null); // DB에서 게임 데이터 삭제.

                                finish();
                            });

                    AlertDialog msgDialog = msgBuilder.create();
                    msgDialog.show();
                }

                // 상대방의 inputNum값이 갱신되는 경우
                // p1의 경우, p2_inputNum 위에 있는 p1_status와 비교
                // p2의 경우, p1_inputNum 위에 있는 isOneExited와 비교
                else if (Objects.equals(previousChildName, "p1_status") && am_i_p1
                        || Objects.equals(previousChildName, "isOneExited") && !am_i_p1) {
                    String inputNum = (String) snapshot.getValue();
                    ir.setInput(inputNum);
                    inputAndResult.add(inputNum);
                    if (inputAndResult.size() >= 2) { // input과 status 둘 다 갱신된 경우
                        InputAndResult new_ir = new InputAndResult(ir);
                        if (am_i_p1) {
                            arrayList_result2.add(new_ir);
                            adapter_result2.notifyDataSetChanged();
                        } else {
                            arrayList_result1.add(new_ir);
                            adapter_result1.notifyDataSetChanged();
                        }
                        inputAndResult.clear(); // 초기화

                    }
                }

                // 상대방의 status(결과)값이 갱신되는 경우
                // p_status 위에 p_solNum이 있으므로 whosAns와 비교
                else if (Objects.equals(previousChildName, whosAns)) {
                    String status = (String) snapshot.getValue();
                    status += "\t\t\n";
                    ir.setResult(status);
                    inputAndResult.add(status);
                    if (inputAndResult.size() >= 2) { // input과 status 둘 다 갱신된 경우
                        InputAndResult new_ir = new InputAndResult(ir);
                        if (am_i_p1) {
                            arrayList_result2.add(new_ir);
                            adapter_result2.notifyDataSetChanged();
                        } else {
                            arrayList_result1.add(new_ir);
                            adapter_result1.notifyDataSetChanged();
                        }
                        inputAndResult.clear(); // 초기화
                    }
                }

                // 턴 정보가 갱신된 경우
                // whosTurn 위에 p2_status가 있으므로 그것과 비교
                else if (Objects.equals(previousChildName, "p2_status")) {
                    Long lVal = (Long) snapshot.getValue();
                    whosTurn = lVal.intValue();
                    displayTurnInfo(); // 턴 정보를 화면에 표시

                    if (isMyTurn()) {
                        // Toast로 자신의 턴임을 알림
                        Toast.makeText(MultiplayActivity.this, "Your Turn", Toast.LENGTH_LONG).show(); // 토스트 문자 출력

                        Timer play_timer = new Timer();
                        TimerTask play_task = new TimerTask() {
                            @Override
                            public void run() {
                                if (submit_queue.poll() == null){ // 시간이 경과할 동안 제출 버튼을 누르지 않은 경우, 자동 제출
                                    getResultAndUpdate();
                                }
                            }
                        };
                        play_timer.schedule(play_task, 15000); // 제한 시간은 15초
                    }
                }

                // 게임이 종료된 경우. 단, 패자만이 이 알림을 받도록 설정.
                // isEnd위엔 아무것도 없으므로 null과 비교
                else if (Objects.equals(previousChildName, null) && !isMyTurn()) {
                    boolean is_end = (boolean) snapshot.getValue();
                    if (is_end) { // 에러 방지... 갱신되지 않았는데 갱신됐다고 뜨는 경우가 존재함
                        int opponentClearTurn;
                        if (am_i_p1) {
                            opponentClearTurn = turn;
                        } else {
                            opponentClearTurn = turn + 1;
                        }
                        String victoryTitle = "상대방이 " + String.valueOf(opponentClearTurn) + "턴 만에 정답을 맞췄습니다";
                        String victoryMsg = "상대방이 입력한 숫자는 " + opponentInputNum + " 였습니다.";

                        // 확인 메시지 창을 띄움.
                        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(MultiplayActivity.this)
                                .setTitle(victoryTitle)
                                .setMessage(victoryMsg)
                                .setPositiveButton("확인", (dialog, which) -> {
                                    // DB에 승률 갱신

                                    DB_game.child(p1_id).setValue(null); // DB에서 게임 데이터 삭제.

                                    finish();
                                });

                        AlertDialog msgDialog = msgBuilder.create();
                        msgDialog.show();
                    }
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        setMemoFunc();
    } // end of onCreate()


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void determineFirstAttack(){
        // 방장이 선공을 결정한다
        if (am_i_p1) { // 이 코드를 수행하는 것이 방장(p1) 이라면
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
                // 확인 버튼을 눌러 제출했을 경우, 시간 제한 타이머가 발동하지 않도록 해야함.
                submit_queue.offer(true);
                getResultAndUpdate();
            }
        });

    }

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

        setInputNumValid();

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


        // 화면에 "내 결과" 표시하는 부분
        resultForDB += "\t\t\n";
        InputAndResult my_ir = new InputAndResult(inputForDB, resultForDB);
        if (am_i_p1){
            arrayList_result1.add(my_ir);
            // adapter_result1.notifyDataSetChanged();

            Handler mHandler = new Handler(Looper.getMainLooper());
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    adapter_result1.notifyDataSetChanged();
                }
            }, 0);
        }
        else{
            arrayList_result2.add(my_ir);
            // adapter_result2.notifyDataSetChanged();

            Handler mHandler = new Handler(Looper.getMainLooper());
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    adapter_result2.notifyDataSetChanged();
                }
            }, 0);
        }


        if (ball_number == strike) { // 게임 종료. 승자만이 이 코드를 실행하게 된다.
            DB_game.child(p1_id).child("isEnd").setValue(true); // DB에 값 갱신

            String victoryTitle = String.valueOf(turn) + "턴 만에 승리하셨습니다!";
            String victoryMsg = "상대방이 입력한 숫자는 " + opponentInputNum + " 였습니다.";

            Handler mHandler = new Handler(Looper.getMainLooper());
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 확인 메시지 창을 띄움.
                    AlertDialog.Builder msgBuilder = new AlertDialog.Builder(MultiplayActivity.this)
                            .setTitle(victoryTitle)
                            .setMessage(victoryMsg)
                            .setPositiveButton("확인", (dialog, which) -> {
                                // DB에 승률 갱신

                                finish();
                            });

                    AlertDialog msgDialog = msgBuilder.create();
                    msgDialog.show();
                }
            }, 0);
        }
        else{
            resetButton();

            // turn 교체
            if (whosTurn == 1)
                whosTurn = 2;
            else
                whosTurn = 1;

            // turn 정보를 DB에 갱신해야함 => isEnd 갱신 후에 갱신하자.
            // 게임이 끝나지 않은 경우에만 턴 갱신. 게임 끝나면 턴을 갱신할 필요가 없다.
            DB_game.child(p1_id).child("whosTurn").setValue(whosTurn);
        }
    } // end of getResultAndUpdate()

    public void resetButton(){
//        rb_select = radio_btn[0];
//        rb_select.setChecked(true);
//        for (int i = 0; i < ball_number; i++) {
//            input_num[i] = -1;
//            radio_btn[i].setText("");
//        }
//        for(int i = 0; i < 10; i++){
//            btn_num[i].setEnabled(true);
//        }
        Message msg = handler.obtainMessage();
        handler.sendMessage(msg);
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

    private void setInputNumValid(){
        // 숫자 3개 중 입력되지 않은 값(-1) 이 있으면 처리해줘야 한다.
        for (int i = 0; i < 3; ++i){
            if (input_num[i] == -1){
                boolean is_dup = true;
                int randomNum = 0;

                while (is_dup){
                    randomNum = random.nextInt(10);
                    for (int j = 0; j < 3; ++j){
                        if (input_num[j] == randomNum){
                            is_dup = true;
                            break;
                        }
                        else{
                            is_dup = false;
                        }
                    }
                }

                input_num[i] = randomNum;
            }
        }
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
            Toast toast = Toast.makeText(this, "뒤로 가기 버튼을 한 번 더 누르시면 처음화면으로 돌아갑니다.", Toast.LENGTH_LONG);
            toast.show();
            return;
        }
        // 마지막으로 뒤로 가기 버튼을 눌렀던 시간에 2.5초를 더해 현재 시간과 비교 후
        // 마지막으로 뒤로 가기 버튼을 눌렀던 시간이 2.5초가 지나지 않았으면 배경화면으로 돌아감
        if (System.currentTimeMillis() <= backKeyPressedTime + 2500) {
            // DB에 플레이어 한명이 나갔다는 값을 true로 만들어야 함.
            DB_game.child(p1_id).child("isOneExited").setValue(true);
            // DB에 승률 갱신해야함
            super.onBackPressed();
            finish();
        }
    }

    private void myDebug(String str){
        System.out.println(str);
    }

}



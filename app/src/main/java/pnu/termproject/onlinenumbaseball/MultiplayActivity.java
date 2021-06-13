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
import android.view.LayoutInflater;
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
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MultiplayActivity extends AppCompatActivity{
    private RadioButton rb_select;
    private RadioButton[] radio_btn = new RadioButton[5];
    private Button[] btn_num = new Button[10];
    private Button btn_memo;
    private Random random = new Random();
    private int turn;
    private Button[] memo_color = new Button[6];
    private Button btn_back;
    private Button btn_clear;
    private TextView guess;

    // 이전 activity로부터 전달받을 정보들
    private String p1_nickname, p2_nickname;
    private String p1_id;
    private int ball_number;

    private RecyclerView.Adapter adapter_result1, adapter_result2;
    private ArrayList<InputAndResult> arrayList_result1, arrayList_result2;
    private InputAndResult ir = new InputAndResult();

    // 게임 진행에 필요한 정보들
    private int sec = 0;
    private TextView tv_info;
    private int[] ans; //맞춰야 할 정답
    private int[] input_num; //사용자가 선택한 정답
    private int whosTurn; // 1이면 방장, 2면 게스트의 차례
    private String opponentInputNum; // 상대가 입력한 숫자
    private boolean am_i_p1; // 내가 플레이어 1인지 아닌지를 나타내는 변수
    private Queue<Boolean> submit_queue = new LinkedList<>();
    private boolean is_game_end = false;
    private boolean is_firstSol_submit = false;

    private String whosInput, whosAns;
    private ArrayList<String> inputAndResult = new ArrayList<>();

    // Firebase DB 관련 전역변수
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference DB_game;

    // 메모 관련 변수
    ArrayList<Point> points = new ArrayList<>();
    LinearLayout drawLinear, resultLinear, drawBtnLinear;
    TableLayout inputTable;
    int color;
    ArrayList<Integer> lastDraw = new ArrayList<>();

    private long backKeyPressedTime = 0;
    private Handler handler;

    private final int SOL_TIME_LIMIT = 10;
    private final int TURN_TIME_LIMIT = 5;

    private View dialogView;
    private SharedPreferences sp;

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
        String p1_photoUrl = intent.getExtras().getString("p1_photoUrl");
        String p2_photoUrl = intent.getExtras().getString("p2_photoUrl");
        p1_id = intent.getExtras().getString("p1_id");
        String p2_id = intent.getExtras().getString("p2_id");

        // 데이터 초기화, findViewById
        int[] radio_Id = {R.id.radioButton1, R.id.radioButton2, R.id.radioButton3, R.id.radioButton4, R.id.radioButton5};
        int[] btn_Id = {R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4, R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9};
        for(int i = 0; i < 5; i++)
            radio_btn[i] = findViewById(radio_Id[i]);
        for(int i = 0; i < 10; i++)
            btn_num[i] = findViewById(btn_Id[i]);
        // 버튼 관련 전역변수
        RadioGroup rg_number = findViewById(R.id.rg_number);
        Button btn_result = findViewById(R.id.btn_result);
        Button btn_cancel = findViewById(R.id.btn_cancel);
        btn_memo = findViewById(R.id.btn_memo);
        btn_clear = findViewById(R.id.btn_clear);
        // tv_turn = findViewById(R.id.turn_text);
        tv_info = findViewById(R.id.text_info);
        int[] memo_color_Id = {R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6};
        for (int i = 0; i < 6; i++) {
            memo_color[i] = findViewById(memo_color_Id[i]);
        }
        btn_back = findViewById(R.id.back);
        guess = findViewById(R.id.guess);
        rb_select = findViewById(R.id.radioButton1);
        radio_btn[0].setChecked(true);

        // 설정 적용
        sp = getSharedPreferences("setting", MODE_PRIVATE);
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
        ((MaterialButton) btn_result).setCornerRadius(cornerRadius);
        btn_cancel.setTextColor(colors[9]);
        btn_cancel.setBackgroundTintList(colors[3]);
        ((MaterialButton) btn_cancel).setCornerRadius(cornerRadius);
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
        am_i_p1 = Objects.equals(currentUser.getUid(), p1_id);
        // RecyclerView관련 전역변수
        RecyclerView recyclerView_result1 = findViewById(R.id.recyclerView_result1);
        RecyclerView recyclerView_result2 = findViewById(R.id.recyclerView_result2);
        recyclerView_result1.setHasFixedSize(true);
        recyclerView_result2.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager_result1 = new LinearLayoutManager(this);
        RecyclerView.LayoutManager layoutManager_result2 = new LinearLayoutManager(this);
        recyclerView_result1.setLayoutManager(layoutManager_result1);
        recyclerView_result2.setLayoutManager(layoutManager_result2);
        arrayList_result1 = new ArrayList<>();
        arrayList_result2 = new ArrayList<>();
        ColorStateList tx = colors[11];
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

        LayoutInflater inflater = getLayoutInflater();
        dialogView = inflater.inflate(R.layout.game_finish, null);

        resetButton();

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
        // 플레이어 2명의 정보를 나타내기 위한 View들
        ImageView iv_photo1 = findViewById(R.id.iv_profile1);
        ImageView iv_photo2 = findViewById(R.id.iv_profile2);
        Glide.with(this).load(p1_photoUrl).into(iv_photo1);
        Glide.with(this).load(p2_photoUrl).into(iv_photo2);
        TextView tv_nickname1 = findViewById(R.id.tv_nickname1);
        TextView tv_nickname2 = findViewById(R.id.tv_nickname2);
        tv_nickname1.setTextColor(colors[11]);
        tv_nickname2.setTextColor(colors[11]);
        tv_nickname1.setText(p1_nickname);
        tv_nickname2.setText(p2_nickname);

        // 라디오 버튼 갯수 설정
        for(int i = ball_number; i < 5; i++)
            radio_btn[i].setVisibility(View.INVISIBLE);
        turn = 0;

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

        // 초기 숫자 입력 제한시간 : 10초
        sec = 0;
        Timer init_timer = new Timer();
        TimerTask init_task = new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                if (sec < SOL_TIME_LIMIT){
                    if (is_game_end){
                        init_timer.cancel();
                    }
                    else{
                        @SuppressLint("DefaultLocale") String str = String.format("해답 입력: %d초 남음", SOL_TIME_LIMIT - sec);
                        Handler mHandler = new Handler(Looper.getMainLooper());
                        mHandler.postDelayed(() -> tv_info.setText(str), 0);
                        sec++;
                    }
                }
                else{ // 시간이 지난 경우 해답 입력
                    // 숫자 3개 중 입력되지 않은 값(-1) 이 있으면 처리해줘야 한다.
                    setInputNumValid();

                    String solNum_str = "";
                    for (int i : input_num){
                        solNum_str += String.valueOf(i);
                    }

                    myDebug("solNum_str : " + solNum_str);

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

                    is_firstSol_submit = true;
                    myDebug("in First, is_firstsol_submit : " + is_firstSol_submit);
                    init_timer.cancel();
                }
            }
        };
        init_timer.schedule(init_task, 0, 1000);

        // 취소 버튼 눌렀을 때
        btn_cancel.setOnClickListener(v -> resetButton());

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
                    }
                }

                // 상대방이 뒤로가기 버튼 / 강제종료를 눌러 게임을 나간 경우
                else if (Objects.equals(previousChildName, "isEnd")){ // isOneExited 위에 isEnd가 있으므로
                    is_game_end = true;
                    String victoryMsg = "상대방이 입력한 숫자는 " + opponentInputNum + " 였습니다.";
                    // 확인 메시지 창을 띄움.
                    if (! MultiplayActivity.this.isFinishing()) {
                        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(MultiplayActivity.this);
                        msgBuilder.setView(dialogView).setCancelable(false);
                        dialogView.setBackgroundColor(sp.getInt("btnbgbg", 0xFFFFFFFF));
                        TextView title = dialogView.findViewById(R.id.tv_title);
                        title.setTextColor(colors[11]);
                        title.setText("상대방이 방을 나가서 게임이 종료됐습니다.");
                        TextView message = dialogView.findViewById(R.id.tv_message);
                        message.setTextColor(colors[11]);
                        message.setText(victoryMsg);
                        Button positive = dialogView.findViewById(R.id.positive);
                        positive.setBackgroundTintList(colors[5]);
                        positive.setTextColor(colors[11]);
                        positive.setOnClickListener(v -> {
                            DB_game.child(p1_id).setValue(null); // DB에서 게임 데이터 삭제.
                            finish();
                        });

                        AlertDialog msgDialog = msgBuilder.create();
                        msgDialog.show();
                    }
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

                    while (true){
                        boolean isOk = is_firstSol_submit;
                        myDebug("in while loop");
                        if (isOk)
                            break;
                    }

                    if (isMyTurn()) {
                        // Toast로 자신의 턴임을 알림
                        // Toast.makeText(MultiplayActivity.this, "Your Turn", Toast.LENGTH_SHORT).show(); // 토스트 문자 출력

                        sec = 0;
                        Timer play_timer = new Timer();
                        TimerTask play_task = new TimerTask() {
                            @Override
                            public void run() {
                                if (sec < TURN_TIME_LIMIT){
                                    if (is_game_end) {
                                        play_timer.cancel();
                                    }
                                    if (submit_queue.poll() != null){
                                        Handler mHandler = new Handler(Looper.getMainLooper());
                                        mHandler.postDelayed(() -> tv_info.setText("상대방의 턴"), 0);
                                        play_timer.cancel();
                                    }
                                    else{
                                        @SuppressLint("DefaultLocale") String str = String.format("나의 턴: %d초 남음", TURN_TIME_LIMIT - sec);
                                        Handler mHandler = new Handler(Looper.getMainLooper());
                                        mHandler.postDelayed(() -> tv_info.setText(str), 0);
                                        sec++;
                                    }
                                }
                                else{
                                    // 시간 지났는데 제출 안된경우 자동제출
                                    if (submit_queue.poll() == null){
                                        getResultAndUpdate();
                                        Handler mHandler = new Handler(Looper.getMainLooper());
                                        mHandler.postDelayed(() -> tv_info.setText("상대방의 턴"), 0);
                                    }
                                    play_timer.cancel();
                                }
                            }
                        };
                        play_timer.schedule(play_task, 0, 1000);
                    }
                    else{
                        Handler mHandler = new Handler(Looper.getMainLooper());
                        mHandler.postDelayed(() -> tv_info.setText("상대방의 턴"), 0);
                    }
                }

                // 게임이 종료된 경우. 단, 패자만이 이 알림을 받도록 설정.
                // isEnd위엔 아무것도 없으므로 null과 비교
                else if (Objects.equals(previousChildName, null) && !isMyTurn()) {
                    boolean is_end = (boolean) snapshot.getValue();
                    if (is_end) { // 에러 방지... 갱신되지 않았는데 갱신됐다고 뜨는 경우가 존재함
                        is_game_end = true;
                        if (!am_i_p1) turn++;
                        String victoryTitle = "상대방이 " + turn + "턴 만에 정답을 맞췄습니다";
                        String victoryMsg = "상대방이 입력한 숫자는 " + opponentInputNum + " 였습니다.";

                        // 확인 메시지 창을 띄움.
                        if (! MultiplayActivity.this.isFinishing()) {
                            AlertDialog.Builder msgBuilder = new AlertDialog.Builder(MultiplayActivity.this);
                            msgBuilder.setView(dialogView).setCancelable(false);
                            dialogView.setBackgroundColor(sp.getInt("btnbgbg", 0xFFFFFFFF));
                            TextView title = dialogView.findViewById(R.id.tv_title);
                            title.setTextColor(colors[11]);
                            title.setText(victoryTitle);
                            TextView message = dialogView.findViewById(R.id.tv_message);
                            message.setTextColor(colors[11]);
                            message.setText(victoryMsg);
                            Button positive = dialogView.findViewById(R.id.positive);
                            positive.setBackgroundTintList(colors[5]);
                            positive.setTextColor(colors[11]);
                            positive.setOnClickListener(v -> {
                                DB_game.child(p1_id).setValue(null); // DB에서 게임 데이터 삭제.
                                finish();
                            });

                            AlertDialog msgDialog = msgBuilder.create();
                            msgDialog.show();
                        }
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
    }

    private boolean isMyTurn(){
        if (am_i_p1){ // 방장이라면
            return whosTurn == 1;
        }
        else{
            return whosTurn == 2;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void getResultAndUpdate(){
        // 이 밑은 '함수'로 만들어야 할듯
        // 왜냐? 버튼을 누르거나, 시간 제한이 다되거나 했을때 둘다 작동해야 하므로!

        // 소모 turn 1회 증가.
        turn++;

        setInputNumValid();

        // 결과를 계산하는 코드
        int strike = 0;
        int ball = 0;
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
            mHandler.postDelayed(() -> adapter_result1.notifyDataSetChanged(), 0);
        }
        else{
            arrayList_result2.add(my_ir);
            // adapter_result2.notifyDataSetChanged();

            Handler mHandler = new Handler(Looper.getMainLooper());
            mHandler.postDelayed(() -> adapter_result2.notifyDataSetChanged(), 0);
        }


        if (ball_number == strike) { // 게임 종료. 승자만이 이 코드를 실행하게 된다.
            DB_game.child(p1_id).child("isEnd").setValue(true); // DB에 값 갱신
            is_game_end = true;

            String victoryTitle = String.valueOf(turn) + "턴 만에 승리하셨습니다!";
            String victoryMsg = "상대방이 입력한 숫자는 " + opponentInputNum + " 였습니다.";

            Handler mHandler = new Handler(Looper.getMainLooper());
            mHandler.postDelayed(() -> {
                // 확인 메시지 창을 띄움.
                if (! MultiplayActivity.this.isFinishing()){
                    AlertDialog.Builder msgBuilder = new AlertDialog.Builder(MultiplayActivity.this);
                    msgBuilder.setView(dialogView).setCancelable(false);
                    int bgColor = sp.getInt("btnbgbg", 0xFFFFFFFF);
                    int textColor = sp.getInt("btnbgtx", 0xFF000000);
                    dialogView.setBackgroundColor(bgColor);
                    TextView title = dialogView.findViewById(R.id.tv_title);
                    title.setTextColor(textColor);
                    title.setText(victoryTitle);
                    TextView message = dialogView.findViewById(R.id.tv_message);
                    message.setTextColor(textColor);
                    message.setText(victoryMsg);
                    Button positive = dialogView.findViewById(R.id.positive);
                    positive.setBackgroundTintList(ColorStateList.valueOf(bgColor));
                    positive.setTextColor(textColor);
                    positive.setOnClickListener(v -> finish());

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
        // 숫자 n개 중 입력되지 않은 값(-1) 이 있으면 처리해줘야 한다.
        for (int i = 0; i < ball_number; ++i){
            if (input_num[i] == -1){
                boolean is_dup = true;
                int randomNum = 0;

                while (is_dup){
                    randomNum = random.nextInt(10);
                    for (int j = 0; j < ball_number; ++j){
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
                myDebug("randomNum : " + randomNum);
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
                drawLinear.setVisibility(View.INVISIBLE);
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
            is_game_end = true;
            super.onBackPressed();
            finish();
        }
    }

    @Override
    public void onDestroy() {
        if (!is_game_end){
            DB_game.child(p1_id).child("isOneExited").setValue(true);
            is_game_end = true;
        }
        super.onDestroy();
    }

    private void myDebug(String str){
        System.out.println(str);
    }

}



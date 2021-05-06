package pnu.termproject.onlinenumbaseball;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

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

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game extends AppCompatActivity{
    //변수 선언
    private RadioGroup rg_number;
    private RadioButton rb_select;
    private RadioButton[] radio_btn = new RadioButton[5];
    private Button[] btn_num = new Button[10];
    private Button btn_result, btn_cancel, btn_memo;
    private TextView tv_turn;
    private Random random = new Random();
    private int strike, ball, turn;
    private ListView result_list;
    private Button[] memo_color = new Button[6];
    private Button btn_back;
    private Button btn_clear;

    private long startTime, endTime, clearTime; // 클리어 시간 측정을 위한 변수
    private long backKeyPressedTime = 0;
    private Toast toast;

    ArrayList<Point> points = new ArrayList<Point>();
    LinearLayout drawLinear, resultLinear, drawBtnLinear;
    TableLayout inputTable;
    int color;
    ArrayList<Integer> lastDraw = new ArrayList<Integer>();

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
            super.onBackPressed();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);

        //game.xml 의 값을 변수에 대입
        int[] radio_Id = {R.id.radioButton1, R.id.radioButton2, R.id.radioButton3, R.id.radioButton4, R.id.radioButton5};
        int[] btn_Id = {R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4, R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9};
        for(int i = 0; i < 5; i++) {
            radio_btn[i] = findViewById(radio_Id[i]);
        }
        for(int i = 0; i < 10; i++) {
            btn_num[i] = findViewById(btn_Id[i]);
        }
        rg_number = findViewById(R.id.rg_number);
        btn_result = findViewById(R.id.btn_result);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_memo = findViewById(R.id.btn_memo);
        btn_clear = findViewById(R.id.btn_clear);
        tv_turn = findViewById(R.id.turn_text);
        result_list = findViewById(R.id.result_ListView);
        int[] memo_color_Id = {R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6};
        for (int i = 0; i < 6; i++) {
            memo_color[i] = findViewById(memo_color_Id[i]);
        }
        btn_back = findViewById(R.id.back);
        TextView guess = findViewById(R.id.guess);

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
        tv_turn.setTextColor(colors[11]);
        tv_turn.getRootView().setBackgroundTintList(colors[5]);
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

        //결과를 나타내는 리스트들을 위한 코드
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

        //공의 개수에 따라서, 랜덤 변수를 생성하는 부분
        Intent intent = getIntent();
        int ball_number = intent.getExtras().getInt("ballNumber");
        int[] ans = new int[ball_number]; //맞춰야 할 정답
        int[] num = new int[ball_number]; //사용자가 선택한 정답
        for(int i = 0; i < ball_number; i++) {
            boolean isOverlap = false;
            while(!isOverlap) {
                ans[i] = random.nextInt(10);
                isOverlap = true;
                for(int j = 0; j < i; j++) {
                    if(ans[i] == ans[j])
                        isOverlap = false;
                }
            }
            num[i] = -1;
        }
        for(int i = ball_number; i < 5; i++)
            radio_btn[i].setVisibility(View.INVISIBLE);
        turn = 1;

        startTime = System.currentTimeMillis(); // 시간 측정 시작

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
                        num[i1] = tmp;
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
                    if(num[i1] != -1)
                        btn_num[num[i1]].setEnabled(false);
            });
        }

        //완료 버튼을 눌렀을때의 동작을 구현하는 코드
        btn_result.setOnClickListener(view -> {
            boolean triger = true;
            for(int i = 0; i < ball_number; i++){
                if(num[i] == -1)
                    triger = false;
            }
            if(triger) {
                //결과를 계산하는 코드
                strike = 0;
                ball = 0;
                for (int i = 0; i < ball_number; i++) {
                    if (ans[i] == num[i])
                        strike++;
                    else {
                        for (int j = 0; j < ball_number; j++) {
                            if (ans[i] == num[j])
                                ball++;
                        }
                    }
                }

                //진행상황을 기록하는 코드
                String result = "";
                for (int i = 0; i < ball_number; i++)
                    result += String.valueOf(num[i]);
                result += "\n" + strike + "S" + " " + ball + "B";
                data.add(result);
                adapter.notifyDataSetChanged();

                if (ball_number == strike) {//종료하는 코드
                    endTime = System.currentTimeMillis(); // 시간 측정 종료
                    clearTime = (endTime - startTime) / 1000;

                    // 랭킹 업데이트 & 결과 출력해주는 Activity로 전환
                    Intent intent2 = new Intent(getApplicationContext(), SingleRankingUpdateActivity.class);
                    intent2.putExtra("clear-time", clearTime);
                    intent2.putExtra("clear-turn", turn);
                    intent2.putExtra("ball-number", ball_number);
                    startActivity(intent2);
                    finish();
                } else {//다음 회를 준비하기 위한 코드
                    turn++;
                    String turnStr = "Turn : " + turn;
                    tv_turn.setText(turnStr);
                    rb_select = radio_btn[0];
                    rb_select.setChecked(true);
                    for (int i = 0; i < ball_number; i++) {
                        num[i] = -1;
                        radio_btn[i].setText("");
                    }
                    for(int i = 0; i < 10; i++){
                        btn_num[i].setEnabled(true);
                    }
                }
            }
        });

        // 취소 버튼 눌렀을 때
        btn_cancel.setOnClickListener(v -> {
            rb_select = radio_btn[0];
            rb_select.setChecked(true);
            for (int i = 0; i < ball_number; i++) {
                num[i] = -1;
                radio_btn[i].setText("");
            }
            for(int i = 0; i < 10; i++){
                btn_num[i].setEnabled(true);
            }
        });

        //아래로는 메모기능을 위한 코드임
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

        resultLinear = findViewById(R.id.result_linear);
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
}

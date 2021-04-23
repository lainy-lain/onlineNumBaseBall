package pnu.termproject.onlinenumbaseball;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game extends AppCompatActivity{
    //변수 선언
    private RadioGroup rg_number;
    private RadioButton rb_select;
    private RadioButton[] radio_btn = new RadioButton[5];
    private Button[] btn_num = new Button[10];
    private Button btn_result;
    private TextView answer, tv_turn;
    private Random random = new Random();
    private int strike, ball, turn;
    private ListView result_list;

    private long startTime, endTime, clearTime; // 클리어 시간 측정을 위한 변수

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
        rb_select = findViewById(R.id.radioButton1);
        btn_result = findViewById(R.id.btn_result);
        answer = findViewById(R.id.textView7);
        tv_turn = findViewById(R.id.turn_text);
        result_list = findViewById(R.id.result_ListView);

        //결과를 나타내는 리스트들을 위한 코드
        List<String> data = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data);
        result_list.setAdapter(adapter);

        //공의 개수에 따라서, 랜덤 변수를 생성하는 부분
        Intent intent = getIntent();
        int ball_number = intent.getExtras().getInt("ballNumber");
        int[] ans = new int[ball_number]; //맞춰야 할 정답
        int[] num = new int[ball_number]; //사용자가 선택한 정답
        for(int i = 0; i < ball_number; i++) {
            boolean isOverlap = false;
            while(isOverlap == false) {
                ans[i] = random.nextInt(10);
                isOverlap = true;
                for(int j = 0; j < i; j++) {
                    if(ans[i] == ans[j])
                        isOverlap = false;
                }
            }
            num[i] = 0;
        }
        for(int i = ball_number; i < 5; i++)
            radio_btn[i].setVisibility(View.INVISIBLE);
        turn = 1;

        //디버깅을 위해서 임시로 추가한 코드, 추후에 삭제할 예정
        radio_btn[0].setSelected(true);
        String ansString;
        ansString = "랜덤 숫자 :";
        for(int i = 0; i < ball_number; i++) {
            ansString += " " + String.valueOf(ans[i]);
        }
        answer.setText(ansString);

        startTime = System.currentTimeMillis(); // 시간 측정 시작

        //라디오 버튼(몇번째 공을 선택했는지 구별)을 눌렀을때의 동작을 구현하는 코드
        rg_number.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                rb_select = findViewById(i);
            }
        });

        //숫자 버튼을 눌렀을때의 동작을 구현하는 코드
        for(int i = 0; i < 10; i++)
        {
            int tmp = i;
            btn_num[tmp].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    rb_select.setText(String.valueOf(tmp));
                    for(int i = 0; i < ball_number; i++) {
                        if(rb_select == radio_btn[i])
                            num[i] = tmp;
                    }
                }
            });
        }

        //완료 버튼을 눌렀을때의 동작을 구현하는 코드
        btn_result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //결과를 계산하는 코드
                strike = 0;
                ball = 0;
                for(int i = 0; i < ball_number; i++) {
                    if(ans[i] == num[i])
                        strike++;
                    else {
                        for(int j = 0; j < ball_number; j++) {
                            if(ans[i] == num[j])
                                ball++;
                        }
                    }
                }

                //결과를 출력하는 코드
                String result = String.valueOf(turn) + "회" + "\t\t" + "입력숫자 : ";
                for(int i = 0; i < ball_number; i++)
                    result += String.valueOf(num[i]) + " ";
                result += "\t\t" + "S : " + String.valueOf(strike) + "\t\t" + "B : " + String.valueOf(ball);
                data.add(result);
                adapter.notifyDataSetChanged();

                if(ball_number == strike) {
                    //종료하는 코드
                    endTime = System.currentTimeMillis(); // 시간 측정 종료
                    clearTime = (endTime - startTime) / 1000;

                    String finalMessage = "축하합니다!\n" + String.valueOf(clearTime / 60) + "분 " + String.valueOf(clearTime % 60) + "초의 시간동안\n"
                            +String.valueOf(turn) +"회 만에 정답을 맞추셨습니다!";
                    //Toast.makeText(getApplicationContext(), finalMessage, Toast.LENGTH_LONG).show();

                    // 랭킹 업데이트 & 결과 출력해주는 Activity로 전환
                    Intent intent2 = new Intent(getApplicationContext(), SingleRankingUpdateActivity.class);
                    intent2.putExtra("clear-time", clearTime);
                    intent2.putExtra("clear-turn", turn);
                    startActivity(intent2);
                    finish();

                    /*
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 1400);

                     */
               }
                else {
                    //다음 회를 준비하기 위한 코드
                    turn++;
                    String turnStr = "Turn : " + String.valueOf(turn);
                    tv_turn.setText(turnStr);
                }
            }
        });
    }
}

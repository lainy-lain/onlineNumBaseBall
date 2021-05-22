package pnu.termproject.onlinenumbaseball;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/*
   방금 플레이한 게임 정보를 토대로 DB 값 갱신 후
   방금 플레이한 게임 정보와 갱신된 DB값을 화면에 표시하는 Activity
*/
public class SingleRankingUpdateActivity extends AppCompatActivity {
    private FirebaseUser currentUser;
    private DatabaseReference mDatabase;
    private long clearTime;
    private int clearTurn;
    private int nBall;
    private TextView tv_avgTime;
    private TextView tv_avgTurn;
    private String mode;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_ranking_update);

        // 이전 Activity로부터 전달받은 값 가져오기
        Intent intent = getIntent();
        clearTime = intent.getLongExtra("clear-time", 0);
        clearTurn = intent.getIntExtra("clear-turn", 0);
        nBall = intent.getIntExtra("ball-number", 0);

        // 전역변수 값 할당
        tv_avgTime = findViewById(R.id.tv_avgTime); // 이것들은 DB에 저장된 값들을 화면에 표시해주기 위한 textView이다.
        tv_avgTurn = findViewById(R.id.tv_avgTurn); // 그렇기 때문에, setText는 여기(onCreate)가 아니라, updateDBwithClass() 안에서 DB 업데이트하면서 해줌.

        currentUser = FirebaseAuth.getInstance().getCurrentUser(); // 로그인 안돼있으면 null이다.
        if (currentUser != null){ // 로그인 되어있는 경우
            mDatabase = FirebaseDatabase.getInstance().getReference();
            updateUser(); // 유저 DB 정보 업데이트
        }
        else{ // 로그인 안돼있는 경우
            tv_avgTime.setText("로그인이 필요한 기능입니다");
            tv_avgTurn.setText("로그인이 필요한 기능입니다");
        }

        // 지역변수 정의 및 값 할당 (플레이 정보 표시를 위한 지역 변수)
        TextView tv_clearTime = findViewById(R.id.tv_clearTime);
        TextView tv_clearTurn = findViewById(R.id.tv_clearTurn);
        Button btn_gotoLobby = findViewById(R.id.btn_gotoLobby);

        String clearTimeStr = (clearTime / 60) + "분 " + (clearTime % 60) + "초";
        tv_clearTime.setText(clearTimeStr);
        tv_clearTurn.setText(String.valueOf(clearTurn));

        SharedPreferences sp = getSharedPreferences("setting", MODE_PRIVATE);
        ColorStateList[] colors = {ColorStateList.valueOf(sp.getInt("btn1bg", 0xFFFFEB3B)),
                ColorStateList.valueOf(sp.getInt("btnbgbg", 0xFFFFFFFF)),
                ColorStateList.valueOf(sp.getInt("btn1tx", 0xFF000000)),
                ColorStateList.valueOf(sp.getInt("btnbgtx", 0xFF000000))
        };
        btn_gotoLobby.setBackgroundTintList(colors[0]);
        btn_gotoLobby.setTextColor(colors[2]);
        btn_gotoLobby.getRootView().setBackgroundTintList(colors[1]);
        tv_avgTime.setTextColor(colors[3]);
        tv_avgTurn.setTextColor(colors[3]);
        tv_clearTime.setTextColor(colors[3]);
        tv_clearTurn.setTextColor(colors[3]);
        ((TextView)findViewById(R.id.tv_gameInfo)).setTextColor(colors[3]);
        ((TextView)findViewById(R.id.tv_dbInfo)).setTextColor(colors[3]);
        ((TextView)findViewById(R.id.tv_time)).setTextColor(colors[3]);
        ((TextView)findViewById(R.id.tv_turn)).setTextColor(colors[3]);
        ((TextView)findViewById(R.id.tv_dbTime)).setTextColor(colors[3]);
        ((TextView)findViewById(R.id.tv_dbTurn)).setTextColor(colors[3]);

        btn_gotoLobby.setOnClickListener(v -> finish());
    }

    private void updateUser(){
        switch (nBall){
            case 3:
                mode = "users_3b";
                break;
            case 4:
                mode = "users_4b";
                break;
            case 5:
                mode = "users_5b";
                break;
            default:
                mode = "incorrect";
                break;
        }

        // mode에 대한 DB reference를 가져온다
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(mode);
        // DB에서 현재 유저에 대한 데이터가 있는지 찾는다
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = null;
                boolean is_find = false;

                for (DataSnapshot ds : snapshot.getChildren()){
                    if (ds.hasChild("userId") && currentUser.getUid().equals(ds.child("userId").getValue())){
                        user = ds.getValue(User.class);
                        is_find = true;
                        break;
                    }
                }

                if (is_find){ // DB에 기존 데이터가 있는 경우
                    // manipulate value
                    double meanTime = user.getMeanTime();
                    double meanTurn = user.getMeanTurn();
                    long playCount = user.getPlayCount();
                    meanTime = meanTime * ((double)playCount / (playCount + 1)) + (double)clearTime / (playCount + 1);
                    meanTurn = meanTurn * ((double)playCount / (playCount + 1)) + (double)clearTurn / (playCount + 1);
                    playCount++;

                    user.setMeanTime(meanTime);
                    user.setMeanTurn(meanTurn);
                    user.setAbility(meanTime + meanTurn);
                    user.setPlayCount(playCount);

                    // DB에 값 갱신 (class 이용)
                    updateDBwithClass(user);
                }
                else{ // DB에 데이터가 없음. 새로 등록해야함.
                    registerNewUser();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void registerNewUser(){
        User user = new User(currentUser.getUid(), currentUser.getDisplayName());
        user.setUserProfile(String.valueOf(currentUser.getPhotoUrl()));
        user.setMeanTime(clearTime);
        user.setMeanTurn(clearTurn);
        user.setAbility(clearTime + clearTurn);

        // DB에 값 갱신 (class 이용)
        updateDBwithClass(user);
    }

    @SuppressLint("DefaultLocale")
    private void updateDBwithClass(User user){
        // DB에 값 쓰기 (class 이용)
        mDatabase.child(mode).child(currentUser.getUid()).setValue(user)
                .addOnSuccessListener(aVoid -> Toast.makeText(SingleRankingUpdateActivity.this, "DB 업데이트 성공", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(SingleRankingUpdateActivity.this, "에러 발생. DB 업데이트 실패", Toast.LENGTH_SHORT).show());

        // 화면에 기록 보여주기 위한 코드. DB와는 관련없음.
        String meanTimeStr = ((long)user.getMeanTime() / 60) + "분 " + ((long)user.getMeanTime() % 60) + "초";
        tv_avgTime.setText(meanTimeStr);
        tv_avgTurn.setText(String.format("%.2f", user.getMeanTurn()));
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        finish();
    }
}
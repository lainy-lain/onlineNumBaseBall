package pnu.termproject.onlinenumbaseball;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
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
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // 이것들은 DB에 저장된 값들을 화면에 표시해주기 위한 textView이다.
        // 그렇기 때문에, setText는 여기(onCreate)가 아니라, updateDBwithClass() 안에서 DB 업데이트하면서 해줌.
        tv_avgTime = findViewById(R.id.tv_avgTime);
        tv_avgTurn = findViewById(R.id.tv_avgTurn);

        updateUser(); // 유저 DB 정보 업데이트
        //Toast.makeText(getApplicationContext(), "Come in", Toast.LENGTH_SHORT).show(); // DEBUG

        // 지역변수 정의 및 값 할당 (플레이 정보 표시를 위한 지역 변수)
        TextView tv_clearTime = findViewById(R.id.tv_clearTime);
        TextView tv_clearTurn = findViewById(R.id.tv_clearTurn);
        Button btn_gotoLobby = findViewById(R.id.btn_gotoLobby);

        String clearTimeStr = String.valueOf(clearTime / 60) + "분 " + String.valueOf(clearTime % 60) + "초";
        tv_clearTime.setText(clearTimeStr);
        tv_clearTurn.setText(String.valueOf(clearTurn));

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

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(mode);
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

                    // DB에 값 갱신
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

        // DB에 값 갱신
        updateDBwithClass(user);
    }

    private void updateDBwithClass(User user){
        mDatabase.child(mode).child(currentUser.getUid()).setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SingleRankingUpdateActivity.this, "DB 업데이트 성공", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SingleRankingUpdateActivity.this, "에러 발생. DB 업데이트 실패", Toast.LENGTH_SHORT).show();
                    }
                });

        String meanTimeStr = String.valueOf((long)user.getMeanTime() / 60) + "분 " + String.valueOf((long)user.getMeanTime() % 60) + "초";
        tv_avgTime.setText(meanTimeStr);
        tv_avgTurn.setText(String.valueOf(user.getMeanTurn()));
    }

    @Override
    public void onBackPressed(){
        finish();
        super.onBackPressed();
    }
}
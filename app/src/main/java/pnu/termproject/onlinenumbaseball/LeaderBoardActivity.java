package pnu.termproject.onlinenumbaseball;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class LeaderBoardActivity extends AppCompatActivity {

    private RecyclerView recyclerView_ranking;
    private RecyclerView recyclerView_myInfo;
    private RecyclerView.Adapter adapter_ranking;
    private RecyclerView.Adapter adapter_myInfo;
    private RecyclerView.LayoutManager layoutManager_ranking;
    private RecyclerView.LayoutManager layoutManager_myInfo;
    private ArrayList<User> arrayList_ranking;
    private ArrayList<User> arrayList_myInfo;
    private DatabaseReference databaseReference;
    private TextView tv_lbMode;
    private FirebaseUser currentUser;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_board);

        recyclerView_ranking = findViewById(R.id.recyclerView_ranking);
        recyclerView_myInfo = findViewById(R.id.recyclerView_myInfo);
        recyclerView_ranking.setHasFixedSize(true);
        recyclerView_myInfo.setHasFixedSize(true);
        layoutManager_ranking = new LinearLayoutManager(this);
        layoutManager_myInfo = new LinearLayoutManager(this);
        recyclerView_ranking.setLayoutManager(layoutManager_ranking);
        recyclerView_myInfo.setLayoutManager(layoutManager_myInfo);
        arrayList_ranking = new ArrayList<>();
        arrayList_myInfo = new ArrayList<>();

        Button btn_3Brank = findViewById(R.id.btn_3Brank);
        Button btn_4Brank = findViewById(R.id.btn_4Brank);
        Button btn_5Brank = findViewById(R.id.btn_5Brank);
        tv_lbMode = findViewById(R.id.tv_lbMode);
        TextView tv_myRankInfo = findViewById(R.id.tv_myRankInfo);

        btn_3Brank.setOnClickListener(v -> getRanking(3));
        btn_4Brank.setOnClickListener(v -> getRanking(4));
        btn_5Brank.setOnClickListener(v -> getRanking(5));

        getRanking(3); // 기본적으로 '3 Ball'의 Ranking을 보여줌.

        SharedPreferences sp = getSharedPreferences("setting", MODE_PRIVATE);
        ColorStateList[] colors = {ColorStateList.valueOf(sp.getInt("btn1bg", 0xFFFFEB3B)),
                ColorStateList.valueOf(sp.getInt("btn2bg", 0xFFCDDC39)),
                ColorStateList.valueOf(sp.getInt("btn3bg", 0xFF8BC34A)),
                ColorStateList.valueOf(sp.getInt("btn1tx", 0xFF000000)),
                ColorStateList.valueOf(sp.getInt("btn2tx", 0xFF000000)),
                ColorStateList.valueOf(sp.getInt("btn3tx", 0xFFFFFFFF)),
                ColorStateList.valueOf(sp.getInt("btnbgbg", 0xFFFFFFFF)),
                ColorStateList.valueOf(sp.getInt("btnbgtx", 0xFF000000))
        };
        btn_3Brank.setBackgroundTintList(colors[0]);
        btn_3Brank.setTextColor(colors[3]);
        btn_4Brank.setBackgroundTintList(colors[1]);
        btn_4Brank.setTextColor(colors[4]);
        btn_5Brank.setBackgroundTintList(colors[2]);
        btn_5Brank.setTextColor(colors[5]);
        int bgColor = sp.getInt("btnbgbg", 0xFFFFFFFF);
        recyclerView_ranking.setBackgroundColor(bgColor);
        recyclerView_myInfo.setBackgroundColor(bgColor);
        tv_lbMode.setBackgroundColor(bgColor);
        tv_myRankInfo.setBackgroundColor(bgColor);
        tv_lbMode.setTextColor(colors[7]);
        tv_myRankInfo.setTextColor(colors[7]);
        tv_lbMode.getRootView().setBackgroundTintList(colors[6]);
        int radiusChecked = sp.getInt("radius", 0);
        int cornerRadius = (radiusChecked + 1) * 8;
        ((MaterialButton)btn_3Brank).setCornerRadius(cornerRadius);
        ((MaterialButton)btn_4Brank).setCornerRadius(cornerRadius);
        ((MaterialButton)btn_5Brank).setCornerRadius(cornerRadius);
    }

    private void getRanking(int nBall){
        String mode, str;
        switch (nBall){
            case 3:
                mode = "users_3b";
                str = "3 Ball Mode Ranking";
                break;
            case 4:
                mode = "users_4b";
                str = "4 Ball Mode Ranking";
                break;
            case 5:
                mode = "users_5b";
                str = "5 Ball Mode Ranking";
                break;
            default:
                mode = "incorrect";
                str = "incorrect";
                break;
        }

        tv_lbMode.setText(str);
        databaseReference = FirebaseDatabase.getInstance().getReference(mode); // DB Table 연결


        // 내 정보를 표시하기 위한 부분. 로그인 돼있는 경우와 안돼있는 경우를 구분함.
        // ******************** BEGIN **********************
        SharedPreferences sp = getSharedPreferences("setting", MODE_PRIVATE);
        ColorStateList tx = ColorStateList.valueOf(sp.getInt("btnbgtx", 0xFF000000));
        adapter_myInfo = new MyInfoAdapter(arrayList_myInfo, this, tx);
        recyclerView_myInfo.setAdapter(adapter_myInfo);

        currentUser = FirebaseAuth.getInstance().getCurrentUser(); // 로그인 안돼있으면 null이다
        if (currentUser != null){ // 로그인 되어있는 경우
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    arrayList_myInfo.clear();
                    User user = null;
                    boolean is_find = false;

                    for (DataSnapshot ds : snapshot.getChildren()){
                        if (ds.hasChild("userId") && currentUser.getUid().equals(ds.child("userId").getValue())){
                            user = ds.getValue(User.class);
                            is_find = true;
                            break;
                        }
                    }

                    if (!is_find){ // 회원가입했지만 아직 플레이하지 않아 DB에 정보가 없는 경우.
                        user = new User();
                        user.setUserProfile(String.valueOf(currentUser.getPhotoUrl()));
                        user.setUserName("아직 플레이 기록이 존재하지 않습니다");
                        user.setMeanTime(0);
                        user.setMeanTurn(0);
                    }
                    arrayList_myInfo.add(user);
                    adapter_myInfo.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else{ // 로그인 안돼있는 경우
            arrayList_myInfo.clear();
            User user = new User();
            user.setUserProfile("https://static.wikia.nocookie.net/fallout/images/c/c0/VaultBoyFO3.png/revision/latest/scale-to-width-down/181?cb=20110809182235");
            user.setUserName("로그인 후 이용할 수 있습니다");
            user.setMeanTime(0);
            user.setMeanTurn(0);

            arrayList_myInfo.add(user);
            adapter_myInfo.notifyDataSetChanged();
        }
        // ********************* END *********************


        // 전체 Ranking을 보여주기 위한 코드
        adapter_ranking = new RankingAdapter(arrayList_ranking, this, tx);
        recyclerView_ranking.setAdapter(adapter_ranking);

        Query query = databaseReference.orderByChild("ability");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayList_ranking.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class); // User 객체에 DB Data를 담는다
                    arrayList_ranking.add(user);
                }
                adapter_ranking.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // 뒤로가기 버튼을 누르면 액티비티가 종료됨.
    public void onBackPressed(){ // 뒤로가기 버튼이 눌렸을 때
        super.onBackPressed();
        finish();
    }
}
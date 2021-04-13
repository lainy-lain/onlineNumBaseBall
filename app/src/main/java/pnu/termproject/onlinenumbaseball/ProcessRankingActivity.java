package pnu.termproject.onlinenumbaseball;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProcessRankingActivity extends AppCompatActivity {
    private FirebaseUser currentUser;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processranking);

        // 변수 값 할당
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserUid = currentUser.getUid();
        String currentUserName = currentUser.getDisplayName();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();


        writeNewUser(currentUserUid, currentUserName, 15, 8);
        Toast.makeText(ProcessRankingActivity.this, "DB 입력 성공", Toast.LENGTH_SHORT).show(); // 토스트 문자 짧게 출력
        finish();

    }

    private void writeNewUser(String userId, String userName, double meanTime, double meanTurn){
        User user = new User(userId, userName);
        user.setUserProfile(String.valueOf(currentUser.getPhotoUrl()));
        user.setMeanTime(meanTime);
        user.setMeanTurn(meanTurn);
        user.setAbility(meanTime + meanTurn);

        mDatabase.child("users").child(userId).setValue(user);
    }
}
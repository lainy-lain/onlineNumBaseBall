package pnu.termproject.onlinenumbaseball;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class LobbyActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_nickname; // 닉네임을 나타내는 Text
    private ImageView iv_profile; // 프로필 사진을 나타내는 Image

    private Button btn_logout, btn_revoke; // 로그아웃 버튼, 탈퇴 버튼


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        Intent intent = getIntent();
        String nickName = intent.getStringExtra("nickName"); // MainActivity로부터 닉네임을 전달받음
        String photoUrl = intent.getStringExtra("photoUrl"); // MainActivity로부터 profile URL 전달받음

        tv_nickname = findViewById(R.id.tv_nickname);
        tv_nickname.setText(nickName); // 닉네임 text를 Text view에 세팅

        iv_profile = findViewById(R.id.iv_profile);
        // Image Load를 도와주는 Glide 이용
        Glide.with(this).load(photoUrl).into(iv_profile); // profile URL을 Image View에 세팅


        btn_logout = findViewById(R.id.btn_logout);
        btn_revoke = findViewById(R.id.btn_revoke);
        btn_logout.setOnClickListener(this); // 이 class에 구현돼있는 onClick()을 이용해서 clickListener 설정
        btn_revoke.setOnClickListener(this); // 바로 밑에 onClick()이 구현돼있음

    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.btn_logout:
                signOut();
                finish();
                break;
            case R.id.btn_revoke:
                revokeAccess();
                break;
            default:
                break;
        }
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut(); // Firebase에서 로그아웃

        // Google에서 로그아웃. 이게 있어야 재로그인할때 계정을 선택할 수 있음.
        AuthUI.getInstance()
                .signOut(LobbyActivity.this)
                .addOnCompleteListener(new OnCompleteListener<Void>(){

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // do something here
                    }
                });

        Toast.makeText(LobbyActivity.this, "성공적으로 로그아웃 되었습니다", Toast.LENGTH_SHORT).show(); // 토스트 문자 짧게 출력
    }

    private void revokeAccess() {
        // 확인 메시지 창을 띄움.
        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(LobbyActivity.this)
                .setTitle("정말로 탈퇴하시겠습니까?")
                .setMessage("탈퇴 후 데이터 복구는 불가능합니다")
                .setPositiveButton("탈퇴", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { // 탈퇴 버튼을 눌렀을 경우
                        FirebaseAuth.getInstance().getCurrentUser().delete(); // 탈퇴 처리

                        // Google에서 로그아웃. 이게 있어야 재로그인할때 계정을 선택할 수 있음.
                        AuthUI.getInstance()
                                .signOut(LobbyActivity.this)
                                .addOnCompleteListener(new OnCompleteListener<Void>(){

                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        // do something here
                                    }
                                });

                        Toast.makeText(LobbyActivity.this, "탈퇴 처리 되었습니다", Toast.LENGTH_SHORT).show(); // 토스트 문자 짧게 출력
                        finish(); // 액티비티 종료. login activity로 넘어감.
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 취소 버튼 누르면 아무것도 수행하지 않음.
                    }
                });

        AlertDialog msgDialog = msgBuilder.create();
        msgDialog.show();
    }

}
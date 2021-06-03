package pnu.termproject.onlinenumbaseball;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.material.button.MaterialButton;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private TextView tv_nickname; // 닉네임을 나타내는 Text
    private ImageView iv_profile; // 프로필 사진을 나타내는 Image

    private SignInButton btn_signin_google; // 구글 로그인 버튼
    private FirebaseAuth auth; // Firebase 인증 객체
    public GoogleApiClient googleApiClient; // Google API Client 객체
    private static final int REQ_SIGN_GOOGLE = 100; // 구글 로그인 결과 코드

    private boolean loginSuccess;
    private static final String[] PREFERENCES = {
            "btn1bg", "btn2bg", "btn3bg", "btn4bg", "btn5bg", "btnbgbg",
            "btn1tx", "btn2tx", "btn3tx", "btn4tx", "btn5tx", "btnbgtx", "radius"
    };

    ColorStateList[] colors = new ColorStateList[12];

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, ForcedTermination.class));
        setContentView(R.layout.activity_main);

        RadioGroup ballCount = findViewById(R.id.ball_count);
        Button play_btn = findViewById(R.id.play_btn);
        Button single_btn = findViewById(R.id.single_btn);
        Button multi_btn = findViewById(R.id.multi_btn);
        Button rank_btn = findViewById(R.id.rank_btn);
        Button set_btn = findViewById(R.id.set_btn);

        // 로그인(Sign in) 버튼에 대한 기본적인 옵션 설정
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // API Client 객체 초기화
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();

        auth = FirebaseAuth.getInstance(); // Firebase 인증 객체 초기화

        btn_signin_google = findViewById(R.id.btn_signin_google);
        // 구글 로그인 버튼을 누르면 이곳이 수행됨
        btn_signin_google.setOnClickListener(v -> {
            // Google Login Intent로 넘어가서 인증 후 MainActivity로 다시 돌아옴.
            Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
            startActivityForResult(intent, REQ_SIGN_GOOGLE); // 이것의 Result가 onActivityResult로 전달됨
        });

        Intent intent = getIntent();
        String nickName = intent.getStringExtra("nickName"); // MainActivity로부터 닉네임을 전달받음
        String photoUrl = intent.getStringExtra("photoUrl"); // MainActivity로부터 profile URL 전달받음
        loginSuccess = intent.getBooleanExtra("success", false);
        if (loginSuccess) {
            btn_signin_google.setVisibility(INVISIBLE);
            findViewById(R.id.login_manage).setVisibility(VISIBLE);
        }
        tv_nickname = findViewById(R.id.tv_nickname);
        tv_nickname.setText(nickName); // 닉네임 text를 Text view에 세팅
        iv_profile = findViewById(R.id.iv_profile);
        // Image Load를 도와주는 Glide 이용
        Glide.with(this).load(photoUrl).into(iv_profile); // profile URL을 Image View에 세팅

        // 설정한 색들 버튼에 세팅
        SharedPreferences sp = getSharedPreferences("setting", MODE_PRIVATE);
        colors[0] = ColorStateList.valueOf(sp.getInt(PREFERENCES[0], 0xFFFFEB3B));
        colors[1] = ColorStateList.valueOf(sp.getInt(PREFERENCES[1], 0xFFCDDC39));
        colors[2] = ColorStateList.valueOf(sp.getInt(PREFERENCES[2], 0xFF8BC34A));
        colors[3] = ColorStateList.valueOf(sp.getInt(PREFERENCES[3], 0xFF00BCD4));
        colors[4] = ColorStateList.valueOf(sp.getInt(PREFERENCES[4], 0xFF03A9F4));
        colors[5] = ColorStateList.valueOf(sp.getInt(PREFERENCES[5], 0xFFFFFFFF));
        colors[6] = ColorStateList.valueOf(sp.getInt(PREFERENCES[6], 0xFF000000));
        colors[7] = ColorStateList.valueOf(sp.getInt(PREFERENCES[7], 0xFF000000));
        colors[8] = ColorStateList.valueOf(sp.getInt(PREFERENCES[8], 0xFFFFFFFF));
        colors[9] = ColorStateList.valueOf(sp.getInt(PREFERENCES[9], 0xFFFFFFFF));
        colors[10] = ColorStateList.valueOf(sp.getInt(PREFERENCES[10], 0xFFFFFFFF));
        colors[11] = ColorStateList.valueOf(sp.getInt(PREFERENCES[11], 0xFF000000));
        play_btn.setBackgroundTintList(colors[0]);
        play_btn.setTextColor(colors[6]);
        single_btn.setBackgroundTintList(colors[1]);
        single_btn.setTextColor(colors[7]);
        multi_btn.setBackgroundTintList(colors[2]);
        multi_btn.setTextColor(colors[8]);
        rank_btn.setBackgroundTintList(colors[3]);
        rank_btn.setTextColor(colors[9]);
        set_btn.setBackgroundTintList(colors[4]);
        set_btn.setTextColor(colors[10]);
        set_btn.getRootView().setBackgroundTintList(colors[5]);
        tv_nickname.setTextColor(colors[11]);
        ((TextView)findViewById(R.id.guide)).setTextColor(colors[11]);
        ((RadioButton)findViewById(R.id.three_ball)).setTextColor(colors[11]);
        ((RadioButton)findViewById(R.id.four_ball)).setTextColor(colors[11]);
        ((RadioButton)findViewById(R.id.five_ball)).setTextColor(colors[11]);
        int radiusChecked = sp.getInt(PREFERENCES[12], 0);
        int cornerRadius = (radiusChecked + 1) * 8;
        ((MaterialButton)play_btn).setCornerRadius(cornerRadius);
        ((MaterialButton)single_btn).setCornerRadius(cornerRadius);
        ((MaterialButton)multi_btn).setCornerRadius(cornerRadius);
        ((MaterialButton)rank_btn).setCornerRadius(cornerRadius);
        ((MaterialButton)set_btn).setCornerRadius(cornerRadius);
        Button logout_btn = findViewById(R.id.logout_btn);
        Button revoke_btn = findViewById(R.id.revoke_btn);
        ((MaterialButton)logout_btn).setCornerRadius(cornerRadius);
        ((MaterialButton)revoke_btn).setCornerRadius(cornerRadius);

        // logout, 탈퇴, 랭킹 버튼 눌렀을때 어떤 method를 수행할지를 설정하는 부분 (setOnClickListener)

        logout_btn.setOnClickListener(v -> signOut());
        revoke_btn.setOnClickListener(v -> revokeAccess());
        rank_btn.setOnClickListener(v -> gotoRanking());

        play_btn.setOnClickListener(new View.OnClickListener() {
            boolean state = false;
            public void onClick(View v) {
                if (!state) {
                    ObjectAnimator.ofFloat(single_btn, "translationY", v.getHeight()).start();
                    ObjectAnimator.ofFloat(multi_btn, "translationY", v.getHeight() * 2).start();
                    if (loginSuccess) {
                        multi_btn.setEnabled(true);
                    }
                }
                else {
                    ObjectAnimator.ofFloat(single_btn, "translationY", 0).start();
                    ObjectAnimator.ofFloat(multi_btn, "translationY", 0).start();
                }
                state = !state;
            }
        });

        //수정한 부분입니다! 2021-04-13 by 해원
        final int[] ballNumber = new int[1];
        int[] radio_Id = {R.id.three_ball, R.id.four_ball, R.id.five_ball};
        ballCount.setOnCheckedChangeListener((radioGroup, i) -> {
            single_btn.setEnabled(true);
            for(int j = 0; j < 3; j++){
                if(i == radio_Id[j]) {
                    ballNumber[0] = 3 + j;
                }
            }
        });

        single_btn.setOnClickListener(v -> {
            Intent toGame = new Intent(getApplicationContext(), Game.class);
            toGame.putExtra("ballNumber", ballNumber[0]);
            startActivity(toGame);
        });

        multi_btn.setOnClickListener(v -> {
            Intent toMulGame = new Intent(getApplicationContext(), MultiList.class);
            startActivity(toMulGame);
        });
        //수정한 부분의 끝입니다!

        set_btn.setOnClickListener(v -> {
            Intent toSetting = new Intent(getApplicationContext(), Setting.class);
            startActivityForResult(toSetting, 0);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { // Google Login Activity 의 결과를 받는 곳.
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_SIGN_GOOGLE){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()){ // 인증 결과가 성공적인 경우
                GoogleSignInAccount account = result.getSignInAccount(); // account 객체는 구글 계정 정보를 모두 담게 됨.
                resultLogin(account); // 로그인 결과값을 출력하는 메소드
            }
        }

        //색깔 바꾼 거 즉시 적용
        if (requestCode == 0 && resultCode == RESULT_OK) {
            colors[0] = ColorStateList.valueOf(data.getExtras().getInt(PREFERENCES[0], 0xFFFFEB3B));
            colors[1] = ColorStateList.valueOf(data.getExtras().getInt(PREFERENCES[1], 0xFFCDDC39));
            colors[2] = ColorStateList.valueOf(data.getExtras().getInt(PREFERENCES[2], 0xFF8BC34A));
            colors[3] = ColorStateList.valueOf(data.getExtras().getInt(PREFERENCES[3], 0xFF00BCD4));
            colors[4] = ColorStateList.valueOf(data.getExtras().getInt(PREFERENCES[4], 0xFF03A9F4));
            colors[5] = ColorStateList.valueOf(data.getExtras().getInt(PREFERENCES[5], 0xFF000000));
            colors[6] = ColorStateList.valueOf(data.getExtras().getInt(PREFERENCES[6], 0xFF000000));
            colors[7] = ColorStateList.valueOf(data.getExtras().getInt(PREFERENCES[7], 0xFF000000));
            colors[8] = ColorStateList.valueOf(data.getExtras().getInt(PREFERENCES[8], 0xFFFFFFFF));
            colors[9] = ColorStateList.valueOf(data.getExtras().getInt(PREFERENCES[9], 0xFFFFFFFF));
            colors[10] = ColorStateList.valueOf(data.getExtras().getInt(PREFERENCES[10], 0xFFFFFFFF));
            colors[11] = ColorStateList.valueOf(data.getExtras().getInt(PREFERENCES[11], 0xFFFFFFFF));
            View[] views = {
                    findViewById(R.id.play_btn), findViewById(R.id.single_btn), findViewById(R.id.multi_btn),
                    findViewById(R.id.rank_btn), findViewById(R.id.set_btn),
                    findViewById(R.id.tv_nickname), findViewById(R.id.guide),
                    findViewById(R.id.three_ball), findViewById(R.id.four_ball), findViewById(R.id.five_ball)
            };
            views[0].setBackgroundTintList(colors[0]);
            ((Button)views[0]).setTextColor(colors[6]);
            views[1].setBackgroundTintList(colors[1]);
            ((Button)views[1]).setTextColor(colors[7]);
            views[2].setBackgroundTintList(colors[2]);
            ((Button)views[2]).setTextColor(colors[8]);
            views[3].setBackgroundTintList(colors[3]);
            ((Button)views[3]).setTextColor(colors[9]);
            views[4].setBackgroundTintList(colors[4]);
            ((Button)views[4]).setTextColor(colors[10]);
            views[5].getRootView().setBackgroundTintList(colors[5]);
            ((TextView)views[5]).setTextColor(colors[11]);
            ((TextView)views[6]).setTextColor(colors[11]);
            ((RadioButton)views[7]).setTextColor(colors[11]);
            ((RadioButton)views[8]).setTextColor(colors[11]);
            ((RadioButton)views[9]).setTextColor(colors[11]);
            int radiusChecked = data.getExtras().getInt(PREFERENCES[12], 0);
            int cornerRadius = (radiusChecked + 1) * 8;
            ((MaterialButton)views[0]).setCornerRadius(cornerRadius);
            ((MaterialButton)views[1]).setCornerRadius(cornerRadius);
            ((MaterialButton)views[2]).setCornerRadius(cornerRadius);
            ((MaterialButton)views[3]).setCornerRadius(cornerRadius);
            ((MaterialButton)views[4]).setCornerRadius(cornerRadius);
        }
    }

    private void resultLogin(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()){ // 로그인 성공했으면
                        Toast.makeText(MainActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show(); // 토스트 문자 짧게 출력
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class); // LoginSuccessActivity로 이동하는 intent
                        intent.putExtra("nickName", account.getDisplayName()); // intent로 구글 계정 닉네임을 넘김
                        intent.putExtra("photoUrl", String.valueOf(account.getPhotoUrl())); // intent로 photoUrl을 전달. String으로 변환해서 전달해야 함.
                        intent.putExtra("success", true);

                        startActivityForResult(intent, 0);
                    }
                    else { // 로그인 실패했으면
                        Toast.makeText(MainActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show(); // 토스트 문자 짧게 출력
                        // 추가적으로 실패했을때 handle 할 거 있으면 여기 밑에다 추가하면 됨
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onBackPressed(){ // 뒤로가기 버튼이 눌렸을 때
        if (loginSuccess) {
            FirebaseAuth.getInstance().signOut(); // Firebase에서 로그아웃. Google에선 로그아웃되지 않음.
            Toast.makeText(MainActivity.this, "로그인 정보가 저장됩니다", Toast.LENGTH_SHORT).show(); // 토스트 문자 짧게 출력
            // 로그인한 채로 설정했을 때도 뒤로가기 버튼 누르면 바로 적용되도록
            Intent intent = new Intent();
            SharedPreferences sp = getSharedPreferences("setting", MODE_PRIVATE);
            intent.putExtra(PREFERENCES[0], sp.getInt(PREFERENCES[0], 0xFFFFEB3B));
            intent.putExtra(PREFERENCES[6], sp.getInt(PREFERENCES[6], 0xFF000000));
            intent.putExtra(PREFERENCES[1], sp.getInt(PREFERENCES[1], 0xFFCDDC39));
            intent.putExtra(PREFERENCES[7], sp.getInt(PREFERENCES[7], 0xFF000000));
            intent.putExtra(PREFERENCES[2], sp.getInt(PREFERENCES[2], 0xFF8BC34A));
            intent.putExtra(PREFERENCES[8], sp.getInt(PREFERENCES[8], 0xFFFFFFFF));
            intent.putExtra(PREFERENCES[3], sp.getInt(PREFERENCES[3], 0xFF00BCD4));
            intent.putExtra(PREFERENCES[9], sp.getInt(PREFERENCES[9], 0xFFFFFFFF));
            intent.putExtra(PREFERENCES[4], sp.getInt(PREFERENCES[4], 0xFF03A9F4));
            intent.putExtra(PREFERENCES[10], sp.getInt(PREFERENCES[10], 0xFFFFFFFF));
            intent.putExtra(PREFERENCES[5], sp.getInt(PREFERENCES[5], 0xFFFFFFFF));
            intent.putExtra(PREFERENCES[11], sp.getInt(PREFERENCES[11], 0xFF000000));
            intent.putExtra(PREFERENCES[12], sp.getInt(PREFERENCES[12], 0));
            setResult(RESULT_OK, intent);
        }
        super.onBackPressed();
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut(); // Firebase에서 로그아웃

        // Google에서 로그아웃. 이게 있어야 재로그인할때 계정을 선택할 수 있음.
        AuthUI.getInstance()
                .signOut(MainActivity.this)
                .addOnCompleteListener(task -> {
                    // do something here
                });

        Toast.makeText(MainActivity.this, "성공적으로 로그아웃 되었습니다", Toast.LENGTH_SHORT).show(); // 토스트 문자 짧게 출력
        finish();
    }

    private void revokeAccess() {
        // 확인 메시지 창을 띄움.
        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(MainActivity.this)
                .setTitle("정말로 탈퇴하시겠습니까?")
                .setMessage("탈퇴 후 데이터 복구는 불가능합니다")
                .setPositiveButton("탈퇴", (dialog, which) -> { // 탈퇴 버튼을 눌렀을 경우
                    deleteData(); // DB에서 값 지우기
                    FirebaseAuth.getInstance().getCurrentUser().delete(); // 탈퇴 처리

                    // Google에서 로그아웃. 이게 있어야 재로그인할때 계정을 선택할 수 있음.
                    AuthUI.getInstance()
                            .signOut(MainActivity.this)
                            .addOnCompleteListener(task -> {
                                // do something here
                            });

                    Toast.makeText(MainActivity.this, "탈퇴 처리 되었습니다", Toast.LENGTH_SHORT).show(); // 토스트 문자 짧게 출력
                    finish(); // 액티비티 종료. login activity로 넘어감.
                })
                .setNegativeButton("취소", (dialog, which) -> {
                    // 취소 버튼 누르면 아무것도 수행하지 않음.
                });

        AlertDialog msgDialog = msgBuilder.create();
        msgDialog.show();
        //finish();
    }

    private void deleteData(){
        String[] modes = {"users_3b", "users_4b", "users_5b"};
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        for (final String str : modes){
            reference.child(str).child(currentUser.getUid()).setValue(null); // 데이터 삭제
        }
    }

    private void gotoRanking(){
        Intent intent = new Intent(this, LeaderBoardActivity.class);
        startActivity(intent);
    }
}
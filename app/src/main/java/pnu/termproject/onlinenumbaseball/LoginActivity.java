package pnu.termproject.onlinenumbaseball;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private SignInButton btn_signin_google; // 구글 로그인 버튼
    private FirebaseAuth auth; // Firebase 인증 객체
    public GoogleApiClient googleApiClient; // Google API Client 객체
    private static final int REQ_SIGN_GOOGLE = 100; // 구글 로그인 결과 코드

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
        btn_signin_google.setOnClickListener(new View.OnClickListener() { // 구글 로그인 버튼을 누르면 이곳이 수행됨
            @Override
            public void onClick(View v) {
                // Google Login Intent로 넘어가서 인증 후 MainActivity로 다시 돌아옴.
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent, REQ_SIGN_GOOGLE);; // 이것의 Result가 onActivityResult로 전달됨
            }
        });

    }

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
    }

    private void resultLogin(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){ // 로그인 성공했으면
                            Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show(); // 토스트 문자 짧게 출력
                            Intent intent = new Intent(getApplicationContext(), LobbyActivity.class); // LoginSuccessActivity로 이동하는 intent
                            intent.putExtra("nickName", account.getDisplayName()); // intent로 구글 계정 닉네임을 넘김
                            intent.putExtra("photoUrl", String.valueOf(account.getPhotoUrl())); // intent로 photoUrl을 전달. String으로 변환해서 전달해야 함.

                            startActivity(intent);
                        }
                        else { // 로그인 실패했으면
                            Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show(); // 토스트 문자 짧게 출력
                            // 추가적으로 실패했을때 handle할거 있으면 여기 밑에다 추가하면 됨
                        }
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
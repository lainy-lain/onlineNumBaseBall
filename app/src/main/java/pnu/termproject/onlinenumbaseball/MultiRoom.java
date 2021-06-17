package pnu.termproject.onlinenumbaseball;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MultiRoom extends AppCompatActivity {
    private FirebaseUser currentUser;
    private DatabaseReference roomRef;
    private DatabaseReference roomIdRef;
    private long backKeyPressedTime = 0;
    private Room currentRoom;
    private int roomId;
    private RoomIdManage roomIdManage;
    private String nickName;
    private int owner;
    private int ball;
    private AlertDialog dialog;

    @SuppressLint("NonConstantResourceId")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_room);
        // 방 만들기 또는 방 입장으로 이 액티비티가 실행됨

        // 변수 설정
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        roomRef = FirebaseDatabase.getInstance().getReference("room");
        roomIdRef = FirebaseDatabase.getInstance().getReference("room id manage");

        Intent intent = getIntent();
        String roomName = intent.getStringExtra("room name");
        String strRoomId = intent.getStringExtra("room id");
        ball = intent.getIntExtra("ball", 0);
        if (strRoomId != null) {
            roomId = Integer.parseInt(strRoomId);
        }
        boolean isOwner = intent.getBooleanExtra("owner", false);

        String uid = currentUser.getUid();
        nickName = currentUser.getDisplayName();
        String photoUrl = currentUser.getPhotoUrl().toString();

        // 설정 적용
        SharedPreferences sp = getSharedPreferences("setting", MODE_PRIVATE);
        ColorStateList[] colors = {
                ColorStateList.valueOf(sp.getInt("btn4bg", 0xFF00BCD4)),
                ColorStateList.valueOf(sp.getInt("btn5bg", 0xFF03A9F4)),
                ColorStateList.valueOf(sp.getInt("btnbgbg", 0xFFFFFFFF)),
                ColorStateList.valueOf(sp.getInt("btn4tx", 0xFFFFFFFF)),
                ColorStateList.valueOf(sp.getInt("btn5tx", 0xFFFFFFFF)),
                ColorStateList.valueOf(sp.getInt("btnbgtx", 0xFF000000))
        };
        TextView[] tvs = {
                findViewById(R.id.room_name), findViewById(R.id.game_info), findViewById(R.id.room_owner),
                findViewById(R.id.user1_name), findViewById(R.id.ready_state1),
                findViewById(R.id.user2_name), findViewById(R.id.ready_state2)
        };
        Button[] buttons = {
                findViewById(R.id.ready_btn), findViewById(R.id.start_btn), findViewById(R.id.game_set)
        };

        buttons[2].getRootView().setBackgroundTintList(colors[2]);
        for (int i = 0; i < 7; i++) {
            tvs[i].setTextColor(colors[5]);
            if (i < 2) {
                buttons[i].setBackgroundTintList(colors[0]);
                buttons[i].setTextColor(colors[3]);
            }
        }
        buttons[2].setBackgroundTintList(colors[1]);
        buttons[2].setTextColor(colors[4]);

        int radiusChecked = sp.getInt("radius", 0);
        int cornerRadius = (radiusChecked + 1) * 8;
        for (int i = 0; i < 3; i++) {
            ((MaterialButton)buttons[i]).setCornerRadius(cornerRadius);
        }

        final String ballText = "공 개수: " + ball;
        if (isOwner) { // 만들어서 들어온 경우
            owner = 1;
            roomIdRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue(RoomIdManage.class) == null) {
                        roomIdManage = new RoomIdManage();
                    } // RoomIdManage 생성, 하나만 필요하다
                    else {
                        roomIdManage = snapshot.getValue(RoomIdManage.class);
                    } // 받아와서 아이디를 받는다
                    roomId = roomIdManage.receiveId();
                    tvs[1].setText(ballText);
                    updateRoomIds(roomIdManage); // 받은 아이디로 방 생성
                    currentRoom = new Room(roomName, uid, nickName, photoUrl);
                    currentRoom.setRoomId(roomId);
                    currentRoom.setBall(ball);
                    updateRoom(); // db에 업데이트
                    findViewById(R.id.user1).setVisibility(View.VISIBLE); // 화면 표시 부분
                    findViewById(R.id.owner).setVisibility(View.VISIBLE);
                    String ownerText = "방장: " + nickName;
                    ((TextView)findViewById(R.id.room_owner)).setText(ownerText);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else { // 플레이어 방 입장
            findViewById(R.id.user1).setVisibility(View.VISIBLE); // 화면 표시
            findViewById(R.id.user2).setVisibility(View.VISIBLE);
            findViewById(R.id.player).setVisibility(View.VISIBLE);
            roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds: snapshot.getChildren()) {
                        if (ds.hasChild("roomId") && roomId == ds.child("roomId").getValue(Integer.class)) {
                            currentRoom = ds.getValue(Room.class);
                            break;
                        }
                    } // 아이디에 해당하는 방을 찾는다
                    if (currentRoom != null) {
                        // 방장 이름을 표시하기 위함
                        TextView roomOwner = findViewById(R.id.room_owner);
                        owner = currentRoom.getOwner();
                        String ownerText = "방장: ";
                        if (owner == 1) {
                            ownerText += currentRoom.getUser1Name();
                        }
                        else {
                            ownerText += currentRoom.getUser2Name();
                        } // 입장 처리(유저 추가), db에 방 정보 업데이트
                        roomOwner.setText(ownerText);
                        ball = currentRoom.getBall();
                        tvs[1].setText(ballText);
                        currentRoom.addUser(uid, nickName, photoUrl);
                        updateRoom();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        String roomNameText = "방 이름: " + roomName;
        tvs[0].setText(roomNameText);
        Button ready_btn = findViewById(R.id.ready_btn);
        ready_btn.setOnClickListener(v -> {
            currentRoom.toggleReady();
            if (currentRoom.getReady()) {
                ready_btn.setText("취소하기");
            }
            else {
                ready_btn.setText("준비하기");
            }
            updateRoom();
        });
        findViewById(R.id.start_btn).setOnClickListener(v -> {
            if (currentRoom.getReady()) { // 준비된 상태면 게임 화면으로
                roomRef.child("room" + currentRoom.getRoomId()).child("start").setValue(true);
            }
        });
        // 방, 게임 정보 바꾸기
        findViewById(R.id.game_set).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MultiRoom.this);
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.game_room_set, null);
            builder.setView(view);
            final EditText nameEditText = view.findViewById(R.id.room_set_edittext);
            final Button btn_apply = view.findViewById(R.id.btn_apply);
            final Button btn_dismiss = view.findViewById(R.id.btn_dismiss);
            final RadioGroup rg = view.findViewById(R.id.ball_select);
            // 설정
            view.setBackgroundColor(sp.getInt("btnbgbg", 0xFFFFFFFF));
            nameEditText.getBackground().mutate().setColorFilter(sp.getInt("btnbgtx", 0xFF000000), PorterDuff.Mode.SRC_ATOP);
            nameEditText.setTextColor(colors[5]);
            ((TextView)view.findViewById(R.id.room_set_name_guide)).setTextColor(colors[5]);
            ((TextView)view.findViewById(R.id.room_set_ball_guide)).setTextColor(colors[5]);
            btn_apply.setBackgroundTintList(colors[0]);
            btn_apply.setTextColor(colors[3]);
            ((MaterialButton)btn_apply).setCornerRadius(cornerRadius);
            btn_dismiss.setBackgroundTintList(colors[1]);
            btn_dismiss.setTextColor(colors[4]);
            ((MaterialButton)btn_dismiss).setCornerRadius(cornerRadius);
            RadioButton[] rb = {view.findViewById(R.id.toball3), view.findViewById(R.id.toball4), view.findViewById(R.id.toball5)};
            for (int i = 0; i < 3; i++) {
                rb[i].setTextColor(colors[5]);
            }
            dialog = builder.create();

            rg.setOnCheckedChangeListener((radioGroup, i) -> {
                switch (i) {
                    case R.id.toball3:
                        ball = 3; break;
                    case R.id.toball4:
                        ball = 4; break;
                    case R.id.toball5:
                        ball = 5; break;
                }
            });
            btn_apply.setOnClickListener(apply -> {
                String str_room = nameEditText.getText().toString();
                if(str_room.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "방 제목은 공란이 될 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
                else if (str_room.contains(":")) {
                    Toast.makeText(getApplicationContext(), "방 제목으로 :은 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
                else {
                    currentRoom.setRoomName(str_room);
                    currentRoom.setBall(ball);
                    updateRoom();
                }
                dialog.dismiss();
            });
            btn_dismiss.setOnClickListener(dont -> dialog.dismiss());

            dialog.show();
        });

        // 방 정보 실시간 업데이트
        roomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    if (ds.hasChild("roomId") && roomId == ds.child("roomId").getValue(Integer.class)) {
                        currentRoom = ds.getValue(Room.class);
                        owner = currentRoom.getOwner();
                        if (currentRoom.getOwnerChanged()) {
                            ownerChanged();
                            currentRoom.setOwnerChanged(false);
                            updateRoom();
                        }
                        if (ds.hasChild("start") && ds.child("start").getValue(Boolean.class)) {
                            startGame();
                        }
                        break;
                    }
                }
                if (currentRoom != null) {
                    setVisibilities(); // 바뀌는 상황에 따라 화면 표시 바꾸기
                    roomSet();
                    readySet();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // 실시간 업데이트 방 아이디 관리
        roomIdRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                roomIdManage = snapshot.getValue(RoomIdManage.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void startGame() {
        Intent multiGameIntent = new Intent(getApplicationContext(), MultiplayActivity.class);
        multiGameIntent.putExtra("p1_id", currentRoom.getUser1Id());
        multiGameIntent.putExtra("p1_nickname", currentRoom.getUser1Name());
        multiGameIntent.putExtra("p1_photoUrl", currentRoom.getUser1Photo());
        multiGameIntent.putExtra("p2_id", currentRoom.getUser2Id());
        multiGameIntent.putExtra("p2_nickname", currentRoom.getUser2Name());
        multiGameIntent.putExtra("p2_photoUrl", currentRoom.getUser2Photo());
        multiGameIntent.putExtra("ballNumber", ball);
        startActivityForResult(multiGameIntent, 0);
    }

    private void setVisibilities() {
        int imageSize = findViewById(R.id.user1).getHeight() / 2;
        if (currentRoom.getUser1State()) { // 유저1이 있을 때 이름과 사진을 띄움
            findViewById(R.id.user1).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.user1_name)).setText(currentRoom.getUser1Name());
            Glide.with(getApplicationContext()).load(currentRoom.getUser1Photo())
                    .override(imageSize, imageSize).into((ImageView)findViewById(R.id.user1_profile));
        }
        else { // 없으면 안 보이게
            findViewById(R.id.user1).setVisibility(View.INVISIBLE);
        }
        if (currentRoom.getUser2State()) {
            findViewById(R.id.user2).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.user2_name)).setText(currentRoom.getUser2Name());
            Glide.with(getApplicationContext()).load(currentRoom.getUser2Photo())
                    .override(imageSize, imageSize).into((ImageView)findViewById(R.id.user2_profile));
        }
        else {
            findViewById(R.id.user2).setVisibility(View.INVISIBLE);
        }
    }

    private void readySet() {
        if (owner == 1) {
            findViewById(R.id.ready_state1).setVisibility(View.INVISIBLE);
            findViewById(R.id.ready_state2).setVisibility(View.VISIBLE);
            if (currentRoom.getReady()) {
                ((TextView)findViewById(R.id.ready_state2)).setText("준비됨");
            }
            else {
                ((TextView)findViewById(R.id.ready_state2)).setText("준비되지 않음");
            }
        }
        else {
            findViewById(R.id.ready_state1).setVisibility(View.VISIBLE);
            findViewById(R.id.ready_state2).setVisibility(View.INVISIBLE);
            if (currentRoom.getReady()) {
                ((TextView)findViewById(R.id.ready_state1)).setText("준비됨");
            }
            else {
                ((TextView)findViewById(R.id.ready_state1)).setText("준비되지 않음");
            }
        }
    }

    private void roomSet() {
        String roomNameText = "방 이름: " + currentRoom.getRoomName();
        ((TextView)findViewById(R.id.room_name)).setText(roomNameText);
        String ballText = "공 개수: " + currentRoom.getBall();
        ((TextView)findViewById(R.id.game_info)).setText(ballText);
    }

    private void ownerChanged() { // 방장이 나가면
        findViewById(R.id.player).setVisibility(View.INVISIBLE);
        findViewById(R.id.owner).setVisibility(View.VISIBLE);
        String ownerText = "방장: " + nickName;
        ((TextView)findViewById(R.id.room_owner)).setText(ownerText);
    }

    private void updateRoom() {
        roomRef.child("room" + currentRoom.getRoomId()).setValue(currentRoom);
    }
    private void deleteRoom() {
        roomRef.child("room" + roomId).setValue(null);
        roomIdManage.add(roomId);
        updateRoomIds(roomIdManage);
    }
    private void updateRoomIds(RoomIdManage idManage) {
        roomIdRef.setValue(idManage);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        roomRef.child("room" + currentRoom.getRoomId()).child("start").setValue(false);
        currentRoom.setReady(false);
        ((Button)findViewById(R.id.ready_btn)).setText("준비하기");
        updateRoom();
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2500) {
            backKeyPressedTime = System.currentTimeMillis();
            Toast toast = Toast.makeText(this, "뒤로 가기 버튼을 한 번 더 누르시면 방을 나갑니다.", Toast.LENGTH_LONG);
            toast.show();
            return;
        }
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), MultiList.class);
        startActivity(intent);
    }

    @Override
    public void onDestroy() { // 퇴장 구현
        if (currentRoom.getNumUser() == 1) {
            deleteRoom();
        }
        else {
            currentRoom.exitUser(currentUser.getUid());
            updateRoom();
        }
        super.onDestroy();
    }
}
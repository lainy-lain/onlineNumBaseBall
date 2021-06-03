package pnu.termproject.onlinenumbaseball;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
                    TextView tv_gameInfo = findViewById(R.id.game_info);
                    tv_gameInfo.setText(ballText);
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
                        TextView tv_gameInfo = findViewById(R.id.game_info);
                        tv_gameInfo.setText(ballText);
                        currentRoom.addUser(uid, nickName, photoUrl);
                        updateRoom();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        TextView tv_roomName = findViewById(R.id.room_name);
        String roomNameText = "방 이름: " + roomName;
        tv_roomName.setText(roomNameText);
        findViewById(R.id.ready_btn).setOnClickListener(v -> {
            currentRoom.toggleReady();
            updateRoom();
        });
        findViewById(R.id.start_btn).setOnClickListener(v -> {
            /*if (currentRoom.getReady()) { // 준비된 상태면 게임 화면으로
                Intent multiGameIntent = new Intent(getApplicationContext(), MultiPlayActivity.class);
                multiGameIntent.putExtra("user1id", currentRoom.getUser1Id());
                multiGameIntent.putExtra("user1name", currentRoom.getUser1Name());
                multiGameIntent.putExtra("user1photo", currentRoom.getUser1Photo());
                multiGameIntent.putExtra("user1id", currentRoom.getUser2Id());
                multiGameIntent.putExtra("user1name", currentRoom.getUser2Name());
                multiGameIntent.putExtra("user1photo", currentRoom.getUser2Photo());
                multiGameIntent.putExtra("ball", ball);
                startActivity(multiGameIntent);
            }*/
        });
        // 방, 게임 정보 바꾸기
        findViewById(R.id.game_set).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MultiRoom.this);
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.game_room_set, null);
            builder.setView(view);

            final EditText nameEditText = view.findViewById(R.id.text_input);
            final Button btn_apply = view.findViewById(R.id.btn_apply);
            final Button btn_dismiss = view.findViewById(R.id.btn_dismiss);
            final RadioGroup rg = view.findViewById(R.id.ball_select);
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

    private void setVisibilities() {
        if (currentRoom.getUser1State()) { // 유저1이 있을 때 이름과 사진을 띄움
            findViewById(R.id.user1).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.user1_name)).setText(currentRoom.getUser1Name());
            Glide.with(getApplicationContext()).load(currentRoom.getUser1Photo())
                    .into((ImageView)findViewById(R.id.user1_profile));
        }
        else { // 없으면 안 보이게
            findViewById(R.id.user1).setVisibility(View.INVISIBLE);
        }
        if (currentRoom.getUser2State()) {
            findViewById(R.id.user2).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.user2_name)).setText(currentRoom.getUser2Name());
            Glide.with(getApplicationContext()).load(currentRoom.getUser2Photo())
                    .into((ImageView)findViewById(R.id.user2_profile));
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
    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2500) {
            backKeyPressedTime = System.currentTimeMillis();
            Toast toast = Toast.makeText(this, "뒤로 가기 버튼을 한 번 더 누르시면 방을 나갑니다.", Toast.LENGTH_LONG);
            toast.show();
            return;
        }
        super.onBackPressed();
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
        dialog.dismiss();
        super.onDestroy();
    }
}
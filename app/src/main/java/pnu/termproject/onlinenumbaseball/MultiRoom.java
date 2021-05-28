package pnu.termproject.onlinenumbaseball;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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
        int ball = intent.getIntExtra("ball", 0);
        if (strRoomId != null) {
            roomId = Integer.parseInt(strRoomId);
        }
        boolean isOwner = intent.getBooleanExtra("owner", false);

        String uid = currentUser.getUid();
        nickName = currentUser.getDisplayName();
        String photoUrl = currentUser.getPhotoUrl().toString();

        if (isOwner) { // 만들어서 들어온 경우
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
                    updateRoomIds(roomIdManage); // 받은 아이디로 방 생성
                    currentRoom = new Room(roomName, uid, nickName, photoUrl);
                    currentRoom.setRoomId(roomId);
                    currentRoom.setBall(ball);
                    updateRoom(); // db에 업데이트
                    findViewById(R.id.user1).setVisibility(View.VISIBLE); // 화면 표시 부분
                    findViewById(R.id.owner).setVisibility(View.VISIBLE);
                    ((TextView)findViewById(R.id.room_owner)).setText(nickName);
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
                        int owner = currentRoom.getOwner(); // 방장 이름을 표시하기 위함
                        TextView roomOwner = findViewById(R.id.room_owner);
                        if (owner == 1) {
                            roomOwner.setText(currentRoom.getUser1Name());
                        }
                        else {
                            roomOwner.setText(currentRoom.getUser2Name());
                        } // 입장 처리(유저 추가), db에 방 정보 업데이트
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
        tv_roomName.setText(roomName);

        // 방 정보 실시간 업데이트
        roomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    if (ds.hasChild("roomId") && roomId == ds.child("roomId").getValue(Integer.class)) {
                        currentRoom = ds.getValue(Room.class);
                        if (currentRoom.getOwnerChanged()) {
                            currentRoom.setOwnerChanged(false);
                            updateRoom();
                            ownerChanged();
                        }
                        break;
                    }
                }
                if (currentRoom != null) {
                    setVisibilities(); // 바뀌는 상황에 따라 화면 표시 바꾸기
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
            if (!MultiRoom.this.isFinishing()) { // 오류 해결
                Glide.with(this).load(currentRoom.getUser1Photo())
                        .into((ImageView)findViewById(R.id.user1_profile));
            }
        }
        else { // 없으면 안 보이게
            findViewById(R.id.user1).setVisibility(View.INVISIBLE);
        }
        if (currentRoom.getUser2State()) {
            findViewById(R.id.user2).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.user2_name)).setText(currentRoom.getUser2Name());
            if (!MultiRoom.this.isFinishing()) {
                Glide.with(this).load(currentRoom.getUser2Photo())
                        .into((ImageView)findViewById(R.id.user2_profile));
            }
        }
        else {
            findViewById(R.id.user2).setVisibility(View.INVISIBLE);
        }
    }

    private void ownerChanged() { // 방장이 나가면
        findViewById(R.id.player).setVisibility(View.INVISIBLE);
        findViewById(R.id.owner).setVisibility(View.VISIBLE);
        ((TextView)findViewById(R.id.room_owner)).setText(nickName);
    }

    public void updateRoom() {
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
        super.onDestroy();
    }
}
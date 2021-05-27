package pnu.termproject.onlinenumbaseball;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_room);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        roomRef = FirebaseDatabase.getInstance().getReference("room");
        roomIdRef = FirebaseDatabase.getInstance().getReference("room id manage");

        // 방 만들기 또는 방 입장으로 이 액티비티가 실행됨
        Intent intent = getIntent();
        String roomName = intent.getStringExtra("room name");
        roomId = intent.getIntExtra("room id", 0);
        boolean isOwner = intent.getBooleanExtra("owner", false);

        String uid = currentUser.getUid();
        String nickName = currentUser.getDisplayName();
        String photoUrl = currentUser.getPhotoUrl().toString();

        if (isOwner) { // 만들어서 들어온 경우
            roomIdRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds: snapshot.getChildren()) {
                        if (ds.getValue(RoomIdManage.class) == null) {
                            roomIdManage = new RoomIdManage();
                            roomId = roomIdManage.receiveId();
                            updateRoomIds(roomIdManage);
                        }
                        else {
                            roomIdManage = ds.getValue(RoomIdManage.class);
                            roomId = roomIdManage.receiveId();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            currentRoom = new Room(roomName, uid, nickName, photoUrl);
            currentRoom.setRoomId(roomId);
            updateRoom(currentRoom);
            findViewById(R.id.user1).setVisibility(View.VISIBLE);
            findViewById(R.id.owner).setVisibility(View.VISIBLE);
        }
        else {
            findViewById(R.id.user1).setVisibility(View.VISIBLE);
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
                    }
                    currentRoom.addUser(uid, nickName, photoUrl);
                    updateRoom(currentRoom);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        TextView tv_roomName = findViewById(R.id.room_name);
        tv_roomName.setText(roomName);
    }

    private void setVisibilities() {
        if (currentRoom.user1State()) {
            findViewById(R.id.user1).setVisibility(View.VISIBLE);
        }
        else {
            findViewById(R.id.user1).setVisibility(View.INVISIBLE);
        }
        if (currentRoom.user2State()) {
            findViewById(R.id.user2).setVisibility(View.VISIBLE);
        }
        else {
            findViewById(R.id.user2).setVisibility(View.INVISIBLE);
        }
    }

    private void ownerChanged() {
        findViewById(R.id.player).setVisibility(View.INVISIBLE);
        findViewById(R.id.owner).setVisibility(View.VISIBLE);
    }

    private void notifyUpdate() {
        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    if (ds.hasChild("roomId") && roomId == ds.child("roomId").getValue(Integer.class)) {
                        currentRoom = ds.getValue(Room.class);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        setVisibilities();
    }

    private void updateRoom(Room room) {
        roomRef.child("room" + room.getRoomId()).setValue(room);
        notifyUpdate();
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
        else {
            if (currentRoom.getNumUser() == 1) {
                deleteRoom();
            }
            else {
                if (currentRoom.exitUser(currentUser.getUid())) {
                    ownerChanged();
                }
                updateRoom(currentRoom);
            }
            super.onBackPressed();
        }
    }
}
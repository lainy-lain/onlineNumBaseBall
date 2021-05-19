package pnu.termproject.onlinenumbaseball;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
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
    private DatabaseReference reference;
    private long backKeyPressedTime = 0;
    private String roomName;
    private Room currentRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_room);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("room");

        // 방 만들기 또는 방 입장으로 이 액티비티가 실행됨
        Intent intent = getIntent();
        roomName = intent.getStringExtra("room name");
        boolean isOwner = intent.getBooleanExtra("owner", false);

        if (isOwner) { // 만들어서 들어온 경우
            currentRoom = new Room(roomName, currentUser.getUid());
            updateRoom(currentRoom);
            findViewById(R.id.user1).setVisibility(View.VISIBLE);
            findViewById(R.id.owner).setVisibility(View.VISIBLE);
        }
        else {
            findViewById(R.id.user1).setVisibility(View.VISIBLE);
            findViewById(R.id.user2).setVisibility(View.VISIBLE);
            findViewById(R.id.player).setVisibility(View.VISIBLE);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds: snapshot.getChildren()) {
                        if (ds.hasChild("roomName") && roomName.equals(ds.child("roomName").getValue())) {
                            currentRoom = ds.getValue(Room.class);
                            break;
                        }
                    }
                    currentRoom.addUser(currentUser.getUid());
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

    private void updateRoom(Room room) {
        reference.child(room.getRoomName()).setValue(room);
    }
    private void deleteRoom(String roomName) {
        reference.child(roomName).setValue(null);
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
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (currentRoom.getNumUser() == 1) {
                        deleteRoom(currentRoom.getRoomName());
                    }
                    else {
                        currentRoom.exitUser(currentUser.getUid());
                        updateRoom(currentRoom);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            super.onBackPressed();
        }
    }
}
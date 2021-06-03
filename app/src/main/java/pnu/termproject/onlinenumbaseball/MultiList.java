package pnu.termproject.onlinenumbaseball;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MultiList extends AppCompatActivity {

    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> arr_roomList = new ArrayList<>();
    private ListView listView;
    private Button btn_create, btn_quick;
    private DatabaseReference reference = FirebaseDatabase.getInstance().getReference().getRoot().child("room");
    private String str_room;
    private boolean isOwner; //대기방의 생성자인지, 참가자인지 구별하는 변수
    final String[] ballArr = new String[] {"3개", "4개", "5개"};

    Map<String, Object> map = new HashMap<String, Object>();
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multiplay_list);

        listView = findViewById(R.id.list);
        btn_create = findViewById(R.id.btn_create);
        btn_quick = findViewById(R.id.btn_quick);

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arr_roomList);
        listView.setAdapter(arrayAdapter);

        //채팅방을 생성하는 코드입니다
        btn_create.setOnClickListener(view -> show());

        //빠른시작을 하는 코드입니다
        btn_quick.setOnClickListener(view -> quick());

        //Database가 변경되었을때 호출되는 코드입니다
        //실시간으로 변경사항을 감지하고 업데이트 합니다
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> set = new HashSet<String>();
                Iterator i = snapshot.getChildren().iterator();

                while(i.hasNext()){
                    DataSnapshot tmp1 = (DataSnapshot) i.next();
                    String tmp2 = "방이름: " + (tmp1).child("roomName").getValue().toString()
                            + "\n방번호: " + (tmp1).child("roomId").getValue().toString()
                            + "\n방인원: " + (tmp1).child("numUser").getValue().toString()
                            + "\n공개수: " + (tmp1).child("ball").getValue().toString();
                    set.add(tmp2);
                }

                arr_roomList.clear();
                arr_roomList.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            //취소되었을때 호출되는 코드입니다
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        //대기방리스트중 하나를 클릭했을때의 코드입니다
        //"방이름"과 "생성자 여부"를 전송합니다
        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            if(!((TextView) view).getText().toString().split(": ")[3].split("\n")[0].equals("2")) {
                Intent intent = new Intent(getApplicationContext(), MultiRoom.class);
                isOwner = false;
                intent.putExtra("room name", ((TextView) view).getText().toString().split(": ")[1].split("\n")[0]);
                intent.putExtra("room id", ((TextView) view).getText().toString().split(": ")[2].split("\n")[0]);
                intent.putExtra("owner", isOwner);
                startActivity(intent);
                finish();
            }
            else{
                Toast.makeText(MultiList.this, "방 인원이 모두 차있습니다", Toast.LENGTH_LONG).show();
            }
        });
    }

    void show(){
        final int[] ball = {3};

        AlertDialog.Builder builder = new AlertDialog.Builder(MultiList.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.roommake, null);
        builder.setView(view);

        final EditText nameEditText = (EditText) view.findViewById(R.id.name);
        final Button btn_create1 = (Button) view.findViewById(R.id.btn_create1);
        final Button btn_dont = (Button) view.findViewById(R.id.btn_dont);
        final RadioGroup rg = (RadioGroup) view.findViewById(R.id.rg);
        dialog = builder.create();

        rg.setOnCheckedChangeListener((radioGroup, i) -> {
            switch (i){
                case R.id.rb_ball3:
                    ball[0] = 3; break;
                case R.id.rb_ball4:
                    ball[0] = 4; break;
                case R.id.rb_ball5:
                    ball[0] = 5; break;
            }
        });
        btn_create1.setOnClickListener(view1 -> {
            str_room = nameEditText.getText().toString();
            if(str_room.isEmpty()) {
                Toast.makeText(getApplicationContext(), "방 제목은 공란이 될 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
            else if(str_room.contains(":")){
                Toast.makeText(getApplicationContext(), "방 제목으로 :은 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
            else {
                isOwner = true;

                Intent intent = new Intent(getApplicationContext(), MultiRoom.class);
                intent.putExtra("room name", str_room);
                intent.putExtra("owner", isOwner);
                intent.putExtra("ball", ball[0]);
                startActivity(intent);
                dialog.dismiss();
                finish();
        }
    });
        btn_dont.setOnClickListener(view12 -> dialog.dismiss());

        dialog.show();
    }

    void quick(){
        final int[] ball = {3};

        AlertDialog.Builder builder = new AlertDialog.Builder(MultiList.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.roomquick, null);
        builder.setView(view);

        final Button btn_find = (Button) view.findViewById(R.id.btn_find);
        final Button btn_dont1 = (Button) view.findViewById(R.id.btn_dont1);
        final RadioGroup rg = (RadioGroup) view.findViewById(R.id.rg_quick);
        dialog = builder.create();

        rg.setOnCheckedChangeListener((radioGroup, i) -> {
            switch (i){
                case R.id.rb_ball3:
                    ball[0] = 3; break;
                case R.id.rb_ball4:
                    ball[0] = 4; break;
                case R.id.rb_ball5:
                    ball[0] = 5; break;
            }
        });
        btn_find.setOnClickListener(view1 -> {
            /*
            if(str_room.isEmpty()) {
                Toast.makeText(getApplicationContext(), "방 제목은 공란이 될 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
            else if(str_room.indexOf(":") != -1 || str_room.indexOf("\\n") != -1){
                Toast.makeText(getApplicationContext(), "방 제목으로 :와 \\n은 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
            else {
                isOwner = true;

                Intent intent = new Intent(getApplicationContext(), MultiRoom.class);
                intent.putExtra("room name", str_room);
                intent.putExtra("owner", isOwner);
                intent.putExtra("ball", ball[0]);
                startActivity(intent);
                finish();
            }
             */
        });
        btn_dont1.setOnClickListener(view12 -> dialog.dismiss());

        dialog.show();
    }
}

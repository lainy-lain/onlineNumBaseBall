package pnu.termproject.onlinenumbaseball;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

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

    Map<String, Object> map = new HashMap<String, Object>();

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
        btn_create.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                final EditText et_inDialog = new EditText(MultiList.this);

                final AlertDialog.Builder builder = new AlertDialog.Builder(MultiList.this);
                builder.setTitle("대기방 이름 입력");
                builder.setView(et_inDialog);
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        str_room = et_inDialog.getText().toString();
                        isOwner = true;

                        Intent intent = new Intent(getApplicationContext(), MultiRoom.class);
                        intent.putExtra("room name", str_room);
                        intent.putExtra("owner", isOwner);
                        startActivity(intent);
                        finish();
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
            }
        });

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
                            + "\n" + "방번호: " + (tmp1).child("roomId").getValue().toString();
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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            //"방이름"과 "생성자 여부"를 전송합니다
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), MultiRoom.class);
                isOwner = false;
                intent.putExtra("room name", (((TextView) view).getText().toString().split(" ")[1]).split("\n")[0]);
                intent.putExtra("room id", ((TextView) view).getText().toString().split(" ")[2]);
                intent.putExtra("owner", isOwner);
                startActivity(intent);
                finish();
            }
        });
    }
}

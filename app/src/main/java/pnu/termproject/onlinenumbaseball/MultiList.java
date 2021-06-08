package pnu.termproject.onlinenumbaseball;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MultiList extends AppCompatActivity {

    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> arr_roomList = new ArrayList<>();
    private final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().getRoot().child("room");
    private String str_room;
    private boolean isOwner; //대기방의 생성자인지, 참가자인지 구별하는 변수
    private AlertDialog dialog;
    private int cornerRadius;
    private ColorStateList[] colors;
    private SharedPreferences sp;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multiplay_list);

        ListView listView = findViewById(R.id.list);
        Button btn_create = findViewById(R.id.btn_create);
        Button btn_quick = findViewById(R.id.btn_quick);

        // 설정 적용을 위한 코드 추가
        sp = getSharedPreferences("setting", MODE_PRIVATE);
        colors = new ColorStateList[] {
                ColorStateList.valueOf(sp.getInt("btn1bg", 0xFFFFEB3B)),
                ColorStateList.valueOf(sp.getInt("btn2bg", 0xFFCDDC39)),
                ColorStateList.valueOf(sp.getInt("btn3bg", 0xFF8BC34A)),
                ColorStateList.valueOf(sp.getInt("btn4bg", 0xFF00BCD4)),
                ColorStateList.valueOf(sp.getInt("btn5bg", 0xFF03A9F4)),
                ColorStateList.valueOf(sp.getInt("btnbgbg", 0xFFFFFFFF)),
                ColorStateList.valueOf(sp.getInt("btn1tx", 0xFF000000)),
                ColorStateList.valueOf(sp.getInt("btn2tx", 0xFF000000)),
                ColorStateList.valueOf(sp.getInt("btn3tx", 0xFFFFFFFF)),
                ColorStateList.valueOf(sp.getInt("btn4tx", 0xFFFFFFFF)),
                ColorStateList.valueOf(sp.getInt("btn5tx", 0xFFFFFFFF)),
                ColorStateList.valueOf(sp.getInt("btnbgtx", 0xFF000000))
        };
        int radiusChecked = sp.getInt("radius", 0);
        cornerRadius = (radiusChecked + 1) * 8;

        TextView room_list = findViewById(R.id.room_list);
        room_list.getRootView().setBackgroundTintList(colors[5]);
        room_list.setBackgroundColor(sp.getInt("btn1bg", 0xFFFFEB3B));
        ((TextView)room_list).setTextColor(colors[6]);
        btn_create.setBackgroundTintList(colors[1]);
        btn_create.setTextColor(colors[7]);
        btn_quick.setBackgroundTintList(colors[2]);
        btn_quick.setTextColor(colors[8]);
        ((MaterialButton) btn_create).setCornerRadius(cornerRadius);
        ((MaterialButton) btn_quick).setCornerRadius(cornerRadius);

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arr_roomList) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = view.findViewById(android.R.id.text1);
                tv.setTextColor(colors[11]);
                return view;
            }
        };
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
                Set<String> set = new HashSet<>();

                for (DataSnapshot tmp1 : snapshot.getChildren()) {
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

    @SuppressLint("NonConstantResourceId")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void show() {
        final int[] ball = {3};

        AlertDialog.Builder builder = new AlertDialog.Builder(MultiList.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.roommake, null);
        builder.setView(view);

        final EditText nameEditText = (EditText) view.findViewById(R.id.room_make_edittext);
        final Button btn_room_create = (Button) view.findViewById(R.id.btn_room_create);
        final Button btn_make_dont = (Button) view.findViewById(R.id.btn_make_dont);
        final RadioGroup rg = (RadioGroup) view.findViewById(R.id.rg);
        // 설정
        view.setBackgroundColor(sp.getInt("btnbgbg", 0xFFFFFFFF));
        nameEditText.getBackground().mutate().setColorFilter(sp.getInt("btnbgtx", 0xFF000000), PorterDuff.Mode.SRC_ATOP);
        nameEditText.setTextColor(colors[11]);
        ((TextView)view.findViewById(R.id.room_make_name_guide)).setTextColor(colors[11]);
        ((TextView)view.findViewById(R.id.room_make_ball_guide)).setTextColor(colors[11]);
        btn_room_create.setBackgroundTintList(colors[3]);
        btn_room_create.setTextColor(colors[9]);
        ((MaterialButton)btn_room_create).setCornerRadius(cornerRadius);
        btn_make_dont.setBackgroundTintList(colors[4]);
        btn_make_dont.setTextColor(colors[10]);
        ((MaterialButton)btn_make_dont).setCornerRadius(cornerRadius);
        RadioButton[] rb = {view.findViewById(R.id.make_ball3), view.findViewById(R.id.make_ball4), view.findViewById(R.id.make_ball5)};
        for (int i = 0; i < 3; i++) {
            rb[i].setTextColor(colors[11]);
        }
        dialog = builder.create();

        rg.setOnCheckedChangeListener((radioGroup, i) -> {
            switch (i){
                case R.id.make_ball3:
                    ball[0] = 3; break;
                case R.id.make_ball4:
                    ball[0] = 4; break;
                case R.id.make_ball5:
                    ball[0] = 5; break;
            }
        });
        btn_room_create.setOnClickListener(view1 -> {
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
        btn_make_dont.setOnClickListener(view12 -> dialog.dismiss());

        dialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("NonConstantResourceId")
    void quick() {
        final int[] ball = {3};

        AlertDialog.Builder builder = new AlertDialog.Builder(MultiList.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.roomquick, null);
        builder.setView(view);

        final Button btn_find = (Button) view.findViewById(R.id.btn_find);
        final Button btn_quick_dont = (Button) view.findViewById(R.id.btn_quick_dont);
        final RadioGroup rg = (RadioGroup) view.findViewById(R.id.rg_quick);
        dialog = builder.create();
        // 설정
        view.setBackgroundColor(sp.getInt("btnbgbg", 0xFFFFFFFF));
        ((TextView)view.findViewById(R.id.condition_guide)).setTextColor(colors[11]);
        btn_find.setBackgroundTintList(colors[3]);
        btn_find.setTextColor(colors[9]);
        ((MaterialButton)btn_find).setCornerRadius(cornerRadius);
        btn_quick_dont.setBackgroundTintList(colors[4]);
        btn_quick_dont.setTextColor(colors[10]);
        ((MaterialButton)btn_quick_dont).setCornerRadius(cornerRadius);
        RadioButton[] rb = {view.findViewById(R.id.quick_ball3), view.findViewById(R.id.quick_ball4), view.findViewById(R.id.quick_ball5)};
        for (int i = 0; i < 3; i++) {
            rb[i].setTextColor(colors[11]);
        }

        rg.setOnCheckedChangeListener((radioGroup, i) -> {
            switch (i){
                case R.id.make_ball3:
                    ball[0] = 3; break;
                case R.id.make_ball4:
                    ball[0] = 4; break;
                case R.id.make_ball5:
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
        btn_quick_dont.setOnClickListener(view12 -> dialog.dismiss());

        dialog.show();
    }
}

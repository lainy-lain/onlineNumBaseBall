package pnu.termproject.onlinenumbaseball;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class MyInfoAdapter extends RecyclerView.Adapter<MyInfoAdapter.CustomViewHolder> {

    private ArrayList<User> arrayList;
    private Context context;
    private ColorStateList tx;

    public MyInfoAdapter(ArrayList<User> arrayList, Context context, ColorStateList tx) {
        this.arrayList = arrayList;
        this.context = context;
        this.tx = tx;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_myinfo, parent, false);

        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        Glide.with(holder.itemView)
                .load(arrayList.get(position).getUserProfile())
                .into(holder.iv_profile);
        holder.tv_userName.setText(arrayList.get(position).getUserName());

        long meanTime = (long)arrayList.get(position).getMeanTime();
        String str_meanTime =  "평균 클리어 시간 : " + (meanTime / 60) + "분 " +
                (meanTime % 60) + "초";
        @SuppressLint("DefaultLocale")
        String str_meanTurn = String.format("평균 클리어 턴 수 : %.2f", arrayList.get(position).getMeanTurn());
        holder.tv_meanTime.setText(str_meanTime);
        holder.tv_meanTurn.setText(str_meanTurn);
        holder.tv_userName.setTextColor(tx);
        holder.tv_meanTurn.setTextColor(tx);
        holder.tv_meanTime.setTextColor(tx);
    }

    @Override
    public int getItemCount() {
        return (arrayList != null ? arrayList.size() : 0);
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_profile;
        TextView tv_userName;
        TextView tv_meanTime;
        TextView tv_meanTurn;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);

            this.iv_profile = itemView.findViewById(R.id.iv_profile);
            this.tv_userName = itemView.findViewById(R.id.tv_userName);
            this.tv_meanTime = itemView.findViewById(R.id.tv_meanTime);
            this.tv_meanTurn = itemView.findViewById(R.id.tv_meanTurn);
        }
    }
}

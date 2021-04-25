package pnu.termproject.onlinenumbaseball;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.CustomViewHolder> {

    private ArrayList<User> arrayList;
    private Context context;

    public RankingAdapter(ArrayList<User> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_ranking, parent, false);
        CustomViewHolder holder = new CustomViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        holder.tv_ranking.setText(String.valueOf(position + 1));

        Glide.with(holder.itemView)
                .load(arrayList.get(position).getUserProfile())
                .into(holder.iv_profile);
        holder.tv_userName.setText(arrayList.get(position).getUserName());

        long meanTime = (long)arrayList.get(position).getMeanTime();
        String str_meanTime =  "평균 클리어 시간 : " + String.valueOf(meanTime / 60) + "분 " +
                String.valueOf(meanTime % 60) + "초";
        @SuppressLint("DefaultLocale")
        String str_meanTurn = String.format("평균 클리어 턴 수 : %.2f", arrayList.get(position).getMeanTurn());
        holder.tv_meanTime.setText(str_meanTime);
        holder.tv_meanTurn.setText(str_meanTurn);
    }

    @Override
    public int getItemCount() {
        return (arrayList != null ? arrayList.size() : 0);
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView tv_ranking;
        ImageView iv_profile;
        TextView tv_userName;
        TextView tv_meanTime;
        TextView tv_meanTurn;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);

            this.tv_ranking = itemView.findViewById(R.id.tv_ranking);
            this.iv_profile = itemView.findViewById(R.id.iv_profile);
            this.tv_userName = itemView.findViewById(R.id.tv_userName);
            this.tv_meanTime = itemView.findViewById(R.id.tv_meanTime);
            this.tv_meanTurn = itemView.findViewById(R.id.tv_meanTurn);

        }
    }
}

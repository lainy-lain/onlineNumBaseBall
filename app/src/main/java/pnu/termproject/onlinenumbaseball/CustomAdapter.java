package pnu.termproject.onlinenumbaseball;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {

    private ArrayList<User> arrayList;
    private Context context;

    public CustomAdapter(ArrayList<User> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        CustomViewHolder holder = new CustomViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        Glide.with(holder.itemView)
                .load(arrayList.get(position).getUserProfile())
                .into(holder.iv_profile);
        holder.tv_userName.setText(arrayList.get(position).getUserName());
        holder.tv_meanTime.setText("평균 클리어 시간(분) : " + String.valueOf(arrayList.get(position).getMeanTime()));
        holder.tv_meanTurn.setText("평균 클리어 턴 수 : " + String.valueOf(arrayList.get(position).getMeanTurn()));
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
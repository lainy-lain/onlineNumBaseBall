package pnu.termproject.onlinenumbaseball;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.CustomViewHolder> {

    private ArrayList<InputAndResult> arrayList;
    private Context context;
    private ColorStateList tx;

    public ResultAdapter(ArrayList<InputAndResult> arrayList, Context context, ColorStateList tx) {
        this.arrayList = arrayList;
        this.context = context;
        this.tx = tx;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_result, parent, false);

        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        String str_input = arrayList.get(position).getInput();
        String str_result = arrayList.get(position).getResult();

        holder.tv_input.setText(str_input);
        holder.tv_result.setText(str_result);
        holder.tv_input.setTextColor(tx);
        holder.tv_result.setTextColor(tx);
    }

    @Override
    public int getItemCount() {
        return (arrayList != null ? arrayList.size() : 0);
    }


    public class CustomViewHolder extends RecyclerView.ViewHolder{
        TextView tv_input;
        TextView tv_result;

        public CustomViewHolder(@NonNull View itemView){
            super(itemView);

            this.tv_input = itemView.findViewById(R.id.tv_inputNum);
            this.tv_result = itemView.findViewById(R.id.tv_result);
        }
    }
}

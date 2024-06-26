package com.zbadev.emotizone;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private List<ScheduleItem> scheduleList;

    public ScheduleAdapter(List<ScheduleItem> scheduleList) {
        this.scheduleList = scheduleList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedule_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScheduleItem item = scheduleList.get(position);
        holder.timeTextView.setText(item.getTime());
        holder.titleTextView.setText(item.getTitle());
        holder.categoryTextView.setText(item.getCategory());
        //holder.iconImageView.setImageResource(item.getIconResId());

        // Set image based on emotional state
        switch (item.getIconResId()) {
            case "Estres Alto":
                holder.iconImageView.setImageResource(R.drawable.estres_alto);
                break;
            case "Estres Moderado":
                holder.iconImageView.setImageResource(R.drawable.estres_moderado);
                break;
            case "Estres Bajo":
                holder.iconImageView.setImageResource(R.drawable.estres_bajo);
                break;
            case "Relajacion":
                holder.iconImageView.setImageResource(R.drawable.relajacion);
                break;
            case "Felicidad":
                holder.iconImageView.setImageResource(R.drawable.felicidad);
                break;
            case "Ansiedad":
                holder.iconImageView.setImageResource(R.drawable.ansiedad);
                break;
            case "Tristeza":
                holder.iconImageView.setImageResource(R.drawable.tristeza);
                break;
            default:
                holder.iconImageView.setImageResource(R.drawable.man_user_circle_icon); // Imagen por defecto
                break;
        }
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView timeTextView;
        public TextView titleTextView;
        public TextView categoryTextView;
        public ImageView iconImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            iconImageView = itemView.findViewById(R.id.iconImageView);
        }
    }
}

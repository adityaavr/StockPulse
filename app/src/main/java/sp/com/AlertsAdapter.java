package sp.com;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.ViewHolder> {

    private List<AlertItem> alertItems;

    public AlertsAdapter(List<AlertItem> alertItems) {
        this.alertItems = alertItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alerts, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AlertItem item = alertItems.get(position);
        holder.title.setText(item.getTitle());
        holder.details.setText(item.getDetails());
    }

    @Override
    public int getItemCount() {
        return alertItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView details;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.alertTitle);
            details = itemView.findViewById(R.id.alertSubtitle);
        }
    }
}



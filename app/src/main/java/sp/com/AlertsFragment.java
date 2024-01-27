package sp.com;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class AlertsFragment extends Fragment {

    private RecyclerView recyclerView;
    private AlertsAdapter adapter;
    private List<AlertItem> alertItems = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private ImageButton clearAlertsButton;

    public AlertsFragment() {
        // Required empty public constructor
    }

    public static AlertsFragment newInstance() {
        return new AlertsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alerts, container, false);

        recyclerView = view.findViewById(R.id.alertsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new SlideOutRightItemAnimator());
        adapter = new AlertsAdapter(alertItems);
        recyclerView.setAdapter(adapter);

        clearAlertsButton = view.findViewById(R.id.clearAlertsButton);
        clearAlertsButton.setOnClickListener(v -> {
            // Delay the clearing to ensure the user sees the animation
            new Handler().postDelayed(() -> clearAlerts(), 500); // Delay by 500 milliseconds
        });

        checkStockLevels();

        return view;
    }

    private void checkStockLevels() {
        db.collection("items").document(userId).collection("user_items")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            alertItems.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Long currentStock = document.getLong("itemQuantity");
                                Long minStockLevel = document.getLong("totalItemQuantity");
                                String itemName = document.getString("itemName");

                                if (currentStock != null && minStockLevel != null &&
                                        (currentStock / minStockLevel * 100) < 50) {
                                    // The stock is low, handle the alert
                                    AlertItem newItem = new AlertItem(
                                            "Low stock for " + itemName,
                                            "Current stock: " + currentStock
                                    );
                                    alertItems.add(newItem);
                                }
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Log.d("AlertsFragment", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
    private void clearAlerts() {
        // Create a temporary list to avoid ConcurrentModificationException
        List<AlertItem> itemsToRemove = new ArrayList<>(alertItems);

        for (AlertItem item : itemsToRemove) {
            int position = alertItems.indexOf(item);
            alertItems.remove(position);
            adapter.notifyItemRemoved(position);
        }
    }

}


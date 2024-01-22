package sp.com;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView heading;
    private TextView totalItemsTextView;
    private LinearLayout stockProgressContainer;
    private FirebaseFirestore db;
    private ListenerRegistration firestoreListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        heading = view.findViewById(R.id.heading);
        totalItemsTextView = view.findViewById(R.id.totalItemsTextView);
        stockProgressContainer = view.findViewById(R.id.stockProgressContainer);
        db = FirebaseFirestore.getInstance();

        fetchUserNameAndDisplay();
        updateTotalItemsAndStocks();

        RecyclerView recyclerView = view.findViewById(R.id.card_dashboard_alerts);
        // Setup RecyclerView, Adapter, and Layout Manager

        ImageView aboutIcon = view.findViewById(R.id.iconAbout);
        aboutIcon.setOnClickListener(v -> {
            // Handle the about icon click event
        });

        // Additional UI initialization and event handling

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        attachFirestoreListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (firestoreListener != null) {
            firestoreListener.remove(); // Detach the listener
        }
    }

    private boolean isLowStock(InventoryItem item) {
        if (item.getTotalItemQuantity() > 0) {
            double stockPercentage = ((double) item.getItemQuantity() / item.getTotalItemQuantity()) * 100;
            return stockPercentage < 50; // You can adjust the threshold as needed
        }
        return false;
    }


    private void attachFirestoreListener() {
        firestoreListener = db.collection("items").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("user_items")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(getContext(), "Error listening to updates", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int totalItemQuantitySum = 0;
                    boolean hasLowStockItems = false;
                    stockProgressContainer.removeAllViews();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                        InventoryItem item = snapshot.toObject(InventoryItem.class);
                        if (item != null) {
                            totalItemQuantitySum += item.getItemQuantity();
                            updateStockProgress(item);

                            if (isLowStock(item)) {
                                hasLowStockItems = true;
                                TextView lowStockItemNameTextView = getView().findViewById(R.id.lowStockItemName);
                                lowStockItemNameTextView.setText(item.getItemName());
                            }
                        }
                    }
                    totalItemsTextView.setText("" + totalItemQuantitySum);

                    CardView lowStockContainer = getView().findViewById(R.id.card_low_stock);
                    lowStockContainer.setVisibility(hasLowStockItems ? View.VISIBLE : View.GONE);

                });
    }

    private void fetchUserNameAndDisplay() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            FirebaseFirestore.getInstance().collection("organisations").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String currentDate = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date());
                            heading.setText("Hello " + name + ", " + currentDate);
                        }
                    });
        }
    }

    private void updateTotalItemsAndStocks() {
        db.collection("items").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("user_items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalItemQuantitySum = 0;
                    stockProgressContainer.removeAllViews();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                        InventoryItem item = snapshot.toObject(InventoryItem.class);
                        if (item != null) {
                            totalItemQuantitySum += item.getItemQuantity();
                            updateStockProgress(item);
                        }
                    }
                    totalItemsTextView.setText("" + totalItemQuantitySum);
                    Log.d("HomeFragment", "TextView should now display: " + totalItemsTextView.getText().toString());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading items", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateStockProgress(InventoryItem item) {
        LinearLayout itemLayout = new LinearLayout(getContext());
        itemLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView itemNameTextView = new TextView(getContext());
        itemNameTextView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        itemNameTextView.setText(item.getItemName());

        ProgressBar progressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f));
        int progress = (int) ((float) item.getItemQuantity() / item.getTotalItemQuantity() * 100);
        progressBar.setProgress(progress);

        itemLayout.addView(itemNameTextView);
        itemLayout.addView(progressBar);

        stockProgressContainer.addView(itemLayout);
    }
}




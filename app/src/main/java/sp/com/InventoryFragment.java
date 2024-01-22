package sp.com;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class InventoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private InventoryAdapter adapter;
    private List<InventoryItem> itemList;
    private FirebaseFirestore db;
    private ListenerRegistration firestoreListener;
    private EditText searchInput;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_inventory, container, false);

        recyclerView = rootView.findViewById(R.id.inventoryRecyclerView);
        searchInput = rootView.findViewById(R.id.search_input);
        itemList = new ArrayList<>();
        adapter = new InventoryAdapter(getContext(), itemList, getChildFragmentManager());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = rootView.findViewById(R.id.fab_add_item);
        fab.setOnClickListener(view -> {
            AddItemBottomSheetDialogFragment bottomSheet = AddItemBottomSheetDialogFragment.newInstance();
            bottomSheet.show(getChildFragmentManager(), bottomSheet.getTag());
        });

        db = FirebaseFirestore.getInstance();

        // Load items from Firestore
        loadItemsFromFirestore();
        // Set up Firestore real-time updates
        setupFirestoreRealTimeUpdates();

        // Setting up search input listener
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });


        return rootView;
    }

    private void loadItemsFromFirestore() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("items").document(userId).collection("user_items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    itemList.clear();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                        InventoryItem item = snapshot.toObject(InventoryItem.class);
                        itemList.add(item);
                        item.setItemId(snapshot.getId());
                    }
                    adapter.updateFullItemList(itemList);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading items", Toast.LENGTH_SHORT).show();
                });
    }


    private void setupFirestoreRealTimeUpdates() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firestoreListener = db.collection("items").document(userId).collection("user_items")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(getContext(), "Error listening to updates", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    itemList.clear();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                        InventoryItem item = snapshot.toObject(InventoryItem.class);
                        itemList.add(item);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (firestoreListener != null) {
            firestoreListener.remove(); // Stop listening to changes when the fragment is destroyed
        }
    }
}

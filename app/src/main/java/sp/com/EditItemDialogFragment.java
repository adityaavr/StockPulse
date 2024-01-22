package sp.com;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditItemDialogFragment extends DialogFragment {

    private TextView itemNameTextView;
    private EditText newItemQuantity;
    private ImageView itemImageView;
    private MaterialButton saveButton;
    private MaterialButton cancelButton;
    private String itemId;
    private FirebaseFirestore db;

    public static EditItemDialogFragment newInstance() {
        return new EditItemDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_update_stock, container, false);

        // Initialize UI elements
        itemNameTextView = rootView.findViewById(R.id.dialog_item_name);
        newItemQuantity = rootView.findViewById(R.id.dialog_item_quantity);
        itemImageView = rootView.findViewById(R.id.dialog_item_image);
        saveButton = rootView.findViewById(R.id.dialog_button_save);
        cancelButton = rootView.findViewById(R.id.dialog_button_cancel);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Load item details into the dialog (you should pass the item details to this fragment)
        // For example, you can use arguments to pass the item details to this fragment.

        if (getArguments() != null) {
            String itemName = getArguments().getString("itemName");
            String imageUrl = getArguments().getString("imageUrl");
            int itemQuantity = getArguments().getInt("itemQuantity");
            itemId = getArguments().getString("itemId");

            itemNameTextView.setText(itemName);
            newItemQuantity.setText(String.valueOf(itemQuantity));
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .override(itemImageView.getWidth(), itemImageView.getHeight()) // Resize image
                        .centerCrop() // Optional, if you want to crop the image
                        .placeholder(R.drawable.ic_placeholder) // Placeholder image
                        .into(itemImageView);
            }
        }

        // Set click listeners
        saveButton.setOnClickListener(view -> {
            // Handle save button click
            saveItemDetails();
        });

        cancelButton.setOnClickListener(view -> {
            // Handle cancel button click
            dismiss();
        });

        return rootView;
    }

    private void saveItemDetails() {
        // Implement the logic to save item details to Firestore here
        // You can use db.collection("items").document(userId).collection("user_items") to update the item details
        // Don't forget to validate user input and handle errors
        // After saving, you can dismiss the dialog
        if (itemId == null || itemId.isEmpty()) {
            Toast.makeText(getContext(), "Error: Item ID is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        int itemQuantity = Integer.parseInt(newItemQuantity.getText().toString());

        Map<String, Object> itemData = new HashMap<>();
        itemData.put("itemQuantity", itemQuantity);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("items").document(userId).collection("user_items").document(itemId)
                .update(itemData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Item updated successfully", Toast.LENGTH_SHORT).show();
                    dismiss(); // Close the dialog
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error updating item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // This method can be used to show the edit item dialog
    public void show(FragmentManager fragmentManager, String tag) {
        if (fragmentManager != null) {
            super.show(fragmentManager, tag);
        }
    }
}


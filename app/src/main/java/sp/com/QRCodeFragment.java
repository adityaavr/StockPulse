package sp.com;

import android.app.AlertDialog;
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
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.button.MaterialButton;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class QRCodeFragment extends Fragment {

    private DecoratedBarcodeView barcodeView;
    private FirebaseFirestore db;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        barcodeView = view.findViewById(R.id.zxing_barcode_scanner);

        barcodeView.decodeContinuous(result -> {
            // Handle the scanned result here
            barcodeView.pause(); // Pause scanning
            fetchItemDetails(result.getText());
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Resume scanning when the fragment is resumed
        barcodeView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Pause scanning when the fragment is paused
        barcodeView.pause();
    }

    private void fetchItemDetails(String documentId) {
        // Fetch item details from Firestore
        db.collection("items")
                .document(userId)
                .collection("user_items")
                .document(documentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            String itemName = documentSnapshot.getString("itemName");
                            String imageUrl = documentSnapshot.getString("imageUrl");
                            int itemQuantity = documentSnapshot.getLong("itemQuantity").intValue();

                            // Call the method to show the custom dialog with the item details
                            showCustomDialog(itemName, imageUrl, itemQuantity, documentId);
                        }
                    }
                });
    }

    private void showCustomDialog(String itemName, String imageUrl, int itemQuantity, String documentId) {
        // Create an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_update_stock, null);
        builder.setView(dialogView);

        ImageView itemImageView = dialogView.findViewById(R.id.dialog_item_image);
        TextView itemNameTextView = dialogView.findViewById(R.id.dialog_item_name);
        EditText itemQuantityEditText = dialogView.findViewById(R.id.dialog_item_quantity);
        MaterialButton saveButton = dialogView.findViewById(R.id.dialog_button_save);
        MaterialButton cancelButton = dialogView.findViewById(R.id.dialog_button_cancel);

        // Set the item name, item quantity, and load the image using Glide
        itemNameTextView.setText(itemName);
        itemQuantityEditText.setText(String.valueOf(itemQuantity));

        // Load the image using Glide into itemImageView
        Glide.with(getContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_placeholder)
                .into(itemImageView);

        AlertDialog dialog = builder.create();

        // Set click listeners for buttons
        saveButton.setOnClickListener(v -> {
            // Handle save button click here
            String updatedQuantity = itemQuantityEditText.getText().toString().trim();
            if (!updatedQuantity.isEmpty()) {
                // Convert the updated quantity to an integer
                int newQuantity = Integer.parseInt(updatedQuantity);

                // Update the itemQuantity field in Firestore
                db.collection("items")
                        .document(userId)
                        .collection("user_items")
                        .document(documentId)
                        .update("itemQuantity", newQuantity)
                        .addOnSuccessListener(aVoid -> {
                            // Update successful
                            Toast.makeText(getContext(), "Data Updated", Toast.LENGTH_SHORT).show();
                            dialog.dismiss(); // Dismiss the dialog
                        })
                        .addOnFailureListener(e -> {
                            // Handle the error if the update fails
                            // You can display an error message or take appropriate action
                            Toast.makeText(getContext(), "Failed to update data", Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Handle the case where the updated quantity is empty
                Toast.makeText(getContext(), "Updated quantity cannot be empty", Toast.LENGTH_LONG).show();
            }
        });

        cancelButton.setOnClickListener(v -> {
            // Dismiss the dialog when the "Cancel" button is clicked
            dialog.dismiss();
            barcodeView.resume(); // Resume scanning
        });

        dialog.show();
    }
}




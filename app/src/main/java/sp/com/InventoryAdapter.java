package sp.com;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    private List<InventoryItem> itemList;

    private List<InventoryItem> fullItemList;
    private final Context context;
    private FragmentManager fragmentManager;
    private FirebaseFirestore db;

    public InventoryAdapter(Context context, List<InventoryItem> itemList, FragmentManager fragmentManager) {
        this.context = context;
        this.itemList = itemList;
        this.fullItemList = new ArrayList<>(itemList);
        this.fragmentManager = fragmentManager;
        this.db = FirebaseFirestore.getInstance();
    }

    public void updateFullItemList(List<InventoryItem> fullItemList) {
        this.fullItemList.clear();
        this.fullItemList.addAll(fullItemList);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory, parent, false);
        return new ViewHolder(view);
    }

    // Method to delete an item from Firestore
    private void deleteItemFromFirestore(InventoryItem item) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("items")
                .document(userId)
                .collection("user_items")
                .document(item.getItemId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Item deleted successfully
                    Toast.makeText(context, "Item deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Error occurred while deleting the item
                    Toast.makeText(context, "Error deleting item", Toast.LENGTH_SHORT).show();
                });
    }

    // Method to show a delete confirmation dialog
    // Method to show a delete confirmation dialog with custom layout
    private void showDeleteConfirmationDialog(InventoryItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.delete_dialog, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Get references to the MaterialButtons in your custom dialog layout
        MaterialButton deleteButton = dialogView.findViewById(R.id.dialog_button_delete);
        MaterialButton cancelButton = dialogView.findViewById(R.id.dialog_button_cancel);

        // Set click listeners for the MaterialButtons
        deleteButton.setOnClickListener(v -> {
            // Handle item deletion here
            deleteItemFromFirestore(item);
            dialog.dismiss(); // Dismiss the dialog after deletion
        });

        cancelButton.setOnClickListener(v -> {
            // Cancel the deletion
            dialog.dismiss(); // Dismiss the dialog on cancel
        });

        dialog.show();
    }




    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InventoryItem item = itemList.get(position);
        holder.itemName.setText(item.getItemName());
        holder.itemQuantity.setText(String.format("Quantity: %d out of %d remaining", item.getItemQuantity(), item.getTotalItemQuantity()));

        ImageView editItem = holder.itemView.findViewById(R.id.item_edit);
        editItem.setOnClickListener(view -> {
            // Open the edit dialog here, passing necessary data to EditItemDialogFragment
            EditItemDialogFragment editItemDialog = EditItemDialogFragment.newInstance();

            Bundle args = new Bundle();
            args.putString("itemName", item.getItemName());
            args.putString("imageUrl", item.getImageUrl());
            args.putInt("itemQuantity", item.getItemQuantity());
            args.putString("itemId", item.getItemId());

            editItemDialog.setArguments(args);

            editItemDialog.show(fragmentManager, editItemDialog.getTag());
        });

        Glide.with(context)
                .asBitmap()
                .load(item.getImageUrl())
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        holder.itemImage.setImageBitmap(resource);
                        Palette.from(resource).generate(palette -> {
                            Palette.Swatch swatch = palette.getDominantSwatch();
                            if (swatch != null) {
                                holder.cardView.setCardBackgroundColor(swatch.getRgb());
                            }
                        });
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });

        ImageView deleteItem = holder.itemView.findViewById(R.id.item_delete);
        deleteItem.setOnClickListener(view -> {
            showDeleteConfirmationDialog(item);
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView itemName, itemQuantity;
        ImageView itemImage;

        ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            itemName = view.findViewById(R.id.item_name);
            itemQuantity = view.findViewById(R.id.item_quantity);
            itemImage = view.findViewById(R.id.item_image);
        }
    }

    // Method to filter data
    public void filter(String text) {
        itemList.clear();
        if (text.isEmpty()) {
            itemList.addAll(fullItemList);
        } else {
            text = text.toLowerCase();
            for (InventoryItem item : fullItemList) {
                if (item.getItemName().toLowerCase().contains(text)) {
                    itemList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

}




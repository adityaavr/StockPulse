package sp.com;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.media.ExifInterface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddItemBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAPTURE_IMAGE_REQUEST = 2;
    private Uri photoURI;
    private ImageView imagePlaceholder;
    private String userId;
    private Uri imageUriToUpload; // Store the URI of the selected or captured image
    private TextInputEditText itemNameInput;
    private TextInputEditText itemQuantityInput;
    private Context context;

    public static AddItemBottomSheetDialogFragment newInstance() {
        return new AddItemBottomSheetDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_dialog, container, false);
        imagePlaceholder = view.findViewById(R.id.image_placeholder);
        itemNameInput = view.findViewById(R.id.item_name_input);
        itemQuantityInput = view.findViewById(R.id.item_quantity_input);

        view.findViewById(R.id.gallery_button).setOnClickListener(v -> openGallery());
        view.findViewById(R.id.camera_button).setOnClickListener(v -> takePhoto());
        view.findViewById(R.id.save_button).setOnClickListener(v -> {
            if (imageUriToUpload != null) {
                uploadImageToFirebaseStorage(imageUriToUpload, userId);
            } else {
                Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
            }
        });

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Handle error
            }
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(getContext(),
                        "sp.com.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                imageUriToUpload = data.getData();
                setResizedImage(imageUriToUpload);
            } else if (requestCode == CAPTURE_IMAGE_REQUEST) {
                imageUriToUpload = photoURI;
                setResizedImage(photoURI);
            }
        }
    }

    private void setResizedImage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);

            // Get rotation
            int rotation = getRotation(imageUri);

            // Apply rotation if necessary
            if (rotation != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotation);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }

            Bitmap resizedBitmap = getResizedBitmap(bitmap, imagePlaceholder.getWidth(), imagePlaceholder.getHeight());
            imagePlaceholder.setImageBitmap(resizedBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap getResizedBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
    }

    private int getRotation(Uri imageUri) throws IOException {
        ExifInterface exif = new ExifInterface(getActivity().getContentResolver().openInputStream(imageUri));
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    private void uploadImageToFirebaseStorage(Uri imageUri, String userId) {
        String itemName = itemNameInput.getText().toString().trim();
        String itemQuantityString = itemQuantityInput.getText().toString().trim();
        if (imageUri == null || userId == null) {
            Toast.makeText(getContext(), "Error: No image to upload", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate inputs
        if (itemName.isEmpty() || itemQuantityString.isEmpty()) {
            Toast.makeText(getContext(), "Please enter all details", Toast.LENGTH_SHORT).show();
            return;
        }

        int itemQuantity;
        try {
            itemQuantity = Integer.parseInt(itemQuantityString);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Creating a file reference
        String fileName = "image_" + System.currentTimeMillis() + ".jpg";
        StorageReference fileRef = storageRef.child("item_images/" + userId + "/" + fileName);

        // Uploading the file
        UploadTask uploadTask = fileRef.putFile(imageUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                // Here you get the image download URL
                Toast.makeText(getContext(), "Image uploaded", Toast.LENGTH_SHORT).show();
                // You can now store this URL in Firestore or do other actions
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("itemName", itemName);
                itemData.put("itemQuantity", itemQuantity);
                itemData.put("totalItemQuantity", itemQuantity);
                itemData.put("imageUrl", downloadUri.toString());

                // Add data to Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("items")
                        .document(userId)
                        .collection("user_items")
                        .add(itemData)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(getContext(), "Item added to Firestore with ID: " + documentReference.getId(), Toast.LENGTH_SHORT).show();
                            showQRCodeDialog(documentReference.getId());
                            dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Error adding item to Firestore", Toast.LENGTH_SHORT).show();
                        });
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private Bitmap generateQRCode(String documentId) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(documentId, BarcodeFormat.QR_CODE, 200, 200);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bmp;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showQRCodeDialog(String documentId) {
        Bitmap qrCodeBitmap = generateQRCode(documentId);

        // Create an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_qr_code, null);
        builder.setView(dialogView);

        ImageView qrCodeImageView = dialogView.findViewById(R.id.qr_code_image_view);
        qrCodeImageView.setImageBitmap(qrCodeBitmap);

        Button downloadButton = dialogView.findViewById(R.id.download_qr_code_button);
        AlertDialog dialog = builder.create();

        downloadButton.setOnClickListener(v -> {
            // Implement code to download/save the QR code image
            downloadQRCode(qrCodeBitmap, documentId);
            dialog.dismiss(); // Dismiss the dialog after downloading
        });

        dialog.show();
    }

    private void downloadQRCode(Bitmap qrCodeBitmap, String documentId) {
        if (context == null) {
            Log.e("QRCodeDownload", "Context is null");
            return;
        }

        try {
            // Save the QR code image to the app's private storage directory
            File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (storageDir == null) {
                Log.e("QRCodeDownload", "External storage directory is null");
                return;
            }

            File qrCodeFile = new File(storageDir, "QR_" + documentId + ".jpg");

            try (FileOutputStream outputStream = new FileOutputStream(qrCodeFile)) {
                qrCodeBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                Log.i("QRCodeDownload", "QR Code saved to: " + qrCodeFile.getAbsolutePath());

                // Insert the image into the media store database
                MediaStore.Images.Media.insertImage(context.getContentResolver(), qrCodeFile.getAbsolutePath(), qrCodeFile.getName(), null);

                Toast.makeText(context, "QR Code saved to Pictures", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e("QRCodeDownload", "Error saving QR Code: " + e.getMessage());
                Toast.makeText(context, "Error saving QR Code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("QRCodeDownload", "Unexpected error: " + e.getMessage());
            Toast.makeText(context, "Unexpected error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}



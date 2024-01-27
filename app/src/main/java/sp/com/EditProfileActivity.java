package sp.com;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etEditCompanyName, etEditPersonName;
    private MaterialButton btnSaveChanges;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);

        // Initialize EditTexts
        etEditCompanyName = findViewById(R.id.etEditCompanyName);
        etEditPersonName = findViewById(R.id.etEditPersonName);

        // Initialize Save Button
        btnSaveChanges = findViewById(R.id.btnSaveChanges);

        // Retrieve data from the Intent
        String currentCompanyName = getIntent().getStringExtra("companyName");
        String currentPersonName = getIntent().getStringExtra("personName");

        // Set the current values to EditTexts
        etEditCompanyName.setText(currentCompanyName);
        etEditPersonName.setText(currentPersonName);

        // Set up the Save Button click listener
        btnSaveChanges.setOnClickListener(v -> saveProfileData());
    }

    private void saveProfileData() {
        String updatedCompanyName = etEditCompanyName.getText().toString().trim();
        String updatedPersonName = etEditPersonName.getText().toString().trim();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("organisations").document(userId)
                .update("organisationName", updatedCompanyName, "name", updatedPersonName)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditProfileActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK); // Indicate the update was successful
                    finish(); // Close the activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditProfileActivity.this, "Error updating profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}

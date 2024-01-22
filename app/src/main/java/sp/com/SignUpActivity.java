package sp.com;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        // Initialise Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get references to the EditTexts and Button
        EditText etOrganisationName = findViewById(R.id.etOrganisationName);
        EditText etYourName = findViewById(R.id.etYourName);
        EditText etBusinessEmail = findViewById(R.id.etBusinessEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        EditText etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button buttonSignUp = findViewById(R.id.buttonSignUp);

        // Set a click listener for the sign-up button
        buttonSignUp.setOnClickListener(v -> {
            String organisationName = etOrganisationName.getText().toString().trim();
            String yourName = etYourName.getText().toString().trim();
            String email = etBusinessEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // Input validation
            if (organisationName.isEmpty() || yourName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Please fill all the fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(SignUpActivity.this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Proceed with creating the user
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign up success, update UI with the signed-in user's information
                            Toast.makeText(SignUpActivity.this, "Registration successful.", Toast.LENGTH_SHORT).show();

                            // Get the UID of the newly created user
                            String uid = mAuth.getCurrentUser().getUid();

                            // Prepare the user data to be stored
                            Map<String, Object> user = new HashMap<>();
                            user.put("organisationName", organisationName);
                            user.put("name", yourName);
                            user.put("email", email);

                            // Add a new document with UID as the document name
                            db.collection("organisations").document(uid)
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> Toast.makeText(SignUpActivity.this, "User data stored successfully.", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(SignUpActivity.this, "Error storing user data: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                            // Optionally, redirect the user to the login screen or main app screen
                            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                        } else {
                            // If sign up fails, display a message to the user.
                            Toast.makeText(SignUpActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            
                        }
                    });
        });
    }
}
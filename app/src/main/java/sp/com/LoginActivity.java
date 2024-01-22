package sp.com;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        EditText editTextEmail = findViewById(R.id.editTextEmail);
        EditText editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);

        // Initialize the TextView here after setContentView
        TextView signUp = findViewById(R.id.textViewSignUp);
        String signUpText = signUp.getText().toString();
        SpannableString spannableString = new SpannableString(signUpText);

        // Click Listener for the Login Button
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve email and password from the EditTexts
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                // Validate inputs
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please fill all the fields.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Firebase Authentication to sign in with email and password
                mAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // Login successful, navigate to HomeFragment
                                if (task.isSuccessful()) {
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Login failed.", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                        });
            }
        });

        // Define the ClickableSpan
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                // Start an activity or perform other actions when 'Sign up here' is clicked
                 Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                 startActivity(intent);
                // TODO: Define your action for the click event here (Firebase Auth)
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.BLUE); // Set text color to blue
                ds.setUnderlineText(false); // Remove the underline
            }
        };

        // Apply the ClickableSpan to the text 'Sign up here'
        int start = signUpText.indexOf("Sign up here");
        int end = start + "Sign up here".length();
        spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set the TextView text to the SpannableString
        signUp.setText(spannableString);
        // Make the links in the SpannableString clickable
        signUp.setMovementMethod(LinkMovementMethod.getInstance());
    }
}

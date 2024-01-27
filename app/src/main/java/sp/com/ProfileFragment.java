package sp.com;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private static final int EDIT_PROFILE_REQUEST = 1; // Request code for startActivityForResult

    private TextView tvCompanyName, tvName, tvEmail;
    private ImageButton btnEdit;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Views
        tvCompanyName = view.findViewById(R.id.tvCompanyName);
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        btnEdit = view.findViewById(R.id.ivEdit);

        loadUserData();

        // Set up the edit button functionality to open EditProfileActivity
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EditProfileActivity.class);
            intent.putExtra("companyName", tvCompanyName.getText().toString());
            intent.putExtra("personName", tvName.getText().toString());
            startActivityForResult(intent, EDIT_PROFILE_REQUEST);
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PROFILE_REQUEST && resultCode == AppCompatActivity.RESULT_OK) {
            // Profile was edited, reload the data
            loadUserData();
        }
    }

    private void loadUserData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("organisations").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    String companyName = document.getString("organisationName");
                    String userName = document.getString("name");
                    String userEmail = document.getString("email");

                    tvCompanyName.setText(companyName);
                    tvName.setText(userName);
                    tvEmail.setText(userEmail);
                }
            } else {
                // Handle the error
            }
        });
    }
}



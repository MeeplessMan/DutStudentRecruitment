package com.example.dutstudentrecruitment;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.reflect.Array;

public class CandidateMainActivity extends AppCompatActivity {
    Uri image;
    FrameLayout frameLayout;
    TabLayout tabLayout;
    ImageView imgProfile;
    Spinner accountOptionsSpinner;
    TextView textViewName;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore db;
    DocumentReference documentReference;
    private static String TAG = "CandidateMainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_candidate_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        imgProfile = (ImageView) findViewById(R.id.profile_pic_imageview);
        textViewName = (TextView) findViewById(R.id.full_name_textview);
        accountOptionsSpinner = (Spinner) findViewById(R.id.account_options_spinner);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        if(user == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            finish();
            startActivity(intent);
            finish();
        }

        documentReference = db.collection("candidate").document(user.getUid());
        loadUserProfile();
        setupAccountOptionsMenu();

        if (savedInstanceState == null) { // Load initial fragment only once
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, new CandidateMainFragment()) // Use the correct FrameLayout ID
                    // .addToBackStack(null) // Optional: depends on navigation needs
                    .commit();
        }


        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment = null;
                switch(tab.getPosition()){
                    case 0:
                        fragment = new CandidateMainFragment();
                        break;
                    case 1:
                        fragment = new CandidateJobOffersFragment();
                        break;
                    case 2:
                        fragment = new CandidateChatFragment();
                        break;
                    case 3:
                        fragment = new CandidateApplicationsFragment();
                        break;
                }
                if(fragment != null){
                    getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, fragment)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupAccountOptionsMenu() {
        if (accountOptionsSpinner == null) {
            Log.e(TAG, "accountOptionsSpinner is null in setupAccountOptionsMenu. Did you call findViewById?");
            return;
        }

        String[] accountOptions = getResources().getStringArray(R.array.account_options_array);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                accountOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accountOptionsSpinner.setAdapter(adapter);
        final boolean[] isUserInteracting = {false};

        accountOptionsSpinner.setOnTouchListener((v, event) -> {
            isUserInteracting[0] = true;
            return false;
        });

        accountOptionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private boolean initialSelectionSet = false;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isUserInteracting[0] && !initialSelectionSet) {
                    initialSelectionSet = true;
                    return;
                }

                String selectedOption = (String) parent.getItemAtPosition(position);

                if (selectedOption.equals(getString(R.string.action_logout))) {
                    logoutUser();
                } else if (selectedOption.equals(getString(R.string.action_reset_password))) {
                    resetPassword();
                } else if (selectedOption.equals(getString(R.string.action_delete_account))) {
                    promptForPasswordBeforeDeletion();
                }
                isUserInteracting[0] = false;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                isUserInteracting[0] = false; // Reset
            }
        });
    }
    private void navigateToLogin() {
        Intent intent = new Intent(CandidateMainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void promptForPasswordBeforeDeletion() {
        user = auth.getCurrentUser(); // Ensure user is current
        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, "User not available or email not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Verify Password to Delete Account");
        builder.setMessage("Please enter your password to proceed with account deletion.");

        // Set up the input
        final EditText inputPassword = new EditText(this);
        inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        inputPassword.setHint("Password");
        builder.setView(inputPassword);

        // Set up the buttons
        builder.setPositiveButton("Verify", (dialog, which) -> {
            String password = inputPassword.getText().toString();
            if (password.isEmpty()) {
                Toast.makeText(CandidateMainActivity.this, "Password cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Re-authenticate user
            reauthenticateAndProceedWithDeletion(password);
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void reauthenticateAndProceedWithDeletion(String password) {
        user = auth.getCurrentUser(); // Ensure user is current
        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, "User not available.", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showFinalDeleteConfirmationDialog();
                    } else {
                        Toast.makeText(CandidateMainActivity.this, "Incorrect password. Please try again.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showFinalDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to permanently delete your account? This action cannot be undone.")
                .setPositiveButton("DELETE PERMANENTLY", (dialog, which) -> deleteUserAccount())
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    private void deleteUserAccount() {
        user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No user logged in to delete.", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        String userId = user.getUid();

        db.collection("candidate").document(userId) // Ensure "candidate" is your correct collection
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User data from Firestore deleted for UID: " + userId))
                .addOnFailureListener(e -> Log.e(TAG, "Error deleting Firestore data for UID: " + userId, e));

        StorageReference profilePicRef = FirebaseStorage.getInstance().getReference().child("profilePic").child(userId);
        profilePicRef.delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User profile picture from Storage deleted for UID: " + userId))
                .addOnFailureListener(e -> Log.e(TAG, "Error deleting profile picture from Storage for UID: " + userId, e));

        user.delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(CandidateMainActivity.this, "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                        navigateToLogin();
                    } else {
                        Toast.makeText(CandidateMainActivity.this, "Failed to delete account: " + task.getException().getMessage() + ". Please try logging in again and then deleting.", Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void logoutUser() {
        auth.signOut();
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(CandidateMainActivity.this, LoginActivity.class); // Replace LoginActivity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void resetPassword() {
        user = auth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            auth.sendPasswordResetEmail(user.getEmail())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(CandidateMainActivity.this, "Password reset email sent to " + user.getEmail(), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(CandidateMainActivity.this, "Failed to send reset email: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "Could not send password reset email. User or email not found.", Toast.LENGTH_LONG).show();
        }
    }
    private void loadUserProfile() {
        if (documentReference != null) {
            documentReference.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        // Assuming "fullName" is the field for the user's name in Firestore
                        String firstName = document.getString("firstName");
                        String lastName = document.getString("lastName");
                        if (textViewName != null) {
                            if (firstName != null && !firstName.isEmpty() && lastName != null && !lastName.isEmpty()) {
                                textViewName.setText(firstName+" "+lastName);
                            } else {
                                textViewName.setText("N/A"); // Default if name not found or empty
                            }
                        }

                        String profileImageUrl = document.getString("profileImageUrl");

                        if (imgProfile != null) {
                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                Glide.with(CandidateMainActivity.this)
                                        .load(profileImageUrl)
                                        .placeholder(R.drawable.ic_profile_placeholder)
                                        .error(R.drawable.ic_profile_placeholder)
                                        .circleCrop()
                                        .into(imgProfile);
                            } else {
                                imgProfile.setImageResource(R.drawable.ic_profile_placeholder);
                            }
                        }

                    } else {
                        if (textViewName != null) textViewName.setText("User Not Found");
                        if (imgProfile != null) imgProfile.setImageResource(R.drawable.ic_profile_placeholder);
                    }
                } else {
                    if (getApplicationContext() != null) { // Check context before showing Toast
                        Toast.makeText(CandidateMainActivity.this, "Error loading profile.", Toast.LENGTH_SHORT).show();
                    }
                    if (textViewName != null) textViewName.setText("Error");
                    if (imgProfile != null) imgProfile.setImageResource(R.drawable.ic_profile_placeholder);
                }
            });
        } else {
            // Optionally handle UI:
            if (textViewName != null) textViewName.setText("N/A");
            if (imgProfile != null) imgProfile.setImageResource(R.drawable.ic_profile_placeholder);
        }
    }
}
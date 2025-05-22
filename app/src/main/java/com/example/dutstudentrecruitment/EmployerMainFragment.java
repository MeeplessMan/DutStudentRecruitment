package com.example.dutstudentrecruitment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EmployerMainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EmployerMainFragment extends Fragment {

    private static final String TAG = "EmployerMainFragment";
    private Button buttonUpdateProfile;
    private ImageView profilePicImageView;
    private TextView fullNameTextView;
    private TextView qualificationTextView;
    private ImageView imageSearch;
    private ImageView imageOffers;
    // Add other views from your fragment layout if needed, e.g., for search and applications
    // private LinearLayout layoutSearch, layoutMyApplications;


    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private StorageReference storageReference;

    public EmployerMainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        // Initialize Firebase Storage reference if you use it for profile pictures
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_employer_main, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views from fragment_candidate_main.xml
        profilePicImageView = view.findViewById(R.id.profile_pic_imageview2);
        fullNameTextView = view.findViewById(R.id.full_name_textview2);
        qualificationTextView = view.findViewById(R.id.qualification_textview2);
        buttonUpdateProfile = view.findViewById(R.id.buttonUpdateProfile);
        imageOffers = view.findViewById(R.id.imageOffers);
        imageSearch = view.findViewById(R.id.imageSearch);

        imageSearch.setOnClickListener(v ->{
            if (getActivity() instanceof EmployerMainActivity) { // **Replace CandidateActivity with your actual Activity class name**
                EmployerMainActivity hostingActivity = (EmployerMainActivity) getActivity();
                int jobOffersTabIndex = 1;
                hostingActivity.selectTabAtIndex(jobOffersTabIndex);
            }
        });

        imageOffers.setOnClickListener(v ->{
            if (getActivity() instanceof EmployerMainActivity) { // **Replace CandidateActivity with your actual Activity class name**
                EmployerMainActivity hostingActivity = (EmployerMainActivity) getActivity();
                int jobOffersTabIndex = 3;
                hostingActivity.selectTabAtIndex(jobOffersTabIndex);
            }
        });

        buttonUpdateProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EmployerUpdateProfileActivity.class);
            startActivity(intent);
        });

        if (currentUser != null) {
            loadCandidateProfileData();
        } else {
            // Handle case where user is not logged in, though typically MainActivity would handle this
            Log.w(TAG, "Current user is null in EmployerMainFragment");
            // Optionally set placeholder data or show an error
            fullNameTextView.setText("N/A");
            qualificationTextView.setText("Please log in");
            if (getContext() != null) { // Check context before loading drawable
                Glide.with(getContext())
                        .load(R.drawable.ic_profile_placeholder) // Default placeholder
                        .circleCrop() // Apply circle crop if your placeholder isn't already a circle
                        .into(profilePicImageView);
            }
        }
    }

    private void loadCandidateProfileData() {
        if (currentUser == null || db == null) {
            Log.e(TAG, "User or Firestore instance is null.");
            return;
        }

        String userId = currentUser.getUid();
        DocumentReference candidateDocRef = db.collection("employer").document(userId);

        candidateDocRef.get().addOnCompleteListener(task -> { // Start of lambda for addOnCompleteListener
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    Log.d(TAG, "Candidate data found: " + document.getData());

                    String company = document.getString("company");
                    if (company != null && !company.isEmpty()) {
                        fullNameTextView.setText(company);
                    } else {
                        fullNameTextView.setText("Name not available");
                    }

                    String qualification = document.getString("slogan");
                    if (qualification != null && !qualification.isEmpty()) {
                        qualificationTextView.setText(qualification);
                    } else {
                        qualificationTextView.setText("Slogan not specified");
                    }

                    String profilePicUrl = document.getString("profileImageUrl");
                    if (profilePicUrl != null && !profilePicUrl.isEmpty() && getContext() != null) {
                        Glide.with(getContext())
                                .load(profilePicUrl)
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .error(R.drawable.ic_profile_placeholder)
                                .circleCrop()
                                .into(profilePicImageView);
                    } else {
                        if (getContext() != null) {
                            Glide.with(getContext())
                                    .load(R.drawable.ic_profile_placeholder)
                                    .circleCrop()
                                    .into(profilePicImageView);
                        }
                        Log.d(TAG, "Profile image URL not found or context is null, loading placeholder.");
                    }
                } else {
                    Log.d(TAG, "No such candidate document for UID: " + userId);
                    fullNameTextView.setText("Candidate Profile Not Found");
                    qualificationTextView.setText("");
                    if (getContext() != null) {
                        Glide.with(getContext())
                                .load(R.drawable.ic_profile_placeholder)
                                .circleCrop()
                                .into(profilePicImageView);
                    }
                }
            } else {
                Log.e(TAG, "Error getting candidate document: ", task.getException());
                fullNameTextView.setText("Error loading profile");
                qualificationTextView.setText("");
                if (getContext() != null) {
                    Glide.with(getContext())
                            .load(R.drawable.ic_profile_placeholder)
                            .circleCrop()
                            .into(profilePicImageView);
                }
                Toast.makeText(getContext(), "Failed to load profile data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
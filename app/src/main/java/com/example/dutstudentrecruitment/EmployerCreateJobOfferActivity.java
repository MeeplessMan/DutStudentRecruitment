package com.example.dutstudentrecruitment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.UUID;

public class EmployerCreateJobOfferActivity extends AppCompatActivity {
    private final String TAG = "EmployerCreateJobOfferActivity";
    EditText editTextJobTitle, editTextJobDescription, editTextAddress;
    LinearLayout skillsContainer;
    Button buttonAddSkillField, buttonCreate, buttonCancel;
    CheckBox checkboxFullTime, checkboxPartTime, checkboxContract, checkboxInternship, checkboxTemporary;

    private DocumentSnapshot employer;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private ArrayList<CheckBox> jobTypeCheckBoxes;

    // Job Types for Spinner (if using Spinner)
    private final String[] JOB_TYPES_ARRAY = {"Full-time", "Part-time", "Internship", "Contract", "Temporary"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employer_create_job_offer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        editTextJobTitle = findViewById(R.id.editTextJobTitle);
        editTextJobDescription = findViewById(R.id.editTextJobDescription);
        editTextAddress = findViewById(R.id.editTextAddress);
        buttonAddSkillField = findViewById(R.id.buttonAddSkillField);
        buttonCreate = findViewById(R.id.buttonCreate);
        buttonCancel = findViewById(R.id.buttonCancel);
        skillsContainer = findViewById(R.id.skillsContainer);
        checkboxFullTime = findViewById(R.id.checkboxFullTime);
        checkboxPartTime = findViewById(R.id.checkboxPartTime);
        checkboxContract = findViewById(R.id.checkboxContract);
        checkboxInternship = findViewById(R.id.checkboxInternship);
        checkboxTemporary = findViewById(R.id.checkboxTemporary);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }
        loadEmployerProfile();
        addSkillView(null);
        setupJobTypeCheckboxes();

        buttonAddSkillField.setOnClickListener(v ->{
            LinearLayout skillsContainer = findViewById(R.id.skillsContainer); // Use binding
            int childCount = skillsContainer.getChildCount();

            if (childCount > 0) {
                View lastSkillRowView = skillsContainer.getChildAt(childCount - 1);
                EditText lastEditTextSkill = lastSkillRowView.findViewById(R.id.editTextSkillItem);

                if (TextUtils.isEmpty(lastEditTextSkill.getText().toString().trim())) {
                    Toast.makeText(EmployerCreateJobOfferActivity.this, "Please fill in the current skill before adding another.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            addSkillView(null);
        });

        buttonCreate.setOnClickListener(v -> createJobOffer());

        buttonCancel.setOnClickListener(v -> finish());
    }

    private void addSkillView(String skillText) {
        // The LinearLayout for all skill rows
        final LinearLayout skillsContainer = findViewById(R.id.skillsContainer);

        // Inflate the skill_input_item.xml layout
        LayoutInflater inflater = LayoutInflater.from(this);
        final View skillRowView = inflater.inflate(R.layout.skill_input_item, skillsContainer, false);

        EditText editTextSkill = skillRowView.findViewById(R.id.editTextSkillItem);
        ImageButton removeButton = skillRowView.findViewById(R.id.buttonRemoveSkillItem);

        // If skillText is provided (e.g., when loading existing data), set it
        if (skillText != null && !skillText.isEmpty()) {
            editTextSkill.setText(skillText);
        }

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When remove is clicked, remove this skillRowView from the skillsContainer
                skillsContainer.removeView(skillRowView);
            }
        });

        // Add the newly created row to the container
        skillsContainer.addView(skillRowView);
    }

    public void loadEmployerProfile(){
        DocumentReference employerProfileRef = db.collection("employer").document(currentUser.getUid());
        employerProfileRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
            } else {
                Log.e(TAG, "Error getting employer profile", task.getException());
            }
        });
    }
    private ArrayList<String> collectSkillsData() {
        ArrayList<String> skillsList = new ArrayList<>();
        LinearLayout skillsContainer = findViewById(R.id.skillsContainer);

        for (int i = 0; i < skillsContainer.getChildCount(); i++) {
            View skillRowView = skillsContainer.getChildAt(i);
            EditText editTextSkill = skillRowView.findViewById(R.id.editTextSkillItem);
            String skill = editTextSkill.getText().toString().trim();
            if (!skill.isEmpty()) {
                skillsList.add(skill);
            }
        }
        return skillsList;
    }

    private void setupJobTypeCheckboxes() {
        jobTypeCheckBoxes = new ArrayList<>();
        if (checkboxFullTime != null) jobTypeCheckBoxes.add(checkboxFullTime);
        if (checkboxPartTime != null) jobTypeCheckBoxes.add(checkboxPartTime);
        if (checkboxContract != null) jobTypeCheckBoxes.add(checkboxContract);
        if (checkboxInternship != null) jobTypeCheckBoxes.add(checkboxInternship);
        if (checkboxTemporary != null) jobTypeCheckBoxes.add(checkboxTemporary);

        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {
            if (isChecked) {
                for (CheckBox cb : jobTypeCheckBoxes) {
                    if (cb != buttonView) {
                        cb.setChecked(false);
                    }
                }
            }
        };
        for (CheckBox cb : jobTypeCheckBoxes) {
            cb.setOnCheckedChangeListener(listener);
        }
    }

    private void createJobOffer() {
        String jobTitle = editTextJobTitle.getText().toString().trim();
        String jobDescription = editTextJobDescription.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        ArrayList<String> skills = collectSkillsData();
        String selectedJobType = ""; // Use a new variable for iteration result
        for (CheckBox cb : jobTypeCheckBoxes) {
            if (cb.isChecked()) {
                selectedJobType = cb.getText().toString();
                break;
            }
        }
        final String jobType = selectedJobType;
        // --- Basic Validation ---
        boolean isValid = true;
        if (TextUtils.isEmpty(jobTitle)) {
            editTextJobTitle.setError("Job title is required.");
            isValid = false;
        }
        if (TextUtils.isEmpty(jobDescription)) {
            editTextJobDescription.setError("Job description is required.");
            isValid = false;
        }
        if (TextUtils.isEmpty(address)) {
            editTextAddress.setError("Address is required.");
            isValid = false;
        }
        if (TextUtils.isEmpty(jobType)) {
            Toast.makeText(this, "Please select a job type.", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        if (!isValid) {
            return;
        }

        buttonCreate.setEnabled(false); // Disable button early

        // --- Get Employer Profile to retrieve Company Name FIRST ---
        DocumentReference employerProfileRef = db.collection("employer").document(currentUser.getUid());
        employerProfileRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String companyName = documentSnapshot.getString("company");
                if (TextUtils.isEmpty(companyName)) {
                    // Handle case where companyName might be missing in the profile
                    // You could set a default, show an error, or make it a required field earlier
                    Toast.makeText(EmployerCreateJobOfferActivity.this, "Company name not found in profile.", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Company name is missing from employer profile: " + currentUser.getUid());
                    buttonCreate.setEnabled(true); // Re-enable button
                    // It might be better to prevent offer creation if company name is essential
                    // For now, let's proceed with an empty company name or a placeholder if desired
                    // companyName = "N/A"; // Example placeholder
                    // return; // Or simply return if company name is absolutely required
                }

                // --- Now that we have companyName, proceed to create JobOffer Object ---
                JobOffer jobOffer = new JobOffer();
                String newOfferId = UUID.randomUUID().toString();
                jobOffer.setOfferId(newOfferId);
                jobOffer.setEmployerUserId(currentUser.getUid());
                jobOffer.setJobTitle(jobTitle);
                jobOffer.setJobDescription(jobDescription);
                jobOffer.setAddress(address);
                jobOffer.setCompanyName(companyName); // Use the fetched companyName
                jobOffer.setRequiredSkills(skills);
                jobOffer.setJobType(jobType);
                jobOffer.setDateCreated(Timestamp.now());
                jobOffer.setStatus("Open");

                // --- Firestore Operation ---
                CollectionReference jobOffersCollection = db.collection("offers");
                WriteBatch batch = db.batch();
                batch.set(jobOffersCollection.document(newOfferId), jobOffer);
                batch.update(employerProfileRef, "offers", FieldValue.arrayUnion(newOfferId));

                batch.commit()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(EmployerCreateJobOfferActivity.this, "Job offer created successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error creating job offer batch", e);
                            Toast.makeText(EmployerCreateJobOfferActivity.this, "Failed to create job offer: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            buttonCreate.setEnabled(true);
                        });

            } else {
                // Employer profile document doesn't exist
                Log.e(TAG, "Employer profile not found for UID: " + currentUser.getUid());
                Toast.makeText(EmployerCreateJobOfferActivity.this, "Employer profile not found. Cannot create offer.", Toast.LENGTH_LONG).show();
                buttonCreate.setEnabled(true);
            }
        }).addOnFailureListener(e -> {
            // Failed to fetch employer profile
            Log.e(TAG, "Error getting employer profile for company name", e);
            Toast.makeText(EmployerCreateJobOfferActivity.this, "Failed to get company details: " + e.getMessage(), Toast.LENGTH_LONG).show();
            buttonCreate.setEnabled(true);
        });
    }
}
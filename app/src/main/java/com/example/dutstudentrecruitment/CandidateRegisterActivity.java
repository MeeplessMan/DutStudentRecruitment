package com.example.dutstudentrecruitment;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CheckBox;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CandidateRegisterActivity extends AppCompatActivity {
    StorageReference storageReference;
    Uri image;
    Uri pdfUri;
    ImageView imgProfile;
    ImageView imgUploadPic;
    Button buttonSubmit;
    EditText editTextFirstName;
    EditText editTextLastName;
    EditText editTextPhone;
    EditText editTextQualification;
    FirebaseFirestore db;
    FirebaseAuth auth;
    FirebaseUser user;
    EditText editTextStudentNumber;
    Button buttonLater;
    ImageView imgUploadCv;
    TextView textViewCv;
    private String profileImageUrlToSave = null;
    private String cvUrlToSave = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_candidate_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        buttonSubmit = findViewById(R.id.buttonSubmit);
        buttonLater = findViewById(R.id.buttonLater);
        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextStudentNumber = findViewById(R.id.editTextStudentNumber);
        editTextQualification = findViewById(R.id.editTextQualification);
        buttonLater = findViewById(R.id.buttonLater);
        imgUploadPic = findViewById(R.id.imgUploadPic);
        imgProfile = findViewById(R.id.imgProfile);
        imgUploadCv = findViewById(R.id.imgUploadCv);
        textViewCv = findViewById(R.id.textViewCv);
        Button buttonAddSkillField = findViewById(R.id.buttonAddSkillField);
        Button buttonAddQualificationField = findViewById(R.id.buttonAddQualificationField);
        Button buttonAddJobField = findViewById(R.id.buttonAddJobField);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        storageReference = FirebaseStorage.getInstance().getReference();

        buttonAddJobField.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               LinearLayout skillsContainer = findViewById(R.id.jobContainer); // Use binding
               int childCount = skillsContainer.getChildCount();

               if (childCount > 0) {
                   View lastSkillRowView = skillsContainer.getChildAt(childCount - 1);
                   EditText lastEditTextSkill = lastSkillRowView.findViewById(R.id.editTextSkillItem);

                   if (TextUtils.isEmpty(lastEditTextSkill.getText().toString().trim())) {
                       Toast.makeText(CandidateRegisterActivity.this, "Please fill in the current job title before adding another.", Toast.LENGTH_SHORT).show();
                       return;
                   }
               }
               addJobView(null);
           }
        });

        buttonAddQualificationField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout skillsContainer = findViewById(R.id.qualificationsContainer); // Use binding
                int childCount = skillsContainer.getChildCount();
                if(editTextQualification.getText().toString().trim().isEmpty()){
                    Toast.makeText(CandidateRegisterActivity.this, "Please fill in the current qualification before adding another.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (childCount > 0) {
                    View lastSkillRowView = skillsContainer.getChildAt(childCount - 1);
                    EditText lastEditTextSkill = lastSkillRowView.findViewById(R.id.editTextSkillItem);

                    if (TextUtils.isEmpty(lastEditTextSkill.getText().toString().trim())) {
                        Toast.makeText(CandidateRegisterActivity.this, "Please fill in the current qualification before adding another.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                addQualificationView(null);
            }
        });

        buttonAddSkillField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout skillsContainer = findViewById(R.id.skillsContainer); // Use binding
                int childCount = skillsContainer.getChildCount();

                if (childCount > 0) {
                    View lastSkillRowView = skillsContainer.getChildAt(childCount - 1);
                    EditText lastEditTextSkill = lastSkillRowView.findViewById(R.id.editTextSkillItem);

                    if (TextUtils.isEmpty(lastEditTextSkill.getText().toString().trim())) {
                        Toast.makeText(CandidateRegisterActivity.this, "Please fill in the current skill before adding another.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                addSkillView(null);
            }
        });

        imgUploadPic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                activityResultLauncher.launch(intent);
            }
        });

        buttonLater.setOnClickListener(v -> {
            AlertDialog alertDialog = new AlertDialog.Builder(CandidateRegisterActivity.this).create();
            alertDialog.setTitle("Alert");
            alertDialog.setMessage("Do you want to skip profile set-up");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", (dialog, which) -> {
                startActivity(new Intent(CandidateRegisterActivity.this, CandidateMainActivity.class));
                finish();
            });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", (dialog, which) -> {
                dialog.dismiss();
            });
            alertDialog.show();
        });

        buttonSubmit.setOnClickListener(v ->{
           String firstName = editTextFirstName.getText().toString();
           String lastName = editTextLastName.getText().toString();
           String phone = editTextPhone.getText().toString();
           String studentNumber = editTextStudentNumber.getText().toString();
           String qualification = editTextQualification.getText().toString();
           if(inputIsValid(firstName, lastName, phone, studentNumber, qualification)){
                updateProfile(firstName, lastName, phone, studentNumber, qualification, collectQualificationsData(), collectSkillsData(), collectJobData());
           }else{
               Toast.makeText(CandidateRegisterActivity.this, "Invalid Input: not all fields are entered", Toast.LENGTH_SHORT).show();
           }
        });

        imgUploadCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT); // Or ACTION_OPEN_DOCUMENT
                intent.setType("application/pdf");
                intent.addCategory(Intent.CATEGORY_OPENABLE); // Good practice for ACTION_GET_CONTENT
                try {
                    pdfPickerLauncher.launch(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(CandidateRegisterActivity.this, "No PDF viewer/picker found.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private ArrayList<String> collectEmploymentTypes() {
        ArrayList<String> selectedTypes = new ArrayList<>();
        CheckBox fullTime = findViewById(R.id.checkboxFullTime);
        CheckBox partTime = findViewById(R.id.checkboxPartTime);
        CheckBox contract = findViewById(R.id.checkboxContract);
        CheckBox internship = findViewById(R.id.checkboxInternship);
        CheckBox temporary = findViewById(R.id.checkboxTemporary);
        if (fullTime.isChecked()) {
            selectedTypes.add("Full-time");
        }
        if (partTime.isChecked()) {
            selectedTypes.add("Part-time");
        }
        if (contract.isChecked()) {
            selectedTypes.add("Contract");
        }
        if (internship.isChecked()) {
            selectedTypes.add("Internship");
        }
        if (temporary.isChecked()) {
            selectedTypes.add("Temporary");
        }
        return selectedTypes;
    }
    private boolean inputIsValid(String firstName, String lastName, String phone, String studentNumber, String qualification){
        return !firstName.isEmpty() && !lastName.isEmpty() && !phone.isEmpty()&& !studentNumber.isEmpty();
    }
    private void updateProfile(String firstName, String lastName, String phone,String studentNumber, String qualification, ArrayList<String> qualifications, ArrayList<String> skills, ArrayList<String> jobs){
        Map<String, Object> profile = new HashMap<>();
        profile.put("firstName", firstName);
        profile.put("lastName", lastName);
        profile.put("phoneNumber", phone);
        profile.put("studentNumber", studentNumber);
        profile.put("qualification", qualification); // Main qualification
        profile.put("skills", skills); // Assuming skills is an ArrayList<String>
        profile.put("qualifications", qualifications); // Assuming qualifications is an ArrayList<String>
        profile.put("jobs", jobs); // Assuming jobs is an ArrayList<String> or similar
        profile.put("employmentTypes", collectEmploymentTypes()); // Or pass it as a parameter

// Conditionally add URLs
        if (profileImageUrlToSave != null && !profileImageUrlToSave.isEmpty()) {
            profile.put("profileImageUrl", profileImageUrlToSave);
        }
        if (cvUrlToSave != null && !cvUrlToSave.isEmpty()) {
            profile.put("cvUrl", cvUrlToSave);
        }
        db.collection("candidate").document(user.getUid()).update(profile)
                .addOnCompleteListener(CandidateRegisterActivity.this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(CandidateRegisterActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(CandidateRegisterActivity.this, CandidateMainActivity.class));
                    finish();
                }else{
                    Toast.makeText(CandidateRegisterActivity.this, "Profile Update Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadImage(Uri image){
        StorageReference reference = storageReference.child("profilePic").child(user.getUid());
        reference.putFile(image)
                .addOnSuccessListener(taskSnapshot -> {
                    // Image uploaded successfully, now get the download URL
                    reference.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        profileImageUrlToSave = downloadUri.toString(); // Store the URL
                        Toast.makeText(CandidateRegisterActivity.this, "Profile Picture Uploaded!", Toast.LENGTH_SHORT).show();

                    }).addOnFailureListener(e -> {
                        Toast.makeText(CandidateRegisterActivity.this, "Failed to get image URL.", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CandidateRegisterActivity.this, "Image Upload Failed.", Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadCv(Uri pdf){

        StorageReference reference = storageReference.child("cv").child(user.getUid());
        reference.putFile(pdf)
                .addOnSuccessListener(taskSnapshot -> {
                    reference.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        cvUrlToSave = downloadUri.toString(); // Store the URL
                        Toast.makeText(CandidateRegisterActivity.this, "CV Uploaded Successfully!", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(CandidateRegisterActivity.this, "Failed to get CV URL.", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CandidateRegisterActivity.this, "CV Upload Failed.", Toast.LENGTH_SHORT).show();
                });
    }

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>(){
        @Override
        public void onActivityResult(ActivityResult result){
            if(result.getResultCode() == RESULT_OK){
                if(result.getData() != null){
                    image = result.getData().getData();
                    imgProfile.setImageURI(image);
                    uploadImage(image);
                    Glide.with(CandidateRegisterActivity.this).load(image).into(imgProfile);
                }
            }else{
                Toast.makeText(CandidateRegisterActivity.this, "No Image Selected", Toast.LENGTH_SHORT).show();
            }
        }
    });

    private final ActivityResultLauncher<Intent> pdfPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            pdfUri = data.getData();
                            String fileName = getFileName(pdfUri);
                            textViewCv.setText(fileName); // Use the TextView you already have: R.id.textViewCv
                            uploadCv(pdfUri);
                            Toast.makeText(CandidateRegisterActivity.this, "PDF Selected: " + fileName, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CandidateRegisterActivity.this, "No PDF selected.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(CandidateRegisterActivity.this, "PDF selection cancelled.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @SuppressLint("Range") // Suppress warning for Document.COLUMN_DISPLAY_NAME
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
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

    private void addQualificationView(String qualificationText){
        // The LinearLayout for all skill rows
        final LinearLayout skillsContainer = findViewById(R.id.qualificationsContainer);

        // Inflate the skill_input_item.xml layout
        LayoutInflater inflater = LayoutInflater.from(this);
        final View skillRowView = inflater.inflate(R.layout.skill_input_item, skillsContainer, false);

        EditText editTextSkill = skillRowView.findViewById(R.id.editTextSkillItem);
        ImageButton removeButton = skillRowView.findViewById(R.id.buttonRemoveSkillItem);

        editTextSkill.setHint("Enter Qualification");
        // If skillText is provided (e.g., when loading existing data), set it
        if (qualificationText != null && !qualificationText.isEmpty()) {
            editTextSkill.setText(qualificationText);
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

    private ArrayList<String> collectQualificationsData() {
        ArrayList<String> skillsList = new ArrayList<>();
        LinearLayout skillsContainer = findViewById(R.id.qualificationsContainer);

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

    private void addJobView(String qualificationText){
        // The LinearLayout for all skill rows
        final LinearLayout skillsContainer = findViewById(R.id.jobContainer);

        // Inflate the skill_input_item.xml layout
        LayoutInflater inflater = LayoutInflater.from(this);
        final View skillRowView = inflater.inflate(R.layout.skill_input_item, skillsContainer, false);

        EditText editTextSkill = skillRowView.findViewById(R.id.editTextSkillItem);
        ImageButton removeButton = skillRowView.findViewById(R.id.buttonRemoveSkillItem);

        editTextSkill.setHint("Enter Job Title");
        // If skillText is provided (e.g., when loading existing data), set it
        if (qualificationText != null && !qualificationText.isEmpty()) {
            editTextSkill.setText(qualificationText);
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

    private ArrayList<String> collectJobData() {
        ArrayList<String> skillsList = new ArrayList<>();
        LinearLayout skillsContainer = findViewById(R.id.jobContainer);

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
}
package com.example.dutstudentrecruitment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Map;

public class EmployerRegisterActivity extends AppCompatActivity {
    StorageReference storageReference;
    Uri image;
    ImageView imgProfile;
    EditText editTextCompany;
    EditText editTextContactEmail;
    EditText editTextPhone;
    EditText editTextSlogan;
    EditText editTextAddress;
    EditText editTextTextBusinessDescription;
    Button buttonSubmit;
    Button buttonLater;
    ImageView imgUploadPic;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employer_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextCompany = findViewById(R.id.editTextCompany);
        editTextContactEmail = findViewById(R.id.editTextContactEmail);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextSlogan = findViewById(R.id.editTextSlogan);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextTextBusinessDescription = findViewById(R.id.editTextTextBusinessDescription);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        buttonLater = findViewById(R.id.buttonLater);
        imgUploadPic = findViewById(R.id.imgUploadPic);
        imgProfile = findViewById(R.id.imgProfile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        imgUploadPic.setOnClickListener(new View.OnClickListener(){
           @Override
           public void onClick(View v){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                activityResultLauncher.launch(intent);
           }
        });

        buttonLater.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                AlertDialog builder = new AlertDialog.Builder(EmployerRegisterActivity.this).create();
                builder.setTitle("Finish Later");
                builder.setMessage("Do you want to skip profile set-up");
                builder.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", (dialog, which) -> {
                    startActivity(new Intent(EmployerRegisterActivity.this, EmployerMainActivity.class));
                    finish();
                });
                builder.setButton(AlertDialog.BUTTON_NEGATIVE, "No", (dialog, which) -> {
                    dialog.dismiss();
                });
                builder.show();
            }
        });

        buttonSubmit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String company = editTextCompany.getText().toString();
                String contactEmail = editTextContactEmail.getText().toString();
                String phone = editTextPhone.getText().toString();
                String slogan = editTextSlogan.getText().toString();
                String address = editTextAddress.getText().toString();
                String businessDescription = editTextTextBusinessDescription.getText().toString();
                if(inputIsValid(company, contactEmail, phone, slogan, businessDescription)){
                    updateProfile(company, contactEmail, phone, slogan, address, businessDescription);
                }else{
                    Toast.makeText(EmployerRegisterActivity.this, "Invalid Input: not all fields are entered", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean inputIsValid(String company, String contactEmail, String phone, String slogan, String businessDescription){
        return !company.isEmpty() && !contactEmail.isEmpty() && !phone.isEmpty() && !slogan.isEmpty() && !businessDescription.isEmpty();
    }

    private void updateProfile(String company, String contactEmail, String phone, String slogan, String address, String businessDescription){
        if(image != null){
            uploadImage(image);
        }
        Map<String, Object> profile = Map.of(
                "company", company,
                "contactEmail", contactEmail,
                "phone", phone,
                "slogan", slogan,
                "address", address,
                "businessDescription", businessDescription
        );
        db.collection("employer").document(user.getUid()).update(profile)
                .addOnCompleteListener(EmployerRegisterActivity.this, task -> {
                   if(task.isSuccessful()){
                       Toast.makeText(EmployerRegisterActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                       startActivity(new Intent(EmployerRegisterActivity.this, EmployerMainActivity.class));
                       finish();
                   }else{
                       Toast.makeText(EmployerRegisterActivity.this, "Profile Update Failed", Toast.LENGTH_SHORT).show();
                   }
                });
    }

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>(){
        @Override
        public void onActivityResult(ActivityResult result){
            if(result.getResultCode() == RESULT_OK){
                if(result.getData() != null){
                    image = result.getData().getData();
                    imgProfile.setImageURI(image);
                    Glide.with(EmployerRegisterActivity.this).load(image).into(imgProfile);
                }
            }else{
                Toast.makeText(EmployerRegisterActivity.this, "No Image Selected", Toast.LENGTH_SHORT).show();
            }
        }
    });

    private void uploadImage(Uri image){
        StorageReference reference = storageReference.child("profilePic").child(user.getUid());
        reference.putFile(image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>(){
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot){
                Toast.makeText(EmployerRegisterActivity.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener(){
            @Override
            public void onFailure(@NonNull Exception e){
                Toast.makeText(EmployerRegisterActivity.this, "Image Upload Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
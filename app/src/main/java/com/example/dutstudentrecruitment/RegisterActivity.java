package com.example.dutstudentrecruitment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    EditText editTextEmail;
    EditText editTextPassword;
    RadioGroup radioGroupType;
    RadioButton radioButtonCandidate;
    RadioButton radioButtonEmployer;
    Button buttonRegister;
    TextView textLoginRedirect;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();

        if(user != null){
            String email = user.getEmail();
            if(email.matches("^\\S+dut4life\\.ac\\.za$") || email.matches("^\\S+dut\\.ac\\.za$")){
                Intent intent = new Intent(RegisterActivity.this, CandidateMainActivity.class);
                startActivity(intent);
            }else{
                Intent intent = new Intent(RegisterActivity.this, EmployerMainActivity.class);
                startActivity(intent);
            }
        }

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        radioGroupType = findViewById(R.id.radioGroupType);
        radioButtonCandidate = findViewById(R.id.radioButtonCandidate);
        radioButtonEmployer = findViewById(R.id.radioButtonEmployer);
        buttonRegister = findViewById(R.id.buttonRegister);
        textLoginRedirect = findViewById(R.id.textLoginRedirect);


        textLoginRedirect.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();
                int selectedId = radioGroupType.getCheckedRadioButtonId();
                boolean type = selectedId == R.id.radioButtonCandidate;

                if(type && !(email.matches("^\\S+dut4life\\.ac\\.za$")||email.matches("^\\S+dut\\.ac\\.za$"))){
                    Toast.makeText(RegisterActivity.this, "Invalid Email or Wrong type selected", Toast.LENGTH_SHORT).show();
                }
                if(isValidEmail(email) && isValidPassword(password)){
                    if(radioButtonCandidate.isChecked() || radioButtonEmployer.isChecked()){
                        registerUser(email, password, type);
                    }else{
                        Toast.makeText(RegisterActivity.this, "Please Select a Type", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(RegisterActivity.this, "Invalid Email or Password to Short", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private boolean isValidEmail(String email){
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    private boolean isValidPassword(String password){
        return password.length() >= 8;
    }
    private void registerUser(String email, String password, boolean type){
        if(type && (email.matches("^\\S+dut4life\\.ac\\.za$")||email.matches("^\\S+dut\\.ac\\.za$"))){
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Map<String, Object> user = new HashMap<>();
                        user.put("email", email);
                        db.collection("candidate").document(auth.getCurrentUser().getUid()).set(user);
                        Toast.makeText(RegisterActivity.this, "Candidate Registered Successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, VerifyActivity.class);
                        intent.putExtra("type", "candidate");
                        startActivity(intent);
                    }else{
                        Toast.makeText(RegisterActivity.this, "Candidate Registration Failed: User Exists or Invalid Email", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else if(!type && !(email.matches("^\\S+dut4life\\.ac\\.za$")||email.matches("^\\S+dut\\.ac\\.za$"))){
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Map<String, Object> user = new HashMap<>();
                        user.put("email", email);
                        db.collection("employer").document(auth.getCurrentUser().getUid()).set(user);
                        Toast.makeText(RegisterActivity.this, "Employer Registered Successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, VerifyActivity.class);
                        intent.putExtra("type", "employer");
                        startActivity(intent);
                    }else{
                        Toast.makeText(RegisterActivity.this, "Employer Registration Failed: User Exists or Invalid Email", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else{
            Toast.makeText(RegisterActivity.this, "Invalid Email or Wrong type selected", Toast.LENGTH_SHORT).show();
        }
    }
}
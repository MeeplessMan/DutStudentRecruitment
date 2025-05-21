package com.example.dutstudentrecruitment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
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

public class LoginActivity extends AppCompatActivity {
    EditText editTextEmail;
    EditText editTextPassword;
    Button buttonLogin;
    TextView textLoginRedirect;
    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textLoginRedirect = findViewById(R.id.textLoginRedirect);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if(user != null){
            String email = user.getEmail();
            if(email.matches("^\\S+dut4life\\.ac\\.za$") || email.matches("^\\S+dut\\.ac\\.za$")){
                Intent intent = new Intent(LoginActivity.this, CandidateMainActivity.class);
                finish();
                startActivity(intent);
            }else{
                Intent intent = new Intent(LoginActivity.this, EmployerMainActivity.class);
                finish();
                startActivity(intent);
            }
        }

        textLoginRedirect.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();

                if(isValidEmail(email) && isValidPassword(password)){
                    loginUser(email, password);
                }else{
                    Toast.makeText(LoginActivity.this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void loginUser(String email, String password){
        if(email.matches("^\\S+dut4life\\.ac\\.za$")||email.matches("^\\S+dut\\.ac\\.za$")){
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(LoginActivity.this, "Candidate Logged In Successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, VerifyActivity.class);
                        intent.putExtra("type", "candidate");
                        intent.putExtra("login", true);
                        finish();
                        startActivity(intent);
                    }else{
                        Toast.makeText(LoginActivity.this, "Candidate Login Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else{
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>(){
                @Override
                public void onComplete(@NonNull Task<AuthResult> task){
                    if(task.isSuccessful()){
                        Toast.makeText(LoginActivity.this, "Employer Logged In Successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, VerifyActivity.class);
                        intent.putExtra("type", "employer");
                        intent.putExtra("login", true);
                        finish();
                        startActivity(intent);
                    }else{
                        Toast.makeText(LoginActivity.this, "Employer Login Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    private Boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    private Boolean isValidPassword(String password) {
        return password.length() >= 8;
    }
}
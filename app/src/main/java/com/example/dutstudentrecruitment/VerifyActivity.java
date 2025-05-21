package com.example.dutstudentrecruitment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import android.widget.Toast;
import android.content.Intent;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.TimerTask;

public class VerifyActivity extends AppCompatActivity {
    FirebaseAuth auth;
    Button buttonSend;
    Button buttonCheck;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verify);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.motionLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if(user == null){
            Intent intent = new Intent(VerifyActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        buttonSend = findViewById(R.id.buttonSend);
        buttonCheck = findViewById(R.id.buttonCheck);
        String type = getIntent().getStringExtra("type");
        Boolean login = getIntent().getBooleanExtra("login", false);
        buttonSend.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                user.sendEmailVerification().addOnCompleteListener(VerifyActivity.this, task -> {
                   if(task.isSuccessful()){
                       Toast.makeText(VerifyActivity.this, "Verification Email Sent", Toast.LENGTH_SHORT).show();
                       buttonSend.setEnabled(false);
                       buttonSend.setText("Email Sent");
                   }else{
                       Toast.makeText(VerifyActivity.this, "Verification Email Failed", Toast.LENGTH_SHORT).show();
                   }
                });
            }
        });

        buttonCheck.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                user.reload();
                if(user.isEmailVerified()){
                    if(login){
                        if(type.equals("candidate")) {
                            Toast.makeText(VerifyActivity.this, "Candidate Logged In Successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(VerifyActivity.this, CandidateMainActivity.class);
                            startActivity(intent);
                            finish();
                        }else{
                            Toast.makeText(VerifyActivity.this, "Employer Logged In Successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(VerifyActivity.this, EmployerMainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }else{
                        if(type.equals("candidate")) {
                            Toast.makeText(VerifyActivity.this, "Candidate Registered Successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(VerifyActivity.this, CandidateRegisterActivity.class);
                            startActivity(intent);
                            finish();
                        }else{
                            Toast.makeText(VerifyActivity.this, "Employer Registered Successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(VerifyActivity.this, EmployerRegisterActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                }else{
                    Toast.makeText(VerifyActivity.this, "Email Not Verified", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
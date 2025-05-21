package com.example.dutstudentrecruitment;

import android.os.Bundle;
import android.widget.Button;
import android.content.Intent;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    Button buttonLogin;
    Button buttonRegister;
    FirebaseAuth auth;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.motionLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if(user != null){
            loginUser();
        }
        buttonLogin.setOnClickListener(v->{
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        buttonRegister.setOnClickListener(v->{
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser(){
        if(isCandidate()){
            Intent intent = new Intent(MainActivity.this, CandidateMainActivity.class);
            Toast.makeText(MainActivity.this, "Welcome back", Toast.LENGTH_SHORT).show();
            startActivity(intent);
            finish();
        }else{
            Intent intent = new Intent(MainActivity.this, EmployerMainActivity.class);
            Toast.makeText(MainActivity.this, "Welcome back", Toast.LENGTH_SHORT).show();
            startActivity(intent);
            finish();
        }
    }

    private boolean isCandidate(){
       return user.getEmail().matches("^\\S+dut4life\\.ac\\.za$") || user.getEmail().matches("^\\S+dut\\.ac\\.za$");
    }
}
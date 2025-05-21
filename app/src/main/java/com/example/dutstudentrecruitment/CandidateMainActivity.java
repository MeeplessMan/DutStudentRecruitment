package com.example.dutstudentrecruitment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CandidateMainActivity extends AppCompatActivity {
    Button buttonSignOut;
    Button buttonProfile;
    FirebaseAuth auth;
    FirebaseUser user;
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

        buttonSignOut = findViewById(R.id.buttonSignOut);
        buttonProfile = findViewById(R.id.buttonProfile);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        buttonSignOut.setOnClickListener(v->{
            auth.signOut();
            Intent intent = new Intent(CandidateMainActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        buttonProfile.setOnClickListener(v->{
            Intent intent = new Intent(CandidateMainActivity.this, EmployerProfileActivity.class);
            startActivity(intent);
        });
    }
}
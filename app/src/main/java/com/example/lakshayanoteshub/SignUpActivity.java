package com.example.lakshayanoteshub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;

public class SignUpActivity extends AppCompatActivity {

    AppCompatButton signUpButton;
    TextInputEditText email, password, name;
    User user;
    ProgressDialog progressDialog;
    FirebaseAuth mAuth;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    CheckBox remember;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        windowFile f = new windowFile(getWindow(), getColor(R.color.backGround)).setStatusBar();

        mAuth = FirebaseAuth.getInstance();
        signUpButton = findViewById(R.id.signUp);
        email = findViewById(R.id.e_mail);
        password = findViewById(R.id.password);
        name = findViewById(R.id.name);
        remember = findViewById(R.id.remember);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (new Checker().isInternetAvailable()) {
                    if (remember.isChecked()) {
                        sharedPreferences = getApplicationContext().getSharedPreferences("userDetails", MODE_PRIVATE);
                        editor = sharedPreferences.edit();
                        editor.putString("email", email.getText().toString());
                        editor.putString("password", password.getText().toString());
                        editor.putBoolean("remember", true);
                        editor.commit();
                    }
                    signUp();
                } else {
                    Toast.makeText(SignUpActivity.this, "No Internet Available!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void signUp() {

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Signing Up...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        try {
            user = new User(email.getText().toString(), password.getText().toString());

            mAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("User");
                        databaseReference.child(email.getText().toString().replaceAll("\\.","dot")).setValue(name.getText().toString());
                        progressDialog.dismiss();
                        Intent intent = new Intent(SignUpActivity.this, Home.class);
                        startActivity(intent);
                        SignUpActivity.this.finish();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(SignUpActivity.this, "Enter Details According to format", Toast.LENGTH_LONG).show();
                    }
                }
            });

        } catch (Exception e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Please Enter Details.", Toast.LENGTH_LONG).show();
        }
    }

}

package com.example.lakshayanoteshub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    TextInputEditText email, password;
    User user;
    ProgressDialog progressDialog;
    CheckBox rememberCheckBox;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;


    public void changeToSignUp(View view) {
        Intent signUpInent = new Intent(this, SignUpActivity.class);
        startActivity(signUpInent);
    }

    public void login(String email, String password) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        if (new Checker().isInternetAvailable()) {
            mAuth = FirebaseAuth.getInstance();
            progressDialog.setMessage("Logging In...");
            progressDialog.show();
            user = new User(email, password);

            try {
                mAuth.signInWithEmailAndPassword(user.getEmail(), user.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent loginIntent = new Intent(MainActivity.this, Home.class);
                            startActivity(loginIntent);
                            MainActivity.this.finish();
                            progressDialog.dismiss();
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Incorrect Details!", Toast.LENGTH_SHORT).show();
                            Log.w("failed", task.getException());
                        }
                    }
                });
            } catch (Exception e) {
                progressDialog.dismiss();
                Toast.makeText(this, "Incorrect Details!", Toast.LENGTH_SHORT).show();
            }
        } else {
            progressDialog.setMessage("Loading Offline...");
            progressDialog.show();

            Intent loginIntent = new Intent(MainActivity.this, Home.class);
            startActivity(loginIntent);
            MainActivity.this.finish();
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getApplicationContext().getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean("remember", false)) {
            login(sharedPreferences.getString("email",""), sharedPreferences.getString("password", ""));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setContentView(R.layout.activity_main);
        rememberCheckBox = findViewById(R.id.remember);

        windowFile f = new windowFile(getWindow(), getColor(R.color.backGround)).setStatusBar();

        email = findViewById(R.id.e_mail);
        password = findViewById(R.id.password);

        AppCompatButton loginButton = findViewById(R.id.login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (new Checker().isInternetAvailable()) {
                    editor = sharedPreferences.edit();

                    if (rememberCheckBox.isChecked()) {
                        editor.putBoolean("remember", true);
                        editor.putString("email", email.getText().toString());
                        editor.putString("password", password.getText().toString());
                        editor.apply();
                    } else {
                        editor.putBoolean("remember", false);
                        editor.commit();
                    }

                    login(email.getText().toString(), password.getText().toString());
                } else {
                    Toast.makeText(MainActivity.this, "No Internet Available!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

package com.example.aurtisticsv;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    EditText mEmailEt;
    TextView mHaveAccountTv;
    EditText mPasswordEt;
    Button mRegisterBtn;
    ProgressDialog progressDialog;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create Account");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        this.mEmailEt = (EditText) findViewById(R.id.emailEt);
        this.mPasswordEt = (EditText) findViewById(R.id.passwordEt);
        this.mRegisterBtn = (Button) findViewById(R.id.register_btn);
        this.mHaveAccountTv = (TextView) findViewById(R.id.have_accountTv);
        this.mAuth = FirebaseAuth.getInstance();
        ProgressDialog progressDialog = new ProgressDialog(this);
        this.progressDialog = progressDialog;
        progressDialog.setMessage("Registering User...");
        this.mRegisterBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String email = RegisterActivity.this.mEmailEt.getText().toString().trim();
                String password = RegisterActivity.this.mPasswordEt.getText().toString().trim();
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    RegisterActivity.this.mEmailEt.setError("Invalid Email");
                    RegisterActivity.this.mEmailEt.setFocusable(true);
                } else if (password.length() < 6) {
                    RegisterActivity.this.mPasswordEt.setError("Password length at least 6 characters");
                    RegisterActivity.this.mPasswordEt.setFocusable(true);
                } else {
                    RegisterActivity.this.registerUser(email, password);
                }
            }
        });
        this.mHaveAccountTv.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                RegisterActivity.this.startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                RegisterActivity.this.finish();
            }
        });
    }

    private void registerUser(String email, String password) {
        this.progressDialog.show();
        this.mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            public void onComplete(Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    RegisterActivity.this.progressDialog.dismiss();
                    FirebaseUser user = RegisterActivity.this.mAuth.getCurrentUser();
                    String email = user.getEmail();
                    String uid = user.getUid();
                    HashMap<Object, String> hashMap = new HashMap();
                    hashMap.put("email", email);
                    hashMap.put("uid", uid);
                    String str = "";
                    hashMap.put("name", str);
                    hashMap.put("onlineStatus", "online");
                    hashMap.put("typingTo", "noOne");
                    hashMap.put("phone", str);
                    hashMap.put("image", str);
                    hashMap.put("cover", str);
                    FirebaseDatabase.getInstance().getReference("Users").child(uid).setValue(hashMap);
                    Toast.makeText(RegisterActivity.this, "Registered...\n" + user.getEmail(), Toast.LENGTH_SHORT).show();
                    RegisterActivity.this.startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                    RegisterActivity.this.finish();
                    return;
                }
                RegisterActivity.this.progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(Exception e) {
                RegisterActivity.this.progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}

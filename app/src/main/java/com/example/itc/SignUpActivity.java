package com.example.itc;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SIGN_UP_ACTIVITY";

    private EditText et_name,et_email,et_password,et_confirm_password;
    private Button btn_signup;
    private ProgressBar mProgressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        et_name = findViewById(R.id.et_name);
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password_signup);
        et_confirm_password = findViewById(R.id.et_confirm_passwor_signup);
        mProgressBar = findViewById(R.id.progressBar_signup);
        btn_signup = findViewById(R.id.button_signup_signup);

        mAuth = FirebaseAuth.getInstance();

        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                String email = et_email.getText().toString().trim();
                String password = et_password.getText().toString().trim();
                createAccountWithEmailPassword(email,password);
            }
        });
    }

    private void createAccountWithEmailPassword(String email,String password){
        if(et_name.getText().toString().trim().isEmpty()){
            et_name.setError("Name is required");
            et_name.requestFocus();
            mProgressBar.setVisibility(View.INVISIBLE);
            return;
        }
        if(email.isEmpty()){
            et_email.setError("Email is required");
            et_email.requestFocus();
            mProgressBar.setVisibility(View.INVISIBLE);
            return;
        }
        if(password.isEmpty()){
            et_password.setError("Password is required");
            et_password.requestFocus();
            mProgressBar.setVisibility(View.INVISIBLE);
            return;
        }
        if(password.length()<6){
            et_password.setError("Passwrod must be atleast 6 digit long");
            et_password.requestFocus();
            mProgressBar.setVisibility(View.INVISIBLE);
            return;
        }
        if(!password.equals(et_confirm_password.getText().toString())){
            et_confirm_password.setError("Password doesn't matches");
            et_confirm_password.requestFocus();
            mProgressBar.setVisibility(View.INVISIBLE);
            return;
        }
        if(mAuth != null){
            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            mProgressBar.setVisibility(View.INVISIBLE);
                            if(task.isSuccessful()){
                                Toast.makeText(SignUpActivity.this,"Account successfully created",Toast.LENGTH_LONG).show();
                                updateUserDetails();
                                openUploadImageActivity();
                            }else {
                                if(task.getException() instanceof FirebaseAuthUserCollisionException){
                                    Toast.makeText(SignUpActivity.this,"Email already exists, plaese try login",Toast.LENGTH_LONG).show();
                                }else {
                                    Toast.makeText(SignUpActivity.this,"Something went wrong!!",Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
        }
    }

    private void updateUserDetails(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if(task.isSuccessful()){
                            String token = task.getResult().getToken();
                            saveUserDetails(token);
                        }else {
                            Toast.makeText(SignUpActivity.this,"Problem fetching token",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserDetails(String token){
        String email = mAuth.getCurrentUser().getEmail();
        String userName = et_name.getText().toString().trim();
        User user  = User.getUserInstance();
        user.setName(userName);user.setEmail(email);user.setToken(token);
        DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference("users");
        dbUser.child(mAuth.getCurrentUser().getUid())
                .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(SignUpActivity.this,"Data saved",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(SignUpActivity.this,"Unable to save user's data",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openLoginActivity(){
        Intent loginIntent = new Intent(this,LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginIntent);
    }
    private void openUploadImageActivity(){
        Intent uploadImageIntent = new Intent(this,UploadImageActivity.class);
        startActivity(uploadImageIntent);
    }
}

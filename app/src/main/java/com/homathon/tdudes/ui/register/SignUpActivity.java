package com.homathon.tdudes.ui.register;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.homathon.tdudes.R;
import com.homathon.tdudes.data.User;
import com.homathon.tdudes.databinding.ActivitySignUpBinding;
import com.homathon.tdudes.ui.hospital.main.HospitalHomeActivity;
import com.homathon.tdudes.ui.users.main.MainActivity;
import com.homathon.tdudes.utills.SharedPrefManager;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding signUpBinding;
    private FirebaseAuth mAuth;
    private static final String TAG = "SignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        signUpBinding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();

        signUpBinding.signUp.setOnClickListener(v -> {
            ((InputMethodManager) Objects.requireNonNull(getSystemService(Context.INPUT_METHOD_SERVICE))).hideSoftInputFromWindow(signUpBinding.getRoot().getWindowToken(), 0);
            if(validate()){
                signUpBinding.loading.setVisibility(View.VISIBLE);
                mAuth.createUserWithEmailAndPassword(signUpBinding.txtUserEmail.getText().toString(), signUpBinding.txtPassword.getText().toString())
                        .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(signUpBinding.txtUserName.getText().toString())
                                .setPhotoUri(Uri.parse(signUpBinding.txtPhoneNumber.getText().toString()))
                                .build();
                        if (firebaseUser != null) {
                            firebaseUser.updateProfile(profileUpdates)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            SharedPrefManager sharedPrefManager = new SharedPrefManager(SignUpActivity.this);
                                            User user = new User();
                                            user.setEmail(signUpBinding.txtUserEmail.getText().toString());
                                            user.setName(signUpBinding.txtUserName.getText().toString());
                                            user.setPhone(signUpBinding.txtPhoneNumber.getText().toString());
                                            user.setId(firebaseUser.getUid());
                                            sharedPrefManager.setUserData(user);
                                            Log.d(TAG, "User profile updated.");
                                            if(user.getPhone().endsWith("0"))
                                                startActivity(new Intent(SignUpActivity.this, HospitalHomeActivity.class));
                                            else if(user.getPhone().endsWith("1"))
                                                startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                            else
                                                startActivity(new Intent(SignUpActivity.this, MainActivity.class));

                                            finishAffinity();
                                        }
                                    });
                        }


                    } else {
                        signUpBinding.loading.setVisibility(View.GONE);
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private boolean validate() {
        if(signUpBinding.txtUserEmail.getText() == null || signUpBinding.txtUserEmail.getText().toString().isEmpty()){
            signUpBinding.txtUserEmail.setError(getResources().getString(R.string.required_field));
            signUpBinding.txtUserEmail.requestFocus();
            return false;
        }
        if(signUpBinding.txtUserName.getText() == null || signUpBinding.txtUserName.getText().toString().isEmpty()){
            signUpBinding.txtUserName.setError(getResources().getString(R.string.required_field));
            signUpBinding.txtUserName.requestFocus();
            return false;
        }
        if(signUpBinding.txtPhoneNumber.getText() == null || signUpBinding.txtPhoneNumber.getText().toString().isEmpty()){
            signUpBinding.txtPhoneNumber.setError(getResources().getString(R.string.required_field));
            signUpBinding.txtPhoneNumber.requestFocus();
            return false;
        }
        if(signUpBinding.txtPassword.getText() == null || signUpBinding.txtPassword.getText().toString().isEmpty()){
            signUpBinding.txtPassword.setError(getResources().getString(R.string.required_field));
            signUpBinding.txtPassword.requestFocus();
            return false;
        }
        return true;
    }
}

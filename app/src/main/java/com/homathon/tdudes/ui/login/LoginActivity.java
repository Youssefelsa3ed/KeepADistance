package com.homathon.tdudes.ui.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.homathon.tdudes.R;
import com.homathon.tdudes.data.User;
import com.homathon.tdudes.databinding.ActivityLoginBinding;
import com.homathon.tdudes.ui.hospital.main.HospitalHomeActivity;
import com.homathon.tdudes.ui.infected.main.MainActivity;
import com.homathon.tdudes.ui.register.SignUpActivity;
import com.homathon.tdudes.utills.SharedPrefManager;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    private ActivityLoginBinding loginBinding;
    private FirebaseAuth mAuth;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        loginBinding.login.setOnClickListener(this);
        loginBinding.signUp.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View v) {
        ((InputMethodManager) Objects.requireNonNull(getSystemService(Context.INPUT_METHOD_SERVICE))).hideSoftInputFromWindow(loginBinding.getRoot().getWindowToken(), 0);
        if(v.getId() == R.id.login){
            if(validate()) {
                loginBinding.loading.setVisibility(View.VISIBLE);
                mAuth.signInWithEmailAndPassword(loginBinding.txtUserEmail.getText().toString(), loginBinding.txtPassword.getText().toString())
                        .addOnCompleteListener(this, task -> {
                            loginBinding.loading.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                SharedPrefManager sharedPrefManager = new SharedPrefManager(LoginActivity.this);
                                User user = new User();
                                user.setEmail(firebaseUser.getEmail());
                                user.setName(firebaseUser.getDisplayName());
                                user.setPhone(firebaseUser.getPhotoUrl().toString());
                                user.setId(firebaseUser.getUid());
                                sharedPrefManager.setUserData(user);
                                if(user.getPhone().endsWith("0"))
                                    startActivity(new Intent(LoginActivity.this, HospitalHomeActivity.class));
                                else if(user.getPhone().endsWith("1"))
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                else
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));

                                finish();
                            } else {
                                loginBinding.loading.setVisibility(View.GONE);
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }

                            // ...
                        });
            }
        }
        if(v.getId() == R.id.signUp)
            startActivity(new Intent(this, SignUpActivity.class));
    }

    private boolean validate() {
        if(loginBinding.txtUserEmail.getText().toString().isEmpty()){
            loginBinding.txtUserEmail.setError(getResources().getString(R.string.required_field));
            loginBinding.txtUserEmail.requestFocus();
            return false;
        }
        if(loginBinding.txtPassword.getText().toString().isEmpty()){
            loginBinding.txtPassword.setError(getResources().getString(R.string.required_field));
            loginBinding.txtPassword.requestFocus();
            return false;
        }
        return true;
    }
}

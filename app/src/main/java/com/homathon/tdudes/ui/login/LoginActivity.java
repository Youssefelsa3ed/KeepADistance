package com.homathon.tdudes.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.homathon.tdudes.R;
import com.homathon.tdudes.data.User;
import com.homathon.tdudes.databinding.ActivityLoginBinding;
import com.homathon.tdudes.ui.hospital.main.HospitalHomeActivity;
import com.homathon.tdudes.ui.infected.main.MainActivity;
import com.homathon.tdudes.utills.SharedPrefManager;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    private ActivityLoginBinding loginBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        loginBinding.login.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.login){
            if(validate()) { // TODO: 4/22/2020 wait some seconds
                /*new CountDownTimer(3000, 1000) {

                    public void onTick(long millisUntilFinished) {
                    }

                    public void onFinish() {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                }.start();*/
                SharedPrefManager sharedPrefManager = new SharedPrefManager(this);
                User user = new User();
                user.setPhone(loginBinding.txtMobileNumber.getText().toString());
                if(loginBinding.txtMobileNumber.getText().toString().endsWith("0")){
                    sharedPrefManager.setUserData(user);
                    startActivity(new Intent(LoginActivity.this, HospitalHomeActivity.class));
                }
                else if(loginBinding.txtMobileNumber.getText().toString().endsWith("1")) {
                    user.setName("John Doe");
                    user.setEmail("john_doe@doee.com");
                    sharedPrefManager.setUserData(user);
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                }
                else {
                    user.setName("Jane Hue");
                    user.setEmail("jane_hue@doee.com");
                    sharedPrefManager.setUserData(user);
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                }

                finish();
            }
        }
    }

    private boolean validate() {
        if(loginBinding.txtMobileNumber.getText().toString().isEmpty()){
            loginBinding.txtMobileNumber.setError(getResources().getString(R.string.required_field));
            loginBinding.txtMobileNumber.requestFocus();
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

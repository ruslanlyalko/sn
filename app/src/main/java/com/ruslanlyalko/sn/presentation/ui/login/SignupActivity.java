package com.ruslanlyalko.sn.presentation.ui.login;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ruslanlyalko.sn.R;
import com.ruslanlyalko.sn.data.FirebaseUtils;
import com.ruslanlyalko.sn.data.configuration.DC;
import com.ruslanlyalko.sn.data.models.User;
import com.ruslanlyalko.sn.presentation.base.BaseActivity;
import com.ruslanlyalko.sn.presentation.ui.splash.SplashActivity;

import butterknife.BindView;
import butterknife.OnClick;

public class SignupActivity extends BaseActivity {

    @BindView(R.id.text_email) EditText inputEmail;
    @BindView(R.id.text_password) EditText inputPassword;
    @BindView(R.id.text_name) EditText inputName;
    @BindView(R.id.text_phone) EditText inputPhone;
    @BindView(R.id.progress_bar) ProgressBar progressBar;

    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_signup;
    }

    @Override
    protected void setupView() {
    }

    @OnClick(R.id.sign_up_button)
    void onSignUpClicked() {
        final String name = inputName.getText().toString().trim();
        final String phone = inputPhone.getText().toString().trim();
        final String email = inputEmail.getText().toString().trim();
        final String password = inputPassword.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            //Toast.makeText(getApplicationContext(), "Enter name!", Toast.LENGTH_SHORT).show();
            inputName.setError("Enter name!");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            //Toast.makeText(getApplicationContext(), "Enter phone!", Toast.LENGTH_SHORT).show();
            inputPhone.setError("Enter phone!");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            inputEmail.setError("Enter email!");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            inputPassword.setError("Enter password!");
            return;
        }
        if (password.length() < 6) {
            inputPassword.setError("Password too short, enter minimum 6 characters!");
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        //create user
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignupActivity.this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUtils.setIsAdmin(false);
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();
                        mUser.updateProfile(profileUpdate).addOnCompleteListener(task1 -> {
                            createUserData(name, phone, email, mUser.getUid());
                            Toast.makeText(SignupActivity.this, getResources().getString(R.string.toast_user_created)
                                    + " " + email, Toast.LENGTH_LONG).show();
                            startActivity(SplashActivity.getLaunchIntent(SignupActivity.this));
                        });
                    } else {
                        Toast.makeText(SignupActivity.this, "Authentication failed." + task.getException(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createUserData(String name, String phone, String email, String uId) {
        DatabaseReference databaseRefCurrentUser = mDatabase.getReference(DC.DB_USERS).child(uId);
        User user = new User(uId, name, phone, email, "01.06.1991", "01.06.1991", "1234", false);
        databaseRefCurrentUser.setValue(user);
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    @OnClick(R.id.button_reset_password)
    void onResetClicked() {
        startActivity(new Intent(SignupActivity.this, ResetPasswordActivity.class));
    }
}
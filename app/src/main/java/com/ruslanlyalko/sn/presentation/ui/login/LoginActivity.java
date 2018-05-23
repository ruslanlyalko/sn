package com.ruslanlyalko.sn.presentation.ui.login;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.ruslanlyalko.sn.R;
import com.ruslanlyalko.sn.presentation.base.BaseActivity;
import com.ruslanlyalko.sn.presentation.ui.main.MainActivity;

import butterknife.BindView;
import butterknife.OnClick;

public class LoginActivity extends BaseActivity {

    @BindView(R.id.text_email) AutoCompleteTextView inputEmail;
    @BindView(R.id.text_password) EditText inputPassword;
    @BindView(R.id.progress_bar) ProgressBar progressBar;

    public static Intent getLaunchIntent(final AppCompatActivity launchActivity) {
        return new Intent(launchActivity, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_login;
    }

    @Override
    protected void setupView() {
    }

    @OnClick(R.id.button_login)
    void onLoginClicked() {
        String email = inputEmail.getText().toString();
        final String password = inputPassword.getText().toString();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Введіть електронну скриньку!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Введіть пароль!", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (!task.isSuccessful()) {
                        if (password.length() < 6) {
                            inputPassword.setError(getString(R.string.toast_minimum_password));
                        } else {
                            Toast.makeText(LoginActivity.this, getString(R.string.toast_auth_failed), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }).addOnFailureListener(e -> Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @OnClick(R.id.button_reset_password)
    void onResetClicked() {
        startActivity(ResetPasswordActivity.getLaunchIntent(this));
    }
}


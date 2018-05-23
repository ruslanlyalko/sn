package com.ruslanlyalko.sn.presentation.ui.login;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.ruslanlyalko.sn.R;
import com.ruslanlyalko.sn.presentation.base.BaseActivity;

import butterknife.BindView;
import butterknife.OnClick;

public class ResetPasswordActivity extends BaseActivity {

    @BindView(R.id.text_email) EditText inputEmail;
    @BindView(R.id.progress_bar) ProgressBar progressBar;

    public static Intent getLaunchIntent(final AppCompatActivity launchActivity) {
        return new Intent(launchActivity, ResetPasswordActivity.class);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_reset_password;
    }

    @Override
    protected void setupView() {
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @OnClick(R.id.button_reset_password)
    void onResetClicked() {
        String email = inputEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplication(), "Введіть Вашу електронну скриньку", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ResetPasswordActivity.this, "Інструкція по відновленню пароля вислана на вашу скриньку!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ResetPasswordActivity.this, "Не можливо відправити лист. Можливо скринька не вірна!", Toast.LENGTH_SHORT).show();
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }
}

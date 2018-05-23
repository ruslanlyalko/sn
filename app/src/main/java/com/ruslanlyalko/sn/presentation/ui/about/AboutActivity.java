package com.ruslanlyalko.sn.presentation.ui.about;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.ruslanlyalko.sn.R;
import com.ruslanlyalko.sn.common.Keys;
import com.ruslanlyalko.sn.data.FirebaseUtils;
import com.ruslanlyalko.sn.data.configuration.DC;
import com.ruslanlyalko.sn.presentation.base.BaseActivity;

import butterknife.BindView;
import butterknife.OnClick;

public class AboutActivity extends BaseActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.text_version) TextView textVersion;
    @BindView(R.id.text_about) TextView textAbout;
    @BindView(R.id.edit_about) EditText editAbout;
    @BindView(R.id.fab) FloatingActionButton fab;

    public static Intent getLaunchIntent(Context context, String text) {
        Intent intent = new Intent(context, AboutActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_ABOUT, text);
        return intent;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_about;
    }

    @Override
    protected void parseExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String aboutText = bundle.getString(Keys.Extras.EXTRA_ABOUT, getString(R.string.text_about_company));
            textAbout.setText(aboutText);
            editAbout.setText(aboutText);
        }
    }

    @Override
    protected void setupView() {
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = getString(R.string.dialog_about_message) + "" + (pInfo != null ? pInfo.versionName : "");
        textVersion.setText(version);
        fab.setVisibility(FirebaseUtils.isAdmin() ? View.VISIBLE : View.GONE);
        if (FirebaseUtils.isAdmin()) {
            textAbout.setVisibility(View.GONE);
            editAbout.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.fab)
    void onFabClicked() {
        if (FirebaseUtils.isAdmin())
            FirebaseDatabase.getInstance().getReference(DC.DB_INFO)
                    .child(DC.ABOUT_TEXT)
                    .setValue(editAbout.getText().toString().trim())
                    .addOnCompleteListener(task ->
                            Toast.makeText(AboutActivity.this, R.string.toast_updated, Toast.LENGTH_SHORT).show());
    }
}

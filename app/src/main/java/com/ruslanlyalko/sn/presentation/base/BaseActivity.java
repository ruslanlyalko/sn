package com.ruslanlyalko.sn.presentation.base;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.ruslanlyalko.sn.R;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Ruslan Lyalko
 * on 28.01.2018.
 */

public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();
    private Unbinder mUnBinder;

    @CallSuper
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isFullScreen())
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        setContentView(getLayoutResource());
        mUnBinder = ButterKnife.bind(this);
        parseExtras();
        initToolbar();
        setupView();
        if (isModalView())
            overridePendingTransition(R.anim.fadein, R.anim.nothing);
        if (isLeftView())
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        if (isRightView())
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
    }

    @Override
    protected void onDestroy() {
        mUnBinder.unbind();
        super.onDestroy();
    }

    protected boolean isRightView() {
        return false;
    }

    protected boolean isLeftView() {
        return false;
    }

    protected boolean isFullScreen() {
        return false;
    }

    @LayoutRes
    protected abstract int getLayoutResource();

    protected void parseExtras() {
        Log.w(TAG, "parseExtras not implemented!");
    }

    protected void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    protected void copyToClipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
        }
    }

    protected abstract void setupView();

    protected boolean isModalView() {
        return false;
    }

    protected FirebaseUser getUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    protected FirebaseDatabase getDatabase() {
        return FirebaseDatabase.getInstance();
    }

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        hideKeyboard();
        super.onBackPressed();
        if (isModalView())
            overridePendingTransition(R.anim.nothing, R.anim.fadeout);
        if (isLeftView())
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        if (isRightView())
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }
}

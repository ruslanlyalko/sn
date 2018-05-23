package com.ruslanlyalko.sn.presentation.ui.main.clients.contacts.details.recharge_edit;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.ruslanlyalko.sn.R;
import com.ruslanlyalko.sn.common.DateUtils;
import com.ruslanlyalko.sn.common.Keys;
import com.ruslanlyalko.sn.data.configuration.DC;
import com.ruslanlyalko.sn.data.models.ContactRecharge;
import com.ruslanlyalko.sn.presentation.base.BaseActivity;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.OnClick;

public class RechargeEditActivity extends BaseActivity {

    @BindView(R.id.edit_title1) EditText mEditTitle1;
    @BindView(R.id.edit_price) EditText mEditPrice;
    @BindView(R.id.text_date) TextView mTextDate;
    private ContactRecharge mContactRecharge = new ContactRecharge();
    private boolean mNeedToSave = false;

    public static Intent getLaunchIntent(final Activity launchIntent, final ContactRecharge contactRecharge) {
        Intent intent = new Intent(launchIntent, RechargeEditActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_ITEM_ID, contactRecharge);
        return intent;
    }

    public static Intent getLaunchIntent(final Activity launchIntent, String contactKey) {
        Intent intent = new Intent(launchIntent, RechargeEditActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_CONTACT_KEY, contactKey);
        return intent;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_recharge_edit;
    }

    @Override
    protected void parseExtras() {
        Bundle bundle = getIntent().getExtras();
        String contactKey = "";
        if (bundle != null) {
            mContactRecharge = (ContactRecharge) bundle.getSerializable(Keys.Extras.EXTRA_ITEM_ID);
            contactKey = bundle.getString(Keys.Extras.EXTRA_CONTACT_KEY);
        }
        if (mContactRecharge == null) {
            mContactRecharge = new ContactRecharge(contactKey, new Date());
        }
    }

    boolean isNew() {
        return mContactRecharge.getKey() == null || mContactRecharge.getKey().isEmpty();
    }

    @Override
    public void setupView() {
        setupChangeWatcher();
        setTitle(isNew() ? R.string.title_activity_add : R.string.title_activity_edit);
        mTextDate.setText(DateUtils.toString(mContactRecharge.getCreatedAt()));
        mEditTitle1.setText(mContactRecharge.getDescription());
        mEditPrice.setText(String.valueOf(mContactRecharge.getPrice()));
        mNeedToSave = false;
    }

    private void setupChangeWatcher() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mNeedToSave = true;
            }
        };
        mEditTitle1.addTextChangedListener(watcher);
        mEditPrice.addTextChangedListener(watcher);
    }

    @Override
    protected boolean isModalView() {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            updateExpense();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mNeedToSave) {
            AlertDialog.Builder builder = new AlertDialog.Builder(RechargeEditActivity.this);
            builder.setTitle(R.string.dialog_discard_changes)
                    .setPositiveButton(R.string.action_discard, (dialog, which) -> {
                        mNeedToSave = false;
                        onBackPressed();
                    })
                    .setNegativeButton(R.string.action_cancel, null)
                    .show();
        } else {
            hideKeyboard();
            super.onBackPressed();
        }
    }

    private void updateModel() {
        if (mEditPrice.getText().toString().isEmpty())
            mEditPrice.setText("0");
        mContactRecharge.setDescription(mEditTitle1.getText().toString());
        mContactRecharge.setPrice(Integer.parseInt(mEditPrice.getText().toString()));
    }

    private void updateExpense() {
        updateModel();
        if (isNew()) {
            DatabaseReference ref = getDatabase().getReference(DC.DB_CONTACTS_RECHARGE)
                    .child(mContactRecharge.getContactKey())
                    .push();
            mContactRecharge.setKey(ref.getKey());
        }
        getDatabase().getReference(DC.DB_CONTACTS_RECHARGE)
                .child(mContactRecharge.getContactKey())
                .child(mContactRecharge.getKey())
                .setValue(mContactRecharge)
                .addOnCompleteListener(task -> {
                    Toast.makeText(RechargeEditActivity.this, getString(R.string.toast_updated), Toast.LENGTH_SHORT).show();
                    mNeedToSave = false;
                    onBackPressed();
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @OnClick(R.id.text_date)
    public void onDateClicked() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mContactRecharge.getCreatedAt());
        new DatePickerDialog(RechargeEditActivity.this, (datePicker, year, month, day)
                -> {
            mContactRecharge.setCreatedAt(DateUtils.getDate(year, month, day));
            mTextDate.setText(DateUtils.toString(mContactRecharge.getCreatedAt()));
        },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }
}

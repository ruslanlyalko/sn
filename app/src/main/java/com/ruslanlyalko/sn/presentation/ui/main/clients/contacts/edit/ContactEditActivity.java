package com.ruslanlyalko.sn.presentation.ui.main.clients.contacts.edit;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.sn.R;
import com.ruslanlyalko.sn.common.ContactHolder;
import com.ruslanlyalko.sn.common.DateUtils;
import com.ruslanlyalko.sn.common.Keys;
import com.ruslanlyalko.sn.common.UserType;
import com.ruslanlyalko.sn.data.configuration.DC;
import com.ruslanlyalko.sn.data.models.Contact;
import com.ruslanlyalko.sn.data.models.User;
import com.ruslanlyalko.sn.presentation.base.BaseActivity;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;

public class ContactEditActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks {

    private static final int RC_READ_CONTACTS = 1001;
    //@BindView(R.id.tab_type_adult) TabItem mTabTypeAdult;
    @BindView(R.id.tabs_user_type) TabLayout mTabsUserType;
    @BindView(R.id.edit_name) EditText mEditName;
    @BindView(R.id.edit_phone1) EditText mEditPhone1;
    @BindView(R.id.edit_phone2) EditText mEditPhone2;
    @BindView(R.id.edit_description) EditText mEditDescription;
    @BindView(R.id.edit_email) EditText mEditEmail;
    @BindView(R.id.edit_birth_day) EditText mEditBirthDay;
    @BindView(R.id.image_contacts) ImageView mImageContacts;
    @BindView(R.id.spinner_teacher) Spinner mSpinnerTeacher;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private Contact mContact = new Contact();
    private String mClientName;
    private String mClientPhone;
    private boolean mNeedToSave = false;
    private boolean mIsNew = false;
    private List<User> mUsers = new ArrayList<>();

    public static Intent getLaunchIntent(final Context launchIntent, final Contact contact) {
        Intent intent = new Intent(launchIntent, ContactEditActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_ITEM_ID, contact);
        return intent;
    }

    public static Intent getLaunchIntent(final Context launchIntent, String name, String phone) {
        Intent intent = new Intent(launchIntent, ContactEditActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_CLIENT_NAME, name);
        intent.putExtra(Keys.Extras.EXTRA_CLIENT_PHONE, phone);
        return intent;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_contact_edit;
    }

    @Override
    protected void parseExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mContact = (Contact) bundle.getSerializable(Keys.Extras.EXTRA_ITEM_ID);
            mClientPhone = bundle.getString(Keys.Extras.EXTRA_CLIENT_PHONE);
            mClientName = bundle.getString(Keys.Extras.EXTRA_CLIENT_NAME);
        }
        mIsNew = mContact == null;
        if (mIsNew) {
            mContact = new Contact();
            mContact.setName(mClientName);
            mContact.setPhone(mClientPhone);
        }
    }

    @Override
    protected void setupView() {
        setupChangeWatcher();
        setTitle(mIsNew ? R.string.title_activity_add : R.string.title_activity_edit);
        mEditName.setText(mContact.getName());
        mEditBirthDay.setText(DateUtils.toString(mContact.getBirthDay(), "dd.MM.yyyy"));
        mEditPhone1.setText(mContact.getPhone());
        mEditPhone2.setText(mContact.getPhone2());
        mEditEmail.setText(mContact.getEmail());
        mEditDescription.setText(mContact.getDescription());
        TabLayout.Tab tab = mTabsUserType.getTabAt(mContact.getUserType() == UserType.ADULT ? 0 : 1);
        loadUsers();
        if (tab != null) tab.select();
        mNeedToSave = false;
    }

    @Override
    protected boolean isModalView() {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            if (mIsNew)
                addClient();
            else
                updateClient();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mNeedToSave) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ContactEditActivity.this);
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

    private void loadUsers() {
        getDatabase().getReference(DC.DB_USERS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        mUsers.clear();
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            User user = data.getValue(User.class);
                            mUsers.add(user);
                        }
                        initSpinnerTeacher();
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }

    private void initSpinnerTeacher() {
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(ContactEditActivity.this,
                android.R.layout.simple_selectable_list_item);
        List<String> userNamesList = new ArrayList<>();
        userNamesList.add(getString(R.string.spinner_not_selected));
        for (int i = 0; i < mUsers.size(); i++) {
            userNamesList.add(mUsers.get(i).getFullName());
        }
        arrayAdapter.addAll(userNamesList);
        mSpinnerTeacher.setAdapter(arrayAdapter);
        for (int i = 0; i < mSpinnerTeacher.getAdapter().getCount(); i++) {
            if (mSpinnerTeacher.getItemAtPosition(i).equals(mContact.getUserName()))
                mSpinnerTeacher.setSelection(i);
        }
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
        mEditName.addTextChangedListener(watcher);
        mEditPhone1.addTextChangedListener(watcher);
        mEditPhone2.addTextChangedListener(watcher);
        mEditEmail.addTextChangedListener(watcher);
        mEditDescription.addTextChangedListener(watcher);
        mTabsUserType.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(final TabLayout.Tab tab) {
                mNeedToSave = true;
            }

            @Override
            public void onTabUnselected(final TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(final TabLayout.Tab tab) {
            }
        });
    }

    private void updateModel() {
        mContact.setName(mEditName.getText().toString().trim());
        mContact.setEmail(mEditEmail.getText().toString().trim());
        mContact.setPhone(mEditPhone1.getText().toString().trim());
        mContact.setPhone2(mEditPhone2.getText().toString().trim());
        mContact.setDescription(mEditDescription.getText().toString().trim());
        long index = mSpinnerTeacher.getSelectedItemId();
        if (index < 1) {
            mContact.setUserName("");
            mContact.setUserId("");
        } else {
            mContact.setUserName(mUsers.get((int) index - 1).getFullName());
            mContact.setUserId(mUsers.get((int) index - 1).getId());
        }
        TabLayout.Tab tab = mTabsUserType.getTabAt(0);
        if (tab != null)
            mContact.setUserType(tab.isSelected() ? UserType.ADULT : UserType.CHILD);
    }

    private void addClient() {
        updateModel();
        if (mContact.getName().isEmpty()) {
            Toast.makeText(this, getString(R.string.error_no_name), Toast.LENGTH_LONG).show();
            return;
        }
        mIsNew = false;
        DatabaseReference ref = database.getReference(DC.DB_CONTACTS)
                .push();
        mContact.setKey(ref.getKey());
        ref.setValue(mContact).addOnCompleteListener(task -> {
            Snackbar.make(mEditName, getString(R.string.toast_client_added), Snackbar.LENGTH_SHORT).show();
            mNeedToSave = false;
            onBackPressed();
        });
    }

    private void updateClient() {
        updateModel();
        if (mContact.getName().isEmpty()) {
            Toast.makeText(this, getString(R.string.error_no_name), Toast.LENGTH_LONG).show();
            return;
        }
        database.getReference(DC.DB_CONTACTS)
                .child(mContact.getKey())
                .setValue(mContact)
                .addOnCompleteListener(task -> {
                    Toast.makeText(ContactEditActivity.this, getString(R.string.toast_updated), Toast.LENGTH_SHORT).show();
                    mNeedToSave = false;
                    onBackPressed();
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @OnClick(R.id.edit_birth_day)
    public void onDate1Clicked() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mContact.getBirthDay());
        DatePickerDialog dialog = DatePickerDialog.newInstance((datePicker, year, month, day)
                        -> {
                    mContact.setBirthDay(DateUtils.getDate(year, month, day));
                    mEditBirthDay.setText(DateUtils.toString(mContact.getBirthDay(), "dd.MM.yyyy"));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.showYearPickerFirst(true);
        dialog.show(getFragmentManager(), "birthday");
    }

    @OnClick(R.id.image_contacts)
    public void onContactsClicked() {
        String[] perms = {Manifest.permission.READ_CONTACTS};
        if (EasyPermissions.hasPermissions(this, perms)) {
            showContacts();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.text_read_contacts_rationale),
                    RC_READ_CONTACTS, perms);
        }
    }

    void showContacts() {
        if (!ContactHolder.hasContacts()) {
            ContactHolder.setContacts(getContacts());
        }
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(ContactEditActivity.this);
        builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle(getString(R.string.text_select_one));
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(ContactEditActivity.this,
                android.R.layout.select_dialog_singlechoice);
        arrayAdapter.addAll(ContactHolder.getContactString());
        builderSingle.setNegativeButton(getString(R.string.action_cancel), (dialog, which) -> dialog.dismiss());
        builderSingle.setAdapter(arrayAdapter, (dialog, which) -> {
            mEditName.setText(ContactHolder.getContact().get(which).first);
            mEditPhone1.setText(ContactHolder.getContact().get(which).second);
            dialog.dismiss();
        });
        builderSingle.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        showContacts();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        Toast.makeText(this, R.string.text_read_contacts_rationale, Toast.LENGTH_SHORT).show();
    }

    List<Pair<String, String>> getContacts() {
        List<Pair<String, String>> list = new ArrayList<>();
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, "display_name ASC");
        while (phones != null && phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            list.add(new Pair<>(name, phoneNumber));
        }
        if (phones != null)
            phones.close();
        return list;
    }
}

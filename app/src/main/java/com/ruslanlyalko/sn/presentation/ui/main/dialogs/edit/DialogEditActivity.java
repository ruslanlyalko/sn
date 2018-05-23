package com.ruslanlyalko.sn.presentation.ui.main.dialogs.edit;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.sn.R;
import com.ruslanlyalko.sn.common.Keys;
import com.ruslanlyalko.sn.data.configuration.DC;
import com.ruslanlyalko.sn.data.models.Message;
import com.ruslanlyalko.sn.data.models.User;
import com.ruslanlyalko.sn.presentation.base.BaseActivity;
import com.ruslanlyalko.sn.presentation.ui.main.dialogs.edit.adapter.MembersAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;

public class DialogEditActivity extends BaseActivity {

    @BindView(R.id.progress_bar) ProgressBar progressBar;
    @BindView(R.id.edit_title1) EditText textTitle1;
    @BindView(R.id.edit_title2) EditText textTitle2;
    @BindView(R.id.edit_link) EditText textLink;
    @BindView(R.id.edit_description) EditText textDescription;
    @BindView(R.id.image_view) ImageView imageView;
    @BindView(R.id.checkbox_comments) CheckBox mCheckBoxComments;
    @BindView(R.id.list_members) RecyclerView mListMembers;
    private boolean isNew = false;
    private boolean needToSave = false;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    private MembersAdapter mMembersAdapter = new MembersAdapter();
    private List<String> mMembersIds = new ArrayList<>();
    private Message mMessage = new Message();
    private String notKey;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_dialog_edit;
    }

    @Override
    protected void parseExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            notKey = bundle.getString(Keys.Extras.EXTRA_ITEM_ID);
        }
    }

    @Override
    protected void setupView() {
        setupRecycler();
        isNew = notKey == null;
        initTextWatchers();
        loadData();
        loadMembers();
        updateUI();
    }

    @Override
    protected boolean isModalView() {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            if (isNew)
                addNotification();
            else
                updateNotification();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (progressBar.getVisibility() == View.VISIBLE) {
            Toast.makeText(this, R.string.toast_photo_uploading, Toast.LENGTH_SHORT).show();
            return;
        }
        if (needToSave) {
            AlertDialog.Builder builder = new AlertDialog.Builder(DialogEditActivity.this);
            builder.setTitle(R.string.dialog_report_save_before_close_title)
                    .setMessage(R.string.dialog_mk_edit_text)
                    .setPositiveButton(R.string.action_save, (dialog, which) -> {
                        if (isNew)
                            addNotification();
                        else
                            updateNotification();
                        onBackPressed();
                    })
                    .setNegativeButton(R.string.action_cancel, (dialog, which) -> {
                        needToSave = false;
                        onBackPressed();
                    })
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    private void setupRecycler() {
        mListMembers.setLayoutManager(new LinearLayoutManager(this));
        mListMembers.setAdapter(mMembersAdapter);
    }

    private void initTextWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                needToSave = true;
            }
        };
        textTitle1.addTextChangedListener(watcher);
        textTitle2.addTextChangedListener(watcher);
        textLink.addTextChangedListener(watcher);
        textDescription.addTextChangedListener(watcher);
    }

    private void loadData() {
        if (isNew) return;
        DatabaseReference ref = database.getReference(DC.DB_DIALOGS).child(notKey);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mMessage = dataSnapshot.getValue(Message.class);
                mMembersIds.clear();
                for (DataSnapshot member : dataSnapshot.child(DC.MESSAGE_MEMBERS).getChildren()) {
                    String id = member.getKey();
                    mMembersIds.add(id);
                }
                mMembersAdapter.updateMembers(mMembersIds);
                updateUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void loadMembers() {
        database.getReference(DC.DB_USERS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            User user = userSnapshot.getValue(User.class);
                            if (user != null && !user.getIsAdmin())
                                mMembersAdapter.add(user);
                        }
                        mMembersAdapter.updateMembers(mMembersIds);
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }

    private void updateUI() {
        if (isNew) {
            setTitle(R.string.title_activity_add);
        } else {
            setTitle(R.string.title_activity_edit);
            if (mMessage != null) {
                textTitle1.setText(mMessage.getTitle1());
                textTitle2.setText(mMessage.getTitle2());
                textLink.setText(mMessage.getLink());
                textDescription.setText(mMessage.getDescription());
                mCheckBoxComments.setChecked(mMessage.getCommentsEnabled());
            }
            //todo
        }
        needToSave = false;
    }

    private void updateNotModel() {
        mMessage.setTitle1(textTitle1.getText().toString());
        mMessage.setTitle2(textTitle2.getText().toString());
        mMessage.setLink(textLink.getText().toString());
        mMessage.setDescription(textDescription.getText().toString());
        mMessage.setCommentsEnabled(mCheckBoxComments.isChecked());
    }

    private void addNotification() {
        updateNotModel();
        isNew = false;
        notKey = database.getReference(DC.DB_DIALOGS).push().getKey();
        mMessage.setKey(notKey);
        mMessage.setDate(new SimpleDateFormat("d-M-yyyy", Locale.US).format(new Date()));
        mMessage.setUserId(mUser.getUid());
        mMessage.setUserName(mUser.getDisplayName());
        database.getReference(DC.DB_DIALOGS)
                .child(notKey).setValue(mMessage).addOnCompleteListener(task ->
                Snackbar.make(imageView, getString(R.string.toast_dialog_added), Snackbar.LENGTH_SHORT).show());
        saveMembers();
        needToSave = false;
    }

    private void updateNotification() {
        updateNotModel();
        database.getReference(DC.DB_DIALOGS)
                .child(mMessage.getKey()).setValue(mMessage).addOnCompleteListener(task ->
                Snackbar.make(imageView, getString(R.string.toast_updated), Snackbar.LENGTH_SHORT).show());
        saveMembers();
        needToSave = false;
    }

    private void saveMembers() {
        List<Boolean> checkedList = mMembersAdapter.getMembers();
        List<User> users = mMembersAdapter.getUsers();
        for (int i = 0; i < users.size(); i++) {
            DatabaseReference ref = database.getReference(DC.DB_DIALOGS)
                    .child(mMessage.getKey())
                    .child(DC.MESSAGE_MEMBERS)
                    .child(users.get(i).getId());
            if (checkedList.get(i))
                ref.setValue(true);
            else
                ref.removeValue();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }
}

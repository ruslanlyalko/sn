package com.ruslanlyalko.sn.presentation.ui.main.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.sn.R;
import com.ruslanlyalko.sn.data.FirebaseUtils;
import com.ruslanlyalko.sn.data.configuration.DC;
import com.ruslanlyalko.sn.data.models.Message;
import com.ruslanlyalko.sn.data.models.Notification;
import com.ruslanlyalko.sn.presentation.base.BaseActivity;
import com.ruslanlyalko.sn.presentation.ui.main.dialogs.adapter.DialogsAdapter;
import com.ruslanlyalko.sn.presentation.ui.main.dialogs.edit.DialogEditActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class DialogsActivity extends BaseActivity {

    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.recycler_view) RecyclerView mMessagesList;

    private DialogsAdapter mDialogsAdapter;
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

    public static Intent getLaunchIntent(final Activity launchIntent) {
        return new Intent(launchIntent, DialogsActivity.class);
    }

    @Override
    protected boolean isRightView() {
        return true;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_dialogs;
    }

    @Override
    protected void setupView() {
        initRecycler();
        loadMessages();
        fab.setVisibility(FirebaseUtils.isAdmin() ? View.VISIBLE : View.GONE);
        loadBadge();
    }

    private void initRecycler() {
        mDialogsAdapter = new DialogsAdapter(this);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        mMessagesList.setLayoutManager(mLayoutManager);
        mMessagesList.setItemAnimator(new DefaultItemAnimator());
        mMessagesList.setAdapter(mDialogsAdapter);
    }

    private void loadMessages() {
        FirebaseDatabase.getInstance().getReference(DC.DB_DIALOGS)
                .orderByChild("updatedAt/time")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        List<Message> list = new ArrayList<>();
                        for (DataSnapshot messageSS : dataSnapshot.getChildren()) {
                            if (FirebaseUtils.isAdmin() || messageSS.child(DC.MESSAGE_MEMBERS).child(mUser.getUid()).exists()) {
                                Message message = messageSS.getValue(Message.class);
                                if (message != null) {
                                    list.add(0, message);
                                }
                            }
                        }
                        mDialogsAdapter.setData(list);
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }

    private void loadBadge() {
        mDatabase.getReference(DC.DB_USERS_NOTIFICATIONS)
                .child(mUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        List<Notification> notifications = new ArrayList<>();
                        for (DataSnapshot notifSS : dataSnapshot.getChildren()) {
                            Notification notification = notifSS.getValue(Notification.class);
                            notifications.add(notification);
                        }
                        mDialogsAdapter.updateNotifications(notifications);
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }

    @OnClick(R.id.fab)
    void onFabCLicked() {
        addNotification();
    }

    private void addNotification() {
        Intent intent = new Intent(DialogsActivity.this, DialogEditActivity.class);
        startActivity(intent);
    }
}

package com.ruslanlyalko.sn.presentation.ui.main.clients.birth;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.sn.R;
import com.ruslanlyalko.sn.common.Constants;
import com.ruslanlyalko.sn.common.DateUtils;
import com.ruslanlyalko.sn.data.configuration.DC;
import com.ruslanlyalko.sn.data.models.Contact;
import com.ruslanlyalko.sn.presentation.base.BaseActivity;
import com.ruslanlyalko.sn.presentation.ui.main.clients.birth.adapter.BirthContactsAdapter;
import com.ruslanlyalko.sn.presentation.ui.main.clients.contacts.adapter.OnContactClickListener;
import com.ruslanlyalko.sn.presentation.ui.main.clients.contacts.details.ContactDetailsActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

public class BirthActivity extends BaseActivity implements OnContactClickListener {

    @BindView(R.id.calendar_view) CompactCalendarView mCalendarView;
    @BindView(R.id.text_month) TextView mTextMonth;
    @BindView(R.id.list_kids) RecyclerView mListKids;
    private BirthContactsAdapter mBirthContactsAdapter;
    private Date mLastDate = new Date();

    public static Intent getLaunchIntent(final Context launchIntent) {
        return new Intent(launchIntent, BirthActivity.class);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_birth;
    }

    @Override
    protected void setupView() {
        setupRecycler();
        loadContacts();
    }

    private void setupRecycler() {
        mBirthContactsAdapter = new BirthContactsAdapter(this, this);
        mListKids.setLayoutManager(new LinearLayoutManager(this));
        mListKids.setAdapter(mBirthContactsAdapter);
        onFilterTextChanged(new Date());
        mCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                onFilterTextChanged(firstDayOfNewMonth);
            }
        });
    }

    private void loadContacts() {
        Query ref = FirebaseDatabase.getInstance()
                .getReference(DC.DB_CONTACTS)
                .orderByChild("birthDay/time");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                List<Contact> contacts = new ArrayList<>();
                for (DataSnapshot clientSS : dataSnapshot.getChildren()) {
                    Contact contact = clientSS.getValue(Contact.class);
                    if (contact != null) {
                        contacts.add(0, contact);
                    }
                }
                mBirthContactsAdapter.setData(contacts);
                onFilterTextChanged(mLastDate);
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
            }
        });
    }

    private void onFilterTextChanged(Date date) {
        if (isDestroyed()) return;
        mLastDate = date;
        Calendar month = Calendar.getInstance();
        month.setTime(date);
        mTextMonth.setText(DateUtils.getMonthWithYear(getResources(), month));
        mBirthContactsAdapter.getFilter().filter(DateUtils.toString(date, "MM"));
    }

    @OnClick({R.id.button_prev, R.id.button_next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.button_prev:
                mCalendarView.showPreviousMonth();
                break;
            case R.id.button_next:
                mCalendarView.showNextMonth();
                break;
        }
    }

    @Override
    public void onItemClicked(final int position, final ActivityOptionsCompat options) {
        startActivity(ContactDetailsActivity.getLaunchIntent(this, mBirthContactsAdapter.getItem(position)), options.toBundle());
    }

    @Override
    public void onItemsCheckedChanged(final List<String> contacts) {
    }
}

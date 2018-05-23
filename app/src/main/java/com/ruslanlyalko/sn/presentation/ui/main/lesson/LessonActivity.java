package com.ruslanlyalko.sn.presentation.ui.main.lesson;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.sn.R;
import com.ruslanlyalko.sn.common.DateUtils;
import com.ruslanlyalko.sn.common.Keys;
import com.ruslanlyalko.sn.data.configuration.DC;
import com.ruslanlyalko.sn.data.models.Lesson;
import com.ruslanlyalko.sn.presentation.base.BaseActivity;
import com.ruslanlyalko.sn.presentation.ui.main.clients.OnFilterListener;
import com.ruslanlyalko.sn.presentation.ui.main.clients.contacts.ContactsFragment;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.OnClick;

public class LessonActivity extends BaseActivity implements OnFilterListener {

    private static final int TAB_ADULT = 0;
    private static final int TAB_CHILD = 1;
    @BindView(R.id.text_date) TextView mTextDate;
    @BindView(R.id.text_time) TextView mTextTime;
    @BindView(R.id.edit_comment) EditText mEditDescription;
    @BindView(R.id.text_lesson_length) TextView mTextLessonLength;
    @BindView(R.id.tabs_room) TabLayout mTabsRoom;
    @BindView(R.id.tabs_lesson) TabLayout mTabsLesson;
    @BindView(R.id.tabs_user) TabLayout mTabsUser;
    @BindView(R.id.container) ViewPager mContainer;
    private boolean mIsChanged;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private Lesson mLesson = new Lesson();

    public static Intent getLaunchIntent(final Context launchIntent) {
        return new Intent(launchIntent, LessonActivity.class);
    }

    public static Intent getLaunchIntent(final Context context, Lesson lesson) {
        Intent intent = new Intent(context, LessonActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_LESSON_MODEL, lesson);
        return intent;
    }

    public static Intent getLaunchIntent(final Context context, Date date) {
        Intent intent = new Intent(context, LessonActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_LESSON_DATE, date);
        return intent;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_lesson;
    }

    @Override
    protected void parseExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey(Keys.Extras.EXTRA_LESSON_MODEL))
            mLesson = (Lesson) bundle.getSerializable(Keys.Extras.EXTRA_LESSON_MODEL);
        else if (bundle != null && bundle.containsKey(Keys.Extras.EXTRA_LESSON_DATE))
            mLesson = new Lesson(getUser().getUid(), getUser().getDisplayName(), (Date) bundle.getSerializable(Keys.Extras.EXTRA_LESSON_DATE));
        else
            mLesson = new Lesson(getUser().getUid(), getUser().getDisplayName());
    }

    @Override
    protected void setupView() {
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mContainer.setAdapter(mSectionsPagerAdapter);
        mContainer.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabsUser));
        mTabsUser.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mContainer));
        updateUI();
        if (!isNew()) {
            loadLesson();
        }
    }

    private void updateUI() {
        if (isDestroyed()) return;
        mTextDate.setText(DateUtils.toString(mLesson.getDateTime(), "dd.MM  EEEE"));
        mTextTime.setText(DateUtils.toString(mLesson.getDateTime(), "HH:mm"));
        mTextLessonLength.setText(mLesson.getLessonLengthId() == 0
                ? R.string.lesson_length_one_hour : R.string.lesson_length_one_half_hour);
        TabLayout.Tab tabLesson = mTabsLesson.getTabAt(mLesson.getLessonType());
        if (tabLesson != null) tabLesson.select();
        TabLayout.Tab tabRoom = mTabsRoom.getTabAt(mLesson.getRoomType());
        if (tabRoom != null) tabRoom.select();
        TabLayout.Tab tabUserType = mTabsUser.getTabAt(mLesson.getUserType());
        if (tabUserType != null) tabUserType.select();
        if (mLesson.getUserType() == 0)
            mSectionsPagerAdapter.getAdultFragment().updateSelected(mLesson.getClients());
        else
            mSectionsPagerAdapter.getChildFragment().updateSelected(mLesson.getClients());
        setTitle(getString(R.string.title_activity_lesson, mLesson.getUserName()));
        mEditDescription.setText(mLesson.getDescription());
    }

    boolean isNew() {
        return mLesson.getKey().isEmpty();
    }

    private void loadLesson() {
        getDatabase()
                .getReference(DC.DB_LESSONS)
                .child(DateUtils.toString(mLesson.getDateTime(), "yyyy/MM/dd"))
                .child(mLesson.getKey()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                Lesson lesson = dataSnapshot.getValue(Lesson.class);
                if (lesson != null) {
                    mLesson = lesson;
                    updateUI();
                }
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add: {
                saveLesson();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mIsChanged) {
            AlertDialog.Builder builder = new AlertDialog.Builder(LessonActivity.this);
            builder.setTitle(R.string.dialog_report_save_before_close_title)
                    .setMessage(R.string.dialog_report_save_before_close_text)
                    .setPositiveButton(R.string.action_save, (dialog, which) -> {
                        saveLesson();
                        onBackPressed();
                    })
                    .setNegativeButton(R.string.action_cancel, (dialog, which) ->
                            super.onBackPressed())
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    private void saveLesson() {
        DatabaseReference ref = getDatabase()
                .getReference(DC.DB_LESSONS)
                .child(DateUtils.toString(mLesson.getDateTime(), "yyyy/MM/dd"));
        if (isNew()) {
            mLesson.setKey(ref.push().getKey());
        }
        mLesson.setDescription(mEditDescription.getText().toString().trim());
        mLesson.setRoomType(mTabsRoom.getSelectedTabPosition());
        mLesson.setLessonType(mTabsLesson.getSelectedTabPosition());
        mLesson.setUserType(mTabsUser.getSelectedTabPosition());
        if (mLesson.getUserType() == 0)
            mLesson.setClients(mSectionsPagerAdapter.getAdultFragment().getSelected());
        else
            mLesson.setClients(mSectionsPagerAdapter.getChildFragment().getSelected());
        ref.child(mLesson.getKey()).setValue(mLesson)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, R.string.toast_saved, Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_report, menu);
        return true;
    }

    @Override
    public void onFilterChanged(final String name, final String phone) {
        //not used
    }

    @OnClick({R.id.text_date, R.id.text_time, R.id.text_lesson_length})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.text_date:
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(mLesson.getDateTime());
                DatePickerDialog dialog = DatePickerDialog.newInstance((datePicker, year, month, day)
                                -> {
                            mLesson.setDateTime(DateUtils.getDate(mLesson.getDateTime(), year, month, day));
                            mTextDate.setText(DateUtils.toString(mLesson.getDateTime(), "dd.MM  EEEE"));
                            if (!isNew()) {
                                mLesson.setKey("");
                                Toast.makeText(this, R.string.toast_duplicate, Toast.LENGTH_LONG).show();
                            }
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );
                dialog.show(getFragmentManager(), "date");
                break;
            case R.id.text_time:
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTime(mLesson.getDateTime());
                TimePickerDialog dialog1 = TimePickerDialog.newInstance((datePicker, hours, minutes, seconds)
                                -> {
                            mLesson.setDateTime(DateUtils.getDate(mLesson.getDateTime(), hours, minutes));
                            mTextTime.setText(DateUtils.toString(mLesson.getDateTime(), "HH:mm"));
                        },
                        calendar1.get(Calendar.HOUR_OF_DAY),
                        calendar1.get(Calendar.MINUTE), true);
                dialog1.show(getFragmentManager(), "time");
                break;
            case R.id.text_lesson_length:
                mLesson.setLessonLengthId(mLesson.getLessonLengthId() == 0 ? 1 : 0);
                mTextLessonLength.setText(mLesson.getLessonLengthId() == 0
                        ? R.string.lesson_length_one_hour : R.string.lesson_length_one_half_hour);
                break;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private ContactsFragment mAdult;
        private ContactsFragment mChild;

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case TAB_ADULT:
                    return getAdultFragment();
                case TAB_CHILD:
                    return getChildFragment();
            }
            return null;
        }

        ContactsFragment getAdultFragment() {
            if (mAdult == null)
                mAdult = ContactsFragment.newInstance(0, true);
            return mAdult;
        }

        ContactsFragment getChildFragment() {
            if (mChild == null)
                mChild = ContactsFragment.newInstance(1, true);
            return mChild;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}

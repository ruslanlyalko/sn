package com.ruslanlyalko.sn.presentation.ui.main.clients.contacts.details;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.sn.R;
import com.ruslanlyalko.sn.common.DateUtils;
import com.ruslanlyalko.sn.common.Keys;
import com.ruslanlyalko.sn.data.FirebaseUtils;
import com.ruslanlyalko.sn.data.configuration.DC;
import com.ruslanlyalko.sn.data.models.Contact;
import com.ruslanlyalko.sn.data.models.ContactRecharge;
import com.ruslanlyalko.sn.data.models.Lesson;
import com.ruslanlyalko.sn.data.models.SettingsSalary;
import com.ruslanlyalko.sn.presentation.base.BaseActivity;
import com.ruslanlyalko.sn.presentation.ui.main.calendar.adapter.LessonsAdapter;
import com.ruslanlyalko.sn.presentation.ui.main.calendar.adapter.OnLessonClickListener;
import com.ruslanlyalko.sn.presentation.ui.main.clients.contacts.details.adapter.ContactRechargesAdapter;
import com.ruslanlyalko.sn.presentation.ui.main.clients.contacts.details.adapter.OnContactRechargeClickListener;
import com.ruslanlyalko.sn.presentation.ui.main.clients.contacts.details.recharge_edit.RechargeEditActivity;
import com.ruslanlyalko.sn.presentation.ui.main.clients.contacts.edit.ContactEditActivity;
import com.ruslanlyalko.sn.presentation.ui.main.lesson.LessonActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class ContactDetailsActivity extends BaseActivity implements OnLessonClickListener, OnContactRechargeClickListener {

    private static final int RC_LESSON = 1001;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.image_avatar) ImageView mImageAvatar;
    @BindView(R.id.text_sub_title) TextView mTextSubTitle;
    @BindView(R.id.text_phone1) TextView mTextPhone1;
    @BindView(R.id.text_phone2) TextView mTextPhone2;
    @BindView(R.id.text_balance) TextView mTextBalance;
    @BindView(R.id.card_phone2) CardView mCardPhone2;
    @BindView(R.id.card_balance) CardView mCardBalance;
    @BindView(R.id.text_description) TextView mTextDescription;
    @BindView(R.id.list_lessons) RecyclerView mListLessons;
    @BindView(R.id.list_income) RecyclerView mListIncome;
    @BindView(R.id.text_user_name) TextView mTextUserName;
    @BindView(R.id.text_income_placeholder) TextView mTextIncomePlaceholder;

    private Contact mContact;
    private SettingsSalary mSettingsSalary = new SettingsSalary();
    private String mContactKey = "";
    private LessonsAdapter mLessonsAdapter = new LessonsAdapter(this);
    private ContactRechargesAdapter mContactRechargesAdapter = new ContactRechargesAdapter(this);
    private ValueEventListener mValueEventListener;
    private ValueEventListener mContactValueEventListener;
    private boolean mHasLessonsWithOtherTeachers;
    private int mTotalCharge = 0;

    public static Intent getLaunchIntent(final Context launchIntent, final Contact contact) {
        Intent intent = new Intent(launchIntent, ContactDetailsActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_ITEM_ID, contact);
        return intent;
    }

    public static Intent getLaunchIntent(final Context launchIntent, final String contactKey) {
        Intent intent = new Intent(launchIntent, ContactDetailsActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_CONTACT_KEY, contactKey);
        return intent;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_contact_details;
    }

    @Override
    protected void parseExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mContact = (Contact) bundle.getSerializable(Keys.Extras.EXTRA_ITEM_ID);
            mContactKey = bundle.getString(Keys.Extras.EXTRA_CONTACT_KEY);
        }
        if (mContact != null)
            mContactKey = mContact.getKey();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void setupView() {
        if (isDestroyed()) return;
        setupRecycler();
        setupBalance();
        loadDetails();
        showContactDetails();
        loadSettingsSalaries();
        loadContacts();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_edit:
                startActivity(ContactEditActivity.getLaunchIntent(this, mContact));
                break;
            case R.id.action_delete:
                removeCurrentContact();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        getDatabase().getReference(DC.DB_LESSONS).removeEventListener(mValueEventListener);
        getDatabase().getReference(DC.DB_CONTACTS_RECHARGE).child(mContactKey).removeEventListener(mContactValueEventListener);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Handler().postDelayed(() -> {
            loadLessons();
            loadContactRecharges();
        }, 400);
    }

    private void setupBalance() {
        mCardBalance.setVisibility(FirebaseUtils.isAdmin() ? View.VISIBLE : View.GONE);
    }

    private void loadContactRecharges() {
        mContactValueEventListener = getDatabase()
                .getReference(DC.DB_CONTACTS_RECHARGE)
                .child(mContactKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        List<ContactRecharge> contactRecharges = new ArrayList<>();
                        mTotalCharge = 0;
                        for (DataSnapshot rechargesSS : dataSnapshot.getChildren()) {
                            ContactRecharge recharge = rechargesSS.getValue(ContactRecharge.class);
                            if (recharge != null) {
                                contactRecharges.add(recharge);
                                mTotalCharge += recharge.getPrice();
                            }
                        }
                        boolean showPlaceholder = contactRecharges.size() == 0;
                        if (isDestroyed()) return;
                        mTextIncomePlaceholder.setVisibility(showPlaceholder ? View.VISIBLE : View.GONE);
                        mListIncome.setVisibility(showPlaceholder ? View.GONE : View.VISIBLE);
                        mContactRechargesAdapter.setData(contactRecharges);
                        calcBalance();
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }

    private void removeCurrentContact() {
        if (mLessonsAdapter.getItemCount() != 0) {
            Toast.makeText(this, R.string.error_delete_contact, Toast.LENGTH_LONG).show();
            return;
        }
        if (mHasLessonsWithOtherTeachers) {
            Toast.makeText(this, R.string.error_delete_contact_other, Toast.LENGTH_LONG).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_remove_contact_title)
                .setMessage(R.string.dialog_remove_contact_message)
                .setPositiveButton("Видалити", (dialog, which) -> {
                    finish();
                    FirebaseDatabase.getInstance()
                            .getReference(DC.DB_CONTACTS)
                            .child(mContact.getKey()).removeValue();
                })
                .setNegativeButton("Повернутись", null)
                .show();
    }

    private void setupRecycler() {
        mListLessons.setLayoutManager(new LinearLayoutManager(this));
        mListLessons.setAdapter(mLessonsAdapter);
        mListIncome.setLayoutManager(new LinearLayoutManager(this));
        mListIncome.setAdapter(mContactRechargesAdapter);
    }

    private void loadDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(DC.DB_CONTACTS)
                .child(mContactKey);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                mContact = dataSnapshot.getValue(Contact.class);
                showContactDetails();
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
            }
        });
    }

    private void loadSettingsSalaries() {
        getDatabase().getReference(DC.DB_SETTINGS_SALARY).child("first_key")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        SettingsSalary settingsSalary = dataSnapshot.getValue(SettingsSalary.class);
                        if (settingsSalary != null) {
                            mSettingsSalary = settingsSalary;
                        }
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }

    private void showContactDetails() {
        if (isDestroyed()) return;
        if (mContact == null) {
            setTitle("");
            return;
        }
        setTitle("");
        mTextUserName.setText(mContact.getName());
        String subtitle = "";
        if (mContact.hasUser())
            subtitle = "[" + mContact.getUserName() + "] \n";
        if (mContact.getBirthDay().getTime() != mContact.getCreatedAt().getTime())
            subtitle += DateUtils.toString(mContact.getBirthDay(), "dd.MM.yyyy");
        subtitle += "\n" + mContact.getEmail();
        mTextSubTitle.setText(subtitle);
        mTextPhone1.setText(mContact.getPhone());
        mTextPhone2.setText(mContact.getPhone2());
        mCardPhone2.setVisibility(mContact.getPhone2() != null & !mContact.getPhone2().isEmpty() ? View.VISIBLE : View.GONE);
        mTextDescription.setText(mContact.getDescription());
        mTextDescription.setVisibility(mContact.getDescription() != null & !mContact.getDescription().isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void loadLessons() {
        if (isDestroyed()) return;
        mValueEventListener = getDatabase().getReference(DC.DB_LESSONS)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        if (isDestroyed()) return;
                        mHasLessonsWithOtherTeachers = false;
                        List<Lesson> lessons = new ArrayList<>();
                        for (DataSnapshot datYears : dataSnapshot.getChildren()) {
                            for (DataSnapshot datYear : datYears.getChildren()) {
                                for (DataSnapshot datMonth : datYear.getChildren()) {
                                    for (DataSnapshot datDay : datMonth.getChildren()) {
                                        Lesson lesson = datDay.getValue(Lesson.class);
                                        if (lesson != null && (lesson.getClients().contains(mContact.getKey()))) {
                                            if (FirebaseUtils.isAdmin() || lesson.getUserId().equals(FirebaseAuth.getInstance().getUid()))
                                                lessons.add(lesson);
                                            else
                                                mHasLessonsWithOtherTeachers = true;
                                        }
                                    }
                                }
                            }
                        }
                        Collections.sort(lessons, (o1, o2) ->
                                Long.compare(o2.getDateTime().getTime(), o1.getDateTime().getTime()));
                        mLessonsAdapter.setData(lessons);
                        calcBalance();
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }

    private void calcBalance() {
        int totalIncome = 0;
        int privateTotalIncome = 0;
        int pairTotalIncome = 0;
        int groupTotalIncome = 0;
        int onlineTotalIncome = 0;
        int privateTotalChildIncome = 0;
        int pairTotalChildIncome = 0;
        int groupTotalChildIncome = 0;
        int onlineTotalChildIncome = 0;
        // local
        int total;
        int privateTotal = 0;
        int pairTotal = 0;
        int groupTotal = 0;
        int onlineTotal = 0;
        int privateTotalChild = 0;
        int pairTotalChild = 0;
        int groupTotalChild = 0;
        int onlineTotalChild = 0;
        int privateTotalCount = 0;
        int pairTotalCount = 0;
        int groupTotalCount = 0;
        int onlineTotalCount = 0;
        int privateTotalChildCount = 0;
        int pairTotalChildCount = 0;
        int groupTotalChildCount = 0;
        int onlineTotalChildCount = 0;
        for (Lesson lesson : mLessonsAdapter.getData()) {
            if (lesson.getUserType() == 0) {
                if (lesson.getLessonLengthId() == 0) {
                    switch (lesson.getLessonType()) {
                        case 0:
                            privateTotalCount += 1;
                            privateTotal += mSettingsSalary.getTeacherPrivate();
                            privateTotalIncome += mSettingsSalary.getStudentPrivate();
                            break;
                        case 1:
                            pairTotalCount += 1;
                            pairTotal += mSettingsSalary.getTeacherPair();
                            pairTotalIncome += mSettingsSalary.getStudentPair();
                            break;
                        case 2:
                            groupTotalCount += 1;
                            groupTotal += mSettingsSalary.getTeacherGroup();
                            groupTotalIncome += mSettingsSalary.getStudentGroup();
                            break;
                        case 3:
                            onlineTotalCount += 1;
                            onlineTotal += mSettingsSalary.getTeacherOnLine();
                            onlineTotalIncome += mSettingsSalary.getStudentOnLine();
                            break;
                    }
                } else {
                    switch (lesson.getLessonType()) {
                        case 0:
                            privateTotalCount += 1;
                            privateTotal += mSettingsSalary.getTeacherPrivate15();
                            privateTotalIncome += mSettingsSalary.getStudentPrivate15();
                            break;
                        case 1:
                            pairTotalCount += 1;
                            pairTotal += mSettingsSalary.getTeacherPair15();
                            pairTotalIncome += mSettingsSalary.getStudentPair15();
                            break;
                        case 2:
                            groupTotalCount += 1;
                            groupTotal += mSettingsSalary.getTeacherGroup15();
                            groupTotalIncome += mSettingsSalary.getStudentGroup15();
                            break;
                        case 3:
                            onlineTotalCount += 1;
                            onlineTotal += mSettingsSalary.getTeacherOnLine15();
                            onlineTotalIncome += mSettingsSalary.getStudentOnLine15();
                            break;
                    }
                }
            } else {
                if (lesson.getLessonLengthId() == 0) {
                    switch (lesson.getLessonType()) {
                        case 0:
                            privateTotalChildCount += 1;
                            privateTotalChild += mSettingsSalary.getTeacherPrivateChild();
                            privateTotalChildIncome += mSettingsSalary.getStudentPrivateChild();
                            break;
                        case 1:
                            pairTotalChildCount += 1;
                            pairTotalChild += mSettingsSalary.getTeacherPairChild();
                            pairTotalChildIncome += mSettingsSalary.getStudentPairChild();
                            break;
                        case 2:
                            groupTotalChildCount += 1;
                            groupTotalChild += mSettingsSalary.getTeacherGroupChild();
                            groupTotalChildIncome += mSettingsSalary.getStudentGroupChild();
                            break;
                        case 3:
                            onlineTotalChildCount += 1;
                            onlineTotalChild += mSettingsSalary.getTeacherOnLineChild();
                            onlineTotalChildIncome += mSettingsSalary.getStudentOnLineChild();
                            break;
                    }
                } else {
                    switch (lesson.getLessonType()) {
                        case 0:
                            privateTotalChildCount += 1;
                            privateTotalChild += mSettingsSalary.getTeacherPrivate15Child();
                            privateTotalChildIncome += mSettingsSalary.getStudentPrivate15Child();
                            break;
                        case 1:
                            pairTotalChildCount += 1;
                            pairTotalChild += mSettingsSalary.getTeacherPair15Child();
                            pairTotalChildIncome += mSettingsSalary.getStudentPair15Child();
                            break;
                        case 2:
                            groupTotalChildCount += 1;
                            groupTotalChild += mSettingsSalary.getTeacherGroup15Child();
                            groupTotalChildIncome += mSettingsSalary.getStudentGroup15Child();
                            break;
                        case 3:
                            onlineTotalChildCount += 1;
                            onlineTotalChild += mSettingsSalary.getTeacherOnLine15Child();
                            onlineTotalChildIncome += mSettingsSalary.getStudentOnLine15Child();
                            break;
                    }
                }
            }
        }//lessons
//        total = privateTotal + pairTotal + groupTotal + onlineTotal +
//                privateTotalChild + pairTotalChild + groupTotalChild + onlineTotalChild;
        totalIncome = privateTotalIncome + pairTotalIncome + groupTotalIncome + onlineTotalIncome +
                privateTotalChildIncome + pairTotalChildIncome + groupTotalChildIncome + onlineTotalChildIncome;
        //
//        iPrivate += privateTotalIncome + privateTotalChildIncome;
//        iPair += pairTotalIncome + pairTotalChildIncome;
//        iGroup += groupTotalIncome + groupTotalChildIncome;
//        iOnLine += onlineTotalIncome + onlineTotalChildIncome;
        mContact.setSaldo(mTotalCharge - totalIncome);
        mTextBalance.setText(String.format(getString(R.string.hrn_d), mContact.getSaldo()));
        mTextBalance.setTextColor(ContextCompat.getColor(this, mContact.getSaldo() < 0 ? R.color.colorPrimary : R.color.colorBlack));
    }

    private void loadContacts() {
        Query ref = FirebaseDatabase.getInstance()
                .getReference(DC.DB_CONTACTS)
                .orderByChild("name");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                List<Contact> contacts = new ArrayList<>();
                for (DataSnapshot clientSS : dataSnapshot.getChildren()) {
                    Contact contact = clientSS.getValue(Contact.class);
                    if (contact != null) {
                        contacts.add(contact);
                    }
                }
                mLessonsAdapter.setContacts(contacts);
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_delete).setVisible(FirebaseUtils.isAdmin());
        menu.findItem(R.id.action_edit).setVisible(true);
        return true;
    }

    @OnClick(R.id.button_recharge)
    public void onViewClicked() {
        startActivity(RechargeEditActivity.getLaunchIntent(this, mContact.getKey()));
    }

    @OnClick({R.id.card_phone1, R.id.card_phone2})
    public void onViewClicked(View view) {
        Intent callIntent;
        switch (view.getId()) {
            case R.id.card_phone1:
                callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + mContact.getPhone()));
                startActivity(callIntent);
                break;
            case R.id.card_phone2:
                callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + mContact.getPhone2()));
                startActivity(callIntent);
                break;
        }
    }

    @Override
    public void onCommentClicked(final Lesson lesson) {
        if (lesson.hasDescription())
            Toast.makeText(this, lesson.getDescription(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onEditClicked(final Lesson lesson) {
        startActivityForResult(LessonActivity.getLaunchIntent(this, lesson), RC_LESSON);
    }

    @Override
    public void onRemoveClicked(final Lesson lesson) {
        if (FirebaseUtils.isAdmin() || lesson.getUserId().equals(FirebaseAuth.getInstance().getUid())) {
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(ContactDetailsActivity.this);
            builder.setTitle(R.string.dialog_calendar_remove_title)
                    .setPositiveButton(R.string.action_remove, (dialog, which) -> {
                        removeLesson(lesson);
                    })
                    .setNegativeButton(R.string.action_cancel, null)
                    .show();
        } else {
            Toast.makeText(this, R.string.toast_lesson_remove_unavailable, Toast.LENGTH_LONG).show();
        }
    }

    private void removeLesson(final Lesson lesson) {
        getDatabase()
                .getReference(DC.DB_LESSONS)
                .child(DateUtils.toString(lesson.getDateTime(), "yyyy/MM/dd"))
                .child(lesson.getKey()).removeValue().addOnSuccessListener(aVoid -> {
            loadLessons();
            Toast.makeText(this, R.string.toast_lesson_removed, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onRemoveClicked(final ContactRecharge contactRecharge) {
        getDatabase().getReference(DC.DB_CONTACTS_RECHARGE)
                .child(contactRecharge.getContactKey())
                .child(contactRecharge.getKey()).removeValue().addOnCompleteListener(task ->
                Snackbar.make(mListLessons, getString(R.string.toast_deleted), Snackbar.LENGTH_LONG).show());
    }

    @Override
    public void onEditClicked(final ContactRecharge contactRecharge) {
        startActivity(RechargeEditActivity.getLaunchIntent(this, contactRecharge));
    }
}

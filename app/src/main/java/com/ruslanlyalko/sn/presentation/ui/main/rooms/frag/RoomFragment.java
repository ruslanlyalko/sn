package com.ruslanlyalko.sn.presentation.ui.main.rooms.frag;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.sn.R;
import com.ruslanlyalko.sn.common.DateUtils;
import com.ruslanlyalko.sn.data.FirebaseUtils;
import com.ruslanlyalko.sn.data.configuration.DC;
import com.ruslanlyalko.sn.data.models.Contact;
import com.ruslanlyalko.sn.data.models.Lesson;
import com.ruslanlyalko.sn.presentation.base.BaseFragment;
import com.ruslanlyalko.sn.presentation.ui.main.calendar.adapter.LessonsAdapter;
import com.ruslanlyalko.sn.presentation.ui.main.calendar.adapter.OnLessonClickListener;
import com.ruslanlyalko.sn.presentation.ui.main.lesson.LessonActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;

/**
 * Created by Ruslan Lyalko
 * on 19.05.2018.
 */
public class RoomFragment extends BaseFragment implements OnLessonClickListener {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final int RC_LESSON = 1001;
    @BindView(R.id.recycler_view) RecyclerView mReportsList;
    private int mCurrentRoomType;
    private Date mCurrentDate = new Date();
    private String mUserId;
    private LessonsAdapter mLessonsAdapter = new LessonsAdapter(this);

    public RoomFragment() {
    }

    public static RoomFragment newInstance(int sectionNumber) {
        RoomFragment fragment = new RoomFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public void setDate(Date date) {
        mCurrentDate = date;
        loadLessons();
    }

    @Override
    protected void onViewReady(final Bundle savedInstanceState) {
        mUserId = FirebaseAuth.getInstance().getUid();
        initRecycler();
        loadLessons();
        loadContacts();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_room;
    }

    @Override
    protected void parseArguments() {
        if (getArguments() != null)
            mCurrentRoomType = getArguments().getInt(ARG_SECTION_NUMBER, 0);
    }

    private void initRecycler() {
        mReportsList.setLayoutManager(new LinearLayoutManager(getContext()));
        mReportsList.setAdapter(mLessonsAdapter);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        reloadLessons();
    }

    private void loadLessons() {
        if (getActivity() != null && getActivity().isDestroyed()) return;
        String aDate = DateFormat.format("yyyy/MM/dd", mCurrentDate).toString();
        FirebaseDatabase.getInstance().getReference(DC.DB_LESSONS)
                .child(aDate)
                .orderByChild("dateTime/time")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        List<Lesson> lessons = new ArrayList<>();
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            Lesson lesson = data.getValue(Lesson.class);
                            if (lesson != null) {
                                if ((FirebaseUtils.isAdmin() || lesson.getUserId().equals(mUserId))
                                        && lesson.getRoomType() == mCurrentRoomType) {
                                    lessons.add(lesson);
                                }
                            }
                        }
                        mLessonsAdapter.setData(lessons);
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
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
    public void onCommentClicked(final Lesson lesson) {
        if (lesson.hasDescription())
            Toast.makeText(getContext(), lesson.getDescription(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onEditClicked(final Lesson lesson) {
        startActivityForResult(LessonActivity.getLaunchIntent(getContext(), lesson), RC_LESSON);
    }

    @Override
    public void onRemoveClicked(final Lesson lesson) {
        if (FirebaseUtils.isAdmin() || lesson.getUserId().equals(mUserId)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getBaseActivity());
            builder.setTitle(R.string.dialog_calendar_remove_title)
                    .setPositiveButton(R.string.action_remove, (dialog, which) -> removeLesson(lesson))
                    .setNegativeButton(R.string.action_cancel, null)
                    .show();
        } else {
            Toast.makeText(getContext(), R.string.toast_lesson_remove_unavailable, Toast.LENGTH_LONG).show();
        }
    }

    private void removeLesson(final Lesson lesson) {
        FirebaseDatabase.getInstance()
                .getReference(DC.DB_LESSONS)
                .child(DateUtils.toString(lesson.getDateTime(), "yyyy/MM/dd"))
                .child(lesson.getKey()).removeValue().addOnSuccessListener(aVoid -> {
            reloadLessons();
            Toast.makeText(getContext(), R.string.toast_lesson_removed, Toast.LENGTH_LONG).show();
        });
    }

    private void reloadLessons() {
        loadLessons();
    }
}

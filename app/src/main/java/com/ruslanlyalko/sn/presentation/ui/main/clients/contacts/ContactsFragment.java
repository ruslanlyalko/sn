package com.ruslanlyalko.sn.presentation.ui.main.clients.contacts;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.sn.R;
import com.ruslanlyalko.sn.common.Keys;
import com.ruslanlyalko.sn.common.UserType;
import com.ruslanlyalko.sn.data.FirebaseUtils;
import com.ruslanlyalko.sn.data.configuration.DC;
import com.ruslanlyalko.sn.data.models.Contact;
import com.ruslanlyalko.sn.data.models.User;
import com.ruslanlyalko.sn.presentation.base.BaseFragment;
import com.ruslanlyalko.sn.presentation.ui.main.clients.OnFilterListener;
import com.ruslanlyalko.sn.presentation.ui.main.clients.contacts.adapter.ContactsAdapter;
import com.ruslanlyalko.sn.presentation.ui.main.clients.contacts.adapter.OnContactClickListener;
import com.ruslanlyalko.sn.presentation.ui.main.clients.contacts.details.ContactDetailsActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * Created by Ruslan Lyalko
 * on 29.01.2018.
 */
public class ContactsFragment extends BaseFragment implements OnContactClickListener {

    @BindView(R.id.list_contacts) RecyclerView mListContacts;
    @BindView(R.id.edit_filter_name) EditText mEditFilterName;
    @BindView(R.id.text_count) TextView mTextCount;
    @BindView(R.id.edit_filter_phone) EditText mEditFilterPhone;
    @BindView(R.id.image_clear) ImageView mImageClear;
    @BindView(R.id.check_box_my) CheckBox mCheckBoxMy;
    @BindView(R.id.spinner_teacher) Spinner mSpinnerTeacher;
    @BindView(R.id.layout_filter_spinner) LinearLayout mLayoutFilterSpinner;

    private ContactsAdapter mContactsAdapter;
    private OnFilterListener mOnFilterListener;
    private int mUserType = UserType.ADULT;
    private boolean mIsSelectable;
    private List<String> mSelectedClients;
    private String mTeacherId = "";
    private List<User> mUsers = new ArrayList<>();

    public ContactsFragment() {
    }

    public static ContactsFragment newInstance(final int tabIndex, final boolean isSelectable) {
        ContactsFragment fragment = new ContactsFragment();
        Bundle args = new Bundle();
        args.putInt(Keys.Extras.EXTRA_TAB_INDEX, tabIndex);
        args.putBoolean(Keys.Extras.EXTRA_IS_SELCTABLE, isSelectable);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_contacts;
    }

    @Override
    protected void onViewReady(final Bundle savedInstanceState) {
        mLayoutFilterSpinner.setVisibility(FirebaseUtils.isAdmin() ? View.VISIBLE : View.GONE);
        mCheckBoxMy.setVisibility(FirebaseUtils.isAdmin() ? View.GONE : View.VISIBLE);
        mContactsAdapter = new ContactsAdapter(this, getActivity(), mIsSelectable);
        if (mSelectedClients != null)
            mContactsAdapter.setSelectedCotacts(mSelectedClients);
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                mImageClear.setVisibility(mEditFilterName.getText().length() > 0
                        || mEditFilterPhone.getText().length() > 0
                        ? View.VISIBLE : View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(final Editable s) {
            }
        };
        mEditFilterName.addTextChangedListener(watcher);
        mEditFilterPhone.addTextChangedListener(watcher);
        setupRecycler();
        loadContacts();
        if (FirebaseUtils.isAdmin())
            loadUsers();
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
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_selectable_list_item);
        List<String> userNamesList = new ArrayList<>();
        userNamesList.add(getString(R.string.spinner_not_selected));
        for (int i = 0; i < mUsers.size(); i++) {
            userNamesList.add(mUsers.get(i).getFullName());
        }
        arrayAdapter.addAll(userNamesList);
        mSpinnerTeacher.setAdapter(arrayAdapter);
        mSpinnerTeacher.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                long index = mSpinnerTeacher.getSelectedItemId();
                if (index < 1) {
                    if (!mTeacherId.equals("")) {
                        mTeacherId = "";
                        onFilterTextChanged();
                    }
                } else {
                    mTeacherId = "/" + mUsers.get((int) index - 1).getId();
                    onFilterTextChanged();
                }
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
            }
        });
    }

    @Override
    protected void parseArguments() {
        if (getArguments() == null) return;
        mUserType = getArguments().getInt(Keys.Extras.EXTRA_TAB_INDEX, 0);
        mIsSelectable = getArguments().getBoolean(Keys.Extras.EXTRA_IS_SELCTABLE, false);
    }

    private void setupRecycler() {
        mListContacts.setLayoutManager(new LinearLayoutManager(getContext()));
        mListContacts.setAdapter(mContactsAdapter);
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
                    if (contact != null && contact.getUserType() == mUserType) {
                        contacts.add(contact);
                    }
                }
                mContactsAdapter.setData(contacts);
                onFilterTextChanged();
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
            }
        });
    }

    @OnTextChanged({R.id.edit_filter_name, R.id.edit_filter_phone})
    void onFilterTextChanged() {
        String name = mEditFilterName.getText().toString().trim();
        String phone = mEditFilterPhone.getText().toString().trim();
        if (name.equals(""))
            name = " ";
        if (phone.equals(""))
            phone = " ";
        String filter = name + "/" + phone + mTeacherId;
        mContactsAdapter.getFilter().filter(filter);
        if (FirebaseUtils.isAdmin()) {
            new Handler().postDelayed(() -> {
                String title = "[" + mContactsAdapter.getItemCount() + "]";
                mTextCount.setText(title);
            }, 300);
        }
        if (mOnFilterListener != null)
            mOnFilterListener.onFilterChanged(name, phone);
    }

    @OnCheckedChanged(R.id.check_box_my)
    void onMyChanged(boolean isChecked) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && isChecked)
            mTeacherId = "/" + user.getUid();
        else
            mTeacherId = "";
        onFilterTextChanged();
    }

    @Override
    public void onItemClicked(final int position, ActivityOptionsCompat options) {
        startActivity(ContactDetailsActivity.getLaunchIntent(getContext(), mContactsAdapter.getItem(position)), options.toBundle());
    }

    @Override
    public void onItemsCheckedChanged(final List<String> contacts) {
        mSelectedClients = contacts;
    }

    @OnClick(R.id.image_clear)
    public void onClearClick() {
        mEditFilterName.setText("");
        mEditFilterPhone.setText("");
        mCheckBoxMy.requestFocus();
        getBaseActivity().hideKeyboard();
    }

    public void updateSelected(final List<String> clients) {
        mSelectedClients = clients;
        if (mContactsAdapter != null)
            mContactsAdapter.setSelectedCotacts(clients);
    }

    public List<String> getSelected() {
        return mSelectedClients;
    }
}

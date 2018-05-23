package com.ruslanlyalko.sn.presentation.ui.main.clients.contacts.adapter;

import android.app.Activity;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ruslanlyalko.sn.R;
import com.ruslanlyalko.sn.common.DateUtils;
import com.ruslanlyalko.sn.data.models.Contact;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.MyViewHolder>
        implements Filterable {

    private final OnContactClickListener mOnContactClickListener;
    private final Activity mActivity;
    private boolean mIsSelectable;
    private List<Contact> mDataSource = new ArrayList<>();
    private List<Contact> mDataSourceFiltered = new ArrayList<>();
    private MyFilter mFilter;
    private List<String> mCheckedContacts = new ArrayList<>();

    public ContactsAdapter(final OnContactClickListener onContactClickListener, final Activity activity, boolean isSelectable) {
        mOnContactClickListener = onContactClickListener;
        mActivity = activity;
        mIsSelectable = isSelectable;
    }

    public void clearAll() {
        mDataSource.clear();
        mDataSourceFiltered.clear();
        notifyDataSetChanged();
    }

    public void add(final Contact contactComment) {
        if (mDataSource.contains(contactComment)) return;
        mDataSource.add(contactComment);
        mDataSourceFiltered.add(contactComment);
        notifyItemInserted(mDataSource.size());
    }

    public void setData(final List<Contact> contactComments) {
        mDataSource.clear();
        mDataSource.addAll(contactComments);
        mDataSourceFiltered.clear();
        mDataSourceFiltered.addAll(contactComments);
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_contact, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final Contact contact = mDataSourceFiltered.get(position);
        holder.bindData(contact);
    }

    @Override
    public int getItemCount() {
        return mDataSourceFiltered.size();
    }

    public Contact getItem(final int position) {
        return mDataSourceFiltered.get(position);
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null)
            mFilter = new MyFilter();
        return mFilter;
    }

    public void setSelectedCotacts(final List<String> clients) {
        mCheckedContacts = clients;
        notifyDataSetChanged();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_avatar) ImageView mImageView;
        @BindView(R.id.text_name) TextView mTextName;
        @BindView(R.id.text_phone1) TextView mTextPhone1;
        @BindView(R.id.text_phone2) TextView mTextPhone2;
        @BindView(R.id.text_sub_title) TextView mTextSubTitle;
        @BindView(R.id.layout_root) LinearLayout mLayoutRoot;
        @BindView(R.id.check_box_selected) CheckBox mCheckBoxSelected;

        MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void bindData(final Contact contact) {
            mTextName.setText(contact.getName());
            mTextPhone1.setText(contact.getPhone());
            if (contact.getPhone2() != null & !contact.getPhone2().isEmpty())
                mTextPhone2.setText(contact.getPhone2());
            else
                mTextPhone2.setText("");
            String subtitle = "";
            if (contact.hasUser())
                subtitle = "[" + contact.getUserName() + "] ";
            if (contact.getBirthDay().getTime() != contact.getCreatedAt().getTime())
                subtitle += DateUtils.toString(contact.getBirthDay(), "dd.MM.yyyy");
            subtitle += " " + contact.getEmail();
            mTextSubTitle.setText(subtitle);
            mCheckBoxSelected.setVisibility(mIsSelectable ? View.VISIBLE : View.GONE);
            mCheckBoxSelected.setChecked(mCheckedContacts.contains(contact.getKey()));
        }

        @OnClick(R.id.check_box_selected)
        void onCheckedChanged() {
            boolean isChecked = mCheckBoxSelected.isChecked();
            if (mOnContactClickListener == null) return;
            if (isChecked && !mCheckedContacts.contains(mDataSourceFiltered.get(getAdapterPosition()).getKey())) {
                mCheckedContacts.add(mDataSourceFiltered.get(getAdapterPosition()).getKey());
                mOnContactClickListener.onItemsCheckedChanged(mCheckedContacts);
            } else {
                mCheckedContacts.remove(mDataSourceFiltered.get(getAdapterPosition()).getKey());
                mOnContactClickListener.onItemsCheckedChanged(mCheckedContacts);
            }
        }

        @OnClick(R.id.layout_root)
        void onItemClicked() {
            if (mOnContactClickListener == null) return;
            final ActivityOptionsCompat options;
            options = ActivityOptionsCompat.makeSceneTransitionAnimation(mActivity,
                    Pair.create(mImageView, "avatar"),
                    Pair.create(mTextName, "user"));
            mOnContactClickListener.onItemClicked(getAdapterPosition(), options);
        }
    }

    class MyFilter extends Filter {

        @Override
        protected FilterResults performFiltering(final CharSequence charSequence) {
            FilterResults filterResults = new FilterResults();
            ArrayList<Contact> tempList = new ArrayList<>();
            for (int i = 0; i < mDataSource.size(); i++) {
                Contact contact = mDataSource.get(i);
                if (isMatchFilter(charSequence, contact)) {
                    tempList.add(contact);
                }
            }
            filterResults.count = tempList.size();
            filterResults.values = tempList;
            return filterResults;
        }

        @Override
        protected void publishResults(final CharSequence charSequence, final FilterResults filterResults) {
            mDataSourceFiltered = (ArrayList<Contact>) filterResults.values;
            notifyDataSetChanged();
            notifyDataSetChanged();
        }

        private boolean isMatchFilter(final CharSequence charSequence, final Contact contact) {
            if (charSequence.toString().equals(" / ")) return true;
            String[] filter = charSequence.toString().split("/", 3);
            String name = filter[0];
            String phone = "";
            String teacher = "";
            if (filter.length > 1)
                phone = filter[1];
            if (filter.length > 2)
                teacher = filter[2];
            if (name.equals(" "))
                name = "";
            if (phone.equals(" "))
                phone = "";
            boolean isNameGood = true;
            boolean isPhoneGood = true;
            boolean isTeacherGood = true;
            if (!name.isEmpty() && !(contact.getName().toLowerCase().contains(name.toLowerCase()))) {
                isNameGood = false;
            }
            if (!phone.isEmpty() && !(contact.getPhone().toLowerCase().contains(phone.toLowerCase())
                    || (contact.getPhone2() == null || contact.getPhone2().toLowerCase().contains(phone.toLowerCase())))) {
                isPhoneGood = false;
            }
            if (!teacher.isEmpty() && !(contact.getUserId() != null && contact.getUserId().toLowerCase().contains(teacher.toLowerCase()))) {
                isTeacherGood = false;
            }
            return isNameGood && isPhoneGood && isTeacherGood;
        }
    }
}
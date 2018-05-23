package com.ruslanlyalko.sn.presentation.ui.main.clients.contacts.details.adapter;

import android.content.res.Resources;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ruslanlyalko.sn.R;
import com.ruslanlyalko.sn.common.Constants;
import com.ruslanlyalko.sn.common.DateUtils;
import com.ruslanlyalko.sn.data.FirebaseUtils;
import com.ruslanlyalko.sn.data.models.ContactRecharge;
import com.ruslanlyalko.sn.presentation.widget.SwipeLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class ContactRechargesAdapter extends RecyclerView.Adapter<ContactRechargesAdapter.MyViewHolder> {

    private OnContactRechargeClickListener mOnContactRechargeClickListener;
    private List<ContactRecharge> mContactRechargeList = new ArrayList<>();
    private FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

    public ContactRechargesAdapter(OnContactRechargeClickListener onContactRechargeClickListener) {
        this.mOnContactRechargeClickListener = onContactRechargeClickListener;
    }

    @Override
    public ContactRechargesAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_contact_recharge, parent, false);
        return new ContactRechargesAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ContactRechargesAdapter.MyViewHolder holder, final int position) {
        final ContactRecharge expense = mContactRechargeList.get(position);
        holder.bindData(expense);
    }

    @Override
    public int getItemCount() {
        return mContactRechargeList.size();
    }

    public List<ContactRecharge> getData() {
        return mContactRechargeList;
    }

    public void setData(final List<ContactRecharge> contactRecharges) {
        mContactRechargeList.clear();
        mContactRechargeList.addAll(contactRecharges);
        notifyDataSetChanged();
    }

    public void clearData() {
        int count = mContactRechargeList.size();
        mContactRechargeList.clear();
        notifyItemRangeRemoved(0, count);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private final Resources mResources;

        @BindView(R.id.text_title1) TextView mTextView;
        @BindView(R.id.text_total) TextView mTextPrice;
        @BindView(R.id.text_date) TextView mTextDate;
        @BindView(R.id.linear_user) LinearLayout mUserLayout;
        @BindView(R.id.button_comment) ImageButton mButtonDelete;
        @BindView(R.id.swipe_menu) LinearLayout mMenuLayout;
        @BindView(R.id.swipe_layout) SwipeLayout mSwipeLayout;
        @BindView(R.id.image_view) ImageView mLogoImage;

        MyViewHolder(View view) {
            super(view);
            mResources = view.getResources();
            ButterKnife.bind(this, view);
        }

        void bindData(final ContactRecharge contactRecharge) {
            mTextView.setText(contactRecharge.getDescription());
            mTextPrice.setText(mResources.getString(R.string.hrn, contactRecharge.getPrice() + ""));
            mTextDate.setText(DateUtils.toString(contactRecharge.getCreatedAt()));
            int diff = DateUtils.getDifference(contactRecharge.getCreatedAt());
            boolean justAdded = (diff <= Constants.COST_EDIT_MIN);
            // Avoid delete
            if (!FirebaseUtils.isAdmin() && justAdded) {
                // start this code after 5* minutes
                new Handler().postDelayed(() -> {
                    mSwipeLayout.close();
                    mSwipeLayout.setRightSwipeEnabled(false);
                    mMenuLayout.setVisibility(View.GONE);
                }, (Constants.COST_EDIT_MIN - diff + 1) * 60 * 1000);
            }
            if (FirebaseUtils.isAdmin() || justAdded) {
                mSwipeLayout.addDrag(SwipeLayout.DragEdge.Right, R.id.swipe_menu);
                mSwipeLayout.setRightSwipeEnabled(true);
                mSwipeLayout.setBottomSwipeEnabled(false);
            } else {
                mMenuLayout.setVisibility(View.GONE);
            }
        }

        @OnClick(R.id.button_comment)
        void onRemoveClicked() {
            if (mOnContactRechargeClickListener != null) {
                mOnContactRechargeClickListener.onRemoveClicked(mContactRechargeList.get(getAdapterPosition()));
                mSwipeLayout.close();
            }
        }

        @OnLongClick(R.id.linear_user)
        boolean onEditClicked() {
            if (mOnContactRechargeClickListener != null) {
                mOnContactRechargeClickListener.onEditClicked(mContactRechargeList.get(getAdapterPosition()));
                return true;
            }
            return false;
        }
    }
}
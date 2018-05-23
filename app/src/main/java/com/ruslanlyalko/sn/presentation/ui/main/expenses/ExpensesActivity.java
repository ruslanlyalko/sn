package com.ruslanlyalko.sn.presentation.ui.main.expenses;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.sn.R;
import com.ruslanlyalko.sn.common.DateUtils;
import com.ruslanlyalko.sn.data.FirebaseUtils;
import com.ruslanlyalko.sn.data.configuration.DC;
import com.ruslanlyalko.sn.data.models.Expense;
import com.ruslanlyalko.sn.presentation.base.BaseActivity;
import com.ruslanlyalko.sn.presentation.ui.main.expenses.adapter.ExpensesAdapter;
import com.ruslanlyalko.sn.presentation.ui.main.expenses.adapter.OnExpenseClickListener;
import com.ruslanlyalko.sn.presentation.ui.main.expenses.edit.ExpenseEditActivity;
import com.ruslanlyalko.sn.presentation.widget.PhotoPreviewActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

public class ExpensesActivity extends BaseActivity implements OnExpenseClickListener {

    @BindView(R.id.text_cost_total) TextSwitcher mTotalSwitcher;
    @BindView(R.id.text_month) TextView mTextMonth;
    @BindView(R.id.text_user_name) TextView mTextUserName;
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.text_cost_common) TextView mTextCostCommon;
    @BindView(R.id.text_cost_mk) TextView mTextCostMk;
    @BindView(R.id.button_cost_delete_all) Button mButtonCostDeleteAll;
    @BindView(R.id.recycler_view) RecyclerView mExpensesList;
    @BindView(R.id.faded_background) View mFadedBackground;
    @BindView(R.id.fab2) FloatingActionButton mFab2;
    @BindView(R.id.textFab2) TextView mTextFab2;
    @BindView(R.id.fab1) FloatingActionButton mFab1;
    @BindView(R.id.textFab1) TextView mTextFab1;
    @BindView(R.id.fab) FloatingActionButton mFab;
    @BindView(R.id.progress_bar_upload) ProgressBar mProgressBarUpload;
    @BindView(R.id.text_expense_placeholder) TextView mPlaceholder;

    private FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
    private Animation fab_open, fab_close, rotate_forward, rotate_backward, fade, fade_back_quick;
    private ExpensesAdapter mExpensesAdapter = new ExpensesAdapter(this);
    private Boolean mIsFabOpen = false;
    private Calendar mCurrentMonth = Calendar.getInstance();

    public static Intent getLaunchIntent(final Context launchIntent) {
        return new Intent(launchIntent, ExpensesActivity.class);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_expenses;
    }

    @Override
    protected void setupView() {
        initSwitcher();
        initRecycler();
        initFAB();
        mButtonCostDeleteAll.setVisibility(FirebaseUtils.isAdmin() ? View.VISIBLE : View.GONE);
        mTextUserName.setText(mCurrentUser.getDisplayName());
        updateMonth();
        loadExpenses();
    }

    @Override
    public void onBackPressed() {
        if (mProgressBarUpload.getVisibility() == View.VISIBLE) {
            Toast.makeText(this, R.string.toast_photo_uploading, Toast.LENGTH_SHORT).show();
            return;
        }
        super.onBackPressed();
    }

    private void updateMonth() {
        mTextMonth.setText(DateUtils.getMonthWithYear(getResources(), mCurrentMonth));
    }

    @OnClick(R.id.button_prev)
    void onPrevClicked() {
        setSwitcherAnim(true);
        mCurrentMonth.add(Calendar.MONTH, -1);
        updateMonth();
        loadExpenses();
    }

    @OnClick(R.id.button_next)
    void onNextClicked() {
        setSwitcherAnim(false);
        mCurrentMonth.add(Calendar.MONTH, 1);
        updateMonth();
        loadExpenses();
    }

    private void initSwitcher() {
        mTotalSwitcher.setFactory(() -> {
            TextView myText = new TextView(ExpensesActivity.this);
            myText.setTextSize(32);
            myText.setTextColor(Color.BLACK);
            return myText;
        });
    }

    private void initRecycler() {
        mExpensesList.setLayoutManager(new LinearLayoutManager(this));
        mExpensesList.setAdapter(mExpensesAdapter);
    }

    private void initFAB() {
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);
        fade = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
        fade_back_quick = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_back_quick);
    }

    private void loadExpenses() {
        getDatabase().getReference(DC.DB_EXPENSES)
                .child(DateUtils.toString(mCurrentMonth.getTime(), "yyyy/M"))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        List<Expense> expenseList = new ArrayList<>();
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            Expense expense = data.getValue(Expense.class);
                            if (expense != null && (FirebaseUtils.isAdmin() || expense.getUserId().equals(mCurrentUser.getUid()))) {
                                expenseList.add(0, expense);
                            }
                        }
                        if (isDestroyed()) return;
                        mExpensesAdapter.setData(expenseList);
                        togglePlaceholder(expenseList.size());
                        calcTotal();
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }

    private void togglePlaceholder(final int size) {
        mPlaceholder.setVisibility(size == 0 ? View.VISIBLE : View.GONE);
        mExpensesList.setVisibility(size != 0 ? View.VISIBLE : View.GONE);
    }

    private void calcTotal() {
        if (isDestroyed()) return;
        int type0 = 0;
        int type1 = 0;
        List<Expense> expenseList = mExpensesAdapter.getData();
        for (Expense expense : expenseList) {
            if (expense.getTypeId() == 0)
                type0 += expense.getPrice();
            if (expense.getTypeId() == 1)
                type1 += expense.getPrice();
        }
        int total = type0 + type1;
        mProgressBar.setMax(total);
        mProgressBar.setProgress(type0);
        mTextCostCommon.setText(getString(R.string.hrn, DateUtils.getIntWithSpace(type0)));
        mTextCostMk.setText(getString(R.string.hrn, DateUtils.getIntWithSpace(type1)));
        mTotalSwitcher.setText(getString(R.string.HRN, DateUtils.getIntWithSpace(total)));
    }

    @OnClick({R.id.fab, R.id.faded_background})
    void onFabClicked() {
        animateFAB();
    }
    /*
     int REQUEST_CODE_CAMERA =123;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_CODE_CAMERA);
     */

    public void animateFAB() {
        if (mIsFabOpen) {
            mFab.startAnimation(rotate_backward);
            mFab1.startAnimation(fab_close);
            mFab2.startAnimation(fab_close);
            mFab1.setClickable(false);
            mFab2.setClickable(false);
            mTextFab1.startAnimation(fab_close);
            mTextFab2.startAnimation(fab_close);
            mFadedBackground.setClickable(false);
            mFadedBackground.startAnimation(fade_back_quick);
            mIsFabOpen = false;
        } else {
            mFab.startAnimation(rotate_forward);
            mFab1.startAnimation(fab_open);
            mFab2.startAnimation(fab_open);
            mTextFab1.startAnimation(fab_open);
            mTextFab2.startAnimation(fab_open);
            mFab1.setClickable(true);
            mFab2.setClickable(true);
            mFadedBackground.setClickable(true);
            mFadedBackground.startAnimation(fade);
            mIsFabOpen = true;
        }
    }

    @OnClick(R.id.fab1)
    void onFab1Clicked() {
        animateFAB();
        startActivity(ExpenseEditActivity.getLaunchIntent(this, 1));
    }

    @OnClick(R.id.fab2)
    void onFab2Clicked() {
        animateFAB();
        startActivity(ExpenseEditActivity.getLaunchIntent(this, 0));
    }

    @OnClick(R.id.button_cost_delete_all)
    void onDeleteAllCLicked() {
        if (FirebaseUtils.isAdmin()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ExpensesActivity.this);
            builder.setTitle(R.string.dialog_cost_delete_all_title)
                    .setMessage(R.string.dialog_cost_delete_all_message)
                    .setPositiveButton(R.string.action_remove, (dialog, which) -> removeAllExpenses())
                    .setNegativeButton(R.string.action_cancel, null)
                    .show();
        }
    }

    private void removeAllExpenses() {
        mExpensesAdapter.clearData();
        String dateStr = new SimpleDateFormat("yyyy/M", Locale.US).format(new Date());
        getDatabase().getReference(DC.DB_EXPENSES)
                .child(dateStr)
                .removeValue();
        calcTotal();
    }

    private void setSwitcherAnim(final boolean right) {
        Animation in;
        Animation out;
        if (right) {
            in = AnimationUtils.loadAnimation(this, R.anim.trans_right_in);
            out = AnimationUtils.loadAnimation(this, R.anim.trans_right_out);
        } else {
            in = AnimationUtils.loadAnimation(this, R.anim.trans_left_in);
            out = AnimationUtils.loadAnimation(this, R.anim.trans_left_out);
        }
        mTotalSwitcher.setInAnimation(in);
        mTotalSwitcher.setOutAnimation(out);
    }

    @Override
    public void onRemoveClicked(final Expense expense) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_cost_delete_title)
                .setMessage(R.string.dialog_cost_delete_message)
                .setPositiveButton(R.string.action_remove, (dialog, which) -> removeExpense(expense))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    @Override
    public void onEditClicked(final Expense expense) {
        if (FirebaseUtils.isAdmin()) {
            startActivity(ExpenseEditActivity.getLaunchIntent(this, expense));
        }
    }

    @Override
    public void onPhotoPreviewClicked(final Expense expense) {
        startActivity(PhotoPreviewActivity.getLaunchIntent(this, expense.getImage(), expense.getUserName(), DC.STORAGE_EXPENSES));
    }

    private void removeExpense(Expense expense) {
        getDatabase().getReference(DC.DB_EXPENSES)
                .child(DateUtils.toString(expense.getExpenseDate(), "yyyy/M"))
                .child(expense.getKey()).removeValue().addOnCompleteListener(task ->
                Snackbar.make(mExpensesList, getString(R.string.toast_deleted), Snackbar.LENGTH_LONG).show());
    }
}

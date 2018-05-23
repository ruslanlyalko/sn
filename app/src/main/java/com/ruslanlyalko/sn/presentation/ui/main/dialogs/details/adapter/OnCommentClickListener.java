package com.ruslanlyalko.sn.presentation.ui.main.dialogs.details.adapter;

import android.view.View;

/**
 * Created by Ruslan Lyalko
 * on 14.01.2018.
 */

public interface OnCommentClickListener {

    void onItemClicked(View v, final int position);
    void onUserClicked(final int position);
    void onItemLongClicked(final int position);
}

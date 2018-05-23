package com.ruslanlyalko.sn.presentation.ui.main.clients.contacts.adapter;

import android.support.v4.app.ActivityOptionsCompat;

import com.ruslanlyalko.sn.data.models.Contact;

import java.util.List;

/**
 * Created by Ruslan Lyalko
 * on 12.11.2017.
 */

public interface OnContactClickListener {

    void onItemClicked(int position, ActivityOptionsCompat options);

    void onItemsCheckedChanged(List<String> contacts);
}

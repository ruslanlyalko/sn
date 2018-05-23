package com.ruslanlyalko.sn.presentation.ui.main.clients;

import com.ruslanlyalko.sn.common.UserType;
import com.ruslanlyalko.sn.data.models.Contact;

import java.util.List;

/**
 * Created by Ruslan Lyalko
 * on 02.02.2018.
 */

public interface OnFilterListener {

    void onFilterChanged(String name, String phone);
}

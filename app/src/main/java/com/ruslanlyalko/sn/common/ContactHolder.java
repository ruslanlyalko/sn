package com.ruslanlyalko.sn.common;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class ContactHolder {

    private static List<Pair<String, String>> sContact;
    private static List<String> sContactString = new ArrayList<>();

    public static void setContacts(List<Pair<String, String>> list) {
        sContact = list;
        sContactString.clear();
        for (int i = 0; i < sContact.size(); i++) {
            Pair<String, String> current = sContact.get(i);
            sContactString.add(current.first);
        }
    }

    public static List<Pair<String, String>> getContact() {
        return sContact;
    }

    public static List<String> getContactString() {
        return sContactString;
    }

    public static boolean hasContacts() {
        return sContact != null;
    }
}

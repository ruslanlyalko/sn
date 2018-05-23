package com.ruslanlyalko.sn.presentation.service;

/**
 * Created by Ruslan Lyalko
 * on 11.03.2018.
 */

public enum NotificationType {
    EXPENSE, LESSON, COMMENT,  DEFAULT;


    public static NotificationType findByKey(String s) {
        for (NotificationType type : values())
            if (type.name().equals(s))
                return type;
        return DEFAULT;
    }
}

package com.ruslanlyalko.sn.presentation.ui.main.calendar.adapter;

import com.ruslanlyalko.sn.data.models.Lesson;

/**
 * Created by Ruslan Lyalko
 * on 12.11.2017.
 */

public interface OnLessonClickListener {

    void onCommentClicked(Lesson lesson);

    void onEditClicked(Lesson lesson);

    void onRemoveClicked(Lesson lesson);
}

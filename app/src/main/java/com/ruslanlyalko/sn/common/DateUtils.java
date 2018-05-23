package com.ruslanlyalko.sn.common;

import android.content.res.Resources;

import com.ruslanlyalko.sn.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ruslan Lyalko
 * on 11.11.2017.
 */

public class DateUtils {

    public static String getCurrentTimeStamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
    }

    public static String getIntWithSpace(int data) {
        String resultStr = data + "";
        if (resultStr.length() > 3)
            resultStr = resultStr.substring(0, resultStr.length() - 3) + " "
                    + resultStr.substring(resultStr.length() - 3, resultStr.length());
        return resultStr;
    }

    public static boolean isCurrentYear(final Date date) {
        return date.getYear() == new Date().getYear();
    }

    public static int getDifference(Date date) {
        // Get msec from each, and subtract.
        long diff = new Date().getTime() - date.getTime();
        return (int) TimeUnit.MILLISECONDS.toMinutes(diff);
    }

    public static String getUpdatedAt(final Date updatedAt) {
        if (isTodayOrFuture(updatedAt))
            return toString(updatedAt, "HH:mm");
        return toString(updatedAt, "dd.MM.yy");
    }

    public static boolean isTodayOrFuture(Date date) {
        Date today = new Date();
        today.setHours(0);
        today.setMinutes(0);
        today.setSeconds(0);
        return date.getTime() >= today.getTime();
    }

    public static String toString(final Date date, final String format) {
        return new SimpleDateFormat(format, Locale.getDefault()).format(date);
    }

    public static String toString(final Date date) {
        return new SimpleDateFormat("d-M-yyyy", Locale.getDefault()).format(date);
    }

    public static String getCurrentYear() {
        return new SimpleDateFormat("yyyy", Locale.US).format(new Date());
    }

    public static String getCurrentMonth() {
        return new SimpleDateFormat("M", Locale.US).format(new Date());
    }

    public static Date getDate(final int year, final int month, final int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        return calendar.getTime();
    }

    public static Date getDate(Date date, final int year, final int month, final int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        return calendar.getTime();
    }

    public static String getMonthWithYear(Resources resources, Calendar date) {
        String[] months = resources.getStringArray(R.array.months);
        String yearSimple = toString(date.getTime(), "yy");
        String result = months[date.get(Calendar.MONTH)];
        if (!DateUtils.isCurrentYear(date.getTime()))
            result = result + "'" + yearSimple;
        return result;
    }

    public static Date getDate(final Date date, final int hours, final int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }

    public static String getAge(Date dateOfBirth) {
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        dob.setTime(dateOfBirth);
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        Integer ageInt = Integer.valueOf(age);
        return " (" + ageInt.toString() + "р)";
    }

    public static String getHowLongTime(final Date date, final String pattern) {
        return isToday(date)
                ? "Сьогодні"
                : isTomorrow(date)
                ? "Завтра"
                : isYesterday(date)
                ? "Вчора"
                : toString(date, pattern);
    }

    private static boolean isToday(final Date date) {
        Calendar today = Calendar.getInstance();
        Calendar calDate = Calendar.getInstance();
        calDate.setTime(date);
        return today.get(Calendar.YEAR) == calDate.get(Calendar.YEAR)
                && today.get(Calendar.MONTH) == calDate.get(Calendar.MONTH)
                && today.get(Calendar.DAY_OF_MONTH) == calDate.get(Calendar.DAY_OF_MONTH);
    }

    private static boolean isTomorrow(final Date date) {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        Calendar calDate = Calendar.getInstance();
        calDate.setTime(date);
        return tomorrow.get(Calendar.YEAR) == calDate.get(Calendar.YEAR)
                && tomorrow.get(Calendar.MONTH) == calDate.get(Calendar.MONTH)
                && tomorrow.get(Calendar.DAY_OF_MONTH) == calDate.get(Calendar.DAY_OF_MONTH);
    }

    private static boolean isYesterday(final Date date) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        Calendar calDate = Calendar.getInstance();
        calDate.setTime(date);
        return yesterday.get(Calendar.YEAR) == calDate.get(Calendar.YEAR)
                && yesterday.get(Calendar.MONTH) == calDate.get(Calendar.MONTH)
                && yesterday.get(Calendar.DAY_OF_MONTH) == calDate.get(Calendar.DAY_OF_MONTH);
    }

    public static int getTimeProgress(final Date dateTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateTime);
        int extra = calendar.get(Calendar.MINUTE) > 28 ? 1 : 0;
        return Math.min((calendar.get(Calendar.HOUR_OF_DAY) * 2) + extra, 45);
    }
}

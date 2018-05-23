package com.ruslanlyalko.sn.data.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Lesson implements Serializable {

    private String key;
    private String description;
    private int roomType;
    private int lessonType;
    private int userType;
    private int lessonLengthId;
    private String userId;
    private String userName;
    private Date dateTime = new Date();
    private List<String> clients = new ArrayList<>();

    public Lesson(final String userId, final String userName) {
        this.key = "";
        this.userId = userId;
        this.userName = userName;
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateTime);
        cal.set(Calendar.HOUR_OF_DAY, 16);
        cal.set(Calendar.MINUTE, 0);
        dateTime = cal.getTime();
    }

    public Lesson(final String userId, final String userName, Date date) {
        this.key = "";
        this.userId = userId;
        this.userName = userName;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 16);
        cal.set(Calendar.MINUTE, 0);
        dateTime = cal.getTime();
    }

    public Lesson() {
        key = "";
    }

    public Lesson(final Date date, final String key) {
        this.key = key;
        this.dateTime = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public List<String> getClients() {
        return clients;
    }

    public void setClients(final List<String> clients) {
        this.clients = clients;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(final int userType) {
        this.userType = userType;
    }

    public int getRoomType() {
        return roomType;
    }

    public void setRoomType(final int roomType) {
        this.roomType = roomType;
    }

    public int getLessonType() {
        return lessonType;
    }

    public void setLessonType(final int lessonType) {
        this.lessonType = lessonType;
    }

    public int getLessonLengthId() {
        return lessonLengthId;
    }

    public void setLessonLengthId(final int lessonLengthId) {
        this.lessonLengthId = lessonLengthId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(final Date dateTime) {
        this.dateTime = dateTime;
    }

    public boolean hasDescription() {
        return getDescription() != null && !getDescription().isEmpty();
    }
}

package com.ruslanlyalko.sn.data.models;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Ruslan Lyalko
 * on 29.01.2018.
 */

public class ContactRecharge implements Serializable {

    private String key;
    private String contactKey;
    private int price;
    private Date createdAt = new Date();
    private String description;

    public ContactRecharge() {
    }

    public ContactRecharge(final String contactKey, final Date createdAt) {
        this.contactKey = contactKey;
        this.createdAt = createdAt;
    }


    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public String getContactKey() {
        return contactKey;
    }

    public void setContactKey(final String contactKey) {
        this.contactKey = contactKey;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(final int price) {
        this.price = price;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}
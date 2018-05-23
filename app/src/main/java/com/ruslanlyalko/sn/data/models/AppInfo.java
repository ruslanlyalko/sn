package com.ruslanlyalko.sn.data.models;

import java.io.Serializable;

/**
 * Created by Ruslan Lyalko
 * on 12.03.2018.
 */

public class AppInfo implements Serializable {

    private String link;
    private boolean showAnyWay;
    private String linkFb;
    private String linkSite;
    private String title1;
    private String title2;
    private String aboutText;
    private int latestVersion;

    public AppInfo() {
        aboutText = "Історія, Місія";
        link = "https://play.google.com/store/apps/details?id=com.ruslanlyalko.ll";
        linkFb = "https://www.facebook.com/LiveAndLearnLangauges/?ref=bookmarks";
        linkSite = "http://www.livelearn.com.ua";
        title1 = "Доступна нова версія!";
        title2 = "натисніть тут щоб оновити";
        latestVersion = 0;
    }

    public boolean getShowAnyWay() {
        return showAnyWay;
    }

    public void setShowAnyWay(final boolean showAnyWay) {
        this.showAnyWay = showAnyWay;
    }

    public String getLinkFb() {
        return linkFb;
    }

    public String getLinkSite() {
        return linkSite;
    }

    public void setLinkSite(final String linkSite) {
        this.linkSite = linkSite;
    }

    public void setLinkFb(final String linkFb) {
        this.linkFb = linkFb;
    }

    public String getLink() {
        return link;
    }

    public void setLink(final String link) {
        this.link = link;
    }

    public String getTitle1() {
        return title1;
    }

    public void setTitle1(final String title1) {
        this.title1 = title1;
    }

    public String getTitle2() {
        return title2;
    }

    public void setTitle2(final String title2) {
        this.title2 = title2;
    }

    public String getAboutText() {
        return aboutText;
    }

    public void setAboutText(final String aboutText) {
        this.aboutText = aboutText;
    }

    public int getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(final int latestVersion) {
        this.latestVersion = latestVersion;
    }
}

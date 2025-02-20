package com.mapswithme.util;

import com.mapswithme.maps.BuildConfig;

public final class Constants
{
  public static final int KB = 1024;
  public static final int MB = 1024 * 1024;
  public static final int GB = 1024 * 1024 * 1024;

  static final int CONNECTION_TIMEOUT_MS = 5000;
  static final int READ_TIMEOUT_MS = 30000;

  public static class Url
  {
    public static final String GE0_PREFIX = "ge0://";
    public static final String MAILTO_SCHEME = "mailto:";
    public static final String MAIL_SUBJECT = "?subject=";
    public static final String MAIL_BODY = "&body=";
    public static final String HTTP_GE0_PREFIX = "http://ge0.me/";

    public static final String PLAY_MARKET_HTTPS_APP_PREFIX = "https://play.google.com/store/apps/details?id=";

    public static final String FB_MAPSME_COMMUNITY_HTTP = "http://www.facebook.com/OMapsApp";
    // Profile id is taken from http://graph.facebook.com/MapsWithMe
    public static final String FB_MAPSME_COMMUNITY_NATIVE = "fb://profile/102378968471811";
    public static final String TWITTER_MAPSME_HTTP = "https://twitter.com/OMapsApp";

    public static final String WEB_SITE = "https://omaps.app";

    public static final String COPYRIGHT = "file:///android_asset/copyright.html";
    public static final String FAQ = "file:///android_asset/faq.html";
    public static final String OPENING_HOURS_MANUAL = "file:///android_asset/opening_hours_how_to_edit.html";

    public static final String OSM_REGISTER = "https://www.openstreetmap.org/user/new";
    public static final String OSM_RECOVER_PASSWORD = "https://www.openstreetmap.org/user/forgot-password";
    public static final String OSM_ABOUT = "https://wiki.openstreetmap.org/wiki/About_OpenStreetMap";

    private Url() {}
  }

  public static class Email
  {
    public static final String FEEDBACK = "android@omaps.app";
    public static final String SUPPORT = BuildConfig.SUPPORT_MAIL;
    public static final String RATING = "rating@omaps.app";

    private Email() {}
  }

  public static class Package
  {
    public static final String FB_PACKAGE = "com.facebook.katana";
    public static final String MWM_PRO_PACKAGE = "com.mapswithme.maps.pro";
    public static final String MWM_LITE_PACKAGE = "com.mapswithme.maps";
    public static final String MWM_SAMSUNG_PACKAGE = "com.mapswithme.maps.samsung";
    public static final String TWITTER_PACKAGE = "com.twitter.android";

    private Package() {}
  }

  public static class Rating
  {
    public static final float RATING_INCORRECT_VALUE = 0.0f;

    private Rating() {}
  }

  private Constants() {}
}

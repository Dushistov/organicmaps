package com.mapswithme.util;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mapswithme.maps.Framework;
import com.mapswithme.maps.R;
import com.mapswithme.maps.bookmarks.data.BookmarkInfo;
import com.mapswithme.maps.bookmarks.data.MapObject;
import com.mapswithme.maps.widget.placepage.Sponsored;

public class SharingUtils
{
  private static final String KMZ_MIME_TYPE = "application/vnd.google-earth.kmz";
  private static final String TEXT_MIME_TYPE = "text/plain";

  // This utility class has only static methods
  private SharingUtils()
  {
  }

  public static void shareLocation(@NonNull Context context, @NonNull Location loc)
  {
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType(TEXT_MIME_TYPE);

    final String subject = context.getString(R.string.share);
    intent.putExtra(Intent.EXTRA_SUBJECT, subject);

    final String geoUrl = Framework.nativeGetGe0Url(loc.getLatitude(), loc.getLongitude(), Framework
        .nativeGetDrawScale(), "");
    final String httpUrl = Framework.getHttpGe0Url(loc.getLatitude(), loc.getLongitude(), Framework
        .nativeGetDrawScale(), "");
    final String text = context.getString(R.string.my_position_share_sms, geoUrl, httpUrl);
    intent.putExtra(Intent.EXTRA_TEXT, text);

    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)));
  }

  public static void shareMapObject(@NonNull Context context, @NonNull MapObject object, @Nullable Sponsored sponsored)
  {
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType(TEXT_MIME_TYPE);

    final String subject = MapObject.isOfType(MapObject.MY_POSITION, object) ?
                           context.getString(R.string.my_position_share_email_subject) :
                           context.getString(R.string.bookmark_share_email_subject);
    intent.putExtra(Intent.EXTRA_SUBJECT, subject);

    final String geoUrl = Framework.nativeGetGe0Url(object.getLat(), object.getLon(),
                                                    object.getScale(), object.getName());
    final String httpUrl = Framework.getHttpGe0Url(object.getLat(), object.getLon(),
                                                   object.getScale(), object.getName());
    final String address = TextUtils.isEmpty(object.getAddress()) ? object.getName() : object.getAddress();
    final String text = context.getString(R.string.my_position_share_email, address, geoUrl, httpUrl);
    intent.putExtra(Intent.EXTRA_TEXT, text);

    // Sponsored is unused.

    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)));
  }

  public static void shareBookmark(@NonNull Context context, @NonNull BookmarkInfo bookmark, @Nullable Sponsored sponsored)
  {
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType(TEXT_MIME_TYPE);

    final String subject = context.getString(R.string.bookmark_share_email_subject);
    intent.putExtra(Intent.EXTRA_SUBJECT, subject);

    final String geoUrl = Framework.nativeGetGe0Url(bookmark.getLat(), bookmark.getLon(),
                                                    bookmark.getScale(), bookmark.getName());
    final String httpUrl = Framework.getHttpGe0Url(bookmark.getLat(), bookmark.getLon(),
                                                   bookmark.getScale(), bookmark.getName());
    StringBuilder text = new StringBuilder();
    text.append(bookmark.getName());
    if (!TextUtils.isEmpty(bookmark.getAddress()))
    {
      text.append(UiUtils.NEW_STRING_DELIMITER);
      text.append(bookmark.getAddress());
    }
    text.append(UiUtils.NEW_STRING_DELIMITER);
    text.append(geoUrl);
    text.append(UiUtils.NEW_STRING_DELIMITER);
    text.append(httpUrl);
    if (sponsored != null && sponsored.getType() == Sponsored.TYPE_BOOKING)
    {
      text.append(UiUtils.NEW_STRING_DELIMITER);
      text.append(context.getString(R.string.sharing_booking));
      text.append(sponsored.getUrl());
    }
    intent.putExtra(Intent.EXTRA_TEXT, text.toString());

    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)));
  }

  public static void shareBookmarkFile(Context context, String fileName)
  {
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType(KMZ_MIME_TYPE);

    final String subject = context.getString(R.string.share_bookmarks_email_subject);
    intent.putExtra(Intent.EXTRA_SUBJECT, subject);

    final String text = context.getString(R.string.share_bookmarks_email_body);
    intent.putExtra(Intent.EXTRA_TEXT, text.toString());

    Uri fileUri = StorageUtils.getUriForFilePath(context, fileName);
    intent.putExtra(android.content.Intent.EXTRA_STREAM, fileUri);

    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)));
  }

  public static void shareApplication(@NonNull Context context)
  {
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType(TEXT_MIME_TYPE);

    final String subject = context.getString(R.string.tell_friends);
    intent.putExtra(Intent.EXTRA_SUBJECT, subject);

    final String text = context.getString(R.string.tell_friends_text);
    intent.putExtra(Intent.EXTRA_TEXT, text);

    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)));
  }
}

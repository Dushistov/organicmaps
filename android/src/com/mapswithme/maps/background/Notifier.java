package com.mapswithme.maps.background;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.mapswithme.maps.MwmActivity;
import com.mapswithme.maps.R;
import com.mapswithme.util.StringUtils;
import com.mapswithme.util.UiUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class Notifier
{
  private static final String EXTRA_CANCEL_NOTIFICATION = "extra_cancel_notification";
  private static final String EXTRA_NOTIFICATION_CLICKED = "extra_notification_clicked";

  public static final int ID_NONE = 0;
  public static final int ID_DOWNLOAD_FAILED = 1;
  public static final int ID_IS_NOT_AUTHENTICATED = 2;
  public static final int ID_LEAVE_REVIEW = 3;
  @NonNull
  private final Application mContext;

  @Retention(RetentionPolicy.SOURCE)
  @IntDef({ ID_NONE, ID_DOWNLOAD_FAILED, ID_IS_NOT_AUTHENTICATED, ID_LEAVE_REVIEW })
  public @interface NotificationId
  {
  }

  private Notifier(@NonNull Application context)
  {
    mContext = context;
  }

  public void notifyDownloadFailed(@Nullable String id, @Nullable String name)
  {
    String title = mContext.getString(R.string.app_name);
    String content = mContext.getString(R.string.download_country_failed, name);

    Intent intent = MwmActivity.createShowMapIntent(mContext, id)
                               .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    PendingIntent pi = PendingIntent.getActivity(mContext, 0, intent,
                                                 PendingIntent.FLAG_UPDATE_CURRENT);

    String channel = NotificationChannelFactory.createProvider(mContext).getDownloadingChannel();
    placeNotification(title, content, pi, ID_DOWNLOAD_FAILED, channel);
  }

//  void notifyAuthentication()
//  {
//    Intent authIntent = MwmActivity.createAuthenticateIntent(mContext);
//    authIntent.putExtra(EXTRA_CANCEL_NOTIFICATION, Notifier.ID_IS_NOT_AUTHENTICATED);
//    PendingIntent pi = PendingIntent.getActivity(mContext, 0, authIntent,
//                                                 PendingIntent.FLAG_UPDATE_CURRENT);
//
//    String channel = NotificationChannelFactory.createProvider(mContext).getUGCChannel();
//    NotificationCompat.Builder builder =
//        getBuilder(mContext.getString(R.string.notification_unsent_reviews_title),
//                   mContext.getString(R.string.notification_unsent_reviews_message),
//                   pi, channel);
//
//    builder.addAction(0, mContext.getString(R.string.authorization_button_sign_in), pi);
//
//    getNotificationManager().notify(ID_IS_NOT_AUTHENTICATED, builder.build());
//
//  }

  public void cancelNotification(@NotificationId int id)
  {
    if (id == ID_NONE)
      return;

    getNotificationManager().cancel(id);
  }

  public void processNotificationExtras(@Nullable Intent intent)
  {
    if (intent == null)
      return;

    if (intent.hasExtra(Notifier.EXTRA_CANCEL_NOTIFICATION))
    {
      @Notifier.NotificationId
      int notificationId = intent.getIntExtra(Notifier.EXTRA_CANCEL_NOTIFICATION, Notifier.ID_NONE);
      cancelNotification(notificationId);
    }

    if (intent.hasExtra(Notifier.EXTRA_NOTIFICATION_CLICKED))
    {
      String eventName = intent.getStringExtra(Notifier.EXTRA_NOTIFICATION_CLICKED);
    }
  }

  private void placeNotification(String title, String content, PendingIntent pendingIntent,
                                 int notificationId, @NonNull String channel)
  {
    final Notification notification = getBuilder(title, content, pendingIntent, channel).build();

    getNotificationManager().notify(notificationId, notification);
  }

  @NonNull
  private NotificationCompat.Builder getBuilder(String title, String content,
                                                PendingIntent pendingIntent, @NonNull String channel)
  {

    return new NotificationCompat.Builder(mContext, channel)
        .setAutoCancel(true)
        .setSmallIcon(R.drawable.img_logo)
        .setColor(UiUtils.getNotificationColor(mContext))
        .setContentTitle(title)
        .setContentText(content)
        .setTicker(getTicker(title, content))
        .setContentIntent(pendingIntent);
  }

  @NonNull
  private CharSequence getTicker(String title, String content)
  {
    int templateResId = StringUtils.isRtl() ? R.string.notification_ticker_rtl
                                            : R.string.notification_ticker_ltr;
    return mContext.getString(templateResId, title, content);
  }

  @SuppressWarnings("ConstantConditions")
  @NonNull
  private NotificationManager getNotificationManager()
  {
    return (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
  }

  @NonNull
  public static Notifier from(Application application)
  {
    return new Notifier(application);
  }
}

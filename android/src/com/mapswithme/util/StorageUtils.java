package com.mapswithme.util;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.mapswithme.maps.BuildConfig;
import com.mapswithme.util.log.Logger;
import com.mapswithme.util.log.LoggerFactory;

import java.io.File;

public class StorageUtils
{
  private static final Logger LOGGER = LoggerFactory.INSTANCE.getLogger(LoggerFactory.Type.STORAGE);
  private final static String TAG = StorageUtils.class.getSimpleName();
  private final static String LOGS_FOLDER = "logs";

  /**
   * Checks if external storage is available for read and write
   *
   * @return true if external storage is mounted and ready for reading/writing
   */
  private static boolean isExternalStorageWritable()
  {
    String state = Environment.getExternalStorageState();
    return Environment.MEDIA_MOUNTED.equals(state);
  }

  /**
   * Safely returns the external files directory path with the preliminary
   * checking the availability of the mentioned directory
   *
   * @return the absolute path of external files directory or null if directory can not be obtained
   * @see Context#getExternalFilesDir(String)
   */
  @Nullable
  private static String getExternalFilesDir(@NonNull Application application)
  {
    if (!isExternalStorageWritable())
      return null;

    File dir = application.getExternalFilesDir(null);
    if (dir != null)
      return dir.getAbsolutePath();

    Log.e(StorageUtils.class.getSimpleName(),
          "Cannot get the external files directory for some reasons", new Throwable());
    return null;
  }

  /**
   * Check existence of the folder for writing the logs. If that folder is absent this method will
   * try to create it and all missed parent folders.
   * @return true - if folder exists, otherwise - false
   */
  public static boolean ensureLogsFolderExistence(@NonNull Application application)
  {
    String externalDir = StorageUtils.getExternalFilesDir(application);
    if (TextUtils.isEmpty(externalDir))
      return false;

    File folder = new File(externalDir + File.separator + LOGS_FOLDER);
    boolean success = true;
    if (!folder.exists())
      success = folder.mkdirs();
    return success;
  }

  @Nullable
  public static String getLogsFolder(@NonNull Application application)
  {
    if (!ensureLogsFolderExistence(application))
      return null;

    String externalDir = StorageUtils.getExternalFilesDir(application);
    return externalDir + File.separator + LOGS_FOLDER;
  }

  @Nullable
  static String getLogsZipPath(@NonNull Application application)
  {
    String zipFile = getExternalFilesDir(application) + File.separator + LOGS_FOLDER + ".zip";
    File file = new File(zipFile);
    return file.isFile() && file.exists() ? zipFile : null;
  }

  @NonNull
  public static String getApkPath(@NonNull Application application)
  {
    try
    {
      return application.getPackageManager()
                        .getApplicationInfo(BuildConfig.APPLICATION_ID, 0).sourceDir;
    }
    catch (final PackageManager.NameNotFoundException e)
    {
      LOGGER.e(TAG, "Can't get apk path from PackageManager", e);
      return "";
    }
  }

  @NonNull
  private static String addTrailingSeparator(@NonNull String dir)
  {
    if (!dir.endsWith("/"))
      return dir + File.separator;
    return dir;
  }

  @NonNull
  public static String getSettingsPath(@NonNull Application application)
  {
    return addTrailingSeparator(application.getFilesDir().getAbsolutePath());
  }

  @NonNull
  public static String getPrivatePath(@NonNull Application application)
  {
    return addTrailingSeparator(application.getFilesDir().getAbsolutePath());
  }

  @NonNull
  public static String getTempPath(@NonNull Application application)
  {
    final File cacheDir = application.getExternalCacheDir();
    if (cacheDir != null)
      return addTrailingSeparator(cacheDir.getAbsolutePath());

    return addTrailingSeparator(application.getCacheDir().getAbsolutePath());
  }

  public static boolean createDirectory(@NonNull Context context, @NonNull String path)
  {
    File directory = new File(path);
    if (!directory.exists() && !directory.mkdirs())
    {
      Throwable error = new IllegalStateException("Can't create directories for: " + path);
      LOGGER.e(TAG, "Can't create directories for: " + path);
      CrashlyticsUtils.INSTANCE.logException(error);
      return false;
    }
    return true;
  }

  static long getFileSize(@NonNull String path)
  {
    File file = new File(path);
    return file.length();
  }

  @NonNull
  public static Uri getUriForFilePath(@NonNull Context context, @NonNull String path)
  {
    return FileProvider.getUriForFile(context.getApplicationContext(),
                                      BuildConfig.FILE_PROVIDER_AUTHORITY, new File(path));
  }
}

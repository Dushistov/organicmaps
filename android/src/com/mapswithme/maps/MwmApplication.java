package com.mapswithme.maps;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDex;

import com.mapswithme.maps.background.AppBackgroundTracker;
import com.mapswithme.maps.background.NotificationChannelFactory;
import com.mapswithme.maps.background.NotificationChannelProvider;
import com.mapswithme.maps.background.Notifier;
import com.mapswithme.maps.base.MediaPlayerWrapper;
import com.mapswithme.maps.bookmarks.data.BookmarkManager;
import com.mapswithme.maps.downloader.CountryItem;
import com.mapswithme.maps.downloader.MapManager;
import com.mapswithme.maps.editor.Editor;
import com.mapswithme.maps.location.LocationHelper;
import com.mapswithme.maps.location.TrackRecorder;
import com.mapswithme.maps.maplayer.guides.GuidesManager;
import com.mapswithme.maps.maplayer.isolines.IsolinesManager;
import com.mapswithme.maps.maplayer.subway.SubwayManager;
import com.mapswithme.maps.maplayer.traffic.TrafficManager;
import com.mapswithme.maps.routing.RoutingController;
import com.mapswithme.maps.search.SearchEngine;
import com.mapswithme.maps.settings.StoragePathManager;
import com.mapswithme.maps.sound.TtsPlayer;
import com.mapswithme.util.Config;
import com.mapswithme.util.ConnectionState;
import com.mapswithme.util.Counters;
import com.mapswithme.util.CrashlyticsUtils;
import com.mapswithme.util.SharedPropertiesUtils;
import com.mapswithme.util.StorageUtils;
import com.mapswithme.util.ThemeSwitcher;
import com.mapswithme.util.UiUtils;
import com.mapswithme.util.Utils;
import com.mapswithme.util.log.Logger;
import com.mapswithme.util.log.LoggerFactory;

import java.util.List;

public class MwmApplication extends Application implements AppBackgroundTracker.OnTransitionListener
{
  @SuppressWarnings("NullableProblems")
  @NonNull
  private Logger mLogger;
  public final static String TAG = "MwmApplication";

  private AppBackgroundTracker mBackgroundTracker;
  @SuppressWarnings("NullableProblems")
  @NonNull
  private SubwayManager mSubwayManager;

  @SuppressWarnings("NullableProblems")
  @NonNull
  private IsolinesManager mIsolinesManager;

  @SuppressWarnings("NullableProblems")
  @NonNull
  private GuidesManager mGuidesManager;

  private boolean mFrameworkInitialized;
  private boolean mPlatformInitialized;

  private Handler mMainLoopHandler;
  private final Object mMainQueueToken = new Object();
  @NonNull
  private final MapManager.StorageCallback mStorageCallbacks = new StorageCallbackImpl();
  @SuppressWarnings("NullableProblems")
  @NonNull
  private PurchaseOperationObservable mPurchaseOperationObservable;
  @SuppressWarnings("NullableProblems")
  @NonNull
  private MediaPlayerWrapper mPlayer;
  private boolean mFirstLaunch;

  @NonNull
  public SubwayManager getSubwayManager()
  {
    return mSubwayManager;
  }

  @NonNull
  public IsolinesManager getIsolinesManager()
  {
    return mIsolinesManager;
  }

  public MwmApplication()
  {
    super();
  }

  @NonNull
  public static MwmApplication from(@NonNull Context context)
  {
    return (MwmApplication) context.getApplicationContext();
  }

  @NonNull
  public static AppBackgroundTracker backgroundTracker(@NonNull Context context)
  {
    return ((MwmApplication) context.getApplicationContext()).getBackgroundTracker();
  }

  @NonNull
  public static SharedPreferences prefs(@NonNull Context context)
  {
    String prefFile = context.getString(R.string.pref_file_name);
    return context.getSharedPreferences(prefFile, MODE_PRIVATE);
  }

  @Override
  protected void attachBaseContext(Context base)
  {
    super.attachBaseContext(base);
    MultiDex.install(this);
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Override
  public void onCreate()
  {
    super.onCreate();
    LoggerFactory.INSTANCE.initialize(this);
    mLogger = LoggerFactory.INSTANCE.getLogger(LoggerFactory.Type.MISC);
    getLogger().d(TAG, "Application is created");
    // Set configuration directory as early as possible.
    // Other methods may explicitly use Config, which requires settingsDir to be set.
    setSettingsDir();
    mMainLoopHandler = new Handler(getMainLooper());
    ConnectionState.INSTANCE.initialize(this);
    CrashlyticsUtils.INSTANCE.initialize(this);
    
    initNotificationChannels();

    mBackgroundTracker = new AppBackgroundTracker(this);
    mSubwayManager = new SubwayManager(this);
    mIsolinesManager = new IsolinesManager(this);
    mGuidesManager = new GuidesManager(this);

    mPurchaseOperationObservable = new PurchaseOperationObservable();
    mPlayer = new MediaPlayerWrapper(this);
    WebView.setWebContentsDebuggingEnabled(Utils.isDebugOrBeta());
  }

  private void initNotificationChannels()
  {
    NotificationChannelProvider channelProvider = NotificationChannelFactory.createProvider(this);
    channelProvider.setDownloadingChannel();
  }

  /**
   * Initialize configuration directory.
   */
  public void setSettingsDir()
  {
    final String settingsPath = StorageUtils.getSettingsPath(this);
    if (!StorageUtils.createDirectory(this, settingsPath))
    {
      throw new AssertionError("Can't create settingsDir " + settingsPath);
    }
    getLogger().d(TAG, "Settings path = " + settingsPath);
    nativeSetSettingsDir(settingsPath);
  }

  /**
   * Initialize native core of application: platform and framework. Caller must handle returned value
   * and do nothing with native code if initialization is failed.
   *
   * @return boolean - indicator whether native initialization is successful or not.
   */
  public boolean initCore()
  {
    initNativePlatform();
    if (!mPlatformInitialized)
      return false;

    initNativeFramework();
    return mFrameworkInitialized;
  }

  private void initNativePlatform()
  {
    if (mPlatformInitialized)
      return;

    final Logger log = getLogger();
    final String apkPath = StorageUtils.getApkPath(this);
    log.d(TAG, "Apk path = " + apkPath);
    // Note: StoragePathManager uses Config, which requires initConfig() to be called.
    final String writablePath = new StoragePathManager().findMapsStorage(this);
    log.d(TAG, "Writable path = " + writablePath);
    final String privatePath = StorageUtils.getPrivatePath(this);
    log.d(TAG, "Private path = " + privatePath);
    final String tempPath = StorageUtils.getTempPath(this);
    log.d(TAG, "Temp path = " + tempPath);

    // If platform directories are not created it means that native part of app will not be able
    // to work at all. So, we just ignore native part initialization in this case, e.g. when the
    // external storage is damaged or not available (read-only).
    if (!createPlatformDirectories(writablePath, privatePath, tempPath))
      return;

    nativeInitPlatform(apkPath,
                       writablePath,
                       privatePath,
                       tempPath,
                       BuildConfig.FLAVOR,
                       BuildConfig.BUILD_TYPE, UiUtils.isTablet(this));

    Config.setStatisticsEnabled(SharedPropertiesUtils.isStatisticsEnabled(this));

    Editor.init(this);
    mPlatformInitialized = true;
  }

  private boolean createPlatformDirectories(@NonNull String writablePath,
                                            @NonNull String privatePath,
                                            @NonNull String tempPath)
  {
    if (SharedPropertiesUtils.shouldEmulateBadExternalStorage(this))
      return false;

    return StorageUtils.createDirectory(this, writablePath) &&
           StorageUtils.createDirectory(this, privatePath) &&
           StorageUtils.createDirectory(this, tempPath);
  }

  private void initNativeFramework()
  {
    if (mFrameworkInitialized)
      return;

    nativeInitFramework();

    MapManager.nativeSubscribe(mStorageCallbacks);

    initNativeStrings();
    ThemeSwitcher.INSTANCE.initialize(this);
    SearchEngine.INSTANCE.initialize(null);
    BookmarkManager.loadBookmarks();
    TtsPlayer.INSTANCE.initialize(this);
    ThemeSwitcher.INSTANCE.restart(false);
    LocationHelper.INSTANCE.initialize(this);
    RoutingController.get().initialize(null);
    TrafficManager.INSTANCE.initialize(null);
    SubwayManager.from(this).initialize(null);
    IsolinesManager.from(this).initialize(null);
    GuidesManager.from(this).initialize(null);
    TrackRecorder.INSTANCE.initialize(this);
    mPurchaseOperationObservable.initialize(null);
    mBackgroundTracker.addListener(this);
    mFrameworkInitialized = true;
  }

  private void initNativeStrings()
  {
    nativeAddLocalization("core_entrance", getString(R.string.core_entrance));
    nativeAddLocalization("core_exit", getString(R.string.core_exit));
    nativeAddLocalization("core_my_places", getString(R.string.core_my_places));
    nativeAddLocalization("core_my_position", getString(R.string.core_my_position));
    nativeAddLocalization("core_placepage_unknown_place", getString(R.string.core_placepage_unknown_place));
    nativeAddLocalization("postal_code", getString(R.string.postal_code));
    nativeAddLocalization("wifi", getString(R.string.wifi));
  }

  public boolean arePlatformAndCoreInitialized()
  {
    return mFrameworkInitialized && mPlatformInitialized;
  }

  @NonNull
  public AppBackgroundTracker getBackgroundTracker()
  {
    return mBackgroundTracker;
  }

  static
  {
    System.loadLibrary("mapswithme");
  }

  @NonNull
  PurchaseOperationObservable getPurchaseOperationObservable()
  {
    return mPurchaseOperationObservable;
  }

  public static void onUpgrade(@NonNull Context context)
  {
    Counters.resetAppSessionCounters(context);
  }

  @SuppressWarnings("unused")
  void forwardToMainThread(final long taskPointer)
  {
    Message m = Message.obtain(mMainLoopHandler, new Runnable()
    {
      @Override
      public void run()
      {
        nativeProcessTask(taskPointer);
      }
    });
    m.obj = mMainQueueToken;
    mMainLoopHandler.sendMessage(m);
  }

  @NonNull
  public MediaPlayerWrapper getMediaPlayer()
  {
    return mPlayer;
  }

  private static native void nativeSetSettingsDir(String settingsPath);
  private native void nativeInitPlatform(String apkPath, String writablePath, String privatePath,
                                         String tmpPath, String flavorName, String buildType,
                                         boolean isTablet);
  private static native void nativeInitFramework();
  private static native void nativeProcessTask(long taskPointer);
  private static native void nativeAddLocalization(String name, String value);
  private static native void nativeOnTransit(boolean foreground);

  @NonNull
  public Logger getLogger()
  {
    return mLogger;
  }

  public void setFirstLaunch(boolean isFirstLaunch)
  {
    mFirstLaunch = isFirstLaunch;
  }

  public boolean isFirstLaunch()
  {
    return mFirstLaunch;
  }

  @Override
  public void onTransit(boolean foreground)
  {
    nativeOnTransit(foreground);
  }

  @NonNull
  public GuidesManager getGuidesManager()
  {
    return mGuidesManager;
  }

  private class StorageCallbackImpl implements MapManager.StorageCallback
  {
    @Override
    public void onStatusChanged(List<MapManager.StorageCallbackData> data)
    {
      Notifier notifier = Notifier.from(MwmApplication.this);
      for (MapManager.StorageCallbackData item : data)
        if (item.isLeafNode && item.newStatus == CountryItem.STATUS_FAILED)
        {
          if (MapManager.nativeIsAutoretryFailed())
          {
            notifier.notifyDownloadFailed(item.countryId, MapManager.nativeGetName(item.countryId));
          }

          return;
        }
    }

    @Override
    public void onProgress(String countryId, long localSize, long remoteSize) {}
  }
}

package com.mapswithme.maps.bookmarks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.mapswithme.maps.base.BaseToolbarActivity;

public class BookmarksCatalogActivity extends BaseToolbarActivity
{
  public static final String EXTRA_DOWNLOADED_CATEGORY = "extra_downloaded_category";
  public static final String EXTRA_ARGS = "extra_args";

  public static void startForResult(@NonNull Fragment fragment, int requestCode,
                                    @NonNull String catalogUrl)
  {
    fragment.startActivityForResult(makeLaunchIntent(fragment.requireContext(), catalogUrl),
                                    requestCode);
  }

  public static void startForResult(@NonNull Activity context, int requestCode, @NonNull String catalogUrl)
  {
    context.startActivityForResult(makeLaunchIntent(context, catalogUrl), requestCode);
  }

  @NonNull
  private static Intent makeLaunchIntent(@NonNull Context context, @NonNull String catalogUrl)
  {
    return new Intent(context, BookmarksCatalogActivity.class)
        .putExtra(BookmarksCatalogFragment.EXTRA_BOOKMARKS_CATALOG_URL, catalogUrl)
        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
  }

  @Override
  protected void setupHomeButton(@NonNull Toolbar toolbar)
  {
  }

  @Override
  protected Class<? extends Fragment> getFragmentClass()
  {
    return BookmarksCatalogFragment.class;
  }

  @Override
  protected void onHomeOptionItemSelected()
  {
    finish();
  }
}

package com.mapswithme.maps.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.mapswithme.maps.R;
import com.mapswithme.maps.base.OnBackPressListener;
import com.mapswithme.util.Constants;
import com.mapswithme.util.Utils;

import java.util.List;
import java.util.Locale;

public class StoragePathFragment extends BaseSettingsFragment
                              implements StoragePathManager.MoveFilesListener,
                                         OnBackPressListener
{
  private TextView mHeader;
  private ListView mList;

  private StoragePathAdapter mAdapter;
  private final StoragePathManager mPathManager = new StoragePathManager();

  @Override
  protected int getLayoutRes()
  {
    return R.layout.fragment_prefs_storage;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    View root = super.onCreateView(inflater, container, savedInstanceState);

    mHeader = root.findViewById(R.id.header);
    mList = root.findViewById(R.id.list);
    mList.setOnItemClickListener(new AdapterView.OnItemClickListener()
    {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id)
      {
        mAdapter.onItemClick(position);
      }
    });

    return root;
  }

  @Override
  public void onResume()
  {
    super.onResume();
    mPathManager.startExternalStorageWatching(getActivity(), new StoragePathManager.OnStorageListChangedListener()
    {
      @Override
      public void onStorageListChanged(List<StorageItem> storageItems, int currentStorageIndex)
      {
        updateList();
      }
    }, this);

    if (mAdapter == null)
      mAdapter = new StoragePathAdapter(mPathManager, getActivity());

    updateList();
    mList.setAdapter(mAdapter);
  }

  @Override
  public void onPause()
  {
    super.onPause();
    mPathManager.stopExternalStorageWatching();
  }

  private void updateList()
  {
    long dirSize = StorageUtils.getWritableDirSize();
    mHeader.setText(getString(R.string.maps) + ": " + getSizeString(dirSize));

    if (mAdapter != null)
      mAdapter.update(mPathManager.getStorageItems(), mPathManager.getCurrentStorageIndex(), dirSize);
  }

  @Override
  public void moveFilesFinished(String newPath)
  {
    updateList();
  }

  @Override
  public void moveFilesFailed(int errorCode)
  {
    if (!isAdded())
      return;

    final String message = "Failed to move maps with internal error: " + errorCode;
    final Activity activity = getActivity();
    if (activity.isFinishing())
      return;

    new AlertDialog.Builder(activity)
        .setTitle(message)
        .setPositiveButton(R.string.report_a_bug,
                           (dialog, which) -> Utils.sendBugReport(activity, message)).show();
  }

  static String getSizeString(long size)
  {
    final String[] units = { "Kb", "Mb", "Gb" };

    long current = Constants.KB;
    int i = 0;
    for (; i < units.length; ++i)
    {
      final long bound = Constants.KB * current;
      if (size < bound)
        break;

      current = bound;
    }

    // left 1 digit after the comma and add postfix string
    return String.format(Locale.US, "%.1f %s", (double) size / current, units[i]);
  }

  @Override
  public boolean onBackPressed()
  {
    return false;
  }
}

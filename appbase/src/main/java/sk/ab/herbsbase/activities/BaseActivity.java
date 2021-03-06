package sk.ab.herbsbase.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Locale;

import sk.ab.common.Constants;
import sk.ab.herbs.billingmodule.BasePlayActivity;
import sk.ab.herbsbase.AndroidConstants;
import sk.ab.herbsbase.BaseApp;
import sk.ab.herbsbase.R;
import sk.ab.herbsbase.commons.PropertyListBaseFragment;
import sk.ab.herbsbase.tools.Utils;

/**
 * User: adrian
 * Date: 11.1.2015
 * <p/>
 * Base Activity
 */

public abstract class BaseActivity extends BasePlayActivity {

    private class DownloadStateReceiver extends BroadcastReceiver
    {
        private DownloadStateReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Integer number = intent.getIntExtra(AndroidConstants.EXTENDED_DATA_COUNT_SYNCHONIZED, -1);
            Integer countAll = intent.getIntExtra(AndroidConstants.EXTENDED_DATA_COUNT_ALL, -1);
            if (AndroidConstants.BROADCAST_DOWNLOAD.equals(intent.getAction())) {
                handleSynchronizationDownload(number, countAll);
            } else {
                handleSynchronizationUpload(number, countAll);
            }
        }
    }

    private static final String TAG = "BaseActivity";

    protected boolean isLoading = false;
    protected View overlay;
    protected DrawerLayout mDrawerLayout;
    protected PropertyListBaseFragment mPropertyMenu;
    protected ActionBarDrawerToggle mDrawerToggle;
    protected AnimationDrawable loadingAnimation;
    protected HashMap<String, String> filter;

    protected int count;

    public FloatingActionButton countButton;
    public TextView countText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        changeLocale();

        DownloadStateReceiver mDownloadStateReceiver = new DownloadStateReceiver();
        IntentFilter downloadIntentFilter = new IntentFilter(AndroidConstants.BROADCAST_DOWNLOAD);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mDownloadStateReceiver,
                downloadIntentFilter);
        IntentFilter uploadIntentFilter = new IntentFilter(AndroidConstants.BROADCAST_UPLOAD);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mDownloadStateReceiver,
                uploadIntentFilter);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        loadingAnimation = (AnimationDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.loading, null);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mDrawerToggle.syncState();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (changeLocale()) {
            Log.i(TAG, "Locale changed in onStart() method.");
            recreate();
        }

        mPropertyMenu.getListView().invalidateViews();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public FloatingActionButton getCountButton() {
        return countButton;
    }

    public BaseApp getApp() {
        return (BaseApp)getApplication();
    }

    public void closeDrawer() {
        mDrawerLayout.closeDrawers();
        mDrawerToggle.syncState();
    }

    public void startLoading() {
        overlay.setVisibility(View.VISIBLE);
        if (countButton != null) {
            countText.setVisibility(View.INVISIBLE);
            countButton.setEnabled(false);
            countButton.setImageDrawable(loadingAnimation);
            loadingAnimation.start();
        }
    }

    public void stopLoading() {
        if (countButton != null) {
            countButton.setEnabled(true);
            countText.setVisibility(View.VISIBLE);
        }
        overlay.setVisibility(View.GONE);
    }

    public HashMap<String, String> getFilter() {
        return filter;
    }

    public int getCounter() {
        return count;
    }

    public PropertyListBaseFragment getMenuFragment() {
        return mPropertyMenu;
    }

    protected void setCountButton() {
        BaseApp app = (BaseApp)getApplication();

        if (app.isLoading()) {
            startLoading();
        } else {
            if (countButton != null) {
                if (count <= Constants.LIST_THRESHOLD && count > 0) {
                    countButton.setBackgroundTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(getApplicationContext(), R.color.FABGreen)));
                } else {
                    countButton.setBackgroundTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(getApplicationContext(), R.color.MenuWhite)));
                }
                countButton.setImageDrawable(null);
                countText.setText("" + count);
            }
        }
    }

    protected boolean changeLocale() {
        SharedPreferences preferences = getSharedPreferences();
        String language = preferences.getString(AndroidConstants.LANGUAGE_DEFAULT_KEY, Locale.getDefault().getLanguage());

        boolean isLocaleChanged = false;
        if (!Locale.getDefault().getLanguage().equals(language)) {
            Utils.changeLocale(getBaseContext(), language);
            isLocaleChanged = true;
        }

        return isLocaleChanged;
    }

    public void handleLogout() {

    }

    public abstract SharedPreferences getSharedPreferences();

    protected abstract PropertyListBaseFragment getNewMenuFragment();

    protected void handleSynchronizationDownload(Integer number, Integer countAll) {

    }

    protected void handleSynchronizationUpload(Integer number, Integer countAll) {

    }

}

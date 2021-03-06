package sk.ab.herbsplus.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import sk.ab.herbsbase.AndroidConstants;
import sk.ab.herbsbase.activities.SplashBaseActivity;
import sk.ab.herbsplus.SpecificConstants;
import sk.ab.herbsplus.services.OfflineService;


/**
 * Splash activity
 *
 * Created by adrian on 11.3.2017.
 */
public class SplashPlusActivity extends SplashBaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    AndroidConstants.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            startOfflineService();
            startApplication();
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case AndroidConstants.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startApplication();
                }
            }
        }
        finish();
    }

    @Override
    protected Class getFilterPlantsActivityClass() {
        return FilterPlantsPlusActivity.class;
    }

    @Override
    protected Class getListPlantsActivityClass() {
        return ListPlantsPlusActivity.class;
    }

    @Override
    protected Class getDisplayPlantActivityClass() {
        return DisplayPlantPlusActivity.class;
    }

    @Override
    public SharedPreferences getSharedPreferences() {
        return getSharedPreferences(SpecificConstants.PACKAGE, Context.MODE_PRIVATE);
    }

    private void startOfflineService() {
        Intent intent = new Intent(this, OfflineService.class);
        OfflineService.enqueueWork(getApplicationContext(), intent);
    }
}

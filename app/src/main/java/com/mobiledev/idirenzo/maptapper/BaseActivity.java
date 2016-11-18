package com.mobiledev.idirenzo.maptapper;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;

/**
 * Created by Italo on 2016-11-16.
 */

public abstract class BaseActivity extends AppCompatActivity {

    protected static final String MAP_CHACHE_FOLDER = "map-cache";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clearMenuItem:
                clearMapCache();
                return true;
            case R.id.listMenuItem:
                startActivity(new Intent(this, MapListActivity.class));
                return true;
            case R.id.aboutMenuItem:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Clears the all saved map files from device storage.
     */
    private void clearMapCache() {
        File mapCacheDir = new File(getExternalFilesDir(null), MAP_CHACHE_FOLDER);
        boolean success = true;
        if (mapCacheDir.exists()) {
            File[] files = mapCacheDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (!f.delete())
                        success = false;
                }
            }
        }
        String message = success ? "Map Cache Cleared" : "Unable to clear Map Cache";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

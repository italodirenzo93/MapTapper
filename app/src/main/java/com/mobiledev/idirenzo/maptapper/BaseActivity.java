package com.mobiledev.idirenzo.maptapper;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

    protected static NfcAdapter nfcAdapter = null;
    protected static File mapCacheDir = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a single instance of the NFC adapter to be shared among activities
        if (nfcAdapter == null) {
            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
            if (!nfcAdapter.isEnabled()) {
                Toast.makeText(this, "NFC is disabled. Please enable is it for full app functionality.", Toast.LENGTH_LONG).show();
            }
        }

        // Get an interface to the map cache directory
        if (mapCacheDir == null) {
            mapCacheDir = new File(getExternalFilesDir(null), MAP_CHACHE_FOLDER);

            // Create the cache folder
            if (!mapCacheDir.exists()) {
                if (!mapCacheDir.mkdirs()) {
                    Toast.makeText(this, "Unable to create cache folder.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scanMenuItem:
                startActivity(new Intent(this, MapActivity.class));
                return true;
            case R.id.listMenuItem:
                startActivity(new Intent(this, MapListActivity.class));
                return true;
            case R.id.clearMenuItem:
                boolean success = clearMapCache();
                String message = success ? "Map Cache Cleared" : "Unable to clear Map Cache";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
     * @return True if succeeded. False otherwise.
     */
    private static boolean clearMapCache() {
        if (mapCacheDir.exists()) {
            File[] files = mapCacheDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (!f.delete())
                        return false;
                }
            }
        }
        return true;
    }
}

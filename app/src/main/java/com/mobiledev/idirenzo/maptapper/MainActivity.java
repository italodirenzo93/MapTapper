package com.mobiledev.idirenzo.maptapper;

import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String MAP_CHACHE_FOLDER = "/map-cache/";

    private NfcAdapter nfcAdapter;
    private DownloadManager downloadManager;
    private long downloadQueue;

    private ImageView imageViewMap;

    private File mapChacheDir;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "Your phone is not equipped with NFC. This you will not be able to use " +
                "the full functionality of this app.", Toast.LENGTH_LONG).show();
        }
        else if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this, "NFC is disabled. Please enable is it for full app functionality.", Toast.LENGTH_LONG).show();
        }

        imageViewMap = (ImageView)findViewById(R.id.imageViewMap);

        // Create the cache folder
        mapChacheDir = new File(getExternalFilesDir(null) + MAP_CHACHE_FOLDER);
        if (!mapChacheDir.exists()) {
            if (!mapChacheDir.mkdirs()) {
                Toast.makeText(this, "Unable to create cache folder.", Toast.LENGTH_SHORT).show();
            }
        }

        downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(downloadQueue);
                    Cursor c = downloadManager.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c
                                .getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c
                                .getInt(columnIndex)) {

                            String uri_String_abcd = c
                                    .getString(c
                                            .getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                            imageViewMap.setImageURI(Uri.parse(uri_String_abcd));
                        }
                    }
                }
            }
        }, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        //return super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuClearCache:
                clearMapCache();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String url = intent.getDataString();
        String[] tokens = url.split("/");
        String filename = tokens[tokens.length-1];

        File mapFile = new File(mapChacheDir, filename);
        if (mapFile.exists()) { // If a file by the same name already exists, use the file on disk
            imageViewMap.setImageURI(Uri.fromFile(mapFile));
            Toast.makeText(this, "Map loaded from cache", Toast.LENGTH_SHORT).show();
        } else {    // Download the file
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setDestinationInExternalFilesDir(this, null, MAP_CHACHE_FOLDER + filename);
            downloadQueue = downloadManager.enqueue(request);
            Toast.makeText(this, "Downloading map...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onPostResume() {
        Intent nfcIntent = new Intent(this, MainActivity.class);
        nfcIntent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, nfcIntent, 0);
        IntentFilter[] intentFilter = new IntentFilter[]{};

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilter, null);
        super.onPostResume();
    }

    // Helpers
    private void clearMapCache() {
        boolean success = true;
        if (mapChacheDir.exists()) {
            File[] files = mapChacheDir.listFiles();
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

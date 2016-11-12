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
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private DownloadManager downloadManager;
    private long downloadQueue;

    private ImageView imageViewMap;

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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String url = intent.getDataString();
        String[] tokens = url.split("/");
        String filename = tokens[tokens.length-1];

        File mapFile = new File(getExternalFilesDir(null) + "/maps/" + filename);
        if (mapFile.exists()) { // If a file by the same name already exists, use the file on disk
            imageViewMap.setImageURI(Uri.fromFile(mapFile));
            Toast.makeText(this, "Map loaded from cache", Toast.LENGTH_SHORT).show();
        } else {    // Download the file
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setDestinationInExternalFilesDir(this, null, "maps/" + filename);
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
}

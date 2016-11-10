package com.mobiledev.idirenzo.maptapper;

import android.Manifest;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private DownloadManager downloadManager;
    private long downloadQueue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "Your phone is not equipped with NFC. This you will not be able to use " +
                "the full functionality of this app.", Toast.LENGTH_LONG).show();
        }
        else if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this, "NFC is disabled. Please enable is it for full app functionality.", Toast.LENGTH_LONG).show();
        }
        else {
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

                                ImageView view = (ImageView) findViewById(R.id.imageViewMap);
                                String uri_String_abcd = c
                                        .getString(c
                                                .getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                                view.setImageURI(Uri.parse(uri_String_abcd));
                            }
                        }
                    }
                }
            }, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Toast.makeText(this, intent.getDataString(), Toast.LENGTH_SHORT).show();

        String url = intent.getDataString();
        String[] tokens = url.split("/");
        String filename = tokens[tokens.length-1];

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
        downloadQueue = downloadManager.enqueue(request);
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

    private void copyDownloadToAppDir() {
        try {
            File tempFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/filename");
            FileInputStream fis = new FileInputStream(tempFile);
            FileChannel inChannel = fis.getChannel();

            FileOutputStream fos = openFileOutput("filename", Context.MODE_PRIVATE);
            FileChannel outChannel = fos.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);

            outChannel.close();
            inChannel.close();
            fis.close();
            fos.close();

            if (!tempFile.delete()) {
                Toast.makeText(this, "Could not delete the temporary file.", Toast.LENGTH_SHORT).show();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

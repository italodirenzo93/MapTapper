package com.mobiledev.idirenzo.maptapper;

import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class MapActivity extends BaseActivity {

    private NfcAdapter nfcAdapter;
    private DownloadManager downloadManager;
    private long downloadQueue;

    private ImageView imageViewMap;
    private ImageScaler mapScaler;

    private File mapCacheDir;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "Your phone is not equipped with NFC. This you will not be able to use " +
                "the full functionality of this app.", Toast.LENGTH_LONG).show();
        }
        else if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this, "NFC is disabled. Please enable is it for full app functionality.", Toast.LENGTH_LONG).show();
        }

        imageViewMap = (ImageView)findViewById(R.id.imageViewMap);
        mapScaler = new ImageScaler();
        imageViewMap.setOnTouchListener(mapScaler);

        // Check if there was an incoming map file to open
        {
            String mapFilename = getIntent().getStringExtra(MapListActivity.MAP_FILE_EXTRA);
            if (mapFilename != null) {
                imageViewMap.setImageURI(Uri.parse(mapFilename));
            }
        }

        // Create the cache folder
        mapCacheDir = new File(getExternalFilesDir(null), MAP_CHACHE_FOLDER);
        if (!mapCacheDir.exists()) {
            if (!mapCacheDir.mkdirs()) {
                Toast.makeText(this, "Unable to create cache folder.", Toast.LENGTH_SHORT).show();
            }
        }

        // Get the download manager service
        downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);

        // Register a BroadcastReciever to respond when the map download is complete
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(downloadQueue);
                    Cursor c = downloadManager.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                            String uri_String_abcd = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                            imageViewMap.setImageURI(Uri.parse(uri_String_abcd));
                            scaleMapToFit();
                            drawCoord();
                            scaleMapToFit();
                        }
                    }
                }
            }
        }, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Tag nfcTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (nfcTag != null) {
            String url = intent.getDataString();
            String[] tokens = url.split("/");
            String filename = tokens[tokens.length - 1];

            File mapFile = new File(mapCacheDir, filename);
            if (mapFile.exists()) { // If a file by the same name already exists, use the file on disk
                imageViewMap.setImageURI(Uri.fromFile(mapFile));
                scaleMapToFit();
                Toast.makeText(this, "Map loaded from cache", Toast.LENGTH_SHORT).show();
            } else {    // Download the file
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setDestinationInExternalFilesDir(this, null, MAP_CHACHE_FOLDER + "/" + filename);
                downloadQueue = downloadManager.enqueue(request);
                Toast.makeText(this, "Downloading map...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onPostResume() {
        Intent nfcIntent = new Intent(this, MapActivity.class);
        nfcIntent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, nfcIntent, 0);
        IntentFilter[] intentFilter = new IntentFilter[]{};

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilter, null);
        super.onPostResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // When the ImageView has finished inflating and an image is set, scale it
        if (imageViewMap.getDrawable() != null) {
            scaleMapToFit();
        }
    }

    // Helpers

    /**
     * Resizes the map image to fit the ImageView container.
     */
    private void scaleMapToFit() {
        if (imageViewMap.getDrawable() != null) {
            // Get the source and destination rectangles
            float imgWidth = imageViewMap.getDrawable().getIntrinsicWidth();
            float imgHeight = imageViewMap.getDrawable().getIntrinsicHeight();
            RectF src = new RectF(0, 0, imgWidth, imgHeight);
            RectF dst = new RectF(0, 0, imageViewMap.getWidth(), imageViewMap.getHeight());

            // Create a matrix for scaling
            Matrix scalingMtx = new Matrix();
            scalingMtx.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER);

            // Apply scaling
            imageViewMap.setImageMatrix(scalingMtx);
            mapScaler.setSourceMatrix(scalingMtx);     // So that the image size does not reset when going to drag/scale it

            // Force the ImageView to redraw itself
            imageViewMap.invalidate();
        }
    }

    private void drawCoord() {
        Resources res = getResources();
        Bitmap map_pin = BitmapFactory.decodeResource(res, R.drawable.map_pin);

        imageViewMap.buildDrawingCache();
        Bitmap bitmap = imageViewMap.getDrawingCache();

        int mapWidth = imageViewMap.getWidth();
        int mapHeight = imageViewMap.getHeight();

        // scale map_pin to map image
        double pin_scaleRatio = 1 / 8.0;
        int pinWidth = (int)(map_pin.getWidth() * pin_scaleRatio);
        int pinHeight = (int)(map_pin.getHeight() * pin_scaleRatio);

        Bitmap scaled_pin = Bitmap.createScaledBitmap(map_pin, pinWidth, pinHeight, false);

        //Create a new image bitmap and attach a new canvas to it
        Bitmap tempBitmap = Bitmap.createBitmap(mapWidth, mapHeight, Bitmap.Config.RGB_565);
        Canvas tempCanvas = new Canvas(tempBitmap);

        //Draw the image bitmap into the cavas
        tempCanvas.drawBitmap(bitmap, 0, 0, null);

        //Draw everything else onto the canvas
        tempCanvas.drawBitmap(scaled_pin, (mapWidth/2)-(pinWidth/2), mapHeight/2 - pinHeight, null);

        //Attach the canvas to the ImageView
        imageViewMap.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
        imageViewMap.invalidate();
    }

}

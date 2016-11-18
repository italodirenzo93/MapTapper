package com.mobiledev.idirenzo.maptapper;

import android.os.Bundle;
import android.widget.ListView;

import java.io.File;

public class MapListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_list);

        final ListView lv = (ListView)findViewById(R.id.listViewMaps);

        final File mapCacheDir = new File(getExternalFilesDir(null), MAP_CHACHE_FOLDER);
        lv.setAdapter(new MapArrayAdapter(this, mapCacheDir.listFiles()));
    }
}

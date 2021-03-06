package com.mobiledev.idirenzo.maptapper;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;

public class MapListActivity extends BaseActivity {

    public static final String MAP_FILE_EXTRA = "MapFilename";

    private File[] files;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_list);

        // Set the window title
        setTitle(R.string.menu_list_activity);

        ListView lv = (ListView)findViewById(R.id.listViewMaps);
        files = mapCacheDir.listFiles();

        // Set the list view contents
        lv.setAdapter(new MapArrayAdapter(this, files));

        // Open the map file when clicked
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent mapIntent = new Intent(MapListActivity.this, MapActivity.class);
                mapIntent.putExtra(MAP_FILE_EXTRA, files[position].getPath());
                startActivity(mapIntent);
            }
        });
    }
}

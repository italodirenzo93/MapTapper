package com.mobiledev.idirenzo.maptapper;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Italo on 2016-11-18.
 */

public class MapArrayAdapter extends ArrayAdapter<File> {

    private File[] items;

    public MapArrayAdapter(Context context, File[] items) {
        super(context, R.layout.map_list_item, items);
        this.items = items;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.map_list_item, null, true);
        }

        File f = items[position];
        if (f != null) {
            // Set the image thumbnail
            ImageView thumbnail = (ImageView)v.findViewById(R.id.lstMapImage);
            if (thumbnail != null) {
                thumbnail.setImageURI(Uri.fromFile(f));
            }

            // Set the title
            TextView tvTitle = (TextView)v.findViewById(R.id.lstMapTitle);
            if (tvTitle != null) {
                String filename = f.getName();

                // Strip hyphens, underscores, and the file extension
                String mapDisplayName = filename.replaceAll("[-_]+", " ").substring(0, filename.indexOf('.'));

                tvTitle.setText(mapDisplayName);
            }

            // Set the file last modified date
            TextView tvDate = (TextView)v.findViewById(R.id.lstMapDate);
            if (tvDate != null) {
                Date lastModDate = new Date(f.lastModified());
                DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
                tvDate.setText(dateFormat.format(lastModDate));
            }
        }
        return v;
    }
}

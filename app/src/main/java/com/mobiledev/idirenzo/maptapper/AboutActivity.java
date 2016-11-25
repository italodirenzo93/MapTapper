package com.mobiledev.idirenzo.maptapper;

import android.os.Bundle;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Set the window title
        setTitle(R.string.menu_about_activity);
    }
}

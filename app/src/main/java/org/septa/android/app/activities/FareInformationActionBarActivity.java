/*
 * FareInformationActionBarActivity.java
 * Last modified on 02-07-2014 15:01-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import android.os.Bundle;
import org.septa.android.app.R;

public class FareInformationActionBarActivity extends BaseAnalyticsActionBarActivity {
    public static final String TAG = FareInformationActionBarActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        String actionBarTitleText = getIntent().getStringExtra(getString(R.string.actionbar_titletext_key));
        String actionBarTitleText = "| Fares";
//        String iconImageNameSuffix = getIntent().getStringExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key));
        String iconImageNameSuffix = "fares";

        String resourceName = getString(R.string.actionbar_iconimage_imagename_base).concat(iconImageNameSuffix);

        int id = getResources().getIdentifier(resourceName, "drawable", getPackageName());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(actionBarTitleText);
        getSupportActionBar().setIcon(id);

        setContentView(R.layout.fareinformation);
    }
}

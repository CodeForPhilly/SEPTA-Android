/*
 * ItinerarySelection_ListViewItem_ArrayAdapter.java
 * Last modified on 05-13-2014 10:33-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.adapters.schedules;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.models.RouteTypes;
import org.septa.android.app.models.StopModel;
import org.septa.android.app.utilities.Constants;
import org.septa.android.app.utilities.StopModelDistanceComparator;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class ItinerarySelection_ListViewItem_ArrayAdapter extends BaseAdapter implements
        StickyListHeadersAdapter, SectionIndexer {
    public static final String TAG = ItinerarySelection_ListViewItem_ArrayAdapter.class.getName();
    private final Context mContext;
    private LayoutInflater mInflater;

    String[] resourceEndNames;

    String[] directionHeadingLabels = null;
    ArrayList<StopModel> stopsForDirection0 = new ArrayList<StopModel>();
    ArrayList<StopModel> stopsForDirection1 = new ArrayList<StopModel>();

    RouteTypes routeType = null;
    String routeShortName;

    private View headerView = null;

    private List<String> sections;
    private boolean useLocations = false;
    NumberFormat numberFormat = new DecimalFormat("#.##mi");

    public ItinerarySelection_ListViewItem_ArrayAdapter(Context context, RouteTypes routeType, String routeShortName) {
        mContext = context;
        mInflater = LayoutInflater.from(context);

        this.routeType = routeType;
        this.routeShortName = routeShortName;

        resourceEndNames = context.getResources().getStringArray(R.array.schedulesfragment_listview_bothimage_endnames);
    }

    /**
     * Build section indices for fast scroll
     */
    private void buildSectionIndices() {
        sections = new ArrayList<String>();

        List<StopModel> stopModels = new ArrayList<StopModel>();
        stopModels.addAll(this.stopsForDirection0);
        stopModels.addAll(this.stopsForDirection1);

        for (StopModel stopModel : stopModels) {
            String section = stopModel.getStopName();
            if (section != null && section.length() > 0) {
                section = section.substring(0, 1);
                sections.add(section);
            }
        }
    }

    public void setDirectionHeadingLabels(String[] directionHeadingLabels) {

        this.directionHeadingLabels = directionHeadingLabels;
    }

    public void setTripDataForDirection0(ArrayList<StopModel>stopsForDirection0) {

        this.stopsForDirection0 = stopsForDirection0;
        Collections.sort(this.stopsForDirection0, new NumberAwareStringComparator());
        buildSectionIndices();
        notifyDataSetChanged();
    }

    public void setTripDataForDirection1(ArrayList<StopModel>stopsForDirection1) {

        this.stopsForDirection1 = stopsForDirection1;
        Collections.sort(this.stopsForDirection1, new NumberAwareStringComparator());
        buildSectionIndices();
        notifyDataSetChanged();
    }

    public Object[] getItems() {
        StopModel[] stopModelsArray = new StopModel[stopsForDirection0.size()+stopsForDirection1.size()];

        int i = 0;
        for (StopModel stopModel : stopsForDirection0) {
            stopModelsArray[i++] = stopModel;
        }
        for (StopModel stopModel : stopsForDirection1) {
            stopModelsArray[i++] = stopModel;
        }

        return stopModelsArray;
    }

    @Override
    public int getCount() {

        return getItems().length;
    }

    @Override
    public Object getItem(int position) {

        return getItems()[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView;

        rowView = mInflater.inflate(R.layout.nexttoarrive_listview_stop_item, parent, false);

        ImageView icon_imageView = (ImageView) rowView.findViewById(R.id.nexttoarrive_stopselection_accessibilityicon_imageview);
        TextView text_TextView = (TextView) rowView.findViewById(R.id.nexttoarrive_stopselection_textview);

        if (((StopModel)getItem(position)).hasWheelBoardingFeature()) {
            icon_imageView.setVisibility(View.VISIBLE);
        } else {
            icon_imageView.setVisibility(View.INVISIBLE);
        }

        if(useLocations) {
            TextView distance_textview = (TextView) rowView.findViewById(R.id.nexttoarrive_stopselection_distance_textview);
            distance_textview.setVisibility(View.VISIBLE);
            distance_textview.setText(numberFormat.format(((StopModel) getItem(position)).getDistance()));
        }

        text_TextView.setText(((StopModel)getItem(position)).getStopName());

        // for the rows that are in the second section, they get a different color background
        if (position > stopsForDirection0.size()-1) {
            LinearLayout mainLayout = (LinearLayout)rowView.findViewById(R.id.nettoarrive_stopselection_mainlayout);
            mainLayout.setBackgroundColor(Color.parseColor("#FFCACACB"));
        }

//        Log.d(TAG, "stop model is id="+((StopModel) getItem(position)).getStopId()+"   name="+((StopModel) getItem(position)).getStopName());

        return rowView;
    }

    /**
     * Sort list of stops by location
     * @param userLocation
     */
    public void sortByLocation(Location userLocation) {
        for(StopModel stopModel : stopsForDirection0) {
            Location stopLocation = new Location("");
            stopLocation.setLatitude(stopModel.getLatitude());
            stopLocation.setLongitude(stopModel.getLongitude());

            stopModel.setDistance(Constants.METER_IN_MILES * userLocation.distanceTo(stopLocation));
        }

        for(StopModel stopModel : stopsForDirection1) {
            Location stopLocation = new Location("");
            stopLocation.setLatitude(stopModel.getLatitude());
            stopLocation.setLongitude(stopModel.getLongitude());

            stopModel.setDistance(Constants.METER_IN_MILES * userLocation.distanceTo(stopLocation));
        }

        Collections.sort(stopsForDirection0, new StopModelDistanceComparator());

        useLocations = true;
        notifyDataSetChanged();
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        View view = null;

        int colorPosition =0;
        switch (routeType) {
            case TROLLEY: {
                colorPosition = 0;
                break;
            }
            case MFL: {
                if (routeShortName.equals("MFO")) {
                    colorPosition = 3;
                } else {
                    colorPosition = 1;
                }
                break;
            }
            case BUS: {
                colorPosition = 3;
                break;
            }
            case BSL: {
                if (routeShortName.equals("BSO")) {
                    colorPosition = 3;
                } else {
                    colorPosition = 4;
                }
                break;
            }
            case NHSL: {
                colorPosition = 5;
                break;
            }
        }

        String backgroundColor = mContext.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[colorPosition];


        view = mInflater.inflate(R.layout.schedules_routeselection_headerview, parent, false);
        TextView textView = (TextView) view.findViewById(R.id.schedules_routeselection_sectionheader_textview);

        LinearLayout mainLayout = (LinearLayout)view.findViewById(R.id.schedules_routeselection_sectionheader_view);
        mainLayout.setBackgroundColor(Color.parseColor(backgroundColor));

        if (position < stopsForDirection0.size()) {
            textView.setText("To "+directionHeadingLabels[0]);
        } else {
            textView.setText("To "+directionHeadingLabels[1]);
        }

        return view;
    }

    @Override
    public long getHeaderId(int position) {
        if (position < stopsForDirection0.size()) {
            return 0;
        }

        return 1;
    }

    @Override
    public Object[] getSections() {
        return sections.toArray();
    }

    @Override
    public int getPositionForSection(int i) {
        return i;
    }

    @Override
    public int getSectionForPosition(int i) {
        return i;
    }
}

class NumberAwareStringComparator implements Comparator<StopModel>{
    public int compare(StopModel stopModel1, StopModel stopModel2) {
        String s1 = stopModel1.getStopName();
        String s2 = stopModel2.getStopName();

        String[] s1Parts = s1.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
        String[] s2Parts = s2.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");

        int i = 0;
        while(i < s1Parts.length && i < s2Parts.length){

            //if parts are the same
            if(s1Parts[i].compareTo(s2Parts[i]) == 0){
                ++i;
            }else{
                try{

                    int intS1 = Integer.parseInt(s1Parts[i]);
                    int intS2 = Integer.parseInt(s2Parts[i]);

                    //if the parse works

                    int diff = intS1 - intS2;
                    if(diff == 0){
                        ++i;
                    }else{
                        return diff;
                    }
                }catch(Exception ex){
                    return s1.compareTo(s2);
                }
            }//end else
        }//end while

        //Handle if one string is a prefix of the other.
        // nothing comes before something.
        if(s1.length() < s2.length()){
            return -1;
        }else if(s1.length() > s2.length()){
            return 1;
        }else{
            return 0;
        }
    }
}
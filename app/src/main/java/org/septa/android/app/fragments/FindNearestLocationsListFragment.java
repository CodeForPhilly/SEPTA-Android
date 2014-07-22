/*
 * FindNearestLocationsListFragment.java
 * Last modified on 03-27-2014 18:24-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.Gson;

import org.septa.android.app.R;
import org.septa.android.app.activities.FindNearestLocationRouteDetailsActionBarActivity;
import org.septa.android.app.adapters.FindNearestLocation_ListViewItem_ArrayAdapter;
import org.septa.android.app.databases.SEPTADatabase;
import org.septa.android.app.models.LocationBasedRouteModel;
import org.septa.android.app.models.LocationModel;
import org.septa.android.app.models.ObjectFactory;
import org.septa.android.app.models.RoutesModel;
import org.septa.android.app.models.TransportationType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FindNearestLocationsListFragment extends ListFragment {
    public static final String TAG = FindNearestLocationsListFragment.class.getName();

    private List<LocationModel> locationList;

    public FindNearestLocationsListFragment() {
        // instantiate an empty array list for the TrainViewModels
        locationList = new ArrayList<LocationModel>(0);
    }

    public void clearLocationLists() {

        this.locationList = new ArrayList<LocationModel>(0);
        this.getListView().invalidate();

    }

    public void setLocationList(List<LocationModel> locationList, TransportationType type) {

        this.locationList.addAll(locationList);
        Collections.sort(this.locationList, new Comparator<LocationModel>() {
            public int compare(LocationModel location1, LocationModel location2) {
                return new Float(location1.getDistance()).compareTo(new Float(location2.getDistance()));
            }
        });

        RoutesModel routesModel;
        switch (type) {
            case BUS:
                routesModel = ObjectFactory.getInstance().getBusRoutes();
                break;
            case RAIL:
                routesModel = ObjectFactory.getInstance().getRailRoutes();
                break;
            case TROLLEY:
                routesModel = ObjectFactory.getInstance().getTrolleyRoutes();
                break;
            default:
                routesModel = null;
                break;
        }

        RouteStopIdLoader routeStopIdLoader = new RouteStopIdLoader(this.getListView(), routesModel);
        routeStopIdLoader.execute(locationList);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setRetainInstance(true);

        ListView lv = getListView();
        lv.setFastScrollEnabled(true);
        lv.setScrollingCacheEnabled(false);
        lv.setSmoothScrollbarEnabled(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.findnearestlocation_fragment_listview, null);

        ArrayAdapter<LocationModel> adapter = new FindNearestLocation_ListViewItem_ArrayAdapter(inflater.getContext(), locationList);
        setListAdapter(adapter);

        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        LocationModel locationModel = locationList.get(position);

        //GSon the locationModel to pass to the activity
        Intent findNearestLocationRouteDetailsIntent = null;

        findNearestLocationRouteDetailsIntent = new Intent(getActivity(), FindNearestLocationRouteDetailsActionBarActivity.class);

        Gson gson = new Gson();
        String locationRouteModelJSONString = gson.toJson(locationModel);
        findNearestLocationRouteDetailsIntent.putExtra(getString(R.string.findNearestLocation_locationRouteModel), locationRouteModelJSONString);

        startActivity(findNearestLocationRouteDetailsIntent);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private class RouteStopIdLoader extends AsyncTask<List<LocationModel>, Integer, Boolean> {
        ListView listView = null;
        RoutesModel routesModel = null;

        public RouteStopIdLoader(ListView listView, RoutesModel routesModel) {

            this.listView = listView;
            this.routesModel = routesModel;
        }

        private void loadRoutesPerStop(List<LocationModel> locationList) {
            Log.d(TAG, "processing routes per stop with a location list size of " + locationList.size());
            SEPTADatabase septaDatabase = new SEPTADatabase(getActivity());
            SQLiteDatabase database = septaDatabase.getReadableDatabase();

            for (LocationModel location : locationList) {
                String queryString = "SELECT route_short_name, stop_id, Direction, dircode, route_type FROM stopIDRouteLookup WHERE stop_id=" + location.getLocationId();
                Cursor cursor = database.rawQuery(queryString, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            location.addRoute(cursor.getString(0), LocationBasedRouteModel.DirectionCode.valueOf(cursor.getString(2)), cursor.getInt(3), cursor.getInt(4));
                        } while (cursor.moveToNext());
                    }

                    cursor.close();
                } else {
                    Log.d(TAG, "cursor is null");
                }
            }

            database.close();
        }

        @Override
        protected Boolean doInBackground(List<LocationModel>... params) {
            List<LocationModel> locationList = params[0];

            loadRoutesPerStop(locationList);

            routesModel.loadRoutes(getActivity());

            return false;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);

            ArrayAdapter<LocationModel> adapter = new FindNearestLocation_ListViewItem_ArrayAdapter(getActivity(), locationList);
            setListAdapter(adapter);

            // after the list has been updated, invalidate the list view to re-render
            listView.invalidate();
        }
    }
}

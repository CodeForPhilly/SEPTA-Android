/*
 * NextToArriveStoredTripModel.java
 * Last modified on 06-06-2014 11:20-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models;

public class  NextToArriveStoredTripModel implements Comparable<NextToArriveStoredTripModel> {

    private String startStopName;
    private String startStopId;
    private String destinationStopName;
    private String destintationStopId;

    public String getStartStopName() {
        return startStopName;
    }

    public void setStartStopName(String startStopName) {
        this.startStopName = startStopName;
    }

    public String getStartStopId() {
        return startStopId;
    }

    public void setStartStopId(String startStopId) {
        this.startStopId = startStopId;
    }

    public String getDestinationStopName() {
        return destinationStopName;
    }

    public void setDestinationStopName(String destinationStopName) {
        this.destinationStopName = destinationStopName;
    }

    public String getDestintationStopId() {
        return destintationStopId;
    }

    public void setDestintationStopId(String destintationStopId) {
        this.destintationStopId = destintationStopId;
    }

    @Override
    public int compareTo(NextToArriveStoredTripModel another) {
        if ((this.getStartStopId().equals(another.getStartStopId())) &&
            (this.getStartStopName().equals(another.getStartStopName())) &&
            (this.getDestintationStopId().equals(another.getDestintationStopId())) &&
            (this.getDestinationStopName().equals(another.getDestinationStopName()))) {

            return 0;
        }

        return -1;
    }
}

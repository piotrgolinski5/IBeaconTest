package pl.gg.ibeaconlibrary;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.SphericalUtil;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import pl.gg.ibeaconlibrary.interfaces.OnUserPositionChangedListener;

/**
 * Created by xxx on 04.11.2015.
 */
public class LocationManager {
    private final String mData = "[{\"name\":\"1\",\"address\":\"ED:87:2B:A3:36:0B\",\"lat\":0.0001,\"lng\":-0.0001},{\"name\":\"2\",\"address\":\"F5:BD:7A:48:8A:98\",\"lat\":0,\"lng\":-0.0001},{\"name\":\"3\",\"address\":\"DF:5C:53:50:B4:D2\",\"lat\":0,\"lng\":0.0001},{\"name\":\"4\",\"address\":\"D8:E5:C2:F6:0E:14\",\"lat\":0.0001,\"lng\":0.0001}]";

    public class LocationBeacon {
        public LocationBeacon(String name,
                              double lat,
                              double lng) {
            this.name = name;
            this.lat = lat;
            this.lng = lng;

        }

        String name;

        double lat;
        double lng;
    }

    private List<LocationBeacon> mList;
    private Gson mGson;

    public LocationManager() {
        mGson = new Gson();
       // mList = new ArrayList<>();
    }

    public void loadData() {
        Type listType = new TypeToken<ArrayList<LocationBeacon>>() {
        }.getType();
        mList = new Gson().fromJson(mData, listType);

    }

    public void setData(List<LocationBeacon> list){
        mList = list;
    }

    public List<LocationBeacon> getList() {
        return mList;
    }

    public LocationManager.LocationBeacon getLocationBeacon(String beaconAddress) {
        if(mList != null) {
            for (LocationBeacon locationBeacon : mList) {
                if (locationBeacon.name.equals(beaconAddress)) {
                    return locationBeacon;
                }
            }
        }

        return null;
    }

    private boolean mFirstIteration = true;
    private static final int countOfAlghoritms = 4;
    private double[] mFilteredLat = new double[countOfAlghoritms];
    private double[] mFilteredLng = new double[countOfAlghoritms];
    private double[] mCurrentLat = new double[countOfAlghoritms];
    private double[] mCurrentLng = new double[countOfAlghoritms];
    private final static double FILTERING_FACTOR = 0.03;

    private List<OnUserPositionChangedListener> mPositionList = new ArrayList<OnUserPositionChangedListener>();

    public void calculatePositionWithTriateration(float sumDistance, List<IBeacon> beaconList) {

        double tempCurrentLat = 0;
        double tempCurrentLng = 0;
        int counter = 0;
        for (IBeacon it : beaconList) {

            if (it.mPosition != null) {
                double weight = it.getDistanceForAlgorithm() / sumDistance;
                tempCurrentLat += it.mPosition.latitude * weight;
                tempCurrentLng += it.mPosition.longitude * weight;
                counter++;
            }
        }

        if (counter < 3) {
            return;
        }

        if (mFirstIteration) {
            this.mFilteredLat[0] = tempCurrentLat;
            this.mFilteredLng[0] = tempCurrentLng;
            mFirstIteration = false;
        }


        if (mCurrentLat[0] != 0) {
           // if ((this.mCurrentLat[0] - tempCurrentLat < 0.0002 && this.mCurrentLat[0] - tempCurrentLat > -0.0002) || (this.mCurrentLng[0] - tempCurrentLng < 0.0002 && this.mCurrentLng[0] - tempCurrentLng > -0.0002)) {
                this.mCurrentLat[0] = tempCurrentLat;
                this.mCurrentLng[0] = tempCurrentLng;

                for (OnUserPositionChangedListener l : mPositionList) {
                    l.positionChanged(getCurrentPosition(0), 0, 0/*this.mMinMaxEstimatedError*/);
                }
            //}
        } else {
            this.mCurrentLat[0] = tempCurrentLat;
            this.mCurrentLng[0] = tempCurrentLng;
            for (OnUserPositionChangedListener l : mPositionList) {
                l.positionChanged(getCurrentPosition(0), 0, 0/*this.mMinMaxEstimatedError*/);
            }
        }

        //calculateFloor(beaconList);
        //calculateZone(getCurrentPosition());

    }

    public LatLng getCurrentPosition(int type) {
        if (type == 3 || type == 2 || type == 1) {
            //     return new LatLng(this.mCurrentLat[type], this.mCurrentLng[type]);
        }
        this.mFilteredLat[type] = (this.mCurrentLat[type] * FILTERING_FACTOR) + (this.mFilteredLat[type] * (1.0 - FILTERING_FACTOR));
        this.mFilteredLng[type] = (this.mCurrentLng[type] * FILTERING_FACTOR) + (this.mFilteredLng[type] * (1.0 - FILTERING_FACTOR));
        LatLng p = new LatLng(this.mFilteredLat[type], this.mFilteredLng[type]);
        return p;
    }

    public void addOnUserPositionChanged(OnUserPositionChangedListener listener) {
        mPositionList.add(listener);
    }

    //#############
    public double mMinMaxEstimatedError = 0;

    public void calculatePositionWithMINMAX(List<IBeacon> beaconList) {
        sortTransmitters(beaconList);
        BeaconSquare intersecting = null;
        int i = 0;
        for (IBeacon it : beaconList) {

            IBeacon b = it;
            if (b.mPosition == null) {
                return;
            }

            if (i == 0) {
                intersecting = new BeaconSquare(b.mPosition.latitude, b.mPosition.longitude, it.getDistance());
                i++;
            } else {
                BeaconSquare square = new BeaconSquare(b.mPosition.latitude, b.mPosition.longitude, it.getDistance() * 2);
                intersecting = square.intersect(intersecting);
            }

        }

        if (intersecting != null) {
            this.mCurrentLat[1] = intersecting.getCenter().latitude;
            this.mCurrentLng[1] = intersecting.getCenter().longitude;

            mMinMaxEstimatedError = intersecting.getRadius();

            //calculateFloor(beaconList);
            //calculateZone(getCurrentPosition());
            if (mMinMaxEstimatedError < 12.0)
                for (OnUserPositionChangedListener l : mPositionList) {
                    l.positionChanged(getCurrentPosition(1), this.mMinMaxEstimatedError, 1);

                }
        }

    }

    private void sortTransmitters(List<IBeacon> beaconList) {
        BeaconComparator comparator = new BeaconComparator();
        Collections.sort(beaconList, comparator);
    }

    public class BeaconComparator implements Comparator<IBeacon> {

        @Override
        public int compare(IBeacon lhs, IBeacon rhs) {
            int returnVal = 0;

            if (lhs.getRSSI() < rhs.getRSSI()) {
                returnVal = 1;
            } else if (lhs.getRSSI() > rhs.getRSSI()) {
                returnVal = -1;
            } else if (lhs.getRSSI() == rhs.getRSSI()) {
                returnVal = 0;
            }
            return returnVal;
        }

    }

    public class BeaconSquare {

        private double west, north, east, south, centerX, centerY, mRadius = 0;
        private LatLng center, ne, nw, se, sw;

        public BeaconSquare(double x, double y, double radius) {
            mRadius = radius;
            center = new LatLng(x, y);
            ne = SphericalUtil.computeOffset(center, radius, 45);
            nw = SphericalUtil.computeOffset(center, radius, 315);
            sw = SphericalUtil.computeOffset(center, radius, 225);
            se = SphericalUtil.computeOffset(center, radius, 135);
            north = ne.latitude;
            south = sw.latitude;
            west = sw.longitude;
            east = ne.longitude;
        }

        public BeaconSquare(double _north, double _east, double _south, double _west) {
            north = _north;
            south = _south;
            east = _east;
            west = _west;
            ne = new LatLng(_north, _east);
            nw = new LatLng(_north, _west);
            se = new LatLng(_south, _east);
            sw = new LatLng(_south, _west);

            centerX = (_north + _south) / 2;
            centerY = (_east + _west) / 2;

            center = new LatLng(centerX, centerY);

            mRadius = SphericalUtil.computeDistanceBetween(center, ne);
        }

        private boolean ifIntersects(BeaconSquare r) {
            return this.north >= r.south &&
                    this.south <= r.north &&
                    this.east >= r.west &&
                    this.west <= r.east;

        }

        public BeaconSquare intersect(BeaconSquare r) {
            if (ifIntersects(r)) {
                double _south = Math.max(this.south, r.south);
                double _west = Math.max(this.west, r.west);
                double _north = Math.min(this.north, r.north);
                double _east = Math.min(this.east, r.east);
                return new BeaconSquare(_north, _east, _south, _west);
            } else {
                return this;
            }
        }

        public double getNorth() {
            return north;
        }

        public double getSouth() {
            return south;
        }

        public double getEast() {
            return east;
        }

        public double getWest() {
            return west;
        }

        public LatLng getCenter() {
            return new LatLng(centerX, centerY);
        }

        public double getRadius() {
            return this.mRadius / 2;
        }
    }

    ///#####################
    private void trimTransmitters(List<IBeacon> beaconList) {
        beaconList.subList(4, beaconList.size()).clear();
    }

    public void calculatePositionWithMaximumProbability(List<IBeacon> beaconList) {
        sortTransmitters(beaconList);
        if (beaconList.size() > 4)
            trimTransmitters(beaconList);
        int counter = 0;
        for (IBeacon b : beaconList) {
            if (b.mPosition != null) {
                counter++;
            }
        }
        if (counter < 4) {
            return;
        }

        int rows = beaconList.size() - 1;

        DenseMatrix64F matrixA = new DenseMatrix64F(rows, 2);
        DenseMatrix64F matrixB = new DenseMatrix64F(rows, 1);
        DenseMatrix64F matrixW = new DenseMatrix64F(rows, rows);

        DenseMatrix64F matrixAT = new DenseMatrix64F(2, rows);
        DenseMatrix64F matrixATW = new DenseMatrix64F(2, rows);
        DenseMatrix64F matrixATWA = new DenseMatrix64F(2, 2);
        DenseMatrix64F matrixInv = new DenseMatrix64F(2, 2);
        DenseMatrix64F matrixInvAT = new DenseMatrix64F(2, rows);
        DenseMatrix64F matrixInvW = new DenseMatrix64F(2, rows);
        DenseMatrix64F matrixRes = new DenseMatrix64F(2, 1);

        populateMatrixA(beaconList, matrixA);
        populateMatrixB(beaconList, matrixB);
        populateMatrixW(beaconList, matrixW);

        CommonOps.transpose(matrixA, matrixAT);
        CommonOps.mult(1, matrixAT, matrixW, matrixATW);
        CommonOps.mult(1, matrixATW, matrixA, matrixATWA);
        CommonOps.invert(matrixATWA, matrixInv);
        CommonOps.mult(1, matrixInv, matrixAT, matrixInvAT);
        CommonOps.mult(1, matrixInvAT, matrixW, matrixInvW);
        CommonOps.mult(1, matrixInvW, matrixB, matrixRes);

        this.mCurrentLng[2] = matrixRes.get(0, 0);
        this.mCurrentLat[2] = matrixRes.get(1, 0);

        //calculateFloor(beaconList);
        //calculateZone(getCurrentPosition());

        for (OnUserPositionChangedListener l : mPositionList) {
            l.positionChanged(getCurrentPosition(2), this.mMinMaxEstimatedError, 2);
        }
    }

    private void populateMatrixW(List<IBeacon> beaconList, DenseMatrix64F matrixW) {
        for (int i = 0; i < beaconList.size() - 1; i++) {
            double weight = 1.0 / Math.pow(beaconList.get(i).getDistance(), 2);
            matrixW.add(i, i, weight);
        }
    }

    private void populateMatrixA(List<IBeacon> beaconList, DenseMatrix64F matrixA) {
        int size = beaconList.size();
        for (int col = 0; col < 2; col++) {
            double nValue = 0;
            if (col == 0)
                nValue = beaconList.get(size - 1).mPosition.longitude;
            else
                nValue = beaconList.get(size - 1).mPosition.latitude;

            for (int row = 0; row < size - 1; row++) {
                IBeacon b = beaconList.get(row);
                double value = 0;
                if (col == 0) {
                    value = 2.0 * (b.mPosition.longitude - nValue);
                } else {
                    value = 2.0 * (b.mPosition.latitude - nValue);
                }
                matrixA.add(row, col, value);
            }
        }
    }

    private void populateMatrixB(List<IBeacon> beaconList, DenseMatrix64F matrixB) {
        int size = beaconList.size();
        double xNValue, yNValue, dNValue;
        IBeacon nBeacon = beaconList.get(size - 1);
        xNValue = nBeacon.mPosition.longitude;
        yNValue = nBeacon.mPosition.latitude;
        dNValue = beaconList.get(size - 1).getDistance();

        for (int row = 0; row < size - 1; row++) {
            IBeacon b = beaconList.get(row);
            double value = Math.pow(b.mPosition.longitude, 2) - Math.pow(xNValue, 2) + Math.pow(b.mPosition.latitude, 2) - Math.pow(yNValue, 2) + Math.pow(dNValue, 2)
                    - Math.pow(beaconList.get(row).getDistance(), 2);
            matrixB.add(row, 0, value);
        }
    }

    //########
    public void userLocation(List<IBeacon> beaconsList) {
        double x_user = 0;
        double y_user = 0;
        double r_user = 1;

        // Check for legitimate beacons, those who have been selected within the area.
        ArrayList<double[]> circleArray = new ArrayList<double[]>();
        for (IBeacon b : beaconsList) {

            double r;
            try {
                r = b.getDistanceForAlgorithm();
            } catch (NullPointerException e) {
                continue;
            }
            double x = b.mPosition.longitude;//preferences.getFloat("x" + minorVal, -1);
            double y = b.mPosition.latitude;//preferences.getFloat("y" + minorVal, 0);
            double z = 1;//preferences.getFloat("z" + minorVal, 0);
            if (x >= 0 && y >= 0) {
                // Remove the height difference between the phone and beacon from the distance.
                r = (double) Math.sqrt((r * r) - (z * z));
                circleArray.add(new double[]{x, y, r});
                x_user += x;
                y_user += y;
            }
        }

        // Only calculate the position when 2 or more beacons are available.
        double circleNumber = circleArray.size();
        if (circleNumber < 2) {
            // new double[]{x_user, y_user, r_user};
            this.mCurrentLng[3] = y_user;
            this.mCurrentLat[3] = x_user;

            for (OnUserPositionChangedListener l : mPositionList) {
                l.positionChanged(getCurrentPosition(3), this.mMinMaxEstimatedError, 3);
            }
        }

        // The average position between all the valid circles.
        x_user = x_user / circleNumber;
        y_user /= circleNumber;

        double prev1Error = 0;
        double currentError = 1000000;

        // If the last error is the same as the current error no better value will be calculated.
        while (prev1Error != currentError) {
            prev1Error = currentError;
            // Calculate the position up, down, left and right of the current one to calculate its error there.
            ArrayList<double[]> newPositions = new ArrayList<double[]>();
            newPositions.add(new double[]{(double) (x_user + 0.1), y_user});
            newPositions.add(new double[]{(double) (x_user - 0.1), y_user});
            newPositions.add(new double[]{x_user, (double) (y_user + 0.1)});
            newPositions.add(new double[]{x_user, (double) (y_user - 0.1)});

            // For each position in a direction calculate the error.
            for (double[] direction : newPositions) {
                double error = 0;
                ArrayList<Double> dist = new ArrayList<Double>();

                // The error on a certain position for each beacon.
                for (int i = 0; i < circleNumber; i++) {
                    double[] circlePos = circleArray.get(i);
                    dist.add((double) (Math.sqrt(Math.pow(circlePos[0] - direction[0], 2) +
                            Math.pow(circlePos[1] - direction[1], 2)) - circlePos[2]));
                    error += Math.pow(dist.get(dist.size() - 1), 2);
                }
                error = (float) Math.sqrt(error);

                // If the error is smaller we take the values.
                if (error < currentError) {
                    Collections.sort(dist);
//                    r_user = dist.get(dist.size() - 1);
                    x_user = direction[0];
                    y_user = direction[1];
                    currentError = error;
                }
            }
        }
        this.mCurrentLng[3] = y_user;
        this.mCurrentLat[3] = x_user;

        for (OnUserPositionChangedListener l : mPositionList) {
            l.positionChanged(getCurrentPosition(3), this.mMinMaxEstimatedError, 3);
        }
    }


}

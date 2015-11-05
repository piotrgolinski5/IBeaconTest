package pl.apg.ibeaconlibrary;

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

import pl.apg.ibeaconlibrary.interfaces.OnUserPositionChangedListener;

/**
 * Created by xxx on 04.11.2015.
 */
public class LocationManager {
    private final String mData = "[{\"name\":\"1\",\"address\":\"ED:87:2B:A3:36:0B\",\"lat\":0.0001,\"lng\":-0.0001},{\"name\":\"2\",\"address\":\"F5:BD:7A:48:8A:98\",\"lat\":0,\"lng\":-0.0001},{\"name\":\"3\",\"address\":\"DF:5C:53:50:B4:D2\",\"lat\":0,\"lng\":0.0001},{\"name\":\"4\",\"address\":\"D8:E5:C2:F6:0E:14\",\"lat\":0.0001,\"lng\":0.0001}]";

    public class LocationBeacon {
        String name;
        String address;
        double lat;
        double lng;
    }

    private List<LocationBeacon> mList;
    private Gson mGson;

    public LocationManager() {
        mGson = new Gson();
        mList = new ArrayList<>();
    }

    public void loadData() {
        Type listType = new TypeToken<ArrayList<LocationBeacon>>() {
        }.getType();
        mList = new Gson().fromJson(mData, listType);
    }

    public List<LocationBeacon> getList() {
        return mList;
    }

    public LocationManager.LocationBeacon getLocationBeacon(String beaconAddress) {
        for (LocationBeacon locationBeacon : mList) {
            if (locationBeacon.address.equals(beaconAddress)) {
                return locationBeacon;
            }
        }

        return null;
    }

    private boolean mFirstIteration = true;
    private double[]  mFilteredLat = new double[3];
    private double[] mFilteredLng = new double[3];
    private double[] mCurrentLat = new double[3];
    private double[] mCurrentLng = new double[3];
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
            if ((this.mCurrentLat[0] - tempCurrentLat < 0.0002 && this.mCurrentLat[0] - tempCurrentLat > -0.0002) || (this.mCurrentLng[0] - tempCurrentLng < 0.0002 && this.mCurrentLng[0] - tempCurrentLng > -0.0002)) {
                this.mCurrentLat[0] = tempCurrentLat;
                this.mCurrentLng[0] = tempCurrentLng;

                for (OnUserPositionChangedListener l : mPositionList) {
                    l.positionChanged(getCurrentPosition(0), 0,0/*this.mMinMaxEstimatedError*/);
                }
            }
        } else {
            this.mCurrentLat[0] = tempCurrentLat;
            this.mCurrentLng[0] = tempCurrentLng;
            for (OnUserPositionChangedListener l : mPositionList) {
                l.positionChanged(getCurrentPosition(0), 0,0/*this.mMinMaxEstimatedError*/);
            }
        }

        //calculateFloor(beaconList);
        //calculateZone(getCurrentPosition());

    }

    public LatLng getCurrentPosition(int type) {

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
            if(b.mPosition == null){
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
                    l.positionChanged(getCurrentPosition(1), this.mMinMaxEstimatedError,1);

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

            if(lhs.getRSSI() < rhs.getRSSI()){
                returnVal =  1;
            }else if(lhs.getRSSI() > rhs.getRSSI()){
                returnVal =  -1;
            }else if(lhs.getRSSI() == rhs.getRSSI()){
                returnVal =  0;
            }
            return returnVal;
        }

    }
    public class BeaconSquare {

        private double west, north, east, south, centerX, centerY, mRadius = 0;
        private LatLng center, ne, nw, se, sw;

        public BeaconSquare(double x, double y, double radius){
            mRadius = radius;
            center = new LatLng(x,y);
            ne = SphericalUtil.computeOffset(center, radius, 45);
            nw = SphericalUtil.computeOffset(center, radius, 315);
            sw = SphericalUtil.computeOffset(center, radius, 225);
            se = SphericalUtil.computeOffset(center, radius, 135);
            north = ne.latitude;
            south = sw.latitude;
            west = sw.longitude;
            east = ne.longitude;
        }

        public BeaconSquare(double _north, double _east, double _south, double _west){
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

        private boolean ifIntersects(BeaconSquare r){
            return this.north >= r.south &&
                    this.south <= r.north &&
                    this.east >= r.west &&
                    this.west <= r.east;

        }

        public BeaconSquare intersect(BeaconSquare r){
            if(ifIntersects(r)){
                double _south = Math.max(this.south, r.south);
                double _west = Math.max(this.west, r.west);
                double _north = Math.min(this.north, r.north);
                double _east = Math.min(this.east, r.east);
                return new BeaconSquare(_north, _east, _south, _west);
            }else{
                return this;
            }
        }

        public double getNorth(){
            return north;
        }

        public double getSouth(){
            return south;
        }

        public double getEast(){
            return east;
        }

        public double getWest(){
            return west;
        }

        public LatLng getCenter(){
            return new LatLng(centerX, centerY);
        }

        public double getRadius(){
            return this.mRadius/2;
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
        int counter =0;
        for(IBeacon b : beaconList){
            if(b.mPosition != null){
                counter++;
            }
        }
        if(counter<4){
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
            l.positionChanged(getCurrentPosition(2), this.mMinMaxEstimatedError,2);
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


}

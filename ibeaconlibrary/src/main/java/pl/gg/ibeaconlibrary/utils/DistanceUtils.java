package pl.gg.ibeaconlibrary.utils;

import com.google.android.gms.maps.model.LatLng;

import pl.gg.ibeaconlibrary.IBeacon;

/**
 * Created by xxx on 04.11.2015.
 */
public class DistanceUtils {
    public final static int CONST_A = -59;
    public final static int CONST_N = 2;
    private final static double kFilteringFactor = 0.22;
    private final static double distanceFactor = 1.0;

    public static double getFilteredDistance(IBeacon beacon) {

        double rssi = beacon.getRSSI();
        if (beacon.mFilteredRSSI == 0){

            beacon.mFilteredRSSI = rssi ;

            return beacon.mFilteredRSSI;
        }

        // 1. Obliczamy deltę, która jest dodawa
        double currentDistance = beacon.getDistanceFromRSSI(rssi);
        double rssiDelta = Math.abs(beacon.getDistanceFromRSSI(currentDistance + distanceFactor) - beacon.getDistanceFromRSSI(currentDistance - distanceFactor));


        // 1. Obcinamy do maksymalnej i minimalnej wartości
        double newRSSI = rssi;
        if (newRSSI > rssi + rssiDelta) {
            newRSSI = rssi + rssiDelta;
        }
        if (newRSSI < rssi - rssiDelta) {
            newRSSI = rssi - rssiDelta;
        }

        // Wygładzanie sygnału
        beacon.mFilteredRSSI = (newRSSI * kFilteringFactor) + (beacon.mFilteredRSSI * (1.0 - kFilteringFactor));
        return Math.pow(10, -1 * (beacon.mFilteredRSSI - CONST_A) / (10 * CONST_N));
    }

    public static double getKontaktDistance(IBeacon beacon){

        if (beacon.getRSSI() == 0.0D) {
            return -1.0D;
        }
        double ratio = beacon.getRSSI() * 1.0D / beacon.mTXPower;
        if (ratio < 1.0D) {
            return Math.pow(ratio, 10.0D);
        }
        double accuracy = 0.89976D * Math.pow(ratio, 7.7095D) + 0.111D;
        return accuracy;

    }

    public static LatLng calcEndPoint(LatLng center , double distance, double  bearing)
    {
        LatLng gp=null;

        double R = 6371000; // meters , earth Radius approx
        double PI = 3.1415926535;
        double RADIANS = PI/180;
        double DEGREES = 180/PI;

        double lat2;
        double lon2;

        double lat1 = center.latitude * RADIANS;
        double lon1 = center.longitude * RADIANS;
        double radbear = bearing * RADIANS;

        // System.out.println("lat1="+lat1 + ",lon1="+lon1);

        lat2 = Math.asin( Math.sin(lat1)*Math.cos(distance / R) +
                Math.cos(lat1)*Math.sin(distance/R)*Math.cos(radbear) );
        lon2 = lon1 + Math.atan2(Math.sin(radbear)*Math.sin(distance / R)*Math.cos(lat1),
                Math.cos(distance/R)-Math.sin(lat1)*Math.sin(lat2));

        // System.out.println("lat2="+lat2*DEGREES + ",lon2="+lon2*DEGREES);

        gp = new LatLng(  lat2*DEGREES,lon2*DEGREES);

        return(gp);
    }
}

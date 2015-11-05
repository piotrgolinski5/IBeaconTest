package pl.apg.ibeaconlibrary;

import android.bluetooth.BluetoothDevice;
import android.util.SparseArray;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import pl.apg.ibeaconlibrary.utils.ConversionUtils;
import pl.apg.ibeaconlibrary.utils.DiscoveryUtils;
import pl.apg.ibeaconlibrary.utils.DistanceUtils;
import pl.apg.ibeaconlibrary.utils.L;
import pl.apg.ibeaconlibrary.utils.StringUtils;

public class IBeacon {
    private BluetoothDevice mBluetoothDevice;
    //
    private int mRSSI;
    private String mDeviceName;
    private String mAddress;
    private int mTimeOut;
    public int mTXPower;
    public boolean isEddyStone;
    public double mFilteredRSSI = 0;
    //
    public Marker mMarker;
    public LatLng mPosition;
    public String mName;


    public IBeacon(BluetoothDevice device, int rssi, String deviceName, byte[] scanRecord) {
        this.mBluetoothDevice = device;
        this.mRSSI = rssi;
        this.mDeviceName = deviceName;
        this.mAddress = this.mBluetoothDevice.getAddress();
        this.mTimeOut = 0;

        this.isEddyStone = DiscoveryUtils.isEddystoneSpecificFrame(scanRecord);
        this.mTXPower = DiscoveryUtils.getTXPower(this.isEddyStone, scanRecord);

        L.e(StringUtils.addStrings("added ", deviceName, " RSSI ", rssi, " address ", this.mAddress, " distance ", getDistance(), " ", DistanceUtils.getFilteredDistance(this)));
    }

    public int checkTimeout() {
        this.mTimeOut++;
        return this.mTimeOut;
    }

    public void setRSSI(int rssi) {
        this.mRSSI = rssi;
        this.mTimeOut = 0;
        L.e(StringUtils.addStrings("modify ", this.mDeviceName, " RSSI ", rssi, " address ", this.mAddress, " distance ", getDistance(), " ", DistanceUtils.getFilteredDistance(this)));
    }

    public BluetoothDevice getBluetoothDevice() {
        return mBluetoothDevice;
    }

    public int getRSSI() {
        return mRSSI;
    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public String getAddress() {
        return mAddress;
    }

    public double getDistance() {
        return Math.pow(10, -1 * (this.mRSSI - DistanceUtils.CONST_A) / (10 * DistanceUtils.CONST_N));
    }

    public double getDistanceFromRSSI(double rssi) {
        return Math.pow(10, -1 * (rssi - DistanceUtils.CONST_A) / (10 * DistanceUtils.CONST_N));
    }

    @Override
    public String toString() {
        return StringUtils.addStrings("", this.mDeviceName, " isEddystone ", isEddyStone, "\n TX Power ", this.mTXPower, "\n RSSI ", this.mRSSI, "\n address ", this.mAddress, "\n distance ", getDistance(), "\nifinity ", DistanceUtils.getFilteredDistance(this), "\nkontakt ", DistanceUtils.getKontaktDistance(this));
    }
    public double getDistanceForAlgorithm() {
        double distance =(float) Math.pow(this.getRadius(), 8);
        L.e(StringUtils.addStrings("Name ", this.mName, " distance ",distance , " rssi ", this.mRSSI ));
        return distance;
    }

    private double getRadius() {
        double radius = 100 - Math.min(Math.max(30, -1 * this.mRSSI), 100);
        L.e(StringUtils.addStrings("Name ", this.mName, " radius ",radius , " rssi ", this.mRSSI ));
        return radius;
    }
}


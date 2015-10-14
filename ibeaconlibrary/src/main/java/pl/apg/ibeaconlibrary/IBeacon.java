package pl.apg.ibeaconlibrary;

import android.bluetooth.BluetoothDevice;

import pl.apg.ibeaconlibrary.utils.L;
import pl.apg.ibeaconlibrary.utils.StringUtils;

public class IBeacon {
    private BluetoothDevice mBluetoothDevice;
    private int mRSSI;
    private String mDeviceName;
    private String mAddress;
    private int mTimeOut;

    public IBeacon(BluetoothDevice device, int rssi, String deviceName) {
        this.mBluetoothDevice = device;
        this.mRSSI = rssi;
        this.mDeviceName = deviceName;
        this.mAddress = this.mBluetoothDevice.getAddress();
        this.mTimeOut = 0;
        L.e(StringUtils.addStrings("added ", deviceName, " RSSI ", rssi, " address ", this.mAddress, " distance ", getDistance(), " ", getFilteredDistance()));
    }

    public int checkTimeout() {
        this.mTimeOut++;
        return this.mTimeOut;
    }

    public void setRSSI(int rssi) {
        this.mRSSI = rssi;
        this.mTimeOut = 0;
        L.e(StringUtils.addStrings("modify ", this.mDeviceName, " RSSI ", rssi, " distance ", getDistance() , " ", getFilteredDistance()));
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

    //Umowne parametry
    private final static int CONST_A = -59;
    private final static int CONST_N = 2;
    private final static double kFilteringFactor = 0.44;
    private final static double distanceFactor = 1.0;

    public double getDistance() {
        return Math.pow(10, -1 * (this.mRSSI - CONST_A) / (10 * CONST_N));
    }

    public double getDistanceFromRSSI(double rssi) {
        return Math.pow(10, -1 * (rssi - CONST_A) / (10 * CONST_N));
    }

    double _filteredRSSI = 0;

    public double getFilteredDistance() {
        double rssi = this.mRSSI;
        if (_filteredRSSI == 0){

                _filteredRSSI = rssi ;

            return _filteredRSSI;
        }

        // 1. Obliczamy deltę, która jest dodawa
        double currentDistance = getDistanceFromRSSI(rssi);
        double rssiDelta = Math.abs(getDistanceFromRSSI(currentDistance + distanceFactor) - getDistanceFromRSSI(currentDistance - distanceFactor));


        // 1. Obcinamy do maksymalnej i minimalnej wartości
        double newRSSI = rssi;
        if (newRSSI > rssi + rssiDelta) {
            newRSSI = rssi + rssiDelta;
        }
        if (newRSSI < rssi - rssiDelta) {
            newRSSI = rssi - rssiDelta;
        }

        // Wygładzanie sygnału
        _filteredRSSI = (newRSSI * kFilteringFactor) + (_filteredRSSI * (1.0 - kFilteringFactor));
        return Math.pow(10, -1 * (_filteredRSSI - CONST_A) / (10 * CONST_N));
    }

}


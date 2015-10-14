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

    public IBeacon(BluetoothDevice device, int rssi, String deviceName){
        this.mBluetoothDevice = device;
        this.mRSSI = rssi;
        this.mDeviceName = deviceName;
        this.mAddress =  this.mBluetoothDevice.getAddress();
        this.mTimeOut = 0;
        L.e(StringUtils.addStrings("added ", deviceName, " RSSI ", rssi," address ", this.mAddress));
    }

    public int checkTimeout(){
        this.mTimeOut++;
        return this.mTimeOut;
    }

    public void setRSSI(int rssi){
        this.mRSSI = rssi;
        this.mTimeOut = 0;
        L.e(StringUtils.addStrings("modify ", this.mDeviceName, " RSSI ", rssi));
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
}


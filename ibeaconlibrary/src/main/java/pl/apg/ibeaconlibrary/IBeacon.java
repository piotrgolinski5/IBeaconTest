package pl.apg.ibeaconlibrary;

import android.bluetooth.BluetoothDevice;
import android.util.SparseArray;

import pl.apg.ibeaconlibrary.utils.ConversionUtils;
import pl.apg.ibeaconlibrary.utils.DiscoveryUtils;
import pl.apg.ibeaconlibrary.utils.L;
import pl.apg.ibeaconlibrary.utils.StringUtils;

public class IBeacon {
    private static byte[] EDDYSTONE_SPECIFIC_HEADER = { 2, 1, 6, 3, 3, -86, -2 };
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final byte[] MANUFACTURER_DATA_IBEACON_PREFIX = { 76, 0, 2, 21 };
    private BluetoothDevice mBluetoothDevice;
    private int mRSSI;
    private String mDeviceName;
    private String mAddress;
    private int mTimeOut;
    public int mTXPower;
    public boolean isEddyStone;

    public IBeacon(BluetoothDevice device, int rssi, String deviceName , byte[] scanRecord) {
        this.mBluetoothDevice = device;
        this.mRSSI = rssi;
        this.mDeviceName = deviceName;
        this.mAddress = this.mBluetoothDevice.getAddress();
        this.mTimeOut = 0;

        this.isEddyStone = isEddystoneSpecificFrame(scanRecord);
        if(isEddyStone){
            this.mTXPower = scanRecord[12];
        }else{
            SparseArray<byte[]> ibeaconMetaData = parseScanRecord(scanRecord);
           /* int deviceHashCode = this.hashCodeBuilder.append(device.getAddress()).append(device.getName()).append((byte[])advertisingData.get(255)).append((byte[])advertisingData.get(22)).build();

            AdvertisingPacketImpl advertisingPackage = (AdvertisingPacketImpl)this.cache.get(Integer.valueOf(deviceHashCode));
            if (advertisingPackage == null)
            {
                advertisingPackage = createNewAdvertisingPackage(device, advertisingData, deviceHashCode, rssi);

                this.cache.put(Integer.valueOf(deviceHashCode), advertisingPackage);

                return advertisingPackage;
            }
            double rssiValue = this.rssiCalculator.calculateRssi(deviceHashCode, rssi);
            */
            //if()
            mTXPower = ((byte[])ibeaconMetaData.get(255))[24];
        }
     //   mTXPower = Math.abs(mTXPower);
        L.e(StringUtils.addStrings("added ", deviceName, " RSSI ", rssi, " address ", this.mAddress, " distance ", getDistance(), " ", getFilteredDistance()));
    }
    public static SparseArray<byte[]> parseScanRecord(byte[] scanRecord)
    {
        SparseArray<byte[]> frameArray = DiscoveryUtils.extractMetaData(scanRecord);
        byte[] manufacturerData = (byte[])frameArray.get(255, EMPTY_BYTE_ARRAY);
        byte[] serviceData = (byte[])frameArray.get(22, EMPTY_BYTE_ARRAY);
        if ((manufacturerData.length >= 25) && (serviceData.length == 9) && (serviceData[0] == 13) && (serviceData[1] == -48)) {
            if (ConversionUtils.doesArrayBeginWith(manufacturerData, MANUFACTURER_DATA_IBEACON_PREFIX)) {
                return frameArray;
            }
        }
        return null;
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
    static boolean isEddystoneSpecificFrame(byte[] scanRecord)
    {
        return (scanRecord != null) && (scanRecord.length >= 12) && (ConversionUtils.doesArrayBeginWith(scanRecord, EDDYSTONE_SPECIFIC_HEADER)) && (scanRecord[9] == -86) && (scanRecord[10] == -2);
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

    public double getKontaktDistance(){

            if (mRSSI == 0.0D) {
                return -1.0D;
            }
            double ratio = mRSSI * 1.0D / this.mTXPower;
            if (ratio < 1.0D) {
                return Math.pow(ratio, 10.0D);
            }
            double accuracy = 0.89976D * Math.pow(ratio, 7.7095D) + 0.111D;
            return accuracy;

    }

    @Override
    public String toString() {
        return  StringUtils.addStrings("", this.mDeviceName," isEddystone ", isEddyStone, "\n TX Power ", this.mTXPower, "\n RSSI ", this.mRSSI, "\n address ", this.mAddress, "\n distance ", getDistance(), "\nifinity ", getFilteredDistance(),"\nkontakt ", getKontaktDistance());
    }
}


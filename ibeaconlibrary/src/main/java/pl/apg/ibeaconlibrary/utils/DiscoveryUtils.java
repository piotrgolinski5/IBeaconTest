package pl.apg.ibeaconlibrary.utils;

import android.util.SparseArray;
import java.util.Arrays;

public final class DiscoveryUtils
{
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final byte[] MANUFACTURER_DATA_IBEACON_PREFIX = { 76, 0, 2, 21 };
    private static byte[] EDDYSTONE_SPECIFIC_HEADER = {2, 1, 6, 3, 3, -86, -2};
    public static double calculateDistance(int txPower, double rssi)
    {
        if (rssi == 0.0D) {
            return -1.0D;
        }
        double ratio = rssi * 1.0D / txPower;
        if (ratio < 1.0D) {
            return Math.pow(ratio, 10.0D);
        }
        double accuracy = 0.89976D * Math.pow(ratio, 7.7095D) + 0.111D;
        return accuracy;
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
    public static SparseArray<byte[]> extractMetaData(byte[] scanRecord)
    {
        int index = 0;
        SparseArray<byte[]> map = new SparseArray();
        int scanRecordLength = scanRecord.length;
        while (index < scanRecordLength)
        {
            int length = scanRecord[(index++)];
            if (length == 0) {
                break;
            }
            int type = ConversionUtils.asInt(scanRecord[index]);
            if (type == 0) {
                break;
            }
            byte[] data = Arrays.copyOfRange(scanRecord, index + 1, index + length);

            map.put(type, data);

            index += length;
        }
        return map;
    }

    public static int getTXPower(boolean isEddyStone,byte[] scanRecord){
        if(isEddyStone){
           return scanRecord[12];
        }else{
            SparseArray<byte[]> ibeaconMetaData = DiscoveryUtils.parseScanRecord(scanRecord);
            return ((byte[])ibeaconMetaData.get(255))[24];
        }
    }

    public static boolean isEddystoneSpecificFrame(byte[] scanRecord) {
        return (scanRecord != null) && (scanRecord.length >= 12) && (ConversionUtils.doesArrayBeginWith(scanRecord, EDDYSTONE_SPECIFIC_HEADER)) && (scanRecord[9] == -86) && (scanRecord[10] == -2);
    }

}

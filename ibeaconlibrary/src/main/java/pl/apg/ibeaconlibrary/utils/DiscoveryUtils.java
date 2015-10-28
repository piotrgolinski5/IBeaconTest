package pl.apg.ibeaconlibrary.utils;

import android.util.SparseArray;
import java.util.Arrays;

public final class DiscoveryUtils
{
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
}

package pl.apg.ibeaconlibrary;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import pl.apg.ibeaconlibrary.exceptions.NullBluetoothException;
import pl.apg.ibeaconlibrary.utils.L;
import pl.apg.ibeaconlibrary.utils.StringUtils;

public class APGBluetoothManager {
    private static volatile APGBluetoothManager mSharedInstance;
    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;

    public String mStickyBeacon = "";
    private boolean mIsScanning = false;
    private Timer mTimeOutTimer, mScanResetTimer;
    private ConcurrentHashMap<String, IBeacon> mIBeacons = new ConcurrentHashMap<String, IBeacon>();
    private List<BluetoothAdapter.LeScanCallback> mLeScanCallbacks = new ArrayList<BluetoothAdapter.LeScanCallback>();
    private final static int THREAD_DELAY_IN_SECONDS = 50;
    private final static int BEACON_TIMEOUT_IN_SECONDS = 10;
    private static int mNearRssi = -25, mMaxRssi = -101;

    public static APGBluetoothManager getInstance() {
        if (mSharedInstance == null) {
            mSharedInstance = new APGBluetoothManager();
        }
        return mSharedInstance;
    }

    private APGBluetoothManager() {
    }

    /*Scan*/
    public void startManager(Context context) throws NullBluetoothException {

        if (!isEnabled()) {
            BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            mContext = context;
            mBluetoothAdapter = bluetoothManager.getAdapter();

            boolean flag = mBluetoothAdapter.startLeScan(mLeScanCallback);
            if(!flag){
                throw new NullBluetoothException();
            }

            mIsScanning = true;

            mTimeOutTimer = new Timer();
            mScanResetTimer = new Timer();

            TimerTask timeoutTimerTask = new TimerTask() {
                @Override
                public void run() {
                    timeoutScan();
                }
            };

            TimerTask scanResetTimerTask = new TimerTask() {
                @Override
                public void run() {
                    leScanReset();
                }
            };

            mTimeOutTimer.schedule(timeoutTimerTask, 0, 1000);
            mScanResetTimer.schedule(scanResetTimerTask, 5000, THREAD_DELAY_IN_SECONDS * 500);
        }
    }

    public void stopManager() {
        if (mBluetoothAdapter != null) {
            if (mLeScanCallback != null) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }

            mIsScanning = false;
            if (mTimeOutTimer != null) {
                mTimeOutTimer.cancel();
                mTimeOutTimer.purge();
                mTimeOutTimer = null;
            }

            if (mScanResetTimer != null) {
                mScanResetTimer.cancel();
                mScanResetTimer.purge();
                mScanResetTimer = null;
            }
        }

        resetIBeacons();
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (device.getName() != null && !device.getName().trim().equals("")) {
                String deviceName = device.getName();
                if (rssi > -50) {
                    mStickyBeacon = deviceName;
                }

                for (BluetoothAdapter.LeScanCallback callback : mLeScanCallbacks) {
                    callback.onLeScan(device, rssi, scanRecord);
                }

                addOrModifyIBeacon(device, rssi, deviceName, scanRecord);
            }
        }

        private void addOrModifyIBeacon(BluetoothDevice device, int rssi, String deviceName, byte[] scanRecord) {
            if (!mIBeacons.containsKey(device.getAddress())) {
                //TODO dodac walidacje
                addIBeacon(device, rssi, deviceName, scanRecord);
            } else {
                IBeacon ibeacon = mIBeacons.get(device.getAddress());
                if (ibeacon != null) {
                    ibeacon.setRSSI(rssi);
                }
            }
        }
    };

    private void leScanReset() {
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        if (!mBluetoothAdapter.startLeScan(mLeScanCallback)) {
            leScanReset();
        }
    }

    public boolean isEnabled() {
        return this.mIsScanning;
    }

    private void timeoutScan() {
        if (mIBeacons.size() != 0) {
            checkTimeouts();
        }
    }

    private void checkTimeouts() {
        IBeacon iBeacon = null;
        Iterator<Map.Entry<String, IBeacon>> it = mIBeacons.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, IBeacon> entry = it.next();
            iBeacon = entry.getValue();

            if (iBeacon.checkTimeout() > BEACON_TIMEOUT_IN_SECONDS) {
                 String deviceName = iBeacon.getDeviceName();
                 L.e(StringUtils.addStrings("timeout ", deviceName));
                 mIBeacons.remove(iBeacon.getAddress());
            }
        }
    }

    /*Beacons*/
    public void addIBeacon(BluetoothDevice device, int rssi, String deviceName, byte[] scanRecord) {
        IBeacon l = new IBeacon(device, rssi, deviceName);
        mIBeacons.put(device.getAddress(), l);
    }

    public void resetIBeacons() {
        mIBeacons.clear();
    }

}

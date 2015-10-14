package pl.apg.ibeacontest;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.List;

import pl.apg.ibeaconlibrary.APGBluetoothManager;
import pl.apg.ibeaconlibrary.IBeacon;
import pl.apg.ibeaconlibrary.IBeaconLibraryManager;
import pl.apg.ibeaconlibrary.utils.L;

public class MainActivity extends AppCompatActivity {
    private IBeaconLibraryManager mIBeaconLibraryManager;
    private APGBluetoothManager mAPGBluetoothManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mIBeaconLibraryManager = new IBeaconLibraryManager();
        mAPGBluetoothManager = mIBeaconLibraryManager.getAPGBluetoothManager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAPGBluetoothManager.startManager(MainActivity.this);

        mAPGBluetoothManager.setIBeaconValidator(new APGBluetoothManager.IBeaconValidator() {
            @Override
            public boolean isValid(BluetoothDevice device, int rssi, String deviceName, byte[] scanRecord) {
                L.e("isValid");
                return true;
            }
        });

        mAPGBluetoothManager.setOnBeaconAddedListener(new APGBluetoothManager.OnBeaconAddedListener() {
            @Override
            public void onBeaconAdded(IBeacon beacon) {
                L.e("onBeaconAdded");
            }
        });

        mAPGBluetoothManager.setOnBeaconTimeOutListener(new APGBluetoothManager.OnBeaconTimeOutListener() {
            @Override
            public void onBeaconTimeOut(IBeacon beacon) {
                L.e("onBeaconTimeOut");
            }
        });

        mAPGBluetoothManager.setOnCountChangedListner(new APGBluetoothManager.OnCountChangedListner() {
            @Override
            public void onCountChanged(int count) {
                L.e("onCountChanged");
            }
        });

        mAPGBluetoothManager.setOnBeaconListChangedListener(new APGBluetoothManager.OnBeaconListChangedListener() {
            @Override
            public void onBeaconListChangedListener(List<IBeacon> beacons) {
                L.e("onBeaconListChangedListener");
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        mIBeaconLibraryManager.getAPGBluetoothManager().stopManager();
    }
}

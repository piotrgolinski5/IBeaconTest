package pl.gg.samplebeacon;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import pl.gg.ibeaconlibrary.APGBluetoothManager;
import pl.gg.ibeaconlibrary.enums.BeaconType;
import pl.gg.samplebeacon.views.GGCanvasView;

/**
 * Created by test on 02.12.2015.
 */
public class GGBaseActivity extends Activity {
    protected APGBluetoothManager mBluetoothManager;
    protected GGCanvasView mCanvasView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_range);

        mBluetoothManager = APGBluetoothManager.getInstance();
        mBluetoothManager.setBeaconTypeScan(BeaconType.ALL);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBluetoothManager.startManager(GGBaseActivity.this);

        if(mCanvasView!= null) {
            mCanvasView.onStart();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBluetoothManager.stopManager();

        if(mCanvasView!= null) {
            mCanvasView.onStop();
        }
    }
}

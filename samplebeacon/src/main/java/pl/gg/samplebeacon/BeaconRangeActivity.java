package pl.gg.samplebeacon;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;

import java.util.Iterator;
import java.util.List;

import pl.gg.ibeaconlibrary.APGBluetoothManager;
import pl.gg.ibeaconlibrary.IBeacon;
import pl.gg.ibeaconlibrary.enums.BeaconType;
import pl.gg.ibeaconlibrary.utils.DistanceUtils;
import pl.gg.ibeaconlibrary.utils.L;
import pl.gg.ibeaconlibrary.utils.StringUtils;
import pl.gg.samplebeacon.views.GGCanvasView;

/**
 * Created by xxx on 24.11.2015.
 */
public class BeaconRangeActivity extends Activity {
    private GGCanvasView mCanvasView;
    private APGBluetoothManager mBluetoothManager;
    private List<IBeacon> mBeacons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_range);

        mCanvasView = (GGCanvasView) findViewById(R.id.activity_beacon_range_vCanvas);
        mCanvasView.setGGCanvasViewListener(mGGCanvasViewListener);
        mCanvasView.setBackgroundColor(Color.parseColor("#2f343a"));

        mBluetoothManager = APGBluetoothManager.getInstance();
        mBluetoothManager.setBeaconTypeScan(BeaconType.ALL);
    }

    private GGCanvasView.GGCanvasViewListener mGGCanvasViewListener = new GGCanvasView.GGCanvasViewListener() {
        @Override
        public void onDraw(Canvas canvas) {
            if (mBeacons != null) {
                Iterator<IBeacon> iterator = mBeacons.iterator();
                int size = mBeacons.size() + 1;
                int width = canvas.getWidth();
                int height = canvas.getHeight();

                int index = 1;
                mCanvasView.setPaintColor(Color.WHITE);
                mCanvasView.setPaintTextSize(14);
                synchronized (mBeacons) {
                    while (iterator.hasNext()) {
                        IBeacon beacon = iterator.next();
                        double distance = DistanceUtils.getFilteredDistance(beacon);
                        int xPos = index * (width / size);
                        int yPos = 100;
                        int heightView = height - yPos - 50;
                        yPos = (int) (heightView - (heightView * (distance/35))) + yPos;
                        mCanvasView.drawText(((int)distance) + "", xPos, 30);
                        mCanvasView.drawText(beacon.getDeviceName(), xPos, yPos - 60);
                        mCanvasView.drawCircle(xPos, yPos, 50);
                        index++;
                    }

                }

                mCanvasView.setPaintColor(Color.parseColor("#45d1a9"));
                mCanvasView.drawText("User", width / 2, height - 140);
                mCanvasView.drawCircle(width / 2, height - 100, 50);
            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        mBluetoothManager.startManager(BeaconRangeActivity.this);
        mBluetoothManager.setOnBeaconListChangedListener(mOnBeaconListChangedListener);
        mCanvasView.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBluetoothManager.stopManager();
        mBluetoothManager.removeOnBeaconListChangedListener(mOnBeaconListChangedListener);
        mCanvasView.onStop();
    }

    private APGBluetoothManager.OnBeaconListChangedListener mOnBeaconListChangedListener = new APGBluetoothManager.OnBeaconListChangedListener() {
        @Override
        public void onBeaconListChangedListener(List<IBeacon> beacons) {
            L.e(StringUtils.addStrings("onBeaconListChangedListener ", beacons.size()));
            // if (mBeacons == null) {
            mBeacons = beacons;
            //}
        }
    };
}

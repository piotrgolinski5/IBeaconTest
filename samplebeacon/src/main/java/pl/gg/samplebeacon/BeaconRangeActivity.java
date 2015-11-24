package pl.gg.samplebeacon;

import android.app.Activity;
import android.graphics.Canvas;
import android.os.Bundle;

import java.util.Iterator;
import java.util.List;

import pl.gg.ibeaconlibrary.APGBluetoothManager;
import pl.gg.ibeaconlibrary.IBeacon;
import pl.gg.ibeaconlibrary.enums.BeaconType;
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

        mBluetoothManager = APGBluetoothManager.getInstance();
        mBluetoothManager.setBeaconTypeScan(BeaconType.IBEACON);
    }

    private GGCanvasView.GGCanvasViewListener mGGCanvasViewListener = new GGCanvasView.GGCanvasViewListener() {
        @Override
        public void onDraw(Canvas canvas) {
            if (mBeacons != null) {
                Iterator<IBeacon> iterator = mBeacons.iterator();
                int size = mBeacons.size() + 2;
                int width = canvas.getWidth();

                int index = 1;
                while (iterator.hasNext()) {
                    mCanvasView.drawCircle(index * (width / size), 100, 5, canvas);
                    //canvas.drawCircle(0,0,10,mCanvasView.getPaint() );
                   // canvas.drawCircle(0,100,10,mCanvasView.getPaint() );
                    //canvas.drawCircle(0,200,10,mCanvasView.getPaint() );
                    //canvas.drawCircle(0,500,10,mCanvasView.getPaint() );

                    //canvas.drawLine(0, 0, canvas.getWidth(),canvas.getHeight(), mCanvasView.getPaint() );
                    index++;
                }
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
            if(mBeacons == null) {
                mBeacons = beacons;
            }
        }
    };
}

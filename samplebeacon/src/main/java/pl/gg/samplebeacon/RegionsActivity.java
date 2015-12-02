package pl.gg.samplebeacon;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pl.gg.ibeaconlibrary.APGBluetoothManager;
import pl.gg.ibeaconlibrary.IBeacon;
import pl.gg.ibeaconlibrary.LocationManager;
import pl.gg.ibeaconlibrary.enums.BeaconType;
import pl.gg.ibeaconlibrary.interfaces.OnUserPositionChangedListener;
import pl.gg.ibeaconlibrary.utils.DistanceUtils;
import pl.gg.ibeaconlibrary.utils.L;
import pl.gg.ibeaconlibrary.utils.StringUtils;
import pl.gg.samplebeacon.views.GGCanvasView;

/**
 * Created by xxx on 30.11.2015.
 */
public class RegionsActivity extends Activity {
    private GGCanvasView mCanvasView;
    private APGBluetoothManager mBluetoothManager;
    private List<Rect> mRooms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_range);

        mCanvasView = (GGCanvasView) findViewById(R.id.activity_beacon_range_vCanvas);
        mCanvasView.setGGCanvasViewListener(mGGCanvasViewListener);
        mCanvasView.setBackgroundColor(Color.parseColor("#2f343a"));

        mBluetoothManager = APGBluetoothManager.getInstance();
        mBluetoothManager.setBeaconTypeScan(BeaconType.ALL);
        mBluetoothManager.IS_USER_BEACON_REQUIRED = true;
    }

    private GGCanvasView.GGCanvasViewListener mGGCanvasViewListener = new GGCanvasView.GGCanvasViewListener() {
        @Override
        public void onDraw(Canvas canvas) {
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            if(mRooms == null){
                mRooms = new ArrayList<>();
                mRooms.add(new Rect(0,0,width/2,height/2));
                mRooms.add(new Rect(width/2,0,width/2+width/2,height/2));
                mRooms.add(new Rect(0,height/2,width/2,height));
                mRooms.add(new Rect(width/2,height/2,width,height));

               List<LocationManager.LocationBeacon> list = new ArrayList<>();
                list.add(mBluetoothManager.getLocationManager().new LocationBeacon("e1",0,0));
                list.add(mBluetoothManager.getLocationManager().new LocationBeacon("e2",width/180,0));
                list.add(mBluetoothManager.getLocationManager().new LocationBeacon("e3",0,height/180));
                list.add(mBluetoothManager.getLocationManager().new LocationBeacon("e4",width/180,height/180));
                mBluetoothManager.getLocationManager().setData(list);
            }else{
                if(x != -1){
                    for(Rect r : mRooms){
                        if(r.contains((int)x,(int)y)) {
                            mCanvasView.drawRect(r);
                            break;
                        }
                    }
                }
            }

            mCanvasView.drawCircle((int)x,(int)y,5);

            mCanvasView.setPaintColor(Color.WHITE);
            mCanvasView.setPaintTextSize(14);

            mCanvasView.drawLine(width / 2, 0, width / 2, height);
            mCanvasView.drawLine(0, height / 2, width, height / 2);

        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        mBluetoothManager.startManager(RegionsActivity.this);
        mBluetoothManager.addOnUserPositionChanged(mOnUserPositionChangedListener);
        mCanvasView.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBluetoothManager.stopManager();
        mBluetoothManager.removeOnUserPositionChanged(mOnUserPositionChangedListener);
        mCanvasView.onStop();
    }

    double x=-1,y;

    OnUserPositionChangedListener mOnUserPositionChangedListener = new OnUserPositionChangedListener() {
        @Override
        public void positionChanged(LatLng position, double estimatedError, int type) {
            x = (position.latitude * 180);
            y = (position.longitude * 180);
            Log.e("positionChanged", (position.latitude * 180) + " " + (position.longitude * 180));
        }
    };
}

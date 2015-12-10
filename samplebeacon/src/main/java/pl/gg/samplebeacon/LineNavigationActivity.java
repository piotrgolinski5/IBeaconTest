package pl.gg.samplebeacon;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pl.gg.ibeaconlibrary.LocationManager;
import pl.gg.ibeaconlibrary.interfaces.OnUserPositionChangedListener;
import pl.gg.ibeaconlibrary.utils.L;
import pl.gg.ibeaconlibrary.utils.StringUtils;
import pl.gg.samplebeacon.views.GGCanvasView;

/**
 * Created by xxx on 10.12.2015.
 */
public class LineNavigationActivity extends GGBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_range);

        mCanvasView = (GGCanvasView) findViewById(R.id.activity_beacon_range_vCanvas);
        mCanvasView.setGGCanvasViewListener(mGGCanvasViewListener);
        mCanvasView.setBackgroundColor(Color.parseColor("#2f343a"));

        mBluetoothManager.IS_USER_BEACON_REQUIRED = true;
    }

    boolean isInit = false;
    private GGCanvasView.GGCanvasViewListener mGGCanvasViewListener = new GGCanvasView.GGCanvasViewListener() {
        @Override
        public void onDraw(Canvas canvas) {
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            if (isInit == false) {
                List<LocationManager.LocationBeacon> list = new ArrayList<>();
                list.add(mBluetoothManager.getLocationManager().new LocationBeacon("e1", 0, 0));
                list.add(mBluetoothManager.getLocationManager().new LocationBeacon("e2", width / 180, 0));
                list.add(mBluetoothManager.getLocationManager().new LocationBeacon("e3", 0, height / 180));
                list.add(mBluetoothManager.getLocationManager().new LocationBeacon("e4", width / 180, height / 180));
                mBluetoothManager.getLocationManager().setData(list);
            }
            mCanvasView.setPaintColor(Color.WHITE);
            mCanvasView.drawCircle((int) x, (int) y, 15);
            mCanvasView.drawLine(width / 2, 0, width / 2, height);
            Line line = new Line();
            line.p1 = new Point(width / 2, 0);
            line.p2 = new Point(width / 2, height);
            Point current, xx = null;
            Point majPont = new Point();
            double distance = 9999;
            Point p = new Point((int) x, (int) y);
            int index = 0;
            for (Iterator<Point> it = new LineIterator(line); it.hasNext(); ) {
                current = it.next();
                double d = distanceBetween2Points(p, current);
                if (d < distance) {
                    xx = current;
                    majPont.x =xx.x;
                    majPont.y = xx.y;
                    distance = d;
                    L.e(StringUtils.addStrings("index ",index," distance ", distance, " c ",xx.x, " ",xx.y," p ",p.x, " ",p.y));
                    index++;
                }

            }

            if(xx!=null) {
                mCanvasView.drawLine((int) majPont.x, (int) majPont.y, (int) p.x, (int)p.y);
                mCanvasView.drawCircle((int) majPont.x, (int) majPont.y, 15);
            }
        }
    };

    public static double distanceBetween2Points(Point p1, Point p2) {
        return Math.abs(Math.sqrt(
                (p1.x - p2.x) * (p1.x - p2.x) +
                        (p1.y - p2.y) * (p1.y - p2.y)
        ));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBluetoothManager.addOnUserPositionChanged(mOnUserPositionChangedListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBluetoothManager.removeOnUserPositionChanged(mOnUserPositionChangedListener);
    }

    double x = -1, y;

    OnUserPositionChangedListener mOnUserPositionChangedListener = new OnUserPositionChangedListener() {
        @Override
        public void positionChanged(LatLng position, double estimatedError, int type) {
            x = (position.latitude * 180);
            y = (position.longitude * 180);
            Log.e("positionChanged", (position.latitude * 180) + " " + (position.longitude * 180));
        }
    };
}

package pl.gg.samplebeacon;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import pl.gg.ibeaconlibrary.APGBluetoothManager;
import pl.gg.ibeaconlibrary.IBeacon;
import pl.gg.ibeaconlibrary.enums.BeaconType;
import pl.gg.ibeaconlibrary.utils.DistanceUtils;
import pl.gg.ibeaconlibrary.utils.L;
import pl.gg.ibeaconlibrary.utils.StringUtils;
import pl.gg.samplebeacon.views.GGCanvasView;

/**
 * Created by test on 02.12.2015.
 */
public class CardsActivity extends ActionBarActivity {
    private GGCanvasView mCanvasView;
    private APGBluetoothManager mBluetoothManager;
    private List<IBeacon> mBeacons;
    private HashMap<String, Card> mCards = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_range);


        mCanvasView = (GGCanvasView) findViewById(R.id.activity_beacon_range_vCanvas);
        mCanvasView.setGGCanvasViewListener(mGGCanvasViewListener);
        mCanvasView.setBackgroundColor(Color.parseColor("#2f343a"));

        mBluetoothManager = APGBluetoothManager.getInstance();
        mBluetoothManager.setBeaconTypeScan(BeaconType.ALL);
        mBluetoothManager.setMaxBeacons(3);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    L.e(StringUtils.addStrings("FPS ", fps));
                    fps = 0;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    int fps = 0;

    int delay = 1000 / 30;

    class Card {
        public int color;
        public String name;
        int y, destinationY;
        int distanceTranslate;
        boolean isTranslating = false;

        public void translate(int pY) {
            if (distanceTranslate == 0) {
                isTranslating = false;
            }

            if (isTranslating) {
                y += distanceTranslate;
                if (y > destinationY - Math.abs(distanceTranslate) && y < destinationY + Math.abs(distanceTranslate)) {
                    isTranslating = false;
                }
            } else {
                distanceTranslate = (pY - y) / (delay);
                destinationY = pY;
                isTranslating = true;
            }
        }
    }

    CardComparator mCardComparator = new CardComparator();

    class CardComparator implements Comparator<IBeacon> {
        @Override
        public int compare(IBeacon lhs, IBeacon rhs) {
            return (rhs.getRSSI()) - (lhs.getRSSI());
        }
    }


    private GGCanvasView.GGCanvasViewListener mGGCanvasViewListener = new GGCanvasView.GGCanvasViewListener() {
        @Override
        public void onDraw(Canvas canvas) {
            fps++;
            /*try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/

            if (mBeacons != null) {

                int size = mBeacons.size() + 1;
                int width = canvas.getWidth();
                int height = canvas.getHeight();
                int halfOfSize = height / 2;
                int y = halfOfSize / mBeacons.size();
                int index = 1;
                Collections.sort(mBeacons, mCardComparator);
                mCanvasView.setPaintColor(Color.WHITE);
                mCanvasView.setPaintTextSize(19);
                synchronized (mBeacons) {
                    //    Collections.reverse(mBeacons);
                    Iterator<IBeacon> iterator = mBeacons.iterator();
                    while (iterator.hasNext()) {
                        IBeacon beacon = iterator.next();
                        Card card = null;
                        if (!mCards.containsKey(beacon.getDeviceName())) {
                            card = new Card();
                            card.name = beacon.getDeviceName();
                            L.e(StringUtils.addStrings((int) (Math.random() * 255), " ", (int) (Math.random() * 255), " ", (int) (Math.random() * 255)));
                            card.color = Color.argb(128, (int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
                            card.y = height;
                            mCards.put(beacon.getDeviceName(), card);
                            mCanvasView.setPaintColor(card.color);
                        } else {
                            card = mCards.get(beacon.getDeviceName());
                            mCanvasView.setPaintColor(card.color);
                        }
                        // double distance = DistanceUtils.getFilteredDistance(beacon);
                        int x = mBeacons.size() + 1 - index;
                        x = x * 30;

                        card.translate(index == 1 ? halfOfSize - (halfOfSize / 2) : halfOfSize + (index * y));
                        mCanvasView.drawRect(new Rect(0 + 20 /*+ x*/, card.y, width - 20 /*- x*/, height));
                        if (beacon.getDeviceName() != null) {
                            L.e(StringUtils.addStrings(index, " ", beacon.getDeviceName()));
                            mCanvasView.setPaintColor(Color.WHITE);
                            mCanvasView.drawText(beacon.getDeviceName(), 0 + 20 /*+ x*/, halfOfSize + (index * y));
                        }
                        index++;
                    }

                }


            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        mBluetoothManager.startManager(CardsActivity.this);
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

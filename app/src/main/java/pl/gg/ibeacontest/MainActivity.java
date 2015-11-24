package pl.gg.ibeacontest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import pl.gg.ibeaconlibrary.APGBluetoothManager;
import pl.gg.ibeaconlibrary.IBeacon;
import pl.gg.ibeaconlibrary.IBeaconLibraryManager;
import pl.gg.ibeaconlibrary.enums.BeaconType;
import pl.gg.ibeaconlibrary.interfaces.OnUserPositionChangedListener;
import pl.gg.ibeaconlibrary.utils.L;
import pl.gg.ibeacontest.list.IBeaconListAdapter;

public class MainActivity extends AppCompatActivity {
    private IBeaconLibraryManager mIBeaconLibraryManager;
    private APGBluetoothManager mAPGBluetoothManager;
    private ListView mListView;
    private IBeaconListAdapter mAdapter;
    private GoogleMap mGoogleMap;
    private List<Marker> mMarkers = new ArrayList<>();
    private Marker[] mUserMarker;

    class asd implements Runnable {
        LatLng position;
        int type;

        asd(LatLng position, int type) {
            this.position = position;
            this.type = type;
        }

        @Override
        public void run() {
            if (mUserMarker[type] == null) {
                if (type == 0) {
                    mUserMarker[type] = mGoogleMap.addMarker(new MarkerOptions().position(position)
                            .icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
                } else if (type == 1) {
                    mUserMarker[type] = mGoogleMap.addMarker(new MarkerOptions().position(position)
                            .icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                } else if (type == 2) {
                    mUserMarker[type] = mGoogleMap.addMarker(new MarkerOptions().position(position)
                            .icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                }
                else if (type == 3) {
                    mUserMarker[type] = mGoogleMap.addMarker(new MarkerOptions().position(position)
                            .icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                }
            } else {
                mUserMarker[type].setPosition(position);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (ListView) findViewById(R.id.activity_main_lvList);
        mAdapter = new IBeaconListAdapter(this);
        mListView.setAdapter(mAdapter);
        mUserMarker = new Marker[4];
        mIBeaconLibraryManager = new IBeaconLibraryManager();
        mAPGBluetoothManager = mIBeaconLibraryManager.getAPGBluetoothManager();
        mAPGBluetoothManager.setBeaconTypeScan(BeaconType.IBEACON);
        mAPGBluetoothManager.addOnUserPositionChanged(new OnUserPositionChangedListener() {
            @Override
            public void positionChanged(LatLng position, double estimatedError, int type) {
                runOnUiThread(new asd(position, type));

            }
        });
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.activity_main_smfMap);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
                LatLng latLng = new LatLng(0, 0);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 203));
            }
        });
    }

    void updateMarkers() {
        if (mGoogleMap == null) {
            return;
        }

        for (IBeacon beacon : mAPGBluetoothManager.getList()) {
            if (beacon.mMarker == null && beacon.mPosition != null) {
                beacon.mMarker = mGoogleMap.addMarker(new MarkerOptions().position(beacon.mPosition)
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mAPGBluetoothManager.startManager(MainActivity.this);

       /* mAPGBluetoothManager.setIBeaconValidator(new APGBluetoothManager.IBeaconValidator() {
            @Override
            public boolean isValid(BluetoothDevice device, int rssi, String deviceName, byte[] scanRecord) {
                //L.d("isValid");
                if (rssi > -40) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        /*mAPGBluetoothManager.setOnBeaconAddedListener(new APGBluetoothManager.OnBeaconAddedListener() {
            @Override
            public void onBeaconAdded(IBeacon beacon) {
                L.d("onBeaconAdded");
            }
        });

        mAPGBluetoothManager.setOnBeaconTimeOutListener(new APGBluetoothManager.OnBeaconTimeOutListener() {
            @Override
            public void onBeaconTimeOut(IBeacon beacon) {
                L.d("onBeaconTimeOut");
            }
        });

        mAPGBluetoothManager.setOnCountChangedListner(new APGBluetoothManager.OnCountChangedListner() {
            @Override
            public void onCountChanged(int count) {
                L.d("onCountChanged");
            }
        });
*/
        mAPGBluetoothManager.setOnBeaconListChangedListener(new APGBluetoothManager.OnBeaconListChangedListener() {
            @Override
            public void onBeaconListChangedListener(List<IBeacon> beacons) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.setList(mAPGBluetoothManager.getList());
                        updateMarkers();
                    }
                });

                L.d("onBeaconListChangedListener");
            }
        });

        mAPGBluetoothManager.setOnLeScanCallback(new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                        updateMarkers();
                    }
                });

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIBeaconLibraryManager.getAPGBluetoothManager().stopManager();
    }
}

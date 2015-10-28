package pl.apg.ibeacontest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import pl.apg.ibeaconlibrary.APGBluetoothManager;
import pl.apg.ibeaconlibrary.IBeacon;
import pl.apg.ibeaconlibrary.IBeaconLibraryManager;
import pl.apg.ibeaconlibrary.utils.L;
import pl.apg.ibeaconlibrary.utils.StringUtils;
import pl.apg.ibeacontest.list.IBeaconListAdapter;

public class MainActivity extends AppCompatActivity {
    private IBeaconLibraryManager mIBeaconLibraryManager;
    private APGBluetoothManager mAPGBluetoothManager;
    private ListView mListView;
    private IBeaconListAdapter mAdapter;
    private GoogleMap mGoogleMap;
    private List<Marker> mMarkers =  new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (ListView) findViewById(R.id.activity_main_lvList);
        mAdapter = new IBeaconListAdapter(this);
        mListView.setAdapter(mAdapter);

        mIBeaconLibraryManager = new IBeaconLibraryManager();
        mAPGBluetoothManager = mIBeaconLibraryManager.getAPGBluetoothManager();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.activity_main_smfMap);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
                LatLng sydney = new LatLng(52.211189, 21.047316);
                googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,17));
            }
        });
    }

    void updateMarkers(){
        if(mGoogleMap == null){
            return;
        }

        mGoogleMap.clear();
        mMarkers.clear();
        LatLng sydney = new LatLng(52.211189, 21.047316);
        L.e("########");
        L.e(StringUtils.addStrings("#",sydney.latitude," ",sydney.longitude));
        mGoogleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        for(int i = 0; i < mAPGBluetoothManager.getList().size(); i++) {
LatLng l = calcEndPoint(sydney, mAPGBluetoothManager.getList().get(i).getFilteredDistance(), 1);
            L.e(StringUtils.addStrings("#",l.latitude," ",l.longitude));
            mGoogleMap.addMarker(new MarkerOptions().position(l).title("Marker in Sydney"));
            //mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 17));
        }
    }
    public static LatLng calcEndPoint(LatLng center , double distance, double  bearing)
    {
        LatLng gp=null;

        double R = 6371000; // meters , earth Radius approx
        double PI = 3.1415926535;
        double RADIANS = PI/180;
        double DEGREES = 180/PI;

        double lat2;
        double lon2;

        double lat1 = center.latitude * RADIANS;
        double lon1 = center.longitude * RADIANS;
        double radbear = bearing * RADIANS;

        // System.out.println("lat1="+lat1 + ",lon1="+lon1);

        lat2 = Math.asin( Math.sin(lat1)*Math.cos(distance / R) +
                Math.cos(lat1)*Math.sin(distance/R)*Math.cos(radbear) );
        lon2 = lon1 + Math.atan2(Math.sin(radbear)*Math.sin(distance / R)*Math.cos(lat1),
                Math.cos(distance/R)-Math.sin(lat1)*Math.sin(lat2));

        // System.out.println("lat2="+lat2*DEGREES + ",lon2="+lon2*DEGREES);

        gp = new LatLng(  lat2*DEGREES,lon2*DEGREES);

        return(gp);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mAPGBluetoothManager.startManager(MainActivity.this);

        /*mAPGBluetoothManager.setIBeaconValidator(new APGBluetoothManager.IBeaconValidator() {
            @Override
            public boolean isValid(BluetoothDevice device, int rssi, String deviceName, byte[] scanRecord) {
                L.d("isValid");
                return true;
            }
        });

        mAPGBluetoothManager.setOnBeaconAddedListener(new APGBluetoothManager.OnBeaconAddedListener() {
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

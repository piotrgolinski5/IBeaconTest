package pl.apg.ibeacontest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import pl.apg.ibeaconlibrary.IBeaconLibraryManager;

public class MainActivity extends AppCompatActivity {
    private IBeaconLibraryManager mIBeaconLibraryManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mIBeaconLibraryManager = new IBeaconLibraryManager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIBeaconLibraryManager.mAPGBluetoothManager.startManager(MainActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIBeaconLibraryManager.mAPGBluetoothManager.stopManager();
    }
}

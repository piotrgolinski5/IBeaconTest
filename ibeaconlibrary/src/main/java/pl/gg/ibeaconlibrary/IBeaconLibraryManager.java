package pl.gg.ibeaconlibrary;


public class IBeaconLibraryManager {
    private APGBluetoothManager mAPGBluetoothManager;

    public IBeaconLibraryManager(){
        mAPGBluetoothManager = APGBluetoothManager.getInstance();
    }

    public APGBluetoothManager getAPGBluetoothManager() {
        return mAPGBluetoothManager;
    }
}

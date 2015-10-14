package pl.apg.ibeaconlibrary;


public class IBeaconLibraryManager {
    public APGBluetoothManager mAPGBluetoothManager;

    public IBeaconLibraryManager(){
        mAPGBluetoothManager = APGBluetoothManager.getInstance();
    }

}

package pl.gg.ibeaconlibrary.interfaces;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by xxx on 04.11.2015.
 */
public interface OnUserPositionChangedListener {
    public void positionChanged(LatLng position, double estimatedError, int type);
}

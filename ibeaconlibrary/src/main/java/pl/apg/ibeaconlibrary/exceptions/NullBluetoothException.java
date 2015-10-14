package pl.apg.ibeaconlibrary.exceptions;

public class NullBluetoothException extends RuntimeException{

    private static final long serialVersionUID = 3728930980035382291L;
    private static final String MESSAGE = "LEScan resulted in null, check if your device supports BT 4.0 or if the LE scanning doesn't occur somewhere else in your app";

    @Override
    public String getMessage() {
        return MESSAGE;
    }

    public NullBluetoothException() {
        super(MESSAGE);
    }

    public NullBluetoothException(String message) {
        super(message);
    }

    public NullBluetoothException(String message, Throwable cause) {
        super(message, cause);
    }

    public NullBluetoothException(Throwable cause) {
        super(cause);
    }
}

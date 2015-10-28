package pl.apg.ibeaconlibrary.utils;


import java.nio.ByteBuffer;
import java.util.UUID;

public final class ConversionUtils
{
    private static final double[] EMPTY_DOUBLE_ARRAY = new double[0];

    public static int asInt(byte value)
    {
        return value & 0xFF;
    }

    public static byte[] extractPayload(byte[] src, int start, int length)
    {
        SDKPreconditions.checkNotNull(src, "Source array is null");
        if (src.length < start + length) {
            throw new IllegalArgumentException("Cannot extract payload. Source array is too short.");
        }
        byte[] array = new byte[length];

        System.arraycopy(src, start, array, 0, length);

        return array;
    }

    public static boolean doesArrayBeginWith(byte[] array, byte[] prefix)
    {
        if (array.length < prefix.length) {
            return false;
        }
        int i = 0;
        for (int size = prefix.length; i < size; i++) {
            if (array[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    public static byte[] invert(byte[] array)
    {
        int size = array.length;
        int limit = size / 2;
        int i = 0;
        while (i < limit)
        {
            byte temp = array[i];
            array[i] = array[(size - 1 - i)];
            array[(size - 1 - i)] = temp;
            i++;
        }
        return array;
    }

    public static int asInt(byte[] input)
    {
        SDKPreconditions.checkArgument((input != null) && (input.length > 0), "Input byte array is null or empty.");

        byte[] result = new byte[4];
        result[0] = 0;
        result[1] = 0;
        result[2] = 0;
        result[3] = 0;
        switch (input.length)
        {
            case 1:
                return asInt(input[0]);
            case 2:
                result[2] = input[0];
                result[3] = input[1];
                break;
            case 3:
                result[1] = input[0];
                result[2] = input[1];
                result[3] = input[2];
                break;
            case 4:
                result[0] = input[0];
                result[1] = input[1];
                result[2] = input[2];
                result[3] = input[3];
                break;
            default:
                throw new IllegalArgumentException("Input byte array exceeds max integer size (4 bytes)");
        }
        return ByteBuffer.wrap(result).getInt();
    }

    public static byte[] to2ByteArray(int value)
    {
        return new byte[] { (byte)value, (byte)(value >> 8) };
    }

    public static byte[] convert(UUID uuid)
    {
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        return ByteBuffer.allocate(16).putLong(msb).putLong(lsb).array();
    }

    /* Error */
    public static byte[] convert(java.io.File file)
            throws java.io.IOException
    {
        // Byte code:
        //   0: aload_0
        //   1: invokevirtual 19	java/io/File:length	()J
        //   4: ldc2_w 21
        //   7: lcmp
        //   8: ifge +7 -> 15
        //   11: iconst_1
        //   12: goto +4 -> 16
        //   15: iconst_0
        //   16: ldc 23
        //   18: invokestatic 9	com/kontakt/sdk/android/common/util/SDKPreconditions:checkArgument	(ZLjava/lang/Object;)V
        //   21: aload_0
        //   22: invokevirtual 19	java/io/File:length	()J
        //   25: l2i
        //   26: newarray <illegal type>
        //   28: astore_1
        //   29: aconst_null
        //   30: astore_2
        //   31: new 24	java/io/FileInputStream
        //   34: dup
        //   35: aload_0
        //   36: invokespecial 25	java/io/FileInputStream:<init>	(Ljava/io/File;)V
        //   39: astore_2
        //   40: aload_2
        //   41: aload_1
        //   42: invokevirtual 26	java/io/InputStream:read	([B)I
        //   45: iconst_m1
        //   46: if_icmpne +13 -> 59
        //   49: new 27	java/io/IOException
        //   52: dup
        //   53: ldc 28
        //   55: invokespecial 29	java/io/IOException:<init>	(Ljava/lang/String;)V
        //   58: athrow
        //   59: aload_2
        //   60: invokestatic 30	com/kontakt/sdk/android/common/util/Closeables:closeQuietly	(Ljava/io/InputStream;)V
        //   63: goto +10 -> 73
        //   66: astore_3
        //   67: aload_2
        //   68: invokestatic 30	com/kontakt/sdk/android/common/util/Closeables:closeQuietly	(Ljava/io/InputStream;)V
        //   71: aload_3
        //   72: athrow
        //   73: aload_1
        //   74: areturn
        // Line number table:
        //   Java source line #158	-> byte code offset #0
        //   Java source line #160	-> byte code offset #21
        //   Java source line #161	-> byte code offset #29
        //   Java source line #163	-> byte code offset #31
        //   Java source line #164	-> byte code offset #40
        //   Java source line #165	-> byte code offset #49
        //   Java source line #168	-> byte code offset #59
        //   Java source line #169	-> byte code offset #63
        //   Java source line #168	-> byte code offset #66
        //   Java source line #171	-> byte code offset #73
        // Local variable table:
        //   start	length	slot	name	signature
        //   0	75	0	file	java.io.File
        //   28	46	1	buffer	byte[]
        //   30	38	2	stream	java.io.InputStream
        //   66	6	3	localObject	Object
        // Exception table:
        //   from	to	target	type
        //   31	59	66	finally
        return null;
    }

    public static int toPowerLevel(byte[] byteValue)
    {
        SDKPreconditions.checkArgument(byteValue.length == 1, "Specified value should be 1 byte long.");
        return toPowerLevel(byteValue[0]);
    }

    public static int toPowerLevel(int value)
    {
        switch (value)
        {
            case -30:
                return 0;
            case -20:
                return 1;
            case -16:
                return 2;
            case -12:
                return 3;
            case -8:
                return 4;
            case -4:
                return 5;
            case 0:
                return 6;
            case 4:
                return 7;
        }
        throw new IllegalArgumentException("Unsupported power level value.");
    }

    public static byte[] convertPowerLevel(int powerLevel)
    {
        switch (powerLevel)
        {
            case 0:
                return new byte[] { -30 };
            case 1:
                return new byte[] { -20 };
            case 2:
                return new byte[] { -16 };
            case 3:
                return new byte[] { -12 };
            case 4:
                return new byte[] { -8 };
            case 5:
                return new byte[] { -4 };
            case 6:
                return new byte[] { 0 };
            case 7:
                return new byte[] { 4 };
        }
        throw new IllegalArgumentException(String.format("Unsupported power level: %d", new Object[] { Integer.valueOf(powerLevel) }));
    }

    public static UUID toUUID(byte[] uuid)
    {
        ByteBuffer byteBuffer = ByteBuffer.wrap(uuid);
        return new UUID(byteBuffer.getLong(), byteBuffer.getLong());
    }

    public static boolean doesArrayContainSubset(byte[] src, byte[] subset, int startIndex)
    {
        if ((src == null) || (subset == null)) {
            return false;
        }
        if (subset.length + startIndex > src.length) {
            return false;
        }
        if (startIndex < 0) {
            return false;
        }
        int i = startIndex;
        for (int size = startIndex + subset.length; i < size; i++) {
            if (src[i] != subset[(i - startIndex)]) {
                return false;
            }
        }
        return true;
    }

    public static double[] toPrimitive(Double[] array)
    {
        if (array == null) {
            return null;
        }
        if (array.length == 0) {
            return EMPTY_DOUBLE_ARRAY;
        }
        double[] result = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i].doubleValue();
        }
        return result;
    }
}


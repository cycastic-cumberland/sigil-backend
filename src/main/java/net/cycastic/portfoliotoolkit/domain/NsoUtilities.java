package net.cycastic.portfoliotoolkit.domain;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class NsoUtilities {
    public static final int KEY_LENGTH;
    private static final byte[] SEPARATOR_SEQUENCE;

    static {
        KEY_LENGTH = 255;
        SEPARATOR_SEQUENCE = new byte[3];
        SEPARATOR_SEQUENCE[0] = '*';
        // The last 2 bytes are null bytes;
    }

    public static void toNaturalSearchOrder(ByteBuffer buffer, long value) {
        var shifted = value ^ Long.MIN_VALUE;
        buffer.putLong(shifted);
    }

    public static void toNaturalSearchOrder(ByteBuffer buffer, double value) {
        var bits = Double.doubleToRawLongBits(value);
        var normalized = (bits ^ ((bits >> 63) & 0x7fffffffffffffffL));
        buffer.putLong(normalized);
    }

    public static byte[] toNaturalSearchOrder(long value) {
        var buffer = ByteBuffer.allocate(Long.BYTES);
        toNaturalSearchOrder(buffer, value);
        return buffer.array();
    }

    public static byte[] toNaturalSearchOrder(double value) {
        var buffer = ByteBuffer.allocate(Double.BYTES);
        toNaturalSearchOrder(buffer, value);
        return buffer.array();
    }

    public void insertSeparator(ByteBuffer buffer){
        buffer.put(SEPARATOR_SEQUENCE);
    }

    public static void padKey(byte[] bytes, int fromIndex){
        Arrays.fill(bytes, fromIndex, KEY_LENGTH, (byte)1);
    }

    public static void padKey(ByteBuffer buffer){
        var currentLength = buffer.position();
        if (currentLength >= KEY_LENGTH){
            throw new IllegalStateException("Buffer length is over the preset limit");
        }
        var arr = new byte[KEY_LENGTH - currentLength];
        Arrays.fill(arr, (byte)1);
        buffer.put(arr);
    }
}

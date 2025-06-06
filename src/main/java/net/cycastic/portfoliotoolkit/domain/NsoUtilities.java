package net.cycastic.portfoliotoolkit.domain;

import sun.misc.Unsafe;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class NsoUtilities {
    public static final int KEY_LENGTH;
    private static final byte[] SEPARATOR_SEQUENCE;
    private static final Unsafe U;
    private static final long BYTE_ARRAY_BASE_OFFSET;

    static {
        KEY_LENGTH = 255;
        SEPARATOR_SEQUENCE = new byte[3];
        // The last 2 bytes are null bytes;
        SEPARATOR_SEQUENCE[0] = '*';
        try {
            var f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            U = (Unsafe) f.get(null);
            BYTE_ARRAY_BASE_OFFSET = U.arrayBaseOffset(byte[].class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    private static long readLongAt(byte[] array, int index) {
        long offset = BYTE_ARRAY_BASE_OFFSET + index;
        return U.getLong(array, offset);
    }

    public static int compareByteArrays(byte[] a, byte[] b) {
        // TODO: Use Vector API when it leaves incubation
        var len = Math.min(a.length, b.length);
        var i = 0;
        var upper = (len / Long.BYTES) * Long.BYTES;

        // Compare 8 bytes at a time
        while (i < upper) {
            var lhs = readLongAt(a, i);
            var rhs = readLongAt(b, i);
            if (lhs != rhs){
                return lhs - rhs > 0 ? 1 : -1;
            }

            i += Long.BYTES;
        }

        for (; i < len; i++) {
            int ai = Byte.toUnsignedInt(a[i]);
            int bi = Byte.toUnsignedInt(b[i]);
            if (ai != bi) {
                return ai - bi > 0 ? 1 : -1;
            }
        }

        return a.length - b.length > 0 ? 1 : -1;
    }
}

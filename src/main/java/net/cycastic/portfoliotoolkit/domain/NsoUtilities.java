package net.cycastic.portfoliotoolkit.domain;

import sun.misc.Unsafe;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NsoUtilities {
    public static final int KEY_LENGTH;
    private static final byte[] HIGHEST_SEARCH_KEY;
    private static final byte[] SEPARATOR_SEQUENCE;
    public static final int SEPARATOR_SEQUENCE_LENGTH;
    private static final Unsafe U;
    private static final long BYTE_ARRAY_BASE_OFFSET;

    static {
        KEY_LENGTH = 255;
        HIGHEST_SEARCH_KEY = new byte[KEY_LENGTH];
        Arrays.fill(HIGHEST_SEARCH_KEY, (byte)1);

        SEPARATOR_SEQUENCE = new byte[3];
        // The last 2 bytes are null bytes;
        SEPARATOR_SEQUENCE[0] = '*';
        SEPARATOR_SEQUENCE_LENGTH = SEPARATOR_SEQUENCE.length;
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
        buffer.putLong(value);
    }

    public static void toNaturalSearchOrder(ByteBuffer buffer, double value) {
        long bits = Double.doubleToRawLongBits(value);
        long mask = (bits >> 63) | 0x8000000000000000L;
        long sortableBits = bits ^ mask;
        buffer.putLong(sortableBits);
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

    public static void insertSeparator(ByteBuffer buffer){
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
            var result = Long.compareUnsigned(lhs, rhs);
            if (result != 0){
                return result > 0 ? 1 : -1;
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

        if (a.length == b.length){
            return 0;
        }
        return a.length - b.length > 0 ? 1 : -1;
    }

    public static String delimeterAsString(){
        return new String(SEPARATOR_SEQUENCE, StandardCharsets.UTF_8);
    }

    /**
     * Splits the input byte array by occurrences of the separator byte array.
     *
     * @param input     The byte array to split.
     * @return An array of byte arrays, each being a segment of the original array separated by the delimiter.
     * @throws IllegalArgumentException if separator is null, empty, or longer than input.
     */
    public static byte[][] split(byte[] input) {
        var separator = SEPARATOR_SEQUENCE;
        if (input == null) {
            return new byte[0][];
        }
        if (separator.length > input.length) {
            // No possible match → single element (the whole input)
            return new byte[][] { Arrays.copyOf(input, input.length) };
        }

        List<byte[]> parts = new ArrayList<>();
        int start = 0;
        int i = 0;

        while (i <= input.length - separator.length) {
            if (matchesAt(input, separator, i)) {
                parts.add(Arrays.copyOfRange(input, start, i));
                i += separator.length;
                start = i;
            } else {
                i++;
            }
        }

        // Add the tail (might be empty if input ends with separator)
        parts.add(Arrays.copyOfRange(input, start, input.length));

        return parts.toArray(new byte[parts.size()][]);
    }

    /**
     * Checks whether the separator matches input at position index.
     */
    private static boolean matchesAt(byte[] input, byte[] separator, int index) {
        for (int j = 0; j < separator.length; j++) {
            if (input[index + j] != separator[j]) {
                return false;
            }
        }
        return true;
    }

    public static byte[] getHighestSearchKey(){
        return HIGHEST_SEARCH_KEY.clone();
    }
}

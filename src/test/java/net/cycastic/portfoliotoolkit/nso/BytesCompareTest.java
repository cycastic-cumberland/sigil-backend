package net.cycastic.portfoliotoolkit.nso;

import net.cycastic.portfoliotoolkit.domain.NsoUtilities;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class BytesCompareTest {
    private static void sameLengthNonEqualTest(int length){
        var lhs = new byte[length];
        var rhs = new byte[length];

        Arrays.fill(lhs, (byte)12);
        Arrays.fill(rhs, (byte)12);

        lhs[length - 3] = 14;
        assert NsoUtilities.compareByteArrays(lhs, rhs) > 0;

        Arrays.fill(lhs, (byte)12);
        rhs[length - 3] = 14;
        assert NsoUtilities.compareByteArrays(lhs, rhs) < 0;
    }

    private static void sameLengthEqualTest(int length){
        var lhs = new byte[length];
        var rhs = new byte[length];

        Arrays.fill(lhs, (byte)12);
        Arrays.fill(rhs, (byte)12);

        assert NsoUtilities.compareByteArrays(lhs, rhs) == 0;
    }

    @Test
    public void diffLengthNonEqualTest(){
        var lhs = new byte[68];
        var rhs = new byte[67];

        Arrays.fill(lhs, (byte)12);
        Arrays.fill(rhs, (byte)12);

        // tail compare
        lhs[65] = 14;
        assert NsoUtilities.compareByteArrays(lhs, rhs) > 0;

        // SIMD compare
        Arrays.fill(lhs, (byte)12);
        rhs[12] = 14;
        assert NsoUtilities.compareByteArrays(lhs, rhs) < 0;

        // length wins
        Arrays.fill(rhs, (byte)12);
        assert NsoUtilities.compareByteArrays(lhs, rhs) > 0;
    }

    @Test
    public void sameLengthNonEqualTest(){
        sameLengthNonEqualTest(7);
        sameLengthNonEqualTest(68);
        sameLengthNonEqualTest(128);
    }

    @Test
    public void sameLengthEqualTest(){
        sameLengthEqualTest(7);
        sameLengthEqualTest(68);
        sameLengthEqualTest(128);
    }
}

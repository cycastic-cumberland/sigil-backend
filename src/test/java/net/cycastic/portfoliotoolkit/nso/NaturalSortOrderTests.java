package net.cycastic.portfoliotoolkit.nso;

import net.cycastic.portfoliotoolkit.domain.NsoUtilities;
import org.junit.jupiter.api.Test;

public class NaturalSortOrderTests {
    @Test
    public void longTest(){
        assert NsoUtilities.compareByteArrays(NsoUtilities.toNaturalSearchOrder(12),
                NsoUtilities.toNaturalSearchOrder(14)) < 0;

        assert NsoUtilities.compareByteArrays(NsoUtilities.toNaturalSearchOrder(2027),
                NsoUtilities.toNaturalSearchOrder(9)) > 0;

        assert NsoUtilities.compareByteArrays(NsoUtilities.toNaturalSearchOrder(39670.129),
                NsoUtilities.toNaturalSearchOrder(14.3)) > 0;
    }
}

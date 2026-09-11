package com.foodtracking.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BarcodesTest {

    @Test
    public void stripsLeadingZerosAndNonDigits() {
        assertEquals("49000006461", Barcodes.normalize("049000006461"));
        assertEquals("49000006461", Barcodes.normalize(" 0490-0000-6461 "));
        assertEquals("49000006461", Barcodes.normalize("00049000006461"));
    }

    @Test
    public void matchesPaddedGtins() {
        assertTrue(Barcodes.matches("049000006461", "00049000006461"));
        assertTrue(Barcodes.matches("16000275287", "016000275287"));
        assertFalse(Barcodes.matches("049000006461", "016000275287"));
        assertFalse(Barcodes.matches("", "049000006461"));
    }

    @Test
    public void blankDetection() {
        assertTrue(Barcodes.isBlank(null));
        assertTrue(Barcodes.isBlank("   "));
        assertFalse(Barcodes.isBlank("049000006461"));
    }
}

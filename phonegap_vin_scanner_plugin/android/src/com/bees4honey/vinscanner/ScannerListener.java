package com.bees4honey.vinscanner;

/**
 * A listener for {@link ScannerFragment}.
 */
public interface ScannerListener {
    /**
     * Method called by ScannerFragment every time VIN code is detected in the camera output.
     * @param code code detected by {@link ScannerFragment}
     */
    void scannedCode(String code);
}

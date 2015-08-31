package com.bees4honey.vinscanner;

import android.content.Context;

/**
 * B4HScanner class provides Java interface for native VIN Scanner libraries.
 */
public class B4HScanner
{
    /**
     * All the possible license statuses of the library. The SdkStatus helps to check if license file is valid and correct.
     * Those statuses inform current library state. The {@link #LICENSE_VERIFICATION_OK} means that license file is found and it is correct for current package.
     * The {@link #LICENSE_VERIFICATION_EVALUATION} status shows that license file is absent and library is functioning in the trial mode.
     * The {@link #LICENSE_VERIFICATION_LIMIT_REACHED} status shows that license file is absent and amount of scans reached the maximum value and library will scan no more codes.
     * All other license statuses show that some errors occurred during license checking process.
     */
    public enum SdkStatus {

        /**
         * License status not specified
         */
        LICENSE_VERIFICATION_NA(0x100),
        /**
         * The library is functioning in the trial mode.
         */
        LICENSE_VERIFICATION_EVALUATION(0x101),

        /**
         * License is valid.
         */
        LICENSE_VERIFICATION_OK(0x102),

        /**
         * Scans limit reached.
         */
        LICENSE_VERIFICATION_LIMIT_REACHED(0x103),

        /**
         * License is not valid. File descriptor of license file not found
         */
        ERROR_FILE_DESCRIPTOR_NOT_FOUND(0x01),

        /**
         * License is not valid. Descriptor of license file not found
         */
        ERROR_FIELD_DESCRIPTOR_NULL(0x02),

        /**
         * License is not valid. License file was found but cannot be opened
         */
        ERROR_FILE_NOT_OPEN(0x03),

        /**
         * License is not valid. License text not found
         */
        ERROR_LICENSETEXT_NULL(0x04),

        /**
         * License is not valid. License public key is not found.
         */
        ERROR_PUBLICKEY_NULL(0x05),

        /**
         * License is not valid. License signature is not found.
         */
        ERROR_SIGNATURE_NULL(0x06),

        /**
         * Application package name is not found.
         */
        ERROR_PACKAGENAME_NULL(0x07),

        /**
         * Licensed application package name is not found.
         */
        ERROR_LICENSEDAPPIDS_NULL(0x08),

        /**
         * Unable to determine application package name.
         */
        ERROR_UNDEF_PACKAGE_NAME (0x09),

        /**
         * License is not valid. Incorrect package name
         */
        ERROR_INCOR_PACKAGE_NAME (0x0a),

        /**
         * License is not valid. Incorrect signature
         */
        ERROR_INCOR_SIGNATURE (0x0b),

        /**
         * Application context is not found.
         */
        ERROR_CONTEXT_NOT_FOUND (0x0c),

        /**
         * Application context is null.
         */
        ERROR_CONTEXT_IS_NULL (0x0d),

        /**
         * Application resources object is not found.
         */
        ERROR_RESOURCES_NOT_FOUND (0x0e),

        /**
         * Asset file descriptor is not found.
         */
        ERROR_ASSET_FILE_DESCRIPTOR_NOT_FOUND (0x0f);

        private final int status;

        SdkStatus(final int status) {
            this.status = status;
        }

        public int getCode() {
            return status;
        }
    }

    // load native library when initializing
    static
    {
        System.loadLibrary("b4hvinscanner");
    }

    public B4HScanner()
    {
        super();
    }

    /**
     * Parse VIN code from given byte array.
     * @param data the image captured from the Android camera and stored in NV21 format.
     * @param width the width of the image.
     * @param height the height of the image.
     * @param context current context.
     * @return scanned code or NULL if VIN code is not found.
     */
    @SuppressWarnings("unused")
    public native String parse( byte []data, int width, int height, Context context);

    /**
     * Check status of scanner license
     * @param context current context
     * @return the int status value of the license.
     */
    @SuppressWarnings("unused")
    public native int status(Context context);

    /**
     * Get enum with human-readable scanner license status.
     * @param context current status.
     * @return enum value containing status of the library.
     */
    @SuppressWarnings("unused")
    public SdkStatus getStatus(Context context) {
        int statusCode = status(context);

        for (SdkStatus sdkStatus : SdkStatus.values()) {
            if (sdkStatus.getCode() == statusCode) {
                return sdkStatus;
            }
        }

        return SdkStatus.LICENSE_VERIFICATION_NA;
    }

    /**
     *  Get version of Bees4honey VIN scanner library. Method returns  "2.0" value by default.
     *  NOTE: Since method wasn't presented in library versions previous to 2.0 it may throw an exception
     *  if you use it with outdated library.
     *  @return Version of scanner library.
     */
    @SuppressWarnings("unused")
    public native String version();
}

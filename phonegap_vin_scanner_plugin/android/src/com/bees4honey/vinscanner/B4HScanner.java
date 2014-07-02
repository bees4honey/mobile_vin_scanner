package com.bees4honey.vinscanner;

import android.content.Context;

// Class-wrapper for working with native code
public class B4HScanner 
{
	public static enum SdkStatus {
		
		LICENSE_VERIFICATION_NA(0x100),					// License status not specified
		LICENSE_VERIFICATION_EVALUATION(0x101),			// License for demo mode
		LICENSE_VERIFICATION_OK(0x102),					// License is valid
		LICENSE_VERIFICATION_LIMIT_REACHED(0x103),		// License is not valid. Scans limit reached
		
		ERROR_FILE_DESCRIPTOR_NOT_FOUND(0x01),			// License is not valid. File descriptor of license file not found
		ERROR_FIELD_DESCRIPTOR_NULL(0x02),				// License is not valid. Descriptor of license file not found
		ERROR_FILE_NOT_OPEN(0x03),						// License is not valid. License file was found but not opened for read
		ERROR_LICENSETEXT_NULL(0x04),					// License is not valid. License text not found
		ERROR_PUBLICKEY_NULL(0x05),						// License is not valid. License public key not found
		ERROR_SIGNATURE_NULL(0x06),						// License is not valid. License signature not found
		ERROR_PACKAGENAME_NULL(0x07),					// License is not valid. Application package name not found
		ERROR_LICENSEDAPPIDS_NULL(0x08),				// License is not valid. Licensed application package names not found
		ERROR_UNDEF_PACKAGE_NAME (0x09),				// License is not valid. Unable determine application package name
		ERROR_INCOR_PACKAGE_NAME (0x0a),				// License is not valid. Incorrect package name
		ERROR_INCOR_SIGNATURE (0x0b),					// License is not valid. Incorrect signature			
		ERROR_CONTEXT_NOT_FOUND (0x0c),					// License is not valid. Application context not found
		ERROR_CONTEXT_IS_NULL (0x0d),					// License is not valid. Application context is null
		ERROR_RESOURCES_NOT_FOUND (0x0e),				// License is not valid. Application resources object not found
		ERROR_ASSET_FILE_DESCRIPTOR_NOT_FOUND (0x0f);	// License is not valid. Asset file descriptor not found
		
		private final int status;
		
		SdkStatus(final int status) {
			this.status = status; 
		}
		
		public int getCode() { 
			return status; 
		}
	}
	
	public float	acuracy;
	
	// load native library when initializing
	static
	{ 
		System.loadLibrary("b4hvinscanner"); 
	};

	// constructor
	public B4HScanner()
	{
		super();
		acuracy = 1.0f;
	}
	
	// parse VIN code from "data" or NULL if VIN code not found
	public native String parse( byte []data, int size, int width, int height, int q, Context context);
	
	// check status of scanner license
	public native int status(Context context);
	
	// get enum with human-readable scanner license status
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
	 *  Get version of Bees4honey VIN scanner library.
	 *  *NOTE* If the method throws an exception, the library has too old version, equals 1.1.0 by default.
	 *  @return Version of scanner library
 	 */
	public native String version();
}

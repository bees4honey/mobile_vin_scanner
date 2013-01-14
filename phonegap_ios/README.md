# Mobile VIN Scanner PhoneGap plugin for iOS.

## Installing iOS VIN Scanner Plugin
1. Put "barcodescanner.js" in your "www/resources/js/" folder.
2. Register plugin in Cordova.plist. Example is shown in "Cordova.plist" file in this repo.
3. Copy "Plugons/" folder in your project's folder.

## Usage Example

window.plugins.VINBarcodeScanner.scan(function(result) 
	{
		if (result.VINCode && result.VINCode.trim().length > 0) {
				//code retrieved and is not null;
			} else {
				//code retrieved and is null - error in code, try again;
			}

		}, function(error) {
			//error while scanning;
		}, ["ScannerMainViewController"]);
			
## Licensing & Support

**For licensing and support questions please contact us directly at vin@bees4honey.com**

Thanks!
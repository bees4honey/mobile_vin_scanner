# Mobile VIN Scanner PhoneGap plugin for Android.

## Installing Android VIN Scanner Plugin

1. Please copy contents of "manifest.txt" into your "AndroidManifest.xml" between <manifest> and </manifest> tags.
2. Please copy contents of "plugins.txt" into your "res/xml/plugin.xml" between <plugins> and </plugins> tags.
3. Copy "assets", "libs", "res", "src" in the root directory of your project.
4. Specify your package name in "/src/com/bees4honey/vinscanner/Scanner.java" in the following line (second from top):
	import <your_package_name>.R;

## Usage Example

window.plugins.VINBarcodeScanner.scan(function(result) {

			if (result.text && result.text.trim().length > 0) {
				//VIN number returned successfully;
			} else {
				//VIN number is empty - try again;
			}

		}, function(error) {
			//error occurred while scanning;
		});
		
		
## Licensing & Support

**For licensing and support questions please contact us directly at vin@bees4honey.com**

Thanks!
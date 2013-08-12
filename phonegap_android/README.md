# Mobile VIN Scanner PhoneGap plugin for Android.

## Installing Android VIN Scanner Plugin

1. Please copy contents of "manifest.txt" into your "AndroidManifest.xml" between  `` `<manifest>` `` and `` `</manifest>` `` tags.
2. Please add into your "res/xml/config.xml" 

``
    <feature name="VinBarScanner">
      <param name="android-package" value="com.bees4honey.vinscanner.plugin.VinBarScanner"/>
    </feature>
``
3. Copy "assets", "libs", "res", "src" in the root directory of your project.
4. Specify your package name in "/src/com/bees4honey/vinscanner/Scanner.java" in the following line (second from top):
	`` `import <your_package_name>.R;` ``

## Usage Example
5. You can see usage example in index.html. 	
		
## Licensing & Support

**For licensing and support questions please contact us directly at vin@bees4honey.com**

Thanks!
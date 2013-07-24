# Mobile VIN Scanner PhoneGap plugin for iOS. new Cordova versions

## Installing iOS VIN Scanner Plugin
1. Put "barcodescanner.js" in your "www/js/" folder.
2. Register plugin in config.xml in project directory, not in www folder.
Add string   
<feature name="com.bees4honey.VINBarcodeScanner">
	<param name="ios-package" value="B4HVINScannerPluginClass" />
</feature>value="B4HVINScannerPluginClass" /> 
3. Copy "Plugins/" folder in your project's folder.
## Adding required frameworks  
4. Press to Main project file in project navigator. Press on target on the left panel and add CoreVideo.framework, Security.framework in "Linked Frameworks and Libraries" section
5. Usage example you can see in index.html. replace www/index.html file with index.html file from our repository.			
## Licensing & Support

**For licensing and support questions please contact us directly at vin@bees4honey.com**

Thanks!
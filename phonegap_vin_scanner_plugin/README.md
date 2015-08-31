# Phonegap plugin 

Vin Scanner Phonegap plugin for Android and iOS platforms.

**WARNING:** since VIN Scanner plugin heavily uses native code for both iOS and Android platforms, the Adobe Phonegap Build cloud service is not supported. To use our plugin you need to build project using Phonegap command line tools.

## Supported platforms

**iOS:** iOS versions starting with 7.0 are supported.

**Android:** only Android versions 4.0 and higher are supported. Please make your project min SDK version equal to 14 (Android 4.0) to get scanner working.

## Phonegap plugin installation

Phonegap VIN scanner plugin for iOS and Android platforms.

Installation instructions:
1. Generate your project using "phonegap create" command
2. Add a plugin to your project using the following command:

```
phonegap plugin add https://github.com/bees4honey/mobile_vin_scanner#:/phonegap_vin_scanner_plugin
```

3. You can find usage example in phonegap_vin_scanner_plugin/index.html file.
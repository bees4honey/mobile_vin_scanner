
function VINBarcodeScanner () {
}

//-------------------------------------------------------------------
VINBarcodeScanner.prototype.scan = function(successCallback, errorCallback) {
    console.log("VINBarcodeScanner.scan method called");
    if (errorCallback == null) { errorCallback = function() {}}

    if (typeof errorCallback != "function")  {
        console.log("VINBarcodeScanner.scan failure: failure parameter not a function");
        return
    }

    if (typeof successCallback != "function") {
        console.log("VINBarcodeScanner.scan failure: success callback parameter must be a function");
        return
    }

    return cordova.exec(successCallback, errorCallback, 'VinBarScanner', 'scan', []);
};

if (!window.plugins) window.plugins = {}
if (!window.plugins.VINBarcodeScanner) {
window.plugins.VINBarcodeScanner = new VINBarcodeScanner()
}
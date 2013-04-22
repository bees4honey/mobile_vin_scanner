;(function()
{

var VINBarcodeScanner = function() {
};

VINBarcodeScanner.Encode = 
{
        TEXT_TYPE:     "TEXT_TYPE",
        EMAIL_TYPE:    "EMAIL_TYPE",
        PHONE_TYPE:    "PHONE_TYPE",
        SMS_TYPE:      "SMS_TYPE",
        CONTACT_TYPE:  "CONTACT_TYPE",
        LOCATION_TYPE: "LOCATION_TYPE"
}

VINBarcodeScanner.prototype.scan = function(success, fail, options) 
{
    function successWrapper(result)
	{
        result.cancelled = (result.cancelled == 1)
        success.call(null, result)
    }

    if (!fail) { fail = function() {}}

    if (typeof fail != "function") 
	{
        console.log("scan failure: failure parameter is not a function")
        return
    }

    if (typeof success != "function")
	{
        fail("success callback parameter must be a function")
        return
    }
  
    if ( null == options ) 
		options = []
  return Cordova.exec(successWrapper, fail, "com.bees4honey.VINBarcodeScanner", "scan", options)
}

VINBarcodeScanner.prototype.encode = function(type, data, success, fail, options)
{
    if (!fail) { fail = function() {}}

    if (typeof fail != "function")  
	{
        console.log("encode failure: failure parameter is not a function")
        return
    }

    if (typeof success != "function") 
	{
        fail("success callback parameter must be a function")
        return
    }
    return Cordova.exec(success, fail, "com.bees4honey.VINBarcodeScanner", "encode", [{type: type, data: data, options: options}])
}

if (!window.plugins) window.plugins = {}
if (!window.plugins.VINBarcodeScanner) {
window.plugins.VINBarcodeScanner = new VINBarcodeScanner()

}

})();

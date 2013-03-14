package com.bees4honey.vinscanner;

import java.lang.reflect.Method;
import android.os.IBinder;

public class TorchControl {

    private Object svc = null;
    private Method getFlashlightEnabled = null;
    private Method setFlashlightEnabled = null;
    
    @SuppressWarnings("unchecked")
    public TorchControl() throws Exception {
        try {
            Class sm = Class.forName("android.os.ServiceManager");
            Object hwBinder = sm.getMethod("getService", String.class).invoke(null, "hardware");
            
            // get the hardware service stub. this seems to just get us one step closer to the proxy
            Class hwsstub = Class.forName("android.os.IHardwareService$Stub");
            Method asInterface = hwsstub.getMethod("asInterface", android.os.IBinder.class);
            svc = asInterface.invoke(null, (IBinder) hwBinder);

            // grab the class (android.os.IHardwareService$Stub$Proxy) so we can reflect on its methods
            Class proxy = svc.getClass();
            
            // save methods
            getFlashlightEnabled = proxy.getMethod("getFlashlightEnabled");
            setFlashlightEnabled = proxy.getMethod("setFlashlightEnabled", boolean.class);
            if( !torch(false))
                throw new Exception("LED could not be initialized");
	    }
	    catch(Exception e) 
	    {
            throw new Exception("LED could not be initialized");
        }
    }
    
    public boolean isEnabled() {
            try {
                    return getFlashlightEnabled.invoke(svc).equals(true);
            }
            catch(Exception e) {
                    return false;
            }
    }
    
    public boolean  torch(boolean tf) {
            try {
                    setFlashlightEnabled.invoke(svc, tf);
                    return true;
            }
            catch(Exception e) 
            {
            	return false;
            }
    }
}
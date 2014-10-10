//
//	B4HScannerController.h
//	ScannerLibrary
//
//	Created by bee4honey developer.
//	Copyright (c) 2014 bees4honey. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>

/** @file B4HScannerController.h
 *  Possible library statuses.
 */
typedef enum
{
    /**
     *  Scanner is ready and able to run in evaluation mode
     */
    B4HState_EvalModeReady = 1,
    
    /**
     *  Scanner is ready and able to run in licenced mode
     */
    B4HState_LicensedModeReady = 2,
    
    /**
     *  Scanner is not ready due to wrong linking options
     */
    B4HState_WrongLinkingOptions = -1,
    
    /**
     *  Some of needed OS libraries are missing
     */
    B4HState_MissingOSLibraries = -2,
    
    /**
     *  Device has no camera available
     */
    B4HState_NoCamera = -3,
    
    /**
     *  Licence file is not valid or corrupted
     */
    B4HState_BadLicense = -4,
    
    /**
     *  Scanner has reached its limit for VIN code recognizing in evaluation mode
     */
    B4HState_ScanLimitReached = -5,
}
B4HScannerLibraryStatus;

/** @file B4HScannerController.h
 Scanning direction for 1D VIN codes.
 */
typedef enum
{
    /**
     *  Scanner searchs for 1D VIN code in horizontal direction.
     */
    B4HScannerHorizontal = 0,
    
    /**
     *  Scanner searchs for 1D VIN Code in vertical direction.
     */
    B4HScannerVertical = 1
}
B4HScannerOrientation;

@protocol B4HScannerDelegate;
@class B4HScannerController;

/**
 *  Base overlay view controller. Inherrit this interface to draw custom UI over camera output.
 */
@interface B4HCameraOverlayViewController : UIViewController { }

/**
 *  Parent scanner for this controller.
 */
@property (nonatomic, assign) B4HScannerController *parentScanner;

@end

/**
 *  Main class to use in your application for capturing VIN codes.
 *  Scanner captures the output from camera and shows it on the screen. Scanner analyses camera output and returns detected code over the delegate
 */
@interface B4HScannerController : UIViewController
- (id)initWithOrientation:(B4HScannerOrientation) ScanOrientation;

/**
 *  Start scanning process with given orientation
 *
 *  @param orientation scanning orientation for 1D scanner.
 */
- (void)startScanningWithOrientation:(B4HScannerOrientation) orientation;

/**
 *  Check if library is scanning for VIN codes right now.
 *
 *  @return YES if scanning is active and NO otherwise.
 */
- (BOOL) isRunning;

/**
 *  Start scanning process. Before starting the scanning process user should check if scanner is running and stop scanning process.
 */
- (void) startScanning;

/**
 *  Stop scanning process.
 */
- (void) stopScanning;

/**
 *  Check current library status.
 *
 *  @return current library status.
 */
- (B4HScannerLibraryStatus) CheckReadyStatus;

/**
 *  Get current library version
 *
 *  @return current version of the library. Returns 2.0 string value by default.
 */
- (NSString*)version;

/**
 *  Overlay controller. The interface of the overlay controller is drawn over the camera output of {@link #B4HScannerController} camera output
 */
@property (nonatomic, retain) B4HCameraOverlayViewController *overlay;

/**
 *  Scanner's delegate. Native library uses the delegate to return the scanned VIN-codes.
 */
@property (nonatomic, assign) id <B4HScannerDelegate> delegate;

/**
 *  Scanning orientation for 1D VIN-scanner. Native library may scan for 1D VIN-codes in both vertical and horizontal directions.
 *  Changing this property will change scanning direction for 1D-scanner.
 */
@property (nonatomic, assign) B4HScannerOrientation scanOrientation;

@end

/**
 *  Scanner's delegate. Conform this protocol to retrieve scanned VIN-codes from library.
 */
@protocol B4HScannerDelegate <NSObject>
@required

/**
 *  Callback method called every time scanner detected VIN barcode. Method is only called if VIN bar code was detected in camera output.
 *
 *  @param scanner scanner calling the method
 *  @param code    scanned VIN code.
 */
- (void)scanner:(B4HScannerController *)scanner gotCode:(NSString *)code;
@end
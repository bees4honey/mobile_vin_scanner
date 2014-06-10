//
//	B4HScannerController.h
//	ScannerLibrary
//
//	Created by bee4honey developer.
//	Copyright (c) 2014 bees4honey. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>

typedef enum
{
	B4HState_EvalModeReady = 1, // scanner is ready and able to run in evaluation mode
	B4HState_LicensedModeReady = 2, // scanner is ready and able to run in licenced mode
	
	B4HState_WrongLinkingOptions = -1, // scanner is not ready due to wrong linking options
	B4HState_MissingOSLibraries = -2, // some of needed OS libraries are missing
	B4HState_NoCamera = -3, // device has no camera available
	B4HState_BadLicense = -4, // licence file is not valid or corrupted
	B4HState_ScanLimitReached = -5, // scanner has reached its limit for VIN code recognizing in evaluation mode
}
B4HScannerLibraryStatus;

typedef enum
{
	B4HOrientationHorizontal = 0,
	B4HOrientationVertical = 1
}
B4HScannerOrientation;

@protocol B4HScannerDelegate;
@class B4HScannerController;

//	Base overlay view controller to inherit own interface layout from
@interface B4HCameraOverlayViewController : UIViewController { }

@property (nonatomic, assign) B4HScannerController *parentScanner;

@end

//	Main class to use in your application for capturing VIN codes
@interface B4HScannerController : UIViewController
- (id)initWithOrientation:(B4HScannerOrientation) ScanOrientation;
- (void)startScanningWithOrientation:(B4HScannerOrientation) orientation;
- (BOOL) isRunning;
- (void) startScanning;
- (void) stopScanning;
- (B4HScannerLibraryStatus) CheckReadyStatus;

@property (nonatomic, retain) B4HCameraOverlayViewController *overlay;
@property (nonatomic, assign) id <B4HScannerDelegate> delegate;
@property (nonatomic, assign) B4HScannerOrientation scanOrientation;

@end

@protocol B4HScannerDelegate <NSObject>
@required
- (void)scanner:(B4HScannerController *)scanner gotCode:(NSString *)code;
@end
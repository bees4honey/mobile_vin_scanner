//
//	B4HVINScannerPluginClass.m
//	ScannerLibrary
//
//	Created by bees4honey developer.
//	Copyright (c) 2012 bees4honey. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
#import <AssetsLibrary/AssetsLibrary.h>
#import "B4HScannerController.h"
#import "B4HVINScannerPluginClass.h"
#import "B4HOverlayViewController.h"

@interface B4HVINScannerPluginClass ()
@property (nonatomic, strong) NSString *callbackId;

- (void)returnSuccess:(NSString*)scannedCode cancelled:(BOOL)cancelled;
- (void)returnError:(NSString*)message;
@end

@interface B4HVINScannerProcessor : NSObject<B4HOverlayDelegate>


@property (nonatomic, strong) B4HVINScannerPluginClass* plugin;
@property (nonatomic, strong) UIViewController* parentViewController;
@property (nonatomic, strong) B4HOverlayViewController* scannerViewController;

- (id)initWithPlugin:(B4HVINScannerPluginClass*)plugin parentViewController:(UIViewController*)parentViewController;
- (void)scanBarcode;
- (void)openScannerView;
- (NSString*)checkLibraryStatus:(B4HScannerController*)scanner;

@end

@implementation B4HVINScannerPluginClass

- (NSString*)isAVFoundationAvailable
{
    NSString* result = nil;
    Class aClass = NSClassFromString(@"AVCaptureSession");
    if (aClass == nil)
	{
        return @"AVFoundation Framework is not available. VIN Scanning is not supported.";
    }
    return result;
}

- (void)scan:(CDVInvokedUrlCommand*)command
{
    self.callbackId = command.callbackId;
    B4HVINScannerProcessor* processor;
    NSString* AVFoundationErrorMessage;
    AVFoundationErrorMessage = [self isAVFoundationAvailable];
    if (AVFoundationErrorMessage)
	{
        [self returnError:AVFoundationErrorMessage];
        return;
    }
    processor = [[B4HVINScannerProcessor alloc] initWithPlugin:self parentViewController:self.viewController];
    [processor performSelector:@selector(scanBarcode) withObject:nil afterDelay:0];
}

- (void)returnSuccess:(NSString*)scannedCode cancelled:(BOOL)cancelled //code successfully scanned or scan was cancelled
{
    NSNumber* cancelledNumber = [NSNumber numberWithInt:(cancelled?1:0)];
    NSMutableDictionary* resultDict = [[NSMutableDictionary alloc] init];
    [resultDict setObject:scannedCode     forKey:@"VINCode"];
    [resultDict setObject:cancelledNumber forKey:@"cancelled"];
    CDVPluginResult* result = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK messageAsDictionary: resultDict];
    [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
}

- (void)returnError:(NSString*)message //error detected
{
    CDVPluginResult* result = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK messageAsString: message];
    [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
}

@end

//scanner wrapper class
@implementation B4HVINScannerProcessor

@synthesize plugin = _plugin;
@synthesize parentViewController = __parentViewController;
@synthesize scannerViewController = _viewController;

- (id)initWithPlugin:(B4HVINScannerPluginClass*)plugin parentViewController:(UIViewController*)parentViewController
{
    self = [super init];
    if (!self) return self;
    self.plugin = plugin;
    self.parentViewController = parentViewController;
    return self;
}

- (void)scanBarcode
{
    self.scannerViewController = [[B4HOverlayViewController alloc] initWithNibName:@"B4HOverlayViewController" bundle:nil];
    self.scannerViewController.delegate = self;
    B4HScannerController *controller = [[B4HScannerController alloc] initWithOrientation:B4HScannerHorizontal];
    
    NSString* errorMessage = [self checkLibraryStatus:controller];
    if (errorMessage)
	{
        [self barcodeScanFailed:errorMessage];
        return;
    }

    [controller setOverlay:self.scannerViewController];
    [controller setDelegate:self.scannerViewController];
    [self.parentViewController presentViewController:controller animated:YES completion:nil];
}

- (void)openScannerView
{
    [self.parentViewController presentViewController:self.scannerViewController animated:YES completion:nil];
}

- (void)barcodeScanDone
{
    [self.parentViewController dismissViewControllerAnimated:NO completion:nil];
}

- (void)barcodeScanSucceeded:(NSString*)text
{
    [self barcodeScanDone];
    [self.plugin returnSuccess:text cancelled:FALSE];
}

- (void)barcodeScanFailed:(NSString*)message
{
    [self barcodeScanDone];
    [self.plugin returnError:message];
}

- (void)barcodeScanCancelled
{
    [self barcodeScanDone];
    [self.plugin returnSuccess:@"" cancelled:TRUE];
}

- (NSString*)checkLibraryStatus:(B4HScannerController*)scanner
{
	B4HScannerLibraryStatus scannerStatus = [scanner CheckReadyStatus];
	
	NSString *errorDescription = nil;
	switch (scannerStatus)
	{
		case B4HState_WrongLinkingOptions:
			errorDescription = @"Scanner is not ready due to wrong linking options";
			break;
			
		case B4HState_MissingOSLibraries:
			errorDescription = @"Some of needed OS libraries are missing";
			break;
			
		case B4HState_NoCamera:
			errorDescription = @"Device has no camera available";
			break;
			
		case B4HState_BadLicense:
			errorDescription = @"Licence file is not valid or corrupted";
			break;
			
		case B4HState_ScanLimitReached:
			errorDescription = @"Scanner has reached its limit for VIN code recognizing in evaluation mode";
			break;
			
		default:
			errorDescription = nil;
			break;
	}
    
	return errorDescription;
}

@end
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



#import <Cordova/CDV.h>



@class B4HVINScannerProcessor;
@class B4HVINScannerViewController;

//****************************************interfaces****************************************

//plugin class
@interface B4HVINScannerPluginClass : CDVPlugin {}

- (NSString*)isAVFoundationAvailable;
- (void)scan:(CDVInvokedUrlCommand*)command;
- (void)returnSuccess:(NSString*)scannedCode cancelled:(BOOL)cancelled callback:(NSString*)callback;
- (void)returnError:(NSString*)message callback:(NSString*)callback;

@end

//scanner wrapper class
@interface B4HVINScannerProcessor : B4HScannerController {}

@property (nonatomic, retain) B4HVINScannerPluginClass* plugin;
@property (nonatomic, retain) NSString* callback;
@property (nonatomic, retain) UIViewController* parentViewController;
@property (nonatomic, assign) B4HVINScannerViewController* scannerViewController;
@property (nonatomic, retain) NSString* alternateXib;

- (id)initWithPlugin:(B4HVINScannerPluginClass*)plugin callback:(NSString*)callback parentViewController:(UIViewController*)parentViewController alterateOverlayXib:(NSString *)alternateXib;
- (void)scanBarcode;
- (void)barcodeScanSucceeded:(NSString*)text;
- (void)barcodeScanFailed:(NSString*)message;
- (void)barcodeScanCancelled;
- (void)openScannerView;
- (NSString*)checkLibraryStatus;

@end

//scanner view controller
@interface B4HVINScannerViewController : B4HCameraOverlayViewController<B4HScannerDelegate> {}
@property (nonatomic, assign) B4HVINScannerProcessor* processor;
@property (nonatomic, retain) NSString* alternateXib;
@property (nonatomic, retain) IBOutlet UIView* overlayView;
@property (nonatomic, retain) IBOutlet UIButton *ledButton;

- (id)initWithProcessor:(B4HVINScannerProcessor*)processor alternateOverlay:(NSString *)alternateXib;
- (UIView*)buildOverlayView;
- (IBAction)cancelButtonPressed:(id)sender;
- (IBAction)ledButtonClick;

@end

//****************************************implementation****************************************


// plugin class
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
    B4HVINScannerProcessor* processor;
    NSString* AVFoundationErrorMessage;
    NSString *overlayXib = nil;
    if ( [command.arguments count] == 1 ) {
        overlayXib = [command.arguments objectAtIndex:0];
    }
    AVFoundationErrorMessage = [self isAVFoundationAvailable];
    if (AVFoundationErrorMessage)
	{
        [self returnError:AVFoundationErrorMessage callback:command.callbackId];
        return;
    }
    processor = [[B4HVINScannerProcessor alloc] initWithPlugin:self callback:command.callbackId parentViewController:self.viewController alterateOverlayXib:overlayXib];
    [processor performSelector:@selector(scanBarcode) withObject:nil afterDelay:0];
}


- (void)returnSuccess:(NSString*)scannedCode cancelled:(BOOL)cancelled callback:(NSString*)callback //code successfully scanned or scan was cancelled
{
    NSNumber* cancelledNumber = [NSNumber numberWithInt:(cancelled?1:0)];
    NSMutableDictionary* resultDict = [[[NSMutableDictionary alloc] init] autorelease];
    [resultDict setObject:scannedCode     forKey:@"VINCode"];
    [resultDict setObject:cancelledNumber forKey:@"cancelled"];
    CDVPluginResult* result = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK messageAsDictionary: resultDict];
    [self.commandDelegate sendPluginResult:result callbackId:callback];
}

- (void)returnError:(NSString*)message callback:(NSString*)callback //error detected
{
    CDVPluginResult* result = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK messageAsString: message];
    [self.commandDelegate sendPluginResult:result callbackId:callback];
}

@end

//scanner wrapper class
@implementation B4HVINScannerProcessor

@synthesize plugin = _plugin;
@synthesize callback = _callback;
@synthesize parentViewController = __parentViewController;
@synthesize scannerViewController = _viewController;
@synthesize alternateXib = _alternateXib;

- (id)initWithPlugin:(B4HVINScannerPluginClass*)plugin callback:(NSString*)callback parentViewController:(UIViewController*)parentViewController alterateOverlayXib:(NSString *)alternateXib
{
    self = [super init];
    if (!self) return self;
    self.plugin = plugin;
    self.callback = callback;
    self.parentViewController = parentViewController;
    self.alternateXib = alternateXib;
    return self;
}

- (void)dealloc
{
    self.plugin = nil;
    self.callback = nil;
    self.parentViewController = nil;
    self.scannerViewController = nil;
    self.alternateXib = nil;
    
    [super dealloc];
}

-(void) loadView
{
	[super loadView];
}

- (void)scanBarcode
{
    NSString* errorMessage = [self checkLibraryStatus];
    if (errorMessage)
	{
        [self barcodeScanFailed:errorMessage];
        return;
    }
	[self loadView];//to init camera
    self.scannerViewController = [[[B4HVINScannerViewController alloc] initWithProcessor: self alternateOverlay:self.alternateXib] autorelease];
	[self setOverlay:self.scannerViewController];
	[self setDelegate:self.scannerViewController];
    
    [self performSelector:@selector(openScannerView) withObject:nil afterDelay:1];
}

- (void)openScannerView
{
    [self.parentViewController presentModalViewController:self.scannerViewController animated:YES];
}

- (void)barcodeScanDone
{
	[self stopScanning];
    [self.parentViewController dismissModalViewControllerAnimated: NO];
    
    [self performSelector:@selector(release) withObject:nil afterDelay:1];
}

- (void)barcodeScanSucceeded:(NSString*)text
{
    [self barcodeScanDone];
    [self.plugin returnSuccess:text cancelled:FALSE callback:self.callback];
}

- (void)barcodeScanFailed:(NSString*)message
{
    [self barcodeScanDone];
    [self.plugin returnError:message callback:self.callback];
}

- (void)barcodeScanCancelled
{
    [self barcodeScanDone];
    [self.plugin returnSuccess:@"" cancelled:TRUE callback:self.callback];
}

- (NSString*)checkLibraryStatus
{
	B4HScannerLibraryStatus scannerStatus = [self CheckReadyStatus];
	
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

//scanner view controller
@implementation B4HVINScannerViewController
@synthesize processor = _processor;
@synthesize alternateXib = _alternateXib;
@synthesize overlayView = _overlayView;
@synthesize ledButton = _ledButton;

- (id)initWithProcessor:(B4HVINScannerProcessor*)processor alternateOverlay:(NSString *)alternateXib
{
    self = [super init];
    if (!self) return self;
	
    self.processor = processor;
    self.alternateXib = alternateXib;
    self.overlayView = nil;
	[self setWantsFullScreenLayout:YES];
    return self;
}

- (void)dealloc
{
    self.view = nil;
    self.processor = nil;
    self.alternateXib = nil;
    self.overlayView = nil;
	self.ledButton = nil;
    [super dealloc];
}

-(void) scanner:(B4HScannerController *)scanner gotCode:(NSString *)code//scanner delegate method, won't be called if library detects that eval mode is over
{
	B4HScannerLibraryStatus scannerStatus = [self.processor CheckReadyStatus];
	if (scannerStatus == B4HState_ScanLimitReached) //for dismissing UI only, otherwise camera will just stop
	{
		[self.processor performSelector:@selector(barcodeScanFailed:) withObject:@"Scanner has reached its limit for VIN code recognizing in evaluation mode" afterDelay:0];
		return;
	}
	[self.processor performSelector:@selector(barcodeScanSucceeded:) withObject:code afterDelay:0];
}

- (void)loadView
{
    self.view = [[[UIView alloc] initWithFrame: self.processor.parentViewController.view.frame] autorelease];
	[self.view addSubview:self.processor.view];
    [self.view addSubview:[self buildOverlayView]];
}

-(void) viewWillAppear:(BOOL)animated
{
	[super viewWillAppear:animated];
	[self.processor startScanning];
}

- (void)viewDidAppear:(BOOL)animated
{
	//[self.processor startScanning];
    [super viewDidAppear:animated];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (UIInterfaceOrientationIsLandscape(interfaceOrientation));
}

- (NSUInteger)supportedInterfaceOrientations
{
    return (1 << UIInterfaceOrientationLandscapeLeft)|(1 << UIInterfaceOrientationLandscapeRight);
}

- (IBAction)cancelButtonPressed:(id)sender
{
    [self.processor performSelector:@selector(barcodeScanCancelled) withObject:nil afterDelay:0];
}

- (void)ledButtonClick
{
	NSError *error;
	
	AVCaptureDevice *capDevice = [AVCaptureDevice defaultDeviceWithMediaType: AVMediaTypeVideo];
	if (capDevice.hasTorch)
	{
		[capDevice lockForConfiguration: &error];
		if (capDevice.torchMode == AVCaptureTorchModeOff && [capDevice isTorchModeSupported: AVCaptureTorchModeOn])
		{
			capDevice.torchMode = AVCaptureTorchModeOn;
			[self.ledButton setSelected: YES];
		}
		else
		{
			capDevice.torchMode = AVCaptureTorchModeOff;
			[self.ledButton setSelected: NO];
		}
		[capDevice unlockForConfiguration];
	}
}

- (UIView *)buildOverlayViewFromXib
{
    [[NSBundle mainBundle] loadNibNamed:self.alternateXib owner:self options:NULL];
    
    if ( self.overlayView == nil )
    {
        NSLog(@"%@", @"An error occurred loading the overlay xib. Check overlayView outlet.");
        return nil;
    }
	
    self.overlayView.autoresizesSubviews = YES;
    self.overlayView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    self.overlayView.opaque = NO;
	[self.overlayView setBackgroundColor:[UIColor clearColor]];
    CGRect bounds = self.view.bounds;
    bounds = CGRectMake(0, 0, bounds.size.width, bounds.size.height);
	[self.overlayView setFrame:bounds];
    
    return self.overlayView;
}

- (UIView*)buildOverlayView
{
    if ( nil != self.alternateXib )
    {
        return [self buildOverlayViewFromXib];
    }
	//just camera with toolbar
    CGRect bounds = self.view.bounds;
    bounds = CGRectMake(0, 0, bounds.size.width, bounds.size.height);
    
    UIView* overlayView = [[[UIView alloc] initWithFrame:bounds] autorelease];
    overlayView.autoresizesSubviews = YES;
    overlayView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    overlayView.opaque = NO;
	[overlayView setBackgroundColor:[UIColor clearColor]];
    
    UIToolbar* toolbar = [[[UIToolbar alloc] init] autorelease];
    toolbar.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleTopMargin;
    
    id cancelButton = [[[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemCancel target:(id)self action:@selector(cancelButtonPressed:)] autorelease];
    
    id flexSpace = [[[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil] autorelease];
    
    toolbar.items = [NSArray arrayWithObjects:flexSpace,cancelButton,flexSpace,nil];
    bounds = overlayView.bounds;
    
    [toolbar sizeToFit];
    CGFloat toolbarHeight  = [toolbar frame].size.height;
    CGFloat rootViewHeight = CGRectGetHeight(bounds);
    CGFloat rootViewWidth  = CGRectGetWidth(bounds);
    CGRect  rectArea       = CGRectMake(0, rootViewHeight - toolbarHeight, rootViewWidth, toolbarHeight);
    [toolbar setFrame:rectArea];
    
    [overlayView addSubview: toolbar];
    return overlayView;
}

@end

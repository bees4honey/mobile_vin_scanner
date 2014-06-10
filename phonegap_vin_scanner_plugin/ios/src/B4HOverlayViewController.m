//
//  B4HOverlayViewController.m
//	ScannerLibrary
//
//	Created by bees4honey developer.
//	Copyright (c) 2014 bees4honey. All rights reserved.
//

#import "B4HOverlayViewController.h"

@interface B4HOverlayViewController ()
@end

@implementation B4HOverlayViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    [self.view setBackgroundColor:[UIColor clearColor]];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)cancelButtonPressed:(id)sender
{
    [self.delegate barcodeScanCancelled];
}

- (IBAction)orientationButtonPressed:(id)sender
{
    if (self.parentScanner.scanOrientation == B4HOrientationVertical) {
        // set horizontal scan orientation
        self.parentScanner.scanOrientation = B4HOrientationHorizontal;
        self.background.image = [UIImage imageNamed:@"demoBackgroundHorizontal.png"];
    } else {
        // set vertical scan orientation
        self.parentScanner.scanOrientation = B4HOrientationVertical;
        self.background.image = [UIImage imageNamed:@"demoBackgroundVertical.png"];
    }
}

- (IBAction)ledButtonPressed:(id)sender
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

- (void)scanner:(B4HScannerController *)scanner gotCode:(NSString *)code
{
    B4HScannerLibraryStatus scannerStatus = [self.parentScanner CheckReadyStatus];
	if (scannerStatus == B4HState_ScanLimitReached) {
        //for dismissing UI only, otherwise camera will just stop
		[self.delegate barcodeScanFailed:@"Scanner has reached its limit for VIN code recognizing in evaluation mode"];
		return;
	}
	[self.delegate barcodeScanSucceeded:code];
}

@end
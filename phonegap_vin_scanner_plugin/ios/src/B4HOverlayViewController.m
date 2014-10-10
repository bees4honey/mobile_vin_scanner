//
//  B4HOverlayViewController.m
//  VinScannerDemo
//
//	Created by bee4honey developer.
//	Copyright (c) 2012 bees4honey. All rights reserved.
//

#import "B4HOverlayViewController.h"
#import "B4HScannerController.h"

@implementation B4HOverlayViewController

- (id)initWithNibName: (NSString *) nibNameOrNil bundle: (NSBundle *) nibBundleOrNil {
	self = [super initWithNibName: nibNameOrNil bundle: nibBundleOrNil];
	if (self) {
		// Custom initialization
	}
	return self;
}

- (void)didReceiveMemoryWarning {
	// Releases the view if it doesn't have a superview.
	[super didReceiveMemoryWarning];
	// Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle

- (void)viewDidLoad {
	[super viewDidLoad];
	//
	// Do any additional setup after loading the view from its nib.
	if ([self.parentScanner isRunning]) {
		[startButton setTitle: @"start" forState: UIControlStateNormal];
	}
	else {
		[startButton setTitle: @"stop" forState: UIControlStateNormal];
	}
   	
	if ([AVCaptureDevice defaultDeviceWithMediaType: AVMediaTypeVideo].hasTorch)
		ledButton.hidden = NO;
}
- (void)viewWillAppear: (BOOL) animated {
	[super viewWillAppear:animated];
	codeLabel.hidden = YES;
}

-(BOOL) shouldAutorotate
{
    return YES;
}

-(NSUInteger)supportedInterfaceOrientations
{
    return UIInterfaceOrientationMaskAll;
}

- (BOOL)shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) interfaceOrientation {
	return YES;
}

- (void)scanner: (B4HScannerController *) scanner gotCode: (NSString *) _code {
    if ([self.parentScanner CheckReadyStatus] == B4HState_ScanLimitReached) {
        //for dismissing UI only, otherwise camera will just stop
        [self.delegate barcodeScanFailed:@"Scanner has reached its limit for VIN code recognizing in evaluation mode"];
        return;
    }
    [self.delegate barcodeScanSucceeded:_code];
}

- (void)ledButtonClick {
	NSError *error;

	AVCaptureDevice *capDevice = [AVCaptureDevice defaultDeviceWithMediaType: AVMediaTypeVideo];
	if (capDevice.hasTorch) {
		[capDevice lockForConfiguration: &error];
		if (capDevice.torchMode == AVCaptureTorchModeOff && [capDevice isTorchModeSupported: AVCaptureTorchModeOn]) {
			capDevice.torchMode = AVCaptureTorchModeOn;
			[ledButton setSelected: YES];
		}
		else {
			capDevice.torchMode = AVCaptureTorchModeOff;
			[ledButton setSelected: NO];
		}
		[capDevice unlockForConfiguration];
	}
}

- (void)toggleScan {
	NSLog(@"%d", [self.parentScanner CheckReadyStatus]);
	if ([self.parentScanner isRunning]) {
		[self.parentScanner stopScanning];
		[startButton setTitle: @"start" forState: UIControlStateNormal];
	}
	else {
		[self.parentScanner startScanningWithOrientation:B4HScannerHorizontal];
        
        [self setBackgroundForScanOrientation:B4HScannerHorizontal];
        
		[startButton setTitle: @"stop" forState: UIControlStateNormal];
	}
	pauseLabel.hidden = !pauseLabel.hidden;
}

- (void)cancelScan {
    [self.parentScanner dismissViewControllerAnimated:YES completion:nil];
    [self.delegate barcodeScanCancelled];
}

- (void)hideLabel {
	codeLabel.hidden = YES;
}

- (void)viewDidLayoutSubviews
{
    [super viewDidLayoutSubviews];
    B4HScannerOrientation scanOrientation = self.parentScanner.scanOrientation;
    [self setBackgroundForScanOrientation:scanOrientation];
}

- (void) changeScanOrientation
{
    if (self.parentScanner.scanOrientation == B4HScannerVertical) {
        // set horizontal scan orientation
        self.parentScanner.scanOrientation = B4HScannerHorizontal;
    } else {
        // set vertical scan orientation
        self.parentScanner.scanOrientation = B4HScannerVertical;
    }
    
    [self setBackgroundForScanOrientation:self.parentScanner.scanOrientation];
}

- (void)setBackgroundForScanOrientation:(B4HScannerOrientation)scanOrientation {
    UIInterfaceOrientation orientation = [UIApplication sharedApplication].statusBarOrientation;
    BOOL isLandscape =  UIDeviceOrientationIsLandscape(orientation);
    
    if (scanOrientation == B4HScannerHorizontal) {
        if (isLandscape) {
            background.image = [UIImage imageNamed:@"backgroundLandscapeHorizontal.png"];
        } else {
            background.image = [UIImage imageNamed:@"backgroundPortraitHorizontal.png"];
        }
    } else {
        if (isLandscape) {
            background.image = [UIImage imageNamed:@"backgroundLandscapeVertical.png"];
        } else {
            background.image = [UIImage imageNamed:@"backgroundPortraitVertical.png"];
        }
    }
}

@end
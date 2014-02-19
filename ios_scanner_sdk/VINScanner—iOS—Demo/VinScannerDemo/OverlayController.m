//
//  OverlayController.m
//  VinScannerDemo
//
//	Created by bee4honey developer.
//	Copyright (c) 2012 bees4honey. All rights reserved.
//

#import "OverlayController.h"
#import "B4HScannerController.h"

@implementation OverlayController

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
	codeLabel.text = _code;
	codeLabel.hidden = NO;
	[NSObject cancelPreviousPerformRequestsWithTarget: self selector: @selector(hideLabel) object: nil];
	// Remove scanned VIN code from screen after 3 seconds
	[self performSelector: @selector(hideLabel) withObject: nil afterDelay: 3];
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
		[self.parentScanner startScanningWithOrientation:B4HOrientationHorizontal];
        background.image = [UIImage imageNamed:@"demoBackgroundHorizontal.png"];
		[startButton setTitle: @"stop" forState: UIControlStateNormal];
	}
	pauseLabel.hidden = !pauseLabel.hidden;
}

- (void)cancelScan {
	[self.parentScanner dismissModalViewControllerAnimated: YES];
}

- (void)hideLabel {
	codeLabel.hidden = YES;
}

- (void) changeScanOrientation
{   
    if (self.parentScanner.scanOrientation == B4HOrientationVertical) {
        // set horizontal scan orientation
        self.parentScanner.scanOrientation = B4HOrientationHorizontal;
        background.image = [UIImage imageNamed:@"demoBackgroundHorizontal.png"];
    } else {
        // set vertical scan orientation
        self.parentScanner.scanOrientation = B4HOrientationVertical;
        background.image = [UIImage imageNamed:@"demoBackgroundVertical.png"];
    }
    
}
@end
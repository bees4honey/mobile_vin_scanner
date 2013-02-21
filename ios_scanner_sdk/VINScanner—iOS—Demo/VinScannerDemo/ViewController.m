//
//  ViewController.m
//  VinScannerDemo
//
//	Created by bee4honey developer.
//	Copyright (c) 2012 bees4honey. All rights reserved.
//

#import "ViewController.h"
#import "B4HScannerController.h"

@implementation ViewController

@synthesize pickerController;

- (void)didReceiveMemoryWarning {
	[super didReceiveMemoryWarning];
	// Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle

- (void)viewDidLoad {
	[super viewDidLoad];
}

- (void)dealloc{
	self.pickerController = nil;
	[super dealloc];
}

-(BOOL) shouldAutorotate
{
    return YES;
}

-(NSUInteger) supportedInterfaceOrientations
{   // UIInterfaceOrientationMaskLandscape
    return UIInterfaceOrientationMaskAll;
//    return (1 << UIInterfaceOrientationLandscapeLeft)|(1 << UIInterfaceOrientationLandscapeRight);
}

- (BOOL)shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) interfaceOrientation {
	return YES;
}

#pragma mark Event Methods
- (void)continousScanButtonClick {
	// create B4HScannerController object
	self.pickerController = [[[B4HScannerController alloc] init] autorelease];
	OverlayController *overlayController = [[OverlayController alloc] initWithNibName: @"OverlayController" bundle: nil];
	// set overlay and delegate view controllers
	[self.pickerController setOverlay: overlayController];
	[self.pickerController setDelegate: overlayController];
	// present B4HScannerController modally
	[self presentModalViewController: self.pickerController animated: TRUE];
	[overlayController release];
}

- (void)scanOneButtonClick {
	// create B4HScannerController object
	self.pickerController = [[[B4HScannerController alloc] init] autorelease];
	NSLog(@"%d", [self.pickerController CheckReadyStatus]);
	[self.pickerController setDelegate: self];
	// overaly isn't necessary for B4HScannerController
	[self presentModalViewController: self.pickerController animated: TRUE];
}

- (void)scanner: (B4HScannerController *) scanner gotCode: (NSString *) code {
	vincode.text = code;
	[scanner dismissModalViewControllerAnimated: YES];
}

@end
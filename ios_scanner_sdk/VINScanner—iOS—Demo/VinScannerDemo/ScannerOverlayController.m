//
//  ScannerOverlayControllerViewController.m
//  VinScannerDemo
//
//  Created by Борис Окурин on 3/26/13.
//
//

#import "ScannerOverlayController.h"

@interface ScannerOverlayController ()

@end

@implementation ScannerOverlayController

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
	// Do any additional setup after loading the view.
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

// 
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return YES;
//    return UIInterfaceOrientationIsLandscape(interfaceOrientation);
}

-(NSUInteger) supportedInterfaceOrientations
{
    return UIInterfaceOrientationMaskAll;
//    return UIInterfaceOrientationMaskLandscape;
}

-(BOOL) shouldAutorotate
{
    return YES;
}
@end

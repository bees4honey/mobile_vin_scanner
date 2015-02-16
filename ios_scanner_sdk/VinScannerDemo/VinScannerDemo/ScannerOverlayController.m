//
//  ScannerOverlayControllerViewController.m
//  VinScannerDemo
//
//  Created by bees4honey developer.
//  Copyright (c) 2015 bees4honey. All rights reserved.
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

-(NSUInteger) supportedInterfaceOrientations
{
    return UIInterfaceOrientationMaskAll;
}

-(BOOL) shouldAutorotate
{
    return YES;
}
@end

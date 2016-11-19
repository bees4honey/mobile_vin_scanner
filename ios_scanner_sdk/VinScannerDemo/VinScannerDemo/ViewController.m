//
//  ViewController.m
//  VinScannerDemo
//
//  Created by bees4honey developer.
//  Copyright (c) 2015 bees4honey. All rights reserved.
//

#import "ViewController.h"
#import "B4HScannerController.h"
#import "ScannerOverlayController.h"

@interface ViewController ()

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskPortrait;
}

-(BOOL) shouldAutorotate {
    return YES;
}

#pragma mark Event Methods
- (void)scanButtonClicked {
    // create custom ScannerOverlayController to use different interface orientation
    self.pickerController = [[ScannerOverlayController alloc] init];
    OverlayController *overlayController = [[OverlayController alloc] initWithNibName: @"OverlayController" bundle: nil];
    // set overlay and delegate view controllers
    [self.pickerController setOverlay: overlayController];
    [self.pickerController setDelegate: self];
    // present B4HScannerController modally
    [self presentViewController:self.pickerController animated:YES completion:nil];
}

- (void)scanner: (B4HScannerController *) scanner gotCode: (NSString *) code {
    self.vincode.text = code;
    [scanner dismissViewControllerAnimated:YES completion:nil];
}

@end

//
//  OverlayController.m
//  VinScannerDemo
//
//  Created by bees4honey developer.
//  Copyright (c) 2015 bees4honey. All rights reserved.
//

#import "OverlayController.h"

@interface OverlayController ()

@end

@implementation OverlayController

- (void)viewDidLoad {
    [super viewDidLoad];

    if ([self.parentScanner isRunning]) {
        [self.startButton setTitle: @"start" forState: UIControlStateNormal];
    }
    else {
        [self.startButton setTitle: @"stop" forState: UIControlStateNormal];
    }
   	
    if ([AVCaptureDevice defaultDeviceWithMediaType: AVMediaTypeVideo].hasTorch)
        self.ledButton.hidden = NO;
}

- (void)viewWillAppear: (BOOL) animated {
    [super viewWillAppear:animated];
    self.codeLabel.hidden = YES;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)scanner: (B4HScannerController *) scanner gotCode: (NSString *) scannedCode {
    NSLog(@"Scanned code: %@", scannedCode);
    self.codeLabel.text = scannedCode;
    self.codeLabel.hidden = NO;
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
            [self.ledButton setSelected: YES];
        }
        else {
            capDevice.torchMode = AVCaptureTorchModeOff;
            [self.ledButton setSelected: NO];
        }
        [capDevice unlockForConfiguration];
    }
}

- (void)toggleScan {
    NSLog(@"%d", [self.parentScanner CheckReadyStatus]);
    if ([self.parentScanner isRunning]) {
        [self.parentScanner stopScanning];
        [self.startButton setTitle: @"start" forState: UIControlStateNormal];
    }
    else {
        [self.parentScanner startScanningWithOrientation:B4HScannerHorizontal];
        
        [self setBackgroundForScanOrientation:B4HScannerHorizontal];
        
        [self.startButton setTitle: @"stop" forState: UIControlStateNormal];
    }
    self.pauseLabel.hidden = !self.pauseLabel.hidden;
}

- (void)cancelScan {
    [self.parentScanner dismissViewControllerAnimated:YES completion:nil];
}

- (void)hideLabel {
    self.codeLabel.hidden = YES;
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
    BOOL isLandscape = ( (orientation) == UIInterfaceOrientationLandscapeLeft ||
                        (orientation) == UIInterfaceOrientationLandscapeRight);
    
    if (scanOrientation == B4HScannerHorizontal) {
        if (isLandscape) {
            self.background.image = [UIImage imageNamed:@"backgroundLandscapeHorizontal.png"];
        } else {
            self.background.image = [UIImage imageNamed:@"backgroundPortraitHorizontal.png"];
        }
    } else {
        if (isLandscape) {
            self.background.image = [UIImage imageNamed:@"backgroundLandscapeVertical.png"];
        } else {
            self.background.image = [UIImage imageNamed:@"backgroundPortraitVertical.png"];
        }
    }
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end

//
//  OverlayController.h
//  VinScannerDemo
//
//  Created by bees4honey developer.
//  Copyright (c) 2015 bees4honey. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "B4HScannerController.h"

@interface OverlayController : B4HCameraOverlayViewController <B4HScannerDelegate>

@property (strong, nonatomic) IBOutlet UIButton *ledButton;
@property (strong, nonatomic) IBOutlet UIButton *startButton;
@property (strong, nonatomic) IBOutlet UIImageView *background;
@property (strong, nonatomic) NSString *code;
@property (strong, nonatomic) IBOutlet UILabel *codeLabel;
@property (strong, nonatomic) IBOutlet UILabel *pauseLabel;

//	Turn on/off torchlight if avaliable
- (IBAction)ledButtonClick;
//	Stop/start scanning process and camera capture
- (IBAction)toggleScan;
//	Go to previous screen
- (IBAction)cancelScan;

// Change the orientation used to search 1D barcodes:
- (IBAction)changeScanOrientation;
@end

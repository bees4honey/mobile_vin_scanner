//
//  OverlayController.h
//  VinScannerDemo
//
//	Created by bee4honey developer.
//	Copyright (c) 2012 bees4honey. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "B4HScannerController.h"

@interface OverlayController : B4HCameraOverlayViewController <B4HScannerDelegate> {
	IBOutlet UIButton *ledButton;
	IBOutlet UIButton *startButton;
	IBOutlet UIImageView *background;
	NSString *code;
	IBOutlet UILabel *codeLabel;
	IBOutlet UILabel *pauseLabel;
}
//	Turn on/off torchlight if avaliable
- (IBAction)ledButtonClick;
//	Stop/start scanning process and camera capture
- (IBAction)toggleScan;
//	Go to previous screen
- (IBAction)cancelScan;
@end
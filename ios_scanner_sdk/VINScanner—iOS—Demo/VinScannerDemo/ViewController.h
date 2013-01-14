//
//  ViewController.h
//  VinScannerDemo
//
//	Created by bee4honey developer.
//	Copyright (c) 2012 bees4honey. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "B4HScannerController.h"
#import "OverlayController.h"

@interface ViewController : UIViewController <B4HScannerDelegate>
{
	IBOutlet UILabel* vincode;
}
//	VIN code picker
@property (nonatomic, retain) B4HScannerController *pickerController;

//	scan VIN code in continous way 
- (IBAction)continousScanButtonClick;
//	scan just one VIN code 
- (IBAction)scanOneButtonClick;
@end

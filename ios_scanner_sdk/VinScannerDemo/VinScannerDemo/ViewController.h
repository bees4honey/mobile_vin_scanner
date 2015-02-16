//
//  ViewController.h
//  VinScannerDemo
//
//  Created by bees4honey developer.
//  Copyright (c) 2015 bees4honey. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "B4HScannerController.h"
#import "OverlayController.h"

@interface ViewController : UIViewController <B4HScannerDelegate>
@property (strong, nonatomic) IBOutlet UILabel *vincode;
@property (strong, nonatomic) B4HScannerController *pickerController;

- (IBAction)scanButtonClicked;
@end

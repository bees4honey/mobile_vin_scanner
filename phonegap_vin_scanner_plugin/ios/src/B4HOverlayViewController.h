//
//  B4HOverlayViewController.h
//	ScannerLibrary
//
//	Created by bees4honey developer.
//	Copyright (c) 2014 bees4honey. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "B4HScannerController.h"

@protocol B4HOverlayDelegate <NSObject>
- (void)barcodeScanSucceeded:(NSString*)text;
- (void)barcodeScanFailed:(NSString*)message;
- (void)barcodeScanCancelled;
@end

@interface B4HOverlayViewController : B4HCameraOverlayViewController<B4HScannerDelegate>
@property (nonatomic, strong) IBOutlet UIImageView *background;
@property (nonatomic, strong) id<B4HOverlayDelegate> delegate;
@property (nonatomic, strong) IBOutlet UIButton *ledButton;

- (IBAction)cancelButtonPressed:(id)sender;
- (IBAction)orientationButtonPressed:(id)sender;
- (IBAction)ledButtonPressed:(id)sender;
@end

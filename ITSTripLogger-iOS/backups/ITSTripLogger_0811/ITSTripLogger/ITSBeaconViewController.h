//
//  ITSBeaconViewController.h
//  ITSTripLogger
//
//  Created by FANTAISIE on 6/3/14.
//  Copyright (c) 2014 ITS. All rights reserved.
//x

#import <UIKit/UIKit.h>
#import "ITSConnectionManager.h"
#import "ITSBeaconManager.h"

@class ITSConnectionManager;
@class ITSBeaconManager;

@interface ITSBeaconViewController : UIViewController <UITableViewDelegate, UITableViewDataSource, UITextFieldDelegate>{
    ITSConnectionManager *iTSConnectionManager;
    ITSBeaconManager *iTSBeaconManager;
    IBOutlet UITableView *beaconTableView;
    IBOutlet UILabel *thresholdLabel;
    IBOutlet UILabel *toleranceLabel;
    IBOutlet UISlider *thresholdSlider;
    IBOutlet UISlider *toleranceSlider;
    
    IBOutlet UIButton *editButton;
    IBOutlet UIView *changeNameView;
    IBOutlet UILabel *changeNameLabel;
    IBOutlet UITextField *changeNameTextField;
    IBOutlet UIButton *changeNameButton;
    
    BOOL editingMode;
    
    NSString * selectedBeaconName;
    NSString * selectedBeaconUUID;
    NSNumber * selectedBeaconMajor;
    NSNumber * selectedBeaconMinor;
}
@property (strong, nonatomic) IBOutlet UITableView *beaconTableView;
- (IBAction)thresholdSliderChanged:(id)sender;
- (IBAction)thresholdSliderEdited:(id)sender;
- (IBAction)toleranceSliderChanged:(id)sender;
- (IBAction)toleranceSliderEdited:(id)sender;
- (IBAction)changeEditMode:(id)sender;
- (IBAction)InsertNewBeaconName:(id)sender;
- (void) callBackFromInsertCarName:(NSDictionary *)iTSReceivedDict;
- (void) callBackFromShowCarNames:(NSDictionary *)iTSReceivedDict;
- (void)printSomething;
@end

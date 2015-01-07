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

typedef enum CalibrationType : NSInteger {
    TypeNone,
    TypeInCar,
    TypeOffCar
} CalibrationType;

@interface ITSBeaconViewController : UIViewController <UITableViewDelegate, UITableViewDataSource, UITextFieldDelegate>{
    ITSConnectionManager *iTSConnectionManager;
    ITSBeaconManager *iTSBeaconManager;
    IBOutlet UITableView *beaconTableView;
    IBOutlet UILabel *thresholdLabel;
    IBOutlet UILabel *toleranceLabel;
    IBOutlet UISlider *thresholdSlider;
    IBOutlet UISlider *toleranceSlider;
    
    IBOutlet UIButton *editButton;
    
    IBOutlet UIView *defaultView;
    
    IBOutlet UIView *changeNameView;
    IBOutlet UILabel *changeNameLabel;
    IBOutlet UITextField *changeNameTextField;
    IBOutlet UIButton *changeNameButton;
    
    IBOutlet UIButton *changeAutoButton;
    IBOutlet UIView *changeAutoView;
    IBOutlet UIButton *inCarButton;
    IBOutlet UIButton *offCarButton;
    IBOutlet UILabel *messageLabel;
    IBOutlet UIButton *resetButton;
    
    IBOutlet UIButton *changeManualButton;
    IBOutlet UIView *changeManualView;
    IBOutlet UILabel *beaconThresholdLabel;
    IBOutlet UILabel *beaconToleranceLabel;
    IBOutlet UISlider *beaconThresholdSlider;
    IBOutlet UISlider *beaconToleranceSlider;
    
    BOOL editingMode;
    BOOL calibrationMode;
    NSInteger calibrationSubMode;
    
    NSString * selectedBeaconName;
    NSString * selectedBeaconUUID;
    NSNumber * selectedBeaconMajor;
    NSNumber * selectedBeaconMinor;
    NSInteger selectedBeaconThreshold;
    NSInteger selectedBeaconTolerance;
    
    NSInteger newRssiThresholdValue;
    NSInteger newRssiToleranceValue;
    NSInteger tempHighValue;
    NSInteger tempLowValue;
}
@property (strong, nonatomic) IBOutlet UITableView *beaconTableView;
- (IBAction)thresholdSliderChanged:(id)sender;
- (IBAction)thresholdSliderEdited:(id)sender;
- (IBAction)toleranceSliderChanged:(id)sender;
- (IBAction)toleranceSliderEdited:(id)sender;
- (IBAction)changeEditMode:(id)sender;
- (IBAction)UpdateBeacon:(id)sender;

- (IBAction)changeAutoButtonClicked:(id)sender;
- (IBAction)inCarButtonClicked:(id)sender;
- (IBAction)offCarButtonClicked:(id)sender;
- (IBAction)resetButtonClicked:(id)sender;
- (void)returnTestBeaconStep:(int)testBeaconCount;
- (void)returnTestBeaconValue:(NSInteger)testBeaconRssiValue;

- (IBAction)changeManualButtonClicked:(id)sender;
- (IBAction)beaconThresholdSliderChanged:(id)sender;
- (IBAction)beaconThresholdSliderEdited:(id)sender;
- (IBAction)beaconToleranceSliderChanged:(id)sender;
- (IBAction)beaconToleranceSliderEdited:(id)sender;

- (void) callBackFromInsertCarName:(NSDictionary *)iTSReceivedDict;
- (void) callBackFromShowCarNames:(NSDictionary *)iTSReceivedDict;
- (void)printSomething;
@end

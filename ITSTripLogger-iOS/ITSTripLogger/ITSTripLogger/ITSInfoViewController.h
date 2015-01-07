//
//  ITSInfoViewController.h
//  ITSTripLogger
//
//  Created by FANTAISIE on 7/16/14.
//  Copyright (c) 2014 ITS. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ITSConnectionManager.h"
#import "ITSBeaconManager.h"

@interface ITSInfoViewController : UIViewController {
    ITSConnectionManager *iTSConnectionManager;
    ITSBeaconManager *iTSBeaconManager;
    
    IBOutlet UILabel * userIdLabel;
    IBOutlet UILabel * userNameLabel;
    IBOutlet UILabel * emailLabel;
    IBOutlet UILabel * lnameLabel;
    IBOutlet UILabel * fnameLabel;
    IBOutlet UILabel * householdIdLabel;
    IBOutlet UILabel * householdNameLabel;
    IBOutlet UILabel * registerTimeLabel;
    IBOutlet UISwitch *locationServiceSwitch;
}
- (IBAction)locationServiceSwitchValueChanged:(id)sender;

@end

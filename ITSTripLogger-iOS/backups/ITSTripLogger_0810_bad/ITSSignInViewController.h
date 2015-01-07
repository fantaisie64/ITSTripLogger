//
//  ITSSignInViewController.h
//  ITSTripLogger
//
//  Created by FANTAISIE on 6/3/14.
//  Copyright (c) 2014 ITS. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ITSDataManager.h"
#import "ITSConnectionManager.h"
#import "ITSBeaconManager.h"
#import "ITSRecordViewController.h"
#import "ITSBeaconViewController.h"

@interface ITSSignInViewController : UIViewController <UINavigationControllerDelegate, UITextFieldDelegate>{
    UITabBarController * mainTabBarViewController;
    ITSDataManager *iTSDataManager;
    ITSConnectionManager *iTSConnectionManager;
    ITSBeaconManager *iTSBeaconManager;
    BOOL signedIn;
    IBOutlet UIButton *signInButton;
    IBOutlet UIButton *signUpButton;
    IBOutlet UITextField * userNameTextField;
    IBOutlet UITextField * passwordTextField;
    //NSString *userNameText;
    //NSString *passwordText;
    IBOutlet UILabel *messageLabel;
    NSMutableDictionary *userPlistDict;
}
@property (strong, nonatomic) UITabBarController * mainTabBarViewController;
- (IBAction)signInButtonClicked:(id)sender;
- (IBAction)signUpButtonClicked:(id)sender;
- (void) connectionTimerTimedOut;
- (void) callBackFromConnection:(NSDictionary *)iTSReceivedDict;
- (void) callBackFromSignin:(NSDictionary *)iTSReceivedDict;
@end

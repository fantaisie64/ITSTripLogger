//
//  ITSConnectionManager.h
//  ITSTripLogger
//
//  Created by FANTAISIE on 7/9/14.
//  Copyright (c) 2014 ITS. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ITSBeaconManager.h"
#import "ITSSignInViewController.h"
#import "ITSRecordViewController.h"
#import "ITSBeaconViewController.h"
#import "ITSRecordData.h"

@class ITSBeaconManager;
@class ITSSignInViewController;
@class ITSRecordViewController;
@class ITSBeaconViewController;

@interface ITSConnectionManager : NSObject <NSURLConnectionDelegate> {
    ITSBeaconManager *iTSBeaconManager;
    ITSSignInViewController *iTSSignInDelegate;
    ITSRecordViewController *iTSRecordDelegate;
    ITSBeaconViewController *iTSBeaconDelegate;
    //NSString * iTSSessionToken;
    NSTimer * connectionTimer;
    //BOOL connectionSucceed;
    
    NSString * user_id;
    NSString * user_name;
    NSString * user_email;
    NSString * user_lname;
    NSString * user_fname;
    NSString * user_household_id;
    NSString * user_household_name;
    NSString * user_timestamp;
    
    NSMutableDictionary * storedRecordData;
}
@property (strong, nonatomic) ITSBeaconManager *iTSBeaconManager;
@property (strong, nonatomic) ITSSignInViewController *iTSSignInDelegate;
@property (strong, nonatomic) ITSRecordViewController *iTSRecordDelegate;
@property (strong, nonatomic) ITSBeaconViewController *iTSBeaconDelegate;
@property (strong, nonatomic) NSString * iTSSessionToken;
//@property BOOL connectionSucceed;

@property (strong, nonatomic) NSString * user_id;
@property (strong, nonatomic) NSString * user_name;
@property (strong, nonatomic) NSString * user_email;
@property (strong, nonatomic) NSString * user_lname;
@property (strong, nonatomic) NSString * user_fname;
@property (strong, nonatomic) NSString * user_household_id;
@property (strong, nonatomic) NSString * user_household_name;
@property (strong, nonatomic) NSString * user_timestamp;

+(ITSConnectionManager *)getInstance;
-(void) connectToServer;
-(void) verifyUser:(NSString *)userName withPassword:(NSString *)userPassword;
-(void) setUserData:(NSDictionary *)userDataDict;
-(void) clearUserData;
-(void) clearRecordData;
-(void) uploadRecordData:(ITSRecordData*)recordData;
-(void) uploadStoredRecordData:(ITSRecordData*)recordData;
-(void) getRecordData;
-(void) insertCarName:(NSString *)selectedBeaconName UUID:(NSString *)selectedBeaconUUID Major:(NSNumber *)selectedBeaconMajor Minor:(NSNumber *)selectedBeaconMinor;
-(void) showCarNames;
-(void)loadRecordPlist;
-(void) clearRecordPlist;
-(void) saveRecordPlist;
-(void) printSomething;
@end

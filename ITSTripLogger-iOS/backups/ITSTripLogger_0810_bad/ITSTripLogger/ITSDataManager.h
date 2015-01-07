//
//  ITSDataManager.h
//  ITSTripLogger
//
//  Created by FANTAISIE on 8/11/14.
//  Copyright (c) 2014 ITS. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ITSConnectionManager.h"
#import "ITSBeaconManager.h"
#import "ITSSignInViewController.h"
#import "ITSRecordViewController.h"
#import "ITSBeaconViewController.h"

@class ITSConnectionManager;
@class ITSBeaconManager;
@class ITSSignInViewController;
@class ITSRecordViewController;
@class ITSBeaconViewController;

@interface ITSDataManager : NSObject {
    ITSConnectionManager    *iTSConnectionManager;
    ITSBeaconManager        *iTSBeaconManager;
    ITSSignInViewController *iTSSignInDelegate;
    ITSRecordViewController *iTSRecordDelegate;
    ITSBeaconViewController *iTSBeaconDelegate;
    
    //connectionManager
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

+(ITSDataManager *)getInstance;

@property (strong, nonatomic) ITSConnectionManager *iTSConnectionManager;
@property (strong, nonatomic) ITSBeaconManager *iTSBeaconManager;
@property (strong, nonatomic) ITSSignInViewController *iTSSignInDelegate;
@property (strong, nonatomic) ITSRecordViewController *iTSRecordDelegate;
@property (strong, nonatomic) ITSBeaconViewController *iTSBeaconDelegate;

//connectionManager
@property (strong, nonatomic) NSString * user_id;
@property (strong, nonatomic) NSString * user_name;
@property (strong, nonatomic) NSString * user_email;
@property (strong, nonatomic) NSString * user_lname;
@property (strong, nonatomic) NSString * user_fname;
@property (strong, nonatomic) NSString * user_household_id;
@property (strong, nonatomic) NSString * user_household_name;
@property (strong, nonatomic) NSString * user_timestamp;
@property (strong, nonatomic) NSMutableDictionary * storedRecordData;

//connectionManager
-(void) setUserData:(NSDictionary *)userDataDict;
-(void) clearUserData;
-(void) clearRecordData;
-(void)loadRecordPlist;
-(void) clearRecordPlist;
-(void) saveRecordPlist;
-(void) printSomething;
@end

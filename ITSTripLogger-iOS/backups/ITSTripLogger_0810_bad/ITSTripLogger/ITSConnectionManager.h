//
//  ITSConnectionManager.h
//  ITSTripLogger
//
//  Created by FANTAISIE on 7/9/14.
//  Copyright (c) 2014 ITS. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ITSDataManager.h"
#import "ITSBeaconManager.h"
#import "ITSSignInViewController.h"
#import "ITSRecordViewController.h"
#import "ITSBeaconViewController.h"
#import "ITSRecordData.h"

@class ITSDataManager;
@class ITSBeaconManager;
@class ITSSignInViewController;
@class ITSRecordViewController;
@class ITSBeaconViewController;

@interface ITSConnectionManager : NSObject <NSURLConnectionDelegate> {
    ITSDataManager *iTSDataManager;
    ITSBeaconManager *iTSBeaconManager;
    ITSSignInViewController *iTSSignInDelegate;
    ITSRecordViewController *iTSRecordDelegate;
    ITSBeaconViewController *iTSBeaconDelegate;
    //NSString * iTSSessionToken;
    NSTimer * connectionTimer;
    //BOOL connectionSucceed;
    
    
}
@property (strong, nonatomic) ITSDataManager *iTSDataManager;
@property (strong, nonatomic) ITSBeaconManager *iTSBeaconManager;
@property (strong, nonatomic) ITSSignInViewController *iTSSignInDelegate;
@property (strong, nonatomic) ITSRecordViewController *iTSRecordDelegate;
@property (strong, nonatomic) ITSBeaconViewController *iTSBeaconDelegate;
@property (strong, nonatomic) NSString * iTSSessionToken;
//@property BOOL connectionSucceed;



+(ITSConnectionManager *)getInstance;
-(void) connectToServer;
-(void) verifyUser:(NSString *)userName withPassword:(NSString *)userPassword;
-(void) uploadRecordData:(ITSRecordData*)recordData;
-(void) uploadStoredRecordData:(ITSRecordData*)recordData;
-(void) getRecordData;
-(void) insertCarName:(NSString *)selectedBeaconName UUID:(NSString *)selectedBeaconUUID Major:(NSNumber *)selectedBeaconMajor Minor:(NSNumber *)selectedBeaconMinor;
-(void) showCarNames;
@end

//
//  ITSBeaconManager.h
//  ITSTripLogger
//
//  Created by FANTAISIE on 6/4/14.
//  Copyright (c) 2014 ITS. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>
#import <UIKit/UIKit.h> 
#import "ITSSignInViewController.h"
#import "ITSBeaconViewController.h"
#import "ITSDataManager.h"
#import "ITSConnectionManager.h"
#import "ITSRecordData.h"

@class ITSDataManager;
@class ITSConnectionManager;
@class ITSSignInViewController;
@class ITSBeaconViewController;

typedef enum UploadType : NSInteger {
    TypeStart,
    TypeEnd
} UploadType;

@interface ITSBeaconManager : NSObject <CLLocationManagerDelegate>{
    ITSDataManager* iTSDataManager;
    ITSConnectionManager* iTSConnectionManager;
    ITSSignInViewController *iTSSignInDelegate;
    ITSBeaconViewController *iTSBeaconDelegate;
    CLBeaconRegion *beaconRegion;
    NSArray *beacons;
    NSArray * iTSCars;
    
    CLLocationManager *locationManager;
    CLLocation *location;
    float currentLatitude;
    float currentLongitude;
    //NSString* currentTime;
    NSDate *now;
    
    NSMutableDictionary *typePlistDict;
    NSMutableDictionary *parameterPlistDict;
    NSMutableDictionary *recordPlistDict;
    
    BOOL gotBeaconBoolean;
    CLBeacon *gotBeacon;
    NSString *gotBeaconUUID;
    NSNumber *gotBeaconMajor;
    NSNumber *gotBeaconMinor;
    NSString *gotBeaconName;
    NSInteger sensingCount;
    NSMutableDictionary *gotBeaconCount;
    NSInteger loseBeaconCount;
    NSInteger rssiThresholdValue;
    NSInteger rssiToleranceValue;
}
@property (strong, nonatomic) ITSDataManager* iTSDataManager;
@property (strong, nonatomic) ITSConnectionManager* iTSConnectionManager;
@property (strong, nonatomic) ITSSignInViewController *iTSSignInDelegate;
@property (strong, nonatomic) ITSBeaconViewController *iTSBeaconDelegate;
@property (strong, nonatomic) NSArray *beacons;
@property (strong, nonatomic) NSArray * iTSCars;
@property (nonatomic) NSInteger rssiThresholdValue;
@property (nonatomic) NSInteger rssiToleranceValue;

+(ITSBeaconManager *)getInstance;
- (void) startMonitoring;
- (void) stopMonitoring;
- (void) uploadRecordDataWithType:(NSInteger)type Rssi:(NSInteger)rssi UUID:(NSString*)uuid Major:(NSNumber*)major Minor:(NSNumber*)minor Name:(NSString*)name;
- (void) callBackFromShowCarNames:(NSDictionary *)iTSReceivedDict;
-(void) loadTypePlist;
-(void) saveTypePlist;
-(void) loadParameterPlist;
-(void) saveParameterPlist;
-(void) loadCarPlist;
-(void) saveCarPlist;
-(void) clearCarData;
@end

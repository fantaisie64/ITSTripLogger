//
//  ITSBeaconManeger.m
//  ITSTripLogger
//
//  Created by FANTAISIE on 6/4/14.
//  Copyright (c) 2014 ITS. All rights reserved.
//

#import "ITSBeaconManager.h"

@implementation ITSBeaconManager


@synthesize iTSSignInDelegate, iTSBeaconDelegate, iTSConnectionManager, beacons, iTSCars;
@synthesize rssiThresholdValue, rssiToleranceValue;
static ITSBeaconManager *instance = nil;

- (id) init
{
    if (self = [super init])
    {
        locationManager = [[CLLocationManager alloc] init];
        locationManager.delegate = self;
        //[self locationManager:locationManager didStartMonitoringForRegion:beaconRegion];
        self.iTSConnectionManager = [ITSConnectionManager getInstance];
        self.iTSConnectionManager.iTSBeaconManager = self;
        [self initRegion];
    }
    return self;
}

+(ITSBeaconManager *)getInstance
{
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        instance = [[ITSBeaconManager alloc] init];
    });
    return instance;
}


- (void)locationManager:(CLLocationManager *)manager didStartMonitoringForRegion:(CLRegion *)region {
    [locationManager startRangingBeaconsInRegion:beaconRegion];
}


- (void)initRegion {
    NSUUID *uuid = [[NSUUID alloc] initWithUUIDString:@"B9407F30-F5F8-466E-AFF9-25556B57FE6D"]; // Estimote
    beaconRegion = [[CLBeaconRegion alloc] initWithProximityUUID:uuid identifier:@"com.devfright.myRegion"];
    beaconRegion.notifyOnEntry = YES;
    beaconRegion.notifyOnExit = YES;
    beaconRegion.notifyEntryStateOnDisplay = YES;
    
    rssiThresholdValue = -76; //default
    rssiToleranceValue = 8; //default
    sensingCount = 5;
    gotBeaconCount = [[NSMutableDictionary alloc] init];
    loseBeaconCount = 0;
    [self loadParameterPlist];
    
    //check last type
    gotBeacon = nil;
    gotBeaconUUID = nil;
    gotBeaconMajor = nil;
    gotBeaconMinor = nil;
    gotBeaconName = @"";
    gotBeaconThresholdValue = rssiThresholdValue;
    gotBeaconToleranceValue = rssiToleranceValue;
    [self loadTypePlist];
    [self saveTypePlist];
    
    iTSCars = [[NSArray alloc] init];
    [self loadCarPlist];
    
    //for testing beacon
    testBeaconMode = NO;
    testBeaconCountThreshold = 5;
    testBeaconCount = 0.0;
    testBeaconRssiValue = 0.0;
}

- (void) startMonitoring {
    NSLog(@"startMonitoring");
    [locationManager  startMonitoringForRegion:beaconRegion];
    [iTSConnectionManager showCarNames];
}

- (void) stopMonitoring {
    [locationManager stopRangingBeaconsInRegion:beaconRegion];
    [locationManager  stopMonitoringForRegion:beaconRegion];
    NSLog(@"stopMonitoring");
}

- (void)locationManager:(CLLocationManager *)manager didEnterRegion:(CLRegion *)region {
    
    /*UILocalNotification* localNotification = [[UILocalNotification alloc] init];
    localNotification.fireDate = [NSDate dateWithTimeIntervalSinceNow:0];
    localNotification.alertBody = [NSString stringWithFormat:@"Beacon Found"];
    localNotification.timeZone = [NSTimeZone defaultTimeZone];
    [[UIApplication sharedApplication] scheduleLocalNotification:localNotification];
    
    NSLog(@"Beacon Found");
    [self.locationManager startRangingBeaconsInRegion:self.beaconRegion];*/
}

-(void)locationManager:(CLLocationManager *)manager didExitRegion:(CLRegion *)region {
    
    /*UILocalNotification* localNotification = [[UILocalNotification alloc] init];
    localNotification.fireDate = [NSDate dateWithTimeIntervalSinceNow:0];
    localNotification.alertBody = [NSString stringWithFormat:@"Left Region"];
    localNotification.timeZone = [NSTimeZone defaultTimeZone];
    [[UIApplication sharedApplication] scheduleLocalNotification:localNotification];
    
    NSLog(@"Left Region");
    [self.locationManager stopRangingBeaconsInRegion:self.beaconRegion];*/
}

- (void)locationManager:(CLLocationManager *)manager didDetermineState:(CLRegionState)state forRegion:(CLRegion *)region
{
    /*NSString *string;
    if(state == CLRegionStateInside) {
        string = [NSString stringWithFormat:@"locationManager didDetermineState INSIDE for %@", region.identifier];
        NSLog(@"locationManager didDetermineState INSIDE for %@", region.identifier);
    }
    else if(state == CLRegionStateOutside) {
        string = [NSString stringWithFormat:@"locationManager didDetermineState OUTSIDE for %@", region.identifier];
        NSLog(@"locationManager didDetermineState OUTSIDE for %@", region.identifier);
    }
    else {
        string = [NSString stringWithFormat:@"locationManager didDetermineState OTHER for %@", region.identifier];
        NSLog(@"locationManager didDetermineState OTHER for %@", region.identifier);
    }
    
    UILocalNotification* localNotification = [[UILocalNotification alloc] init];
    localNotification.fireDate = [NSDate dateWithTimeIntervalSinceNow:0];
    localNotification.alertBody = [NSString stringWithFormat:@"%@", string];
    localNotification.timeZone = [NSTimeZone defaultTimeZone];
    [[UIApplication sharedApplication] scheduleLocalNotification:localNotification];*/
}

-(void)locationManager:(CLLocationManager *)manager didRangeBeacons:(NSArray *)beaconsArray inRegion:(CLBeaconRegion *)region {
    beacons = [beaconsArray sortedArrayUsingComparator:^NSComparisonResult(id obj1, id obj2) {
        return [[(CLBeacon *)obj1 major] integerValue] - [[(CLBeacon *)obj2 major] integerValue];
    }];
    
    if(iTSBeaconDelegate != nil)
        [iTSBeaconDelegate.beaconTableView reloadData];
    
    BOOL gotSpecificBeacon = NO;
    NSInteger tempCount;
    NSString *tempBeaconName = @"";
    NSInteger tempBeaconThresholdValue = rssiThresholdValue;
    NSInteger tempBeaconToleranceValue = rssiToleranceValue;
    
    for (CLBeacon* b in beacons) {
        if(b.rssi!= 0){
            //test beacon
            if(testBeaconMode){
                if(testBeaconMajor != nil && testBeaconMinor != nil &&
                   [testBeaconMajor isEqualToNumber:b.major] && [testBeaconMinor isEqualToNumber:b.minor]){
                    [self returnTestBeaconStep:b.rssi];
                }
            }
            
            tempBeaconName = @"";
            tempBeaconThresholdValue = rssiThresholdValue;
            tempBeaconToleranceValue = rssiToleranceValue;
            if(!gotBeaconBoolean){
                //search beacon
                for(NSDictionary * car in iTSCars){
                    if([b.major isEqualToNumber:[NSNumber numberWithInt:[[car objectForKey:@"bt_major"]  intValue]]]
                       &&
                       [b.minor isEqualToNumber:[NSNumber numberWithInt:[[car objectForKey:@"bt_minor"]  intValue]]]){
                           if([car objectForKey:@"bt_name"] != nil)
                               tempBeaconName = [NSString stringWithFormat:@"%@", [car objectForKey:@"bt_name"]];
                           if([car objectForKey:@"bt_threshold"] != nil)
                               tempBeaconThresholdValue = [[car objectForKey:@"bt_threshold"] integerValue];
                           if([car objectForKey:@"bt_tolerance"] != nil)
                               tempBeaconToleranceValue = [[car objectForKey:@"bt_tolerance"] integerValue];
                           break;
                       }
                }
                if(b.rssi > (tempBeaconThresholdValue+tempBeaconToleranceValue)){
                    gotSpecificBeacon = YES; // always true for getting beacon
                    if([gotBeaconCount objectForKey:b.major] == nil){
                        [gotBeaconCount setObject:[[NSNumber alloc] initWithInteger:1] forKey:b.major];
                        tempCount = 1;
                    }else{
                        tempCount = [[gotBeaconCount objectForKey:b.major] intValue];
                        tempCount += 1;
                    }
                    
                    //check if larger than sensingCount
                    if(tempCount >= sensingCount){
                        gotBeaconBoolean = true;
                        gotBeacon = b;
                        gotBeaconUUID = b.proximityUUID.UUIDString;
                        gotBeaconMajor = b.major;
                        gotBeaconMinor = b.minor;
                        gotBeaconName = [NSString stringWithFormat:@"%@", tempBeaconName];
                        gotBeaconThresholdValue = tempBeaconThresholdValue;
                        gotBeaconToleranceValue = tempBeaconToleranceValue;
                        [self saveTypePlist];
                        if(gotBeaconName != nil && ![gotBeaconName isEqualToString:@""]){
                            NSLog(@"In Car %@", gotBeaconName);
                            if(iTSSignInDelegate != nil && iTSSignInDelegate.mainTabBarViewController != nil)
                                iTSSignInDelegate.mainTabBarViewController.title = [NSString stringWithFormat:@"In %@ Now", gotBeaconName];
                        }else{
                            NSLog(@"In Car %@,%@", gotBeaconMajor, gotBeaconMinor);
                            if(iTSSignInDelegate != nil && iTSSignInDelegate.mainTabBarViewController != nil)
                                iTSSignInDelegate.mainTabBarViewController.title = [NSString stringWithFormat:@"In %@,%@ Now", gotBeaconMajor,gotBeaconMinor];
                        }
                        [self uploadRecordDataWithType:TypeStart Rssi:gotBeacon.rssi UUID:gotBeaconUUID Major:gotBeaconMajor Minor:gotBeaconMinor Name:gotBeaconName];
                        [gotBeaconCount removeAllObjects];
                        break;
                    }

                    [gotBeaconCount setObject:[[NSNumber alloc] initWithInteger:tempCount] forKey:b.major];
                
                }else{
                    if(iTSSignInDelegate != nil && iTSSignInDelegate.mainTabBarViewController != nil)
                        iTSSignInDelegate.mainTabBarViewController.title = @"";
                    [gotBeaconCount setObject:[[NSNumber alloc] initWithInteger:0] forKey:b.major];
                }
                
            }else if(gotBeaconBoolean){
                if(b.rssi < (gotBeaconThresholdValue-gotBeaconToleranceValue)){
                    if(gotBeaconUUID != nil &&
                       gotBeaconMajor != nil &&
                       gotBeaconMinor != nil){
                        if([b.proximityUUID.UUIDString isEqualToString:gotBeaconUUID] &&
                           [b.major isEqualToNumber:gotBeaconMajor] &&
                           [b.minor isEqualToNumber:gotBeaconMinor]){
                            gotBeacon = b;
                            gotBeaconUUID = b.proximityUUID.UUIDString;
                            gotBeaconMajor = b.major;
                            gotBeaconMinor = b.minor;
                            gotSpecificBeacon = YES;
                            loseBeaconCount += 1;
                            
                            //check if larger than sensingCount
                            if(loseBeaconCount >= sensingCount){
                                if(gotBeaconName != nil && ![gotBeaconName isEqualToString:@""]){
                                    NSLog(@"Left Car1 %@", gotBeaconName);
                                    if(iTSSignInDelegate != nil && iTSSignInDelegate.mainTabBarViewController != nil)
                                        iTSSignInDelegate.mainTabBarViewController.title = @"";
                                }else{
                                    NSLog(@"Left Car1 %@,%@", gotBeaconMajor, gotBeaconMinor);
                                    if(iTSSignInDelegate != nil && iTSSignInDelegate.mainTabBarViewController != nil)
                                        iTSSignInDelegate.mainTabBarViewController.title = @"";
                                }
                                [self uploadRecordDataWithType:TypeEnd Rssi:gotBeacon.rssi UUID:gotBeaconUUID Major:gotBeaconMajor Minor:gotBeaconMinor Name:gotBeaconName];
                                gotBeaconBoolean = NO;
                                gotBeacon = nil;
                                gotBeaconUUID = nil;
                                gotBeaconMajor = nil;
                                gotBeaconMinor = nil;
                                gotBeaconName = @"";
                                gotBeaconThresholdValue = rssiThresholdValue;
                                gotBeaconToleranceValue = rssiToleranceValue;
                                [self saveTypePlist];
                                loseBeaconCount = 0;
                                break;
                            }
                        }
                    }
                }else{
                    if(gotBeaconUUID != nil &&
                       gotBeaconMajor != nil &&
                       gotBeaconMinor != nil){
                        if([b.proximityUUID.UUIDString isEqualToString:gotBeaconUUID] &&
                           [b.major isEqualToNumber:gotBeaconMajor] &&
                           [b.minor isEqualToNumber:gotBeaconMinor]){
                            if(gotBeaconName != nil && ![gotBeaconName isEqualToString:@""]){
                                if(iTSSignInDelegate != nil && iTSSignInDelegate.mainTabBarViewController != nil)
                                    iTSSignInDelegate.mainTabBarViewController.title = [NSString stringWithFormat:@"In %@ Now", gotBeaconName];
                            }else{
                                if(iTSSignInDelegate != nil && iTSSignInDelegate.mainTabBarViewController != nil)
                                    iTSSignInDelegate.mainTabBarViewController.title = [NSString stringWithFormat:@"In %@,%@ Now", gotBeaconMajor,gotBeaconMinor];
                            }
                            loseBeaconCount = 0;
                            gotSpecificBeacon = YES;
                            break;
                        }
                    }
                }
            }
        }
    }
    
    if(gotBeaconBoolean && !gotSpecificBeacon){
        loseBeaconCount += 1;
        if(loseBeaconCount >= sensingCount){
            if(gotBeaconName != nil && ![gotBeaconName isEqualToString:@""]){
                NSLog(@"Left Car2 %@", gotBeaconName);
                if(iTSSignInDelegate != nil && iTSSignInDelegate.mainTabBarViewController != nil)
                    iTSSignInDelegate.mainTabBarViewController.title = @"";
            }else{
                NSLog(@"Left Car2 %@,%@", gotBeaconMajor, gotBeaconMinor);
                if(iTSSignInDelegate != nil && iTSSignInDelegate.mainTabBarViewController != nil)
                    iTSSignInDelegate.mainTabBarViewController.title = @"";
            }
            [self uploadRecordDataWithType:TypeEnd Rssi:gotBeacon.rssi UUID:gotBeaconUUID Major:gotBeaconMajor Minor:gotBeaconMinor Name:gotBeaconName];
            gotBeaconBoolean = NO;
            gotBeacon = nil;
            gotBeaconUUID = nil;
            gotBeaconMajor = nil;
            gotBeaconMinor = nil;
            gotBeaconName = @"";
            gotBeaconThresholdValue = rssiThresholdValue;
            gotBeaconToleranceValue = rssiToleranceValue;
            [self saveTypePlist];
            loseBeaconCount = 0;
        }
    }
    
//    NSLog(@"gotBeaconCount = %d", [[gotBeaconCount objectForKey:[NSNumber numberWithInt:10546]] intValue]);
//    NSLog(@"loseBeaconCount = %d", loseBeaconCount);
    
    //single beacon for test
    /*if(beacon != nil){
        
        if(!gotBeacon && beacon.rssi!=0){
            if(beacon.rssi > (rssiThresholdValue+rssiToleranceValue)){
                gotBeaconCount++;
            }else{
                gotBeaconCount = 0;
            }
            
            if(gotBeaconCount >= sensingCount){
                [self uploadRecordDataWithType:TypeStart Rssi:beacon.rssi UUID:beacon.proximityUUID.UUIDString Major:beacon.major Minor:beacon.minor];
                gotBeacon = YES;
                gotBeaconCount = 0;
                loseBeaconCount = 0;
            }
        }
        
        if(gotBeacon){
            if(beacon.rssi < (rssiThresholdValue-rssiToleranceValue)){
                loseBeaconCount++;
            }else{
                loseBeaconCount = 0;
            }
            
            if(loseBeaconCount >= sensingCount){
                [self uploadRecordDataWithType:TypeEnd Rssi:beacon.rssi UUID:beacon.proximityUUID.UUIDString Major:beacon.major Minor:beacon.minor];
                gotBeacon = NO;
                gotBeaconCount = 0;
                loseBeaconCount = 0;
            }
        }
    }*/
}

- (void) uploadRecordDataWithType:(NSInteger)type Rssi:(NSInteger)rssi UUID:(NSString*)uuid Major:(NSNumber*)major Minor:(NSNumber*)minor Name:(NSString*)name{
    
    [locationManager startUpdatingLocation];
    [locationManager stopUpdatingLocation];
    location = [locationManager location];
    currentLatitude =location.coordinate.latitude;
    currentLongitude =location.coordinate.longitude;
    now = [NSDate date];
    //currentTime = [self getTime:now];
    
    ITSRecordData * recordData = [ITSRecordData new];
    
    if(type == TypeStart)
        recordData.type = @"start";
    else
        recordData.type = @"end";
    NSDecimalNumber *decimalLatitude = [[NSDecimalNumber alloc] initWithFloat:currentLatitude];
    recordData.latitude = decimalLatitude;
    NSDecimalNumber *decimalLongitude = [[NSDecimalNumber alloc] initWithFloat:currentLongitude];
    recordData.longitude = decimalLongitude;
    recordData.time_stamp = now;
    recordData.bt_id = uuid;
    recordData.bt_major = [major stringValue];
    recordData.bt_minor = [minor stringValue];
    recordData.bt_name = name;
    
    if(iTSConnectionManager != nil)
        [iTSConnectionManager uploadRecordData:recordData];
    
    NSString * rssiString = [NSString stringWithFormat:@"RSSI=%i", rssi];
    
    UILocalNotification* localNotification = [[UILocalNotification alloc] init];
    localNotification.fireDate = [NSDate dateWithTimeIntervalSinceNow:0];
    if(name != nil && ![name isEqualToString:@""])
        localNotification.alertBody = [NSString stringWithFormat:@"%@ trip with %@, %@", recordData.type, name, rssiString];
    else
        localNotification.alertBody = [NSString stringWithFormat:@"%@ trip with Major:%@, Minor:%@, %@", recordData.type, major, minor, rssiString];
    localNotification.timeZone = [NSTimeZone defaultTimeZone];
    [[UIApplication sharedApplication] scheduleLocalNotification:localNotification];
    NSLog(@"%@ with %@",recordData.type,rssiString);
}

- (NSString *) getTime:(NSDate *)date{
    
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    dateFormatter.dateFormat = @"yyyy-MM-dd HH:mm:ss";
    [dateFormatter setTimeZone:[NSTimeZone systemTimeZone]];
    NSString* nowTime = [dateFormatter stringFromDate:date];
    NSLog(@"The Current Time is %@",nowTime);
    return nowTime;
}

- (void) callBackFromShowCarNames:(NSDictionary *)iTSReceivedDict{
    if([[NSString stringWithFormat:@"%@", [iTSReceivedDict objectForKey:@"message"]] isEqualToString:@"showCarSuccess"]){
        iTSCars = [iTSReceivedDict objectForKey:@"data"];
        [self saveCarPlist];
        if(iTSBeaconDelegate != nil)
            [iTSBeaconDelegate callBackFromShowCarNames:iTSReceivedDict];
    }else{
        NSLog(@"%@", [NSString stringWithFormat:@"%@", [iTSReceivedDict objectForKey:@"message"]]);
    }
}

- (void) testBeaconRSSIMajor:(NSNumber *)beaconMajor Minor:(NSNumber *)beaconMinor {
    testBeaconMajor = beaconMajor;
    testBeaconMinor = beaconMinor;
    testBeaconMode = YES;
}

-(void) returnTestBeaconStep:(NSInteger)newTestBeaconRssiValue {
    testBeaconCount+=1.0;
    testBeaconRssiValue += ((float)newTestBeaconRssiValue - testBeaconRssiValue) / testBeaconCount;
    if(iTSBeaconDelegate != nil){
        [iTSBeaconDelegate returnTestBeaconStep:testBeaconCount];
    }
    if(testBeaconCount >= testBeaconCountThreshold){
        [self returnTestBeaconValue];
    }
}

-(void) returnTestBeaconValue {
    testBeaconMode = NO;
    testBeaconMajor = nil;
    testBeaconMinor = nil;
    testBeaconCount = 0.0;
    NSNumber *testBeaconRssiNumber = [NSNumber numberWithFloat:testBeaconRssiValue];
    [iTSBeaconDelegate returnTestBeaconValue:[testBeaconRssiNumber integerValue]];
    testBeaconRssiValue = 0.0;
}

-(void) resetTestBeacon{
    testBeaconMode = NO;
    testBeaconMajor = nil;
    testBeaconMinor = nil;
    testBeaconCount = 0.0;
    testBeaconRssiValue = 0.0;
}

-(void) loadTypePlist {
    NSString *rootPath = [NSSearchPathForDirectoriesInDomains
                          (NSDocumentDirectory,NSUserDomainMask, YES)
                          objectAtIndex:0];
    NSString *plistPath = [rootPath stringByAppendingPathComponent:@"type.plist"];
    if (![[NSFileManager defaultManager] fileExistsAtPath:plistPath])
    {
        plistPath = [[NSBundle mainBundle] pathForResource:@"type" ofType:@"plist"];
    }
    typePlistDict = [[NSMutableDictionary alloc] initWithContentsOfFile:plistPath];
    if([typePlistDict objectForKey:@"type"] != nil){
        gotBeaconBoolean = [[typePlistDict objectForKey:@"type"] boolValue];
        NSLog(@"last type is %@", gotBeaconBoolean ? @"YES" : @"NO");
        if(gotBeaconBoolean){
            if([typePlistDict objectForKey:@"beaconUUID"] != nil){
                gotBeaconUUID = [NSString stringWithFormat:@"%@", [typePlistDict objectForKey:@"beaconUUID"]];
            }
            if([typePlistDict objectForKey:@"beaconMajor"] != nil){
                gotBeaconMajor = [NSNumber numberWithInt:[[typePlistDict objectForKey:@"beaconMajor"] intValue]];
            }
            if([typePlistDict objectForKey:@"beaconMinor"] != nil){
                gotBeaconMinor = [NSNumber numberWithInt:[[typePlistDict objectForKey:@"beaconMinor"] intValue]];
            }
            if([typePlistDict objectForKey:@"beaconName"] != nil){
                gotBeaconName = [NSString stringWithFormat:@"%@", [typePlistDict objectForKey:@"beaconName"]];
            }
            if([typePlistDict objectForKey:@"beaconThreshold"] != nil){
                gotBeaconThresholdValue = [[typePlistDict objectForKey:@"beaconThreshold"] integerValue];
            }
            if([typePlistDict objectForKey:@"beaconTolerance"] != nil){
                gotBeaconToleranceValue = [[typePlistDict objectForKey:@"beaconTolerance"] integerValue];
            }
        }
    }else
        gotBeaconBoolean = NO;
}

-(void) saveTypePlist {
    if(typePlistDict != nil){
        [typePlistDict setObject:[NSNumber numberWithBool:gotBeaconBoolean] forKey:@"type"];
        if(gotBeaconBoolean){
            if(gotBeaconUUID != nil){
                [typePlistDict setObject:[NSString stringWithFormat:@"%@", gotBeaconUUID] forKey:@"beaconUUID"];
            }
            if(gotBeaconMajor != nil){
                [typePlistDict setObject:[NSNumber numberWithInt:[gotBeaconMajor intValue]] forKey:@"beaconMajor"];
            }
            if(gotBeaconMinor != nil){
                [typePlistDict setObject:[NSNumber numberWithInt:[gotBeaconMinor intValue]] forKey:@"beaconMinor"];
            }
            if(gotBeaconName != nil){
                [typePlistDict setObject:[NSString stringWithFormat:@"%@", gotBeaconName] forKey:@"beaconName"];
            }
            [typePlistDict setObject:[NSNumber numberWithInteger:gotBeaconThresholdValue] forKey:@"beaconThreshold"];
            [typePlistDict setObject:[NSNumber numberWithInteger:gotBeaconToleranceValue] forKey:@"beaconTolerance"];
        }else{
            [typePlistDict removeObjectForKey:@"beacon"];
        }
        NSString *SaveRootPath = [NSSearchPathForDirectoriesInDomains
                                  (NSDocumentDirectory,NSUserDomainMask, YES)
                                  objectAtIndex:0];
        NSString *SavePath = [SaveRootPath stringByAppendingPathComponent:@"type.plist"];
        [typePlistDict writeToFile:SavePath atomically:YES];
    }
}


-(void) loadParameterPlist{
    NSString *rootPath = [NSSearchPathForDirectoriesInDomains
                          (NSDocumentDirectory,NSUserDomainMask, YES)
                          objectAtIndex:0];
    NSString *plistPath = [rootPath stringByAppendingPathComponent:@"parameter.plist"];
    if (![[NSFileManager defaultManager] fileExistsAtPath:plistPath])
    {
        plistPath = [[NSBundle mainBundle] pathForResource:@"parameter" ofType:@"plist"];
    }
    parameterPlistDict = [[NSMutableDictionary alloc] initWithContentsOfFile:plistPath];
    if([parameterPlistDict objectForKey:@"rssiThreshold"] != nil)
        rssiThresholdValue = [[parameterPlistDict objectForKey:@"rssiThreshold"] integerValue];
    if([parameterPlistDict objectForKey:@"rssiTolerance"] != nil)
            rssiToleranceValue = [[parameterPlistDict objectForKey:@"rssiTolerance"] integerValue];
}

-(void) saveParameterPlist{
    if(parameterPlistDict != nil){
        [parameterPlistDict setObject:[NSNumber numberWithInteger:rssiThresholdValue] forKey:@"rssiThreshold"];
        [parameterPlistDict setObject:[NSNumber numberWithInteger:rssiToleranceValue] forKey:@"rssiTolerance"];
        
        NSString *SaveRootPath = [NSSearchPathForDirectoriesInDomains
                                  (NSDocumentDirectory,NSUserDomainMask, YES)
                                  objectAtIndex:0];
        NSString *SavePath = [SaveRootPath stringByAppendingPathComponent:@"parameter.plist"];
        [parameterPlistDict writeToFile:SavePath atomically:YES];
    }
}

-(void) loadCarPlist{
    NSString *rootPath = [NSSearchPathForDirectoriesInDomains
                          (NSDocumentDirectory,NSUserDomainMask, YES)
                          objectAtIndex:0];
    NSString *plistPath = [rootPath stringByAppendingPathComponent:@"car.plist"];
    if (![[NSFileManager defaultManager] fileExistsAtPath:plistPath])
    {
        plistPath = [[NSBundle mainBundle] pathForResource:@"car" ofType:@"plist"];
    }
    NSArray * tempITSCars = [[NSArray alloc] initWithContentsOfFile:plistPath];
    if(tempITSCars != nil){
        iTSCars = [[NSArray alloc] initWithContentsOfFile:plistPath];
    }
}

-(void) saveCarPlist{
    if(iTSCars != nil){
        NSString *SaveRootPath = [NSSearchPathForDirectoriesInDomains
                                  (NSDocumentDirectory,NSUserDomainMask, YES)
                                  objectAtIndex:0];
        NSString *SavePath = [SaveRootPath stringByAppendingPathComponent:@"car.plist"];
        [iTSCars writeToFile:SavePath atomically:YES];
    }
}

-(void) clearCarData{
    iTSCars = [[NSArray alloc] init];
    [self saveCarPlist];
}

@end

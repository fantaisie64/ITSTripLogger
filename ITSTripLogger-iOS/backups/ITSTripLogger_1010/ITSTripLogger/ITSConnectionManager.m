//
//  ITSConnectionManager.m
//  ITSTripLogger
//
//  Created by FANTAISIE on 7/9/14.
//  Copyright (c) 2014 ITS. All rights reserved.
//

#import "ITSConnectionManager.h"
#import "ITSSignInViewController.h"

@implementation ITSConnectionManager

@synthesize iTSBeaconManager, iTSSignInDelegate, iTSRecordDelegate, iTSBeaconDelegate, iTSSessionToken=privateITSSessionToken;

@synthesize user_id, user_name, user_email, user_lname, user_fname, user_household_id, user_household_name, user_timestamp;

@synthesize iTSRecordArray;

static ITSConnectionManager *instance = nil;

static NSString * const iTSGlobalToken = @"ITSTRIPPROJECT2014";
static NSString * privateITSSessionToken = nil;
static NSString * const iTSServerURLConnectionString = @"http://wayne.cs.ucdavis.edu:5000/mobile_connection";
static NSString * const iTSServerURLSigninString = @"http://wayne.cs.ucdavis.edu:5000/mobile_signin";
static NSString * const iTSServerURLInsertCarString = @"http://wayne.cs.ucdavis.edu:5000/mobile_insertCar";
static NSString * const iTSServerURLShowCarString = @"http://wayne.cs.ucdavis.edu:5000/mobile_showCar";
static NSString * const iTSServerURLInsertRecordString = @"http://wayne.cs.ucdavis.edu:5000/mobile_insertRecord";
static NSString * const iTSServerURLInsertStoredRecordString = @"http://wayne.cs.ucdavis.edu:5000/mobile_insertStoredRecord";
static NSString * const iTSServerURLShowRecordString = @"http://wayne.cs.ucdavis.edu:5000/mobile_showRecord";

static NSURL * iTSServerURL = nil;
static NSMutableURLRequest *iTSServerRequest = nil;
static NSURLConnection *iTSServerConnection = nil;
static NSDictionary *iTSServerPostDict = nil;
static NSDictionary *iTSServerPostDictVerification = nil;
static NSDictionary *iTSServerPostDictData = nil;
static NSData *iTSServerPostData = nil;
static NSMutableData* iTSReceivedData = nil;
static NSDictionary* iTSReceivedDict = nil;

- (id) init
{
    if (self = [super init]){
        iTSRecordArray = [[NSArray alloc] init];
        iTSReceivedData = [[NSMutableData alloc] init];
    }
    return self;
}

+(ITSConnectionManager *)getInstance
{
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        instance = [[ITSConnectionManager alloc] init];
    });
    return instance;
}

-(void) connectToServer{
    NSLog(@"Connecting to Server");
    iTSServerURL = [NSURL URLWithString:iTSServerURLConnectionString];
    iTSServerRequest = [NSMutableURLRequest requestWithURL:iTSServerURL];
    [iTSServerRequest setHTTPMethod:@"GET"];
    iTSServerConnection = [[NSURLConnection alloc] initWithRequest:iTSServerRequest delegate:self];
    //[iTSServerConnection start];
    
    connectionTimer = [NSTimer scheduledTimerWithTimeInterval:5
                                             target:self
                                           selector:@selector(connectionTimerTimedOut:)
                                           userInfo:nil
                                            repeats:NO];
}

- (void)connectionTimerInvalidate {
    [connectionTimer invalidate];
}

- (void)connectionTimerTimedOut:(NSTimer *)theTimer {
    [iTSSignInDelegate connectionTimerTimedOut];
}


-(void) verifyUser:(NSString *)userName withPassword:(NSString *)userPassword{
    NSLog(@"Signing In");
    iTSServerURL = [NSURL URLWithString:iTSServerURLSigninString];
    iTSServerRequest = [NSMutableURLRequest requestWithURL:iTSServerURL];
    [iTSServerRequest setHTTPMethod:@"POST"];
    iTSServerPostDictVerification = [NSDictionary dictionaryWithObjectsAndKeys:
                        iTSGlobalToken, @"globalToken",
                        nil];
    iTSServerPostDictData = [NSDictionary dictionaryWithObjectsAndKeys:
                        userName,@"username",
                        userPassword,@"password",
                        nil];
    iTSServerPostDict = [NSDictionary dictionaryWithObjectsAndKeys:
                        @"siginin",@"message",
                        iTSServerPostDictVerification,@"verification",
                        iTSServerPostDictData,@"data",
                         nil];

    NSError *error;
    iTSServerPostData = [NSJSONSerialization dataWithJSONObject:iTSServerPostDict options:0 error:&error];
    //[iTSServerRequest setValue:@"application/json" forHTTPHeaderField:@"Accept"];
    [iTSServerRequest setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    [iTSServerRequest setHTTPBody:iTSServerPostData];
    iTSServerConnection = [[NSURLConnection alloc] initWithRequest:iTSServerRequest delegate:self];
}

/*-(NSData*)getJsonStringByDictionary:(NSDictionary*)dictionary{
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dictionary options:0 error:&error];
    //return [[[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding] dataUsingEncoding:NSUTF8StringEncoding];
    return jsonData;
}

- (NSData*)encodeDictionary:(NSDictionary*)dictionary {
    NSMutableArray *parts = [[NSMutableArray alloc] init];
    for (NSString *key in dictionary) {
        NSString *encodedValue = [[dictionary objectForKey:key] stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
        NSString *encodedKey = [key stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
        NSString *part = [NSString stringWithFormat: @"%@=%@", encodedKey, encodedValue];
        [parts addObject:part];
    }
    NSString *encodedDictionary = [parts componentsJoinedByString:@"&"];
    return [encodedDictionary dataUsingEncoding:NSUTF8StringEncoding];
}

- (NSString*)encodeDictionaryData:(NSDictionary*)dictionary {
    NSMutableArray *parts = [[NSMutableArray alloc] init];
    for (NSString *key in dictionary) {
        NSString *encodedValue = [[dictionary objectForKey:key] stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
        NSString *encodedKey = [key stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
        NSString *part = [NSString stringWithFormat: @"%@:\'%@\'", encodedKey, encodedValue];
        [parts addObject:part];
    }
    NSString *encodedDictionary = [parts componentsJoinedByString:@","];
    return encodedDictionary;
}*/


//callback functions
- (void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response {
    [iTSReceivedData setLength:0];
}

- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data {
    [iTSReceivedData appendData:data];
}

- (void)connectionDidFinishLoading:(NSURLConnection *)connection {
    //NSString *iTSReceivedString = [[NSString alloc] initWithData:iTSReceivedData encoding:NSUTF8StringEncoding];
    //NSLog(@"%@",iTSReceivedString);
    NSError *error = nil;
    id iTSReceivedObject = [NSJSONSerialization JSONObjectWithData:iTSReceivedData options:NSJSONReadingMutableContainers error:&error];
    
    if(error) {}
    
    if([iTSReceivedObject isKindOfClass:[NSDictionary class]]){
        iTSReceivedDict = iTSReceivedObject;
    }
    else{
    }
    
    if([[NSString stringWithFormat:@"%@", [iTSReceivedDict objectForKey:@"mode"]] isEqualToString:@"connection"]){
        [self connectionTimerInvalidate];
        if(iTSSignInDelegate != nil)
            [iTSSignInDelegate callBackFromConnection:iTSReceivedDict];
    }else if([[NSString stringWithFormat:@"%@", [iTSReceivedDict objectForKey:@"mode"]] isEqualToString:@"signin"]){
        if(iTSSignInDelegate != nil)
            [iTSSignInDelegate callBackFromSignin:iTSReceivedDict];
    }else if([[NSString stringWithFormat:@"%@", [iTSReceivedDict objectForKey:@"mode"]] isEqualToString:@"insertRecord"]){
        if(iTSRecordDelegate != nil)
            [iTSRecordDelegate callBackFromInsertRecord:iTSReceivedDict];
    }else if([[NSString stringWithFormat:@"%@", [iTSReceivedDict objectForKey:@"mode"]] isEqualToString:@"insertStoredRecord"]){
        if(iTSRecordDelegate != nil)
            [iTSRecordDelegate callBackFromInsertStoredRecord:iTSReceivedDict];
    }else if([[NSString stringWithFormat:@"%@", [iTSReceivedDict objectForKey:@"mode"]] isEqualToString:@"showRecord"]){
        if(iTSRecordDelegate != nil)
            [iTSRecordDelegate callBackFromShowRecord:iTSReceivedDict];
    }else if([[NSString stringWithFormat:@"%@", [iTSReceivedDict objectForKey:@"mode"]] isEqualToString:@"insertCar"]){
        if(iTSBeaconDelegate != nil)
            [iTSBeaconDelegate callBackFromInsertCarName:iTSReceivedDict];
    }else if([[NSString stringWithFormat:@"%@", [iTSReceivedDict objectForKey:@"mode"]] isEqualToString:@"showCar"]){
        if(iTSBeaconManager != nil){ //let BeaconManager call BeaconViewController
            [iTSBeaconManager callBackFromShowCarNames:iTSReceivedDict];
        }
    }

}

-(void) setUserData:(NSDictionary *)userDataDict{
    user_id = [[userDataDict objectForKey:@"id"] stringValue];
    user_name = [userDataDict objectForKey:@"username"];
    user_email = [userDataDict objectForKey:@"email"];
    user_lname = [userDataDict objectForKey:@"lname"];
    user_fname = [userDataDict objectForKey:@"fname"];
    user_household_id = [[userDataDict objectForKey:@"household_id"] stringValue];
    user_household_name = [userDataDict objectForKey:@"household_name"];
    user_timestamp = [userDataDict objectForKey:@"timestamp"];
}

-(void) clearUserData{
    user_id = nil;
    user_name = nil;
    user_email = nil;
    user_lname = nil;
    user_fname = nil;
    user_household_id = nil;
    user_household_name = nil;
}

-(void) clearRecordData{
    iTSRecordArray = [[NSArray alloc] init];
    if(iTSRecordDelegate != nil){
        [iTSRecordDelegate clearRecordData];
    }
}

-(void) uploadRecordData:(ITSRecordData*)recordData {
    NSLog(@"uploading data...");
    recordData.user_id = user_id;
    recordData.username = user_name;
    recordData.email = user_email;
    recordData.lname = user_lname;
    recordData.fname = user_fname;
    recordData.household_id = user_household_id;
    recordData.household_name = user_household_name;
    
    iTSServerPostDictData = [NSDictionary dictionaryWithObjectsAndKeys:
                             recordData.user_id, @"user_id",
                             recordData.username,@"user_username",
                             recordData.email, @"user_email",
                             recordData.lname, @"user_lname",
                             recordData.fname, @"user_fname",
                             recordData.household_id, @"household_id",
                             recordData.household_name, @"household_name",
                             recordData.type, @"type",
                             recordData.latitude.stringValue, @"latitude",
                             recordData.longitude.stringValue, @"longitude",
                             [self getTime:recordData.time_stamp], @"timestamp",
                             recordData.bt_id, @"bt_id",
                             recordData.bt_major, @"bt_major",
                             recordData.bt_minor, @"bt_minor",
                             recordData.bt_name, @"bt_name",
                             nil];
    
    
    //backup new data
    [self loadRecordPlist];
    if(storedRecordData != nil && storedRecordData.count != 0){
        [storedRecordData setObject:iTSServerPostDictData forKey:[NSString stringWithFormat:@"%d", storedRecordData.count]];
        [self saveRecordPlist];
        [self uploadStoredRecordData:recordData];
        return;
    }else{
        [storedRecordData setObject:iTSServerPostDictData forKey:[NSString stringWithFormat:@"%d", storedRecordData.count]];
        [self saveRecordPlist];
    }
    
    
    iTSServerURL = [NSURL URLWithString:iTSServerURLInsertRecordString];
    iTSServerRequest = [NSMutableURLRequest requestWithURL:iTSServerURL];
    [iTSServerRequest setHTTPMethod:@"POST"];
    iTSServerPostDictVerification = [NSDictionary dictionaryWithObjectsAndKeys:
                                     iTSGlobalToken, @"globalToken",
                                     privateITSSessionToken, @"token",
                                     recordData.user_id,@"id",
                                     recordData.username,@"username",
                                     recordData.email,@"email",
                                     nil];
    iTSServerPostDict = [NSDictionary dictionaryWithObjectsAndKeys:
                         @"insertRecord",@"message",
                         iTSServerPostDictVerification,@"verification",
                         iTSServerPostDictData,@"data",
                         nil];
    
    NSError *error;
    iTSServerPostData = [NSJSONSerialization dataWithJSONObject:iTSServerPostDict options:0 error:&error];
    //[iTSServerRequest setValue:@"application/json" forHTTPHeaderField:@"Accept"];
    [iTSServerRequest setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    [iTSServerRequest setHTTPBody:iTSServerPostData];
    iTSServerConnection = [[NSURLConnection alloc] initWithRequest:iTSServerRequest delegate:self];
}

-(void) uploadStoredRecordData:(ITSRecordData*)recordData {
    NSLog(@"uploading stored data...");
    iTSServerURL = [NSURL URLWithString:iTSServerURLInsertStoredRecordString];
    iTSServerRequest = [NSMutableURLRequest requestWithURL:iTSServerURL];
    [iTSServerRequest setHTTPMethod:@"POST"];
    iTSServerPostDictVerification = [NSDictionary dictionaryWithObjectsAndKeys:
                                     iTSGlobalToken, @"globalToken",
                                     privateITSSessionToken, @"token",
                                     recordData.user_id,@"id",
                                     recordData.username,@"username",
                                     recordData.email,@"email",
                                     nil];
    iTSServerPostDict = [NSDictionary dictionaryWithObjectsAndKeys:
                         @"insertStoredRecord",@"message",
                         iTSServerPostDictVerification,@"verification",
                         storedRecordData,@"data",
                         nil];
    NSError *error;
    iTSServerPostData = [NSJSONSerialization dataWithJSONObject:iTSServerPostDict options:0 error:&error];
    //[iTSServerRequest setValue:@"application/json" forHTTPHeaderField:@"Accept"];
    [iTSServerRequest setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    [iTSServerRequest setHTTPBody:iTSServerPostData];
    iTSServerConnection = [[NSURLConnection alloc] initWithRequest:iTSServerRequest delegate:self];
}

-(void) getRecordData {
    iTSServerURL = [NSURL URLWithString:iTSServerURLShowRecordString];
    iTSServerRequest = [NSMutableURLRequest requestWithURL:iTSServerURL];
    [iTSServerRequest setHTTPMethod:@"POST"];
    iTSServerPostDictVerification = [NSDictionary dictionaryWithObjectsAndKeys:
                                     iTSGlobalToken, @"globalToken",
                                     privateITSSessionToken, @"token",
                                     user_id,@"id",
                                     user_name,@"username",
                                     user_email,@"email",
                                     nil];
    iTSServerPostDict = [NSDictionary dictionaryWithObjectsAndKeys:
                         @"showRecord",@"message",
                         iTSServerPostDictVerification,@"verification",
                         nil];
    NSError *error;
    iTSServerPostData = [NSJSONSerialization dataWithJSONObject:iTSServerPostDict options:0 error:&error];
    [iTSServerRequest setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    [iTSServerRequest setHTTPBody:iTSServerPostData];
    iTSServerConnection = [[NSURLConnection alloc] initWithRequest:iTSServerRequest delegate:self];
}

- (NSString *) getTime:(NSDate *)date{
    
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    dateFormatter.dateFormat = @"yyyy-MM-dd HH:mm:ss";
    [dateFormatter setTimeZone:[NSTimeZone systemTimeZone]];
    NSString* nowTime = [dateFormatter stringFromDate:date];
    //NSLog(@"The Current Time is %@",[dateFormatter stringFromDate:now]);
    return nowTime;
}

-(void) insertCarName:(NSString *)selectedBeaconName UUID:(NSString *)selectedBeaconUUID Major:(NSNumber *)selectedBeaconMajor Minor:(NSNumber *)selectedBeaconMinor rssiThresholdValue:(NSInteger) selectedBeaconRssiThresholdValue rssiToleranceValue:(NSInteger) selectedBeaconRssiToleranceValue{
    iTSServerURL = [NSURL URLWithString:iTSServerURLInsertCarString];
    iTSServerRequest = [NSMutableURLRequest requestWithURL:iTSServerURL];
    [iTSServerRequest setHTTPMethod:@"POST"];
    iTSServerPostDictVerification = [NSDictionary dictionaryWithObjectsAndKeys:
                                     iTSGlobalToken, @"globalToken",
                                     privateITSSessionToken, @"token",
                                     user_id,@"id",
                                     user_name,@"username",
                                     user_email,@"email",
                                     nil];
    iTSServerPostDictData = [NSDictionary dictionaryWithObjectsAndKeys:
                             selectedBeaconUUID, @"bt_id",
                             selectedBeaconMajor,@"bt_major",
                             selectedBeaconMinor, @"bt_minor",
                             selectedBeaconName, @"bt_name",
                             [NSNumber numberWithInteger:selectedBeaconRssiThresholdValue], @"bt_threshold",
                             [NSNumber numberWithInteger:selectedBeaconRssiToleranceValue], @"bt_tolerance",
                             user_household_id, @"household_id",
                             user_household_name, @"household_name",
                             [self getTime:[NSDate date]], @"timestamp",
                             nil];
    iTSServerPostDict = [NSDictionary dictionaryWithObjectsAndKeys:
                         @"insertCar",@"message",
                         iTSServerPostDictVerification,@"verification",
                         iTSServerPostDictData,@"data",
                         nil];
    
    NSError *error;
    iTSServerPostData = [NSJSONSerialization dataWithJSONObject:iTSServerPostDict options:0 error:&error];
    //[iTSServerRequest setValue:@"application/json" forHTTPHeaderField:@"Accept"];
    [iTSServerRequest setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    [iTSServerRequest setHTTPBody:iTSServerPostData];
    iTSServerConnection = [[NSURLConnection alloc] initWithRequest:iTSServerRequest delegate:self];
}

-(void) showCarNames {
    NSLog(@"connection showCarNames");
    iTSServerURL = [NSURL URLWithString:iTSServerURLShowCarString];
    iTSServerRequest = [NSMutableURLRequest requestWithURL:iTSServerURL];
    [iTSServerRequest setHTTPMethod:@"POST"];
    iTSServerPostDictVerification = [NSDictionary dictionaryWithObjectsAndKeys:
                                     iTSGlobalToken, @"globalToken",
                                     privateITSSessionToken, @"token",
                                     user_id,@"id",
                                     user_name,@"username",
                                     user_email,@"email",
                                     nil];
    iTSServerPostDictData = [NSDictionary dictionaryWithObjectsAndKeys:
                             user_household_id, @"household_id",
                             nil];
    iTSServerPostDict = [NSDictionary dictionaryWithObjectsAndKeys:
                         @"showCar",@"message",
                         iTSServerPostDictVerification,@"verification",
                         iTSServerPostDictData,@"data",
                         nil];
    NSError *error;
    iTSServerPostData = [NSJSONSerialization dataWithJSONObject:iTSServerPostDict options:0 error:&error];
    [iTSServerRequest setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    [iTSServerRequest setHTTPBody:iTSServerPostData];
    iTSServerConnection = [[NSURLConnection alloc] initWithRequest:iTSServerRequest delegate:self];
}

-(void)loadRecordPlist{
    NSString *rootPath = [NSSearchPathForDirectoriesInDomains
                          (NSDocumentDirectory,NSUserDomainMask, YES)
                          objectAtIndex:0];
    NSString *plistPath = [rootPath stringByAppendingPathComponent:@"record.plist"];
    if (![[NSFileManager defaultManager] fileExistsAtPath:plistPath])
    {
        plistPath = [[NSBundle mainBundle] pathForResource:@"record" ofType:@"plist"];
    }
    storedRecordData = [[NSMutableDictionary alloc] initWithContentsOfFile:plistPath];
}

-(void) clearRecordPlist{
    [storedRecordData removeAllObjects];
    NSString *SaveRootPath = [NSSearchPathForDirectoriesInDomains
                              (NSDocumentDirectory,NSUserDomainMask, YES)
                              objectAtIndex:0];
    NSString *SavePath = [SaveRootPath stringByAppendingPathComponent:@"record.plist"];
    [storedRecordData writeToFile:SavePath atomically:YES];
}

-(void) saveRecordPlist{
    NSString *SaveRootPath = [NSSearchPathForDirectoriesInDomains
                              (NSDocumentDirectory,NSUserDomainMask, YES)
                              objectAtIndex:0];
    NSString *SavePath = [SaveRootPath stringByAppendingPathComponent:@"record.plist"];
    [storedRecordData writeToFile:SavePath atomically:YES];
}

-(void) printSomething {
    NSLog(@"privateITSSessionToken = %@", privateITSSessionToken);
    NSLog(@"user_id = %@",user_id);
    NSLog(@"user_name = %@",user_name);
    NSLog(@"user_email = %@",user_email);
    NSLog(@"user_lname = %@",user_lname);
    NSLog(@"user_fname = %@",user_fname);
    NSLog(@"user_household_id = %@",user_household_id);
    NSLog(@"user_household_name = %@",user_household_name);
    NSLog(@"user_household_name = %@",user_household_name);
    NSLog(@"user_timestamp = %@",user_timestamp);
}
@end

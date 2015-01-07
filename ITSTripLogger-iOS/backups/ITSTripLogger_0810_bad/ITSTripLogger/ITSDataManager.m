//
//  ITSDataManager.m
//  ITSTripLogger
//
//  Created by FANTAISIE on 8/11/14.
//  Copyright (c) 2014 ITS. All rights reserved.
//

#import "ITSDataManager.h"

@implementation ITSDataManager

@synthesize iTSConnectionManager, iTSBeaconManager, iTSSignInDelegate, iTSRecordDelegate, iTSBeaconDelegate;

@synthesize user_id, user_name, user_email, user_lname, user_fname, user_household_id, user_household_name, user_timestamp;
@synthesize storedRecordData;
    
static ITSDataManager *instance = nil;

- (id) init
{
    if (self = [super init]){
        self.iTSConnectionManager = [ITSConnectionManager getInstance];
        self.iTSConnectionManager.iTSDataManager = self;
        self.iTSBeaconManager = [ITSBeaconManager getInstance];
        self.iTSBeaconManager.iTSDataManager = self;
    }
    return self;
}

+(ITSDataManager *)getInstance
{
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        instance = [[ITSDataManager alloc] init];
    });
    return instance;
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
//    if(iTSRecordDelegate != nil){
//        [iTSRecordDelegate clearRecordData];
//    }
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

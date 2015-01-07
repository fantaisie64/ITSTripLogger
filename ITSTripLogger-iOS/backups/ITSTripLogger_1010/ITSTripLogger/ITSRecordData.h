//
//  ITSRecordData.h
//  ITSTripLogger
//
//  Created by FANTAISIE on 7/14/14.
//  Copyright (c) 2014 ITS. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface ITSRecordData : NSObject
@property (nonatomic) NSString *user_id;
@property (nonatomic) NSString *username;
@property (nonatomic) NSString *email;
@property (nonatomic) NSString *lname;
@property (nonatomic) NSString *fname;
@property (nonatomic) NSString *household_id;
@property (nonatomic) NSString *household_name;
@property (nonatomic) NSString *type;
@property (nonatomic) NSDecimalNumber *latitude;
@property (nonatomic) NSDecimalNumber *longitude;
@property (nonatomic) NSDate *time_stamp;
@property (nonatomic) NSString *bt_id;
@property (nonatomic) NSString *bt_major;
@property (nonatomic) NSString *bt_minor;
@property (nonatomic) NSString *bt_name;
@end

//
//  MapViewAnnotation.m
//  ITSTripLogger
//
//  Created by FANTAISIE on 7/23/14.
//  Copyright (c) 2014 ITS. All rights reserved.
//

#import "MapViewAnnotation.h"

@implementation MapViewAnnotation
@synthesize coordinate=_coordinate;
@synthesize title=_title;
-(id) initWithTitle:(NSString *) title AndCoordinate:(CLLocationCoordinate2D)coordinate
{
    self = [super init];
    _title = title;
    _coordinate = coordinate;
    return self;
}
@end

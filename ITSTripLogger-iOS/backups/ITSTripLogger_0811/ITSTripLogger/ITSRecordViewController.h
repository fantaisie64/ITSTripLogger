//
//  ITSRecordViewController.h
//  ITSTripLogger
//
//  Created by FANTAISIE on 7/16/14.
//  Copyright (c) 2014 ITS. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <mapkit/mapkit.h>
#import <CoreLocation/CoreLocation.h>
#import "ITSConnectionManager.h"
#import "MapViewAnnotation.h"

@class ITSConnectionManager;

@interface ITSRecordViewController : UIViewController <MKMapViewDelegate, UITableViewDelegate, UITableViewDataSource> {
    MKMapView *mapView;
    NSMutableArray *annotations;
    IBOutlet UITableView *recordTableView;
    ITSConnectionManager *iTSConnectionManager;
    NSArray *iTSRecordArray;
}
@property (strong, nonatomic) IBOutlet MKMapView *mapView;
- (void) callBackFromInsertRecord:(NSDictionary *)iTSReceivedDict;
- (void) callBackFromInsertStoredRecord:(NSDictionary *)iTSReceivedDict;
- (void) callBackFromShowRecord:(NSDictionary *)iTSReceivedDict;
- (IBAction)refreshButtonClicked:(id)sender;
- (void) clearRecordData;
@end

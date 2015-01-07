//
//  ITSRecordViewController.m
//  ITSTripLogger
//
//  Created by FANTAISIE on 7/16/14.
//  Copyright (c) 2014 ITS. All rights reserved.
//

#import "ITSRecordViewController.h"

#define METERS_PER_MILE 1609.344

@implementation ITSRecordViewController

@synthesize mapView;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (id)initWithCoder:(NSCoder *)decoder{
    if (self = [super initWithCoder:decoder]) {
        iTSConnectionManager = [ITSConnectionManager getInstance];
        iTSConnectionManager.iTSRecordDelegate = self;
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    annotations = [[NSMutableArray alloc] init];
//    firstFormatter = [[NSDateFormatter alloc] init];
//    [firstFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss.000Z"];
//    secondFormatter = [[NSDateFormatter alloc] init];
//    [secondFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
}

-(void)viewWillAppear:(BOOL)animated {
    self.navigationController.navigationBar.backItem.title = @"Sign Out";
}


-(void)viewDidAppear:(BOOL)animated {
    self.navigationController.navigationBar.backItem.title = @"Sign Out";
}


- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    //#warning Potentially incomplete method implementation.
    // Return the number of sections.
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    //#warning Incomplete method implementation.
    // Return the number of rows in the section.
    return [iTSConnectionManager.iTSRecordArray count];
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *simpleTableIdentifier = @"SimpleTableCell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:simpleTableIdentifier];
    
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:simpleTableIdentifier];
    }
    
//    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
//    [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
//    NSDate *date = [dateFormatter dateFromString:[[iTSConnectionManager.iTSRecordArray objectAtIndex:indexPath.row] objectForKey:@"timestamp"]];
//    NSLog(@"current date1 = %@", [[iTSConnectionManager.iTSRecordArray objectAtIndex:indexPath.row] objectForKey:@"timestamp"]);
//    NSLog(@"current date2 = %@", date.description);
    
    //Title
//    NSDate *date = [firstFormatter dateFromString:[NSString stringWithFormat:@"%@", [[iTSConnectionManager.iTSRecordArray objectAtIndex:indexPath.row] objectForKey:@"timestamp"]]];
    cell.textLabel.text = [NSString stringWithFormat:@"%@: %@", [[iTSConnectionManager.iTSRecordArray objectAtIndex:indexPath.row] objectForKey:@"type"], [[iTSConnectionManager.iTSRecordArray objectAtIndex:indexPath.row] objectForKey:@"timestamp"]];
    cell.textLabel.font = [UIFont systemFontOfSize:18];
    
    //subtitle
    if([[iTSConnectionManager.iTSRecordArray objectAtIndex:indexPath.row] objectForKey:@"bt_name"] != nil &&
       ![[[iTSConnectionManager.iTSRecordArray objectAtIndex:indexPath.row] objectForKey:@"bt_name"] isEqualToString:@""]){
        cell.detailTextLabel.text = [NSString stringWithFormat:@"%@", [[iTSConnectionManager.iTSRecordArray objectAtIndex:indexPath.row] objectForKey:@"bt_name"]];
    }else{
        cell.detailTextLabel.text = [NSString stringWithFormat:@"Major: %@, Minor: %@", [[iTSConnectionManager.iTSRecordArray objectAtIndex:indexPath.row] objectForKey:@"bt_major"], [[iTSConnectionManager.iTSRecordArray objectAtIndex:indexPath.row] objectForKey:@"bt_minor"]];
    }
    
    //image
    CGSize size = {22,52};
    if([[[iTSConnectionManager.iTSRecordArray objectAtIndex:indexPath.row] objectForKey:@"type"] isEqualToString:@"start"])
        cell.imageView.image = [self imageWithImage:[UIImage imageNamed:@"route1.png"] scaledToSize:size];
    else if([[[iTSConnectionManager.iTSRecordArray objectAtIndex:indexPath.row] objectForKey:@"type"] isEqualToString:@"end"])
        cell.imageView.image = [self imageWithImage:[UIImage imageNamed:@"route2.png"] scaledToSize:size];
    
    return cell;
}

- (UIImage*)imageWithImage:(UIImage*)image
              scaledToSize:(CGSize)newSize;
{
    UIGraphicsBeginImageContext( newSize );
    [image drawInRect:CGRectMake(0,0,newSize.width,newSize.height)];
    UIImage* newImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    return newImage;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [mapView removeAnnotations:annotations];
    [annotations removeAllObjects];
    
    //Read locations details from plist
    double latitude = [[[iTSConnectionManager.iTSRecordArray objectAtIndex:indexPath.row] objectForKey:@"latitude"] doubleValue];
    double longitude = [[[iTSConnectionManager.iTSRecordArray objectAtIndex:indexPath.row] objectForKey:@"longitude"] doubleValue];
    NSString *title = [[iTSConnectionManager.iTSRecordArray objectAtIndex:indexPath.row] objectForKey:@"type"];
    //Create coordinates from the latitude and longitude values
    CLLocationCoordinate2D coord;
    coord.latitude = latitude;
    coord.longitude = longitude;
    MapViewAnnotation *annotation = [[MapViewAnnotation alloc] initWithTitle:title AndCoordinate:coord];
    [annotations addObject:annotation];
    [mapView addAnnotations:annotations];
    
    //Zoom to location
    CLLocationCoordinate2D zoomLocation;
    zoomLocation.latitude = latitude;
    zoomLocation.longitude= longitude;
    MKCoordinateRegion viewRegion = MKCoordinateRegionMakeWithDistance(zoomLocation, 1.0*METERS_PER_MILE,1.0*METERS_PER_MILE);
    [mapView setRegion:viewRegion animated:YES];
    [mapView regionThatFits:viewRegion];
    
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

- (void) callBackFromInsertRecord:(NSDictionary *)iTSReceivedDict{
    if([[NSString stringWithFormat:@"%@", [iTSReceivedDict objectForKey:@"message"]] isEqualToString:@"insertRecordSuccess"]){
        [iTSConnectionManager clearRecordPlist];
        [iTSConnectionManager getRecordData];
    }else{
        NSLog(@"%@", [NSString stringWithFormat:@"%@", [iTSReceivedDict objectForKey:@"message"]]);
    }
}

- (void) callBackFromInsertStoredRecord:(NSDictionary *)iTSReceivedDict {
    if([[NSString stringWithFormat:@"%@", [iTSReceivedDict objectForKey:@"message"]] isEqualToString:@"insertStoredRecordSuccess"]){
        [iTSConnectionManager clearRecordPlist];
        [iTSConnectionManager getRecordData];
    }else{
        NSLog(@"%@", [NSString stringWithFormat:@"%@", [iTSReceivedDict objectForKey:@"message"]]);
    }
}

- (void) callBackFromShowRecord:(NSDictionary *)iTSReceivedDict{
    if([[NSString stringWithFormat:@"%@", [iTSReceivedDict objectForKey:@"message"]] isEqualToString:@"showRecordSuccess"]){
        iTSConnectionManager.iTSRecordArray = [iTSReceivedDict objectForKey:@"data"];
        [recordTableView reloadData];
    }else{
        NSLog(@"%@", [NSString stringWithFormat:@"%@", [iTSReceivedDict objectForKey:@"message"]]);
    }
}


- (IBAction)refreshButtonClicked:(id)sender {
    [iTSConnectionManager getRecordData];
}

- (void) clearRecordData{
    [recordTableView reloadData];
}

@end

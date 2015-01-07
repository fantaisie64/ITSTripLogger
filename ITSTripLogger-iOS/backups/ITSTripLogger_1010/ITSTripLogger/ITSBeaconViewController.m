//
//  ITSBeaconViewController.m
//  ITSTripLogger
//
//  Created by FANTAISIE on 6/3/14.
//  Copyright (c) 2014 ITS. All rights reserved.
//

#import "ITSBeaconViewController.h"


@implementation ITSBeaconViewController

@synthesize beaconTableView;

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
        iTSConnectionManager.iTSBeaconDelegate = self;
        iTSBeaconManager = [ITSBeaconManager getInstance];
        iTSBeaconManager.iTSBeaconDelegate = self;
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    thresholdLabel.text = [NSString stringWithFormat:@"Threshold: %i", iTSBeaconManager.rssiThresholdValue];
    toleranceLabel.text = [NSString stringWithFormat:@"Tolerance: %i", iTSBeaconManager.rssiToleranceValue];
    thresholdSlider.value = iTSBeaconManager.rssiThresholdValue;
    toleranceSlider.value = iTSBeaconManager.rssiToleranceValue;
    // Do any additional setup after loading the view.
    
    editingMode = NO;
    changeNameView.hidden = YES;
    changeNameTextField.delegate = self;
    
    [self resetAll];
}

-(void)viewWillAppear:(BOOL)animated {
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
    if(iTSBeaconManager.beacons == nil)
        return 0;
    else
        return [iTSBeaconManager.beacons count];
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *simpleTableIdentifier = @"SimpleTableCell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:simpleTableIdentifier];
    
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:simpleTableIdentifier];
    }
    
    if(iTSBeaconManager.beacons == nil){
    
    }
    else{
        NSString * foundCarName = nil;
        if(iTSBeaconManager.iTSCars != nil && iTSBeaconManager.iTSCars.count != 0){
            for(NSDictionary * car in iTSBeaconManager.iTSCars){
                if([[[iTSBeaconManager.beacons objectAtIndex:indexPath.row] major] isEqualToNumber:
                    [NSNumber numberWithInt:[[car objectForKey:@"bt_major"]  intValue]]]
                   &&
                   [[[iTSBeaconManager.beacons objectAtIndex:indexPath.row] minor] isEqualToNumber:
                    [NSNumber numberWithInt:[[car objectForKey:@"bt_minor"]  intValue]]]){
                    if([car objectForKey:@"bt_name"] != nil)
                        foundCarName = [NSString stringWithFormat:@"%@", [car objectForKey:@"bt_name"]];
                    break;
                }
            }
        }
        
        if(foundCarName != nil){
            cell.textLabel.text = foundCarName;
            cell.detailTextLabel.text = [NSString stringWithFormat:@"Major: %@, Minor: %@, RSSI=%i", [[[iTSBeaconManager.beacons objectAtIndex:indexPath.row] major] stringValue], [[[iTSBeaconManager.beacons objectAtIndex:indexPath.row] minor] stringValue], [[iTSBeaconManager.beacons objectAtIndex:indexPath.row] rssi]];
            CGSize size = {48,30};
            cell.imageView.image = [self imageWithImage:[UIImage imageNamed:@"car.png"] scaledToSize:size];
        }else{
            cell.textLabel.text = @"Noname";
            cell.detailTextLabel.text = [NSString stringWithFormat:@"Major: %@, Minor: %@, RSSI=%i", [[[iTSBeaconManager.beacons objectAtIndex:indexPath.row] major] stringValue], [[[iTSBeaconManager.beacons objectAtIndex:indexPath.row] minor] stringValue], [[iTSBeaconManager.beacons objectAtIndex:indexPath.row] rssi]];
            CGSize size = {48,30};
            cell.imageView.image = [self imageWithImage:[UIImage imageNamed:@"car.png"] scaledToSize:size];
        }
    }
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
    if(!calibrationMode){
        NSLog(@"select=%@", [NSString stringWithFormat:@"Major:%@, Minor:%@", [[[iTSBeaconManager.beacons objectAtIndex:indexPath.row] major] stringValue], [[[iTSBeaconManager.beacons objectAtIndex:indexPath.row] minor] stringValue]]);
        changeNameLabel.text = [NSString stringWithFormat:@"Major:%@ Minor:%@", [[[iTSBeaconManager.beacons objectAtIndex:indexPath.row] major] stringValue], [[[iTSBeaconManager.beacons objectAtIndex:indexPath.row] minor] stringValue]];
        
        [self resetSelected];
        
        //Search beacon
        if(iTSBeaconManager.iTSCars != nil && iTSBeaconManager.iTSCars.count != 0){
            for(NSDictionary * car in iTSBeaconManager.iTSCars){
                if([[[iTSBeaconManager.beacons objectAtIndex:indexPath.row] major] isEqualToNumber:
                    [NSNumber numberWithInt:[[car objectForKey:@"bt_major"]  intValue]]]
                   &&
                   [[[iTSBeaconManager.beacons objectAtIndex:indexPath.row] minor] isEqualToNumber:
                    [NSNumber numberWithInt:[[car objectForKey:@"bt_minor"]  intValue]]]){
                       if([car objectForKey:@"bt_name"] != nil)
                           selectedBeaconName = [NSString stringWithFormat:@"%@", [car objectForKey:@"bt_name"]];
                       if([car objectForKey:@"bt_threshold"] != nil)
                           selectedBeaconThreshold = [[car objectForKey:@"bt_threshold"] integerValue];
                       if([car objectForKey:@"bt_tolerance"] != nil)
                           selectedBeaconTolerance = [[car objectForKey:@"bt_tolerance"] integerValue];
                       break;
                   }
            }
        }
        changeNameTextField.text = [NSString stringWithFormat:@"%@", selectedBeaconName];
        
        CLBeacon * selectedBeacon = [iTSBeaconManager.beacons objectAtIndex:indexPath.row];
        selectedBeaconUUID = [NSString stringWithFormat:@"%@", selectedBeacon.proximityUUID.UUIDString];
        selectedBeaconMajor = [NSNumber numberWithInt:[[selectedBeacon major] intValue]];
        selectedBeaconMinor = [NSNumber numberWithInt:[[selectedBeacon minor] intValue]];
        
        [self resetCalibration];
        [self resetCalibrationUI];
        [self resetManualUI];
    }
}

/*
 // Override to support conditional editing of the table view.
 - (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
 {
 // Return NO if you do not want the specified item to be editable.
 return YES;
 }
 */

/*
 // Override to support editing the table view.
 - (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
 {
 if (editingStyle == UITableViewCellEditingStyleDelete) {
 // Delete the row from the data source
 [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
 } else if (editingStyle == UITableViewCellEditingStyleInsert) {
 // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
 }
 }
 */

/*
 // Override to support rearranging the table view.
 - (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath
 {
 }
 */

/*
 // Override to support conditional rearranging of the table view.
 - (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath
 {
 // Return NO if you do not want the item to be re-orderable.
 return YES;
 }
 */

/*
 #pragma mark - Navigation
 
 // In a storyboard-based application, you will often want to do a little preparation before navigation
 - (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
 {
 // Get the new view controller using [segue destinationViewController].
 // Pass the selected object to the new view controller.
 }
 */

- (IBAction)thresholdSliderChanged:(id)sender {
    //NSLog(@"ThresholdSLider Value = %i", (int)[thresholdSlider value]);
    thresholdLabel.text = [NSString stringWithFormat:@"Threshold: %i", (int)[thresholdSlider value]];
}

- (IBAction)thresholdSliderEdited:(id)sender {
    //NSLog(@"ThresholdSLider Changed = %i", (int)[thresholdSlider value]);
    iTSBeaconManager.rssiThresholdValue =(int)[thresholdSlider value];
    thresholdLabel.text = [NSString stringWithFormat:@"Threshold: %i", iTSBeaconManager.rssiThresholdValue];
    [iTSBeaconManager saveParameterPlist];
}

- (IBAction)toleranceSliderChanged:(id)sender {
    //NSLog(@"ToleranceSLider Value = %i", (int)[toleranceSlider value]);
    toleranceLabel.text = [NSString stringWithFormat:@"Tolerance: %i", (int)[toleranceSlider value]];
}

- (IBAction)toleranceSliderEdited:(id)sender {
    //NSLog(@"ToleranceSLider Changed = %i", (int)[toleranceSlider value]);
    iTSBeaconManager.rssiToleranceValue = (int)[toleranceSlider value];
    toleranceLabel.text = [NSString stringWithFormat:@"Tolerance: %i", iTSBeaconManager.rssiToleranceValue];
    [iTSBeaconManager saveParameterPlist];
}

- (IBAction)changeEditMode:(id)sender {
    [self.view endEditing:YES];
    if(editingMode){
        editingMode = NO;
        [editButton setTitle:@"Edit" forState:UIControlStateNormal];
        [editButton setBackgroundColor:[UIColor blueColor]];
        changeNameLabel.text = @"Not Selected";
        changeNameTextField.text = @"";
        changeNameView.hidden = YES;
        [self resetAll];
        
    }else{
        editingMode = YES;
        [editButton setTitle:@"Done" forState:UIControlStateNormal];
        [editButton setBackgroundColor:[UIColor redColor]];
        selectedBeaconMajor = nil;
        selectedBeaconMinor = nil;
        [self resetCalibration];
        changeNameLabel.text = @"Not Selected";
        changeNameTextField.text = @"";
        changeNameView.hidden = NO;
        [self resetAll];
    }
}

- (BOOL) textFieldShouldReturn:(UITextField *)textField {
    [self.view endEditing:YES];
    return YES;
}

- (IBAction)UpdateBeacon:(id)sender {
    [self.view endEditing:YES];
    if(selectedBeaconUUID != nil && selectedBeaconMajor != nil && selectedBeaconMinor != nil){
        if(![changeNameTextField.text isEqualToString:@""]){
            [iTSConnectionManager insertCarName:changeNameTextField.text UUID:selectedBeaconUUID Major:selectedBeaconMajor Minor:selectedBeaconMinor rssiThresholdValue:newRssiThresholdValue rssiToleranceValue:newRssiToleranceValue];
        }
    }
}

- (IBAction)changeAutoButtonClicked:(id)sender{
    [changeAutoButton setBackgroundColor:[UIColor whiteColor]];
    [changeAutoView setHidden:NO];
    [changeManualButton setBackgroundColor:[UIColor cyanColor]];
    [changeManualView setHidden:YES];
}

- (IBAction)inCarButtonClicked:(id)sender{
    if(selectedBeaconMajor != nil && selectedBeaconMinor != nil){
        calibrationMode = YES;
        calibrationSubMode = TypeInCar;
        [inCarButton setTitle:[NSString stringWithFormat:@"Sensing...0"]  forState:UIControlStateNormal];
        [iTSBeaconManager testBeaconRSSIMajor:selectedBeaconMajor Minor:selectedBeaconMinor];
    }
}

- (IBAction)offCarButtonClicked:(id)sender{
    if(selectedBeaconMajor != nil && selectedBeaconMinor != nil){
        calibrationMode = YES;
        calibrationSubMode = TypeOffCar;
        [offCarButton setTitle:[NSString stringWithFormat:@"Sensing...0"]  forState:UIControlStateNormal];
        [iTSBeaconManager testBeaconRSSIMajor:selectedBeaconMajor Minor:selectedBeaconMinor];
    }
}

-(void) returnTestBeaconStep:(int)testBeaconCount {
    if(calibrationSubMode == TypeInCar){
        [inCarButton setTitle:[NSString stringWithFormat:@"Sensing...%i", testBeaconCount]  forState:UIControlStateNormal];
        [messageLabel setText:@"Sensing In Car Signal Strength..."];
    }else if(calibrationSubMode == TypeOffCar){
        [offCarButton setTitle:[NSString stringWithFormat:@"Sensing...%i", testBeaconCount]  forState:UIControlStateNormal];
        [messageLabel setText:@"Sensing Off Car Signal Strength..."];
    }
}

-(void) returnTestBeaconValue:(NSInteger)testBeaconRssiValue {
    if(calibrationSubMode == TypeInCar){
        [inCarButton setTitle:@"Done"  forState:UIControlStateNormal];
        calibrationSubMode = TypeNone;
        [inCarButton setBackgroundColor:[UIColor redColor]];
        [offCarButton setBackgroundColor:[UIColor purpleColor]];
        [inCarButton setEnabled:NO];
        [offCarButton setEnabled:YES];
        tempHighValue = testBeaconRssiValue;
        NSLog(@"in car testBeaconRssiValue = %i", testBeaconRssiValue);
        [messageLabel setText:@"Get off your car, and press \"Off Car\" Button"];
    }else if(calibrationSubMode == TypeOffCar){
        [offCarButton setTitle:@"Done"  forState:UIControlStateNormal];
        calibrationMode = NO;
        calibrationSubMode = TypeNone;
        [inCarButton setBackgroundColor:[UIColor redColor]];
        [offCarButton setBackgroundColor:[UIColor redColor]];
        [inCarButton setEnabled:YES];
        [offCarButton setEnabled:NO];
        tempLowValue = testBeaconRssiValue;
        
        //find threshold and tolerance
        newRssiThresholdValue = (tempHighValue + tempLowValue) / 2;
        newRssiToleranceValue = abs(tempHighValue - newRssiThresholdValue);
        
        //update manual
        [self resetManualUI];
        
        NSLog(@"off car testBeaconRssiValue = %i", testBeaconRssiValue);
        [messageLabel setText:[NSString stringWithFormat:@"Done Calibration with Threshold:%i Tolerance:%i", newRssiThresholdValue, newRssiToleranceValue]];
    }
}

- (IBAction)resetButtonClicked:(id)sender{
    [self reset];
}

- (void) resetAll {
    [self resetMode];
    [self resetSelected];
    [self resetCalibration];
    [self resetCalibrationUI];
    [self resetManualUI];
    [iTSBeaconManager resetTestBeacon];
}

- (void) reset {
    [self resetMode];
    [self resetCalibration];
    [self resetCalibrationUI];
    [self resetManualUI];
    [iTSBeaconManager resetTestBeacon];
}

- (void) resetMode {
    calibrationMode = NO;
    calibrationSubMode = TypeNone;
}

- (void) resetSelected {
    selectedBeaconName = @"";
    selectedBeaconUUID = nil;
    selectedBeaconMajor = nil;
    selectedBeaconMinor = nil;
    selectedBeaconThreshold = [iTSBeaconManager rssiThresholdValue];
    selectedBeaconTolerance = [iTSBeaconManager rssiToleranceValue];
}

- (void) resetCalibration {
    newRssiThresholdValue = selectedBeaconThreshold;
    newRssiToleranceValue = selectedBeaconTolerance;
    tempHighValue = newRssiThresholdValue + newRssiToleranceValue;
    tempLowValue = newRssiThresholdValue - newRssiToleranceValue;
}

- (void) resetCalibrationUI {
    [inCarButton setBackgroundColor:[UIColor purpleColor]];
    [offCarButton setBackgroundColor:[UIColor cyanColor]];
    [inCarButton setEnabled:YES];
    [offCarButton setEnabled:NO];
    [inCarButton setTitle:@"In Car"  forState:UIControlStateNormal];
    [offCarButton setTitle:@"Off Car"  forState:UIControlStateNormal];
    [messageLabel setText:@"Get into your car, and press \"In Car\" Button"];
}

-(void) resetManualUI {
    beaconThresholdLabel.text = [NSString stringWithFormat:@"Threshold: %i", newRssiThresholdValue];
    beaconToleranceLabel.text = [NSString stringWithFormat:@"Tolerance: %i", newRssiToleranceValue];
    beaconThresholdSlider.value = newRssiThresholdValue;
    beaconToleranceSlider.value = newRssiToleranceValue;
}

- (IBAction)changeManualButtonClicked:(id)sender{
    [changeAutoButton setBackgroundColor:[UIColor cyanColor]];
    [changeAutoView setHidden:YES];
    [changeManualButton setBackgroundColor:[UIColor whiteColor]];
    [changeManualView setHidden:NO];
}

- (IBAction)beaconThresholdSliderChanged:(id)sender {
    //NSLog(@"ThresholdSLider Value = %i", (int)[thresholdSlider value]);
    beaconThresholdLabel.text = [NSString stringWithFormat:@"Threshold: %i", (int)[beaconThresholdSlider value]];
}

- (IBAction)beaconThresholdSliderEdited:(id)sender {
    //NSLog(@"ThresholdSLider Changed = %i", (int)[thresholdSlider value]);
    [self resetCalibrationUI];
    newRssiThresholdValue =(int)[beaconThresholdSlider value];
    beaconThresholdLabel.text = [NSString stringWithFormat:@"Threshold: %i", newRssiThresholdValue];
}

- (IBAction)beaconToleranceSliderChanged:(id)sender {
    //NSLog(@"ToleranceSLider Value = %i", (int)[toleranceSlider value]);
    beaconToleranceLabel.text = [NSString stringWithFormat:@"Tolerance: %i", (int)[beaconToleranceSlider value]];
}

- (IBAction)beaconToleranceSliderEdited:(id)sender {
    //NSLog(@"ToleranceSLider Changed = %i", (int)[toleranceSlider value]);
    [self resetCalibrationUI];
    newRssiToleranceValue = (int)[beaconToleranceSlider value];
    beaconToleranceLabel.text = [NSString stringWithFormat:@"Tolerance: %i", newRssiToleranceValue];
}

- (void) callBackFromInsertCarName:(NSDictionary *)iTSReceivedDict{
    if([[NSString stringWithFormat:@"%@", [iTSReceivedDict objectForKey:@"message"]] isEqualToString:@"insertCarSuccess"]){
        [iTSConnectionManager showCarNames];
    }else{
        NSLog(@"%@", [NSString stringWithFormat:@"%@", [iTSReceivedDict objectForKey:@"message"]]);
    }
}

- (void) callBackFromShowCarNames:(NSDictionary *)iTSReceivedDict{
    if([[NSString stringWithFormat:@"%@", [iTSReceivedDict objectForKey:@"message"]] isEqualToString:@"showCarSuccess"]){
        [self resetAll];
        changeNameLabel.text = @"Updated";
        changeNameTextField.text = @"";
        [beaconTableView reloadData];
    }else{
        NSLog(@"%@", [NSString stringWithFormat:@"%@", [iTSReceivedDict objectForKey:@"message"]]);
    }
}

- (void)printSomething {
    NSLog(@"This is beacon view controller");
}

@end

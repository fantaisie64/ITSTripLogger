//
//  ITSInfoViewController.m
//  ITSTripLogger
//
//  Created by FANTAISIE on 7/16/14.
//  Copyright (c) 2014 ITS. All rights reserved.
//

#import "ITSInfoViewController.h"

@interface ITSInfoViewController ()

@end

@implementation ITSInfoViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    iTSDataManager = [ITSDataManager getInstance];
    iTSConnectionManager = [ITSConnectionManager getInstance];
    //iTSBeaconManager = [ITSBeaconManager getInstance];
    // Do any additional setup after loading the view.
}

-(void)viewWillAppear:(BOOL)animated {
    self.navigationController.navigationBar.backItem.title = @"Sign Out";
    
    [userIdLabel setText:iTSDataManager.user_id];
    [userNameLabel setText:iTSDataManager.user_name];
    [emailLabel setText:iTSDataManager.user_email];
    [lnameLabel setText:iTSDataManager.user_lname];
    [fnameLabel setText:iTSDataManager.user_fname];
    [householdIdLabel setText:iTSDataManager.user_household_id];
    [householdNameLabel setText:iTSDataManager.user_household_name];
    [registerTimeLabel setText:iTSDataManager.user_timestamp];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
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

@end

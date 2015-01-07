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
    iTSConnectionManager = [ITSConnectionManager getInstance];
    //iTSBeaconManager = [ITSBeaconManager getInstance];
    // Do any additional setup after loading the view.
}

-(void)viewWillAppear:(BOOL)animated {
    self.navigationController.navigationBar.backItem.title = @"Sign Out";
    
    [userIdLabel setText:iTSConnectionManager.user_id];
    [userNameLabel setText:iTSConnectionManager.user_name];
    [emailLabel setText:iTSConnectionManager.user_email];
    [lnameLabel setText:iTSConnectionManager.user_lname];
    [fnameLabel setText:iTSConnectionManager.user_fname];
    [householdIdLabel setText:iTSConnectionManager.user_household_id];
    [householdNameLabel setText:iTSConnectionManager.user_household_name];
    [registerTimeLabel setText:iTSConnectionManager.user_timestamp];
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

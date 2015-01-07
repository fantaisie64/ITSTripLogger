//
//  ITSSignInViewController.m
//  ITSTripLogger
//
//  Created by FANTAISIE on 6/3/14.
//  Copyright (c) 2014 ITS. All rights reserved.
//

#import "ITSSignInViewController.h"


@implementation ITSSignInViewController

@synthesize mainTabBarViewController;

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
//        NSLog(@"sigin init");
//        UILocalNotification* localNotification = [[UILocalNotification alloc] init];
//        localNotification.fireDate = [NSDate dateWithTimeIntervalSinceNow:0];
//        localNotification.alertBody = @"ITSsigin init";
//        localNotification.timeZone = [NSTimeZone defaultTimeZone];
//        [[UIApplication sharedApplication] scheduleLocalNotification:localNotification];
        
        iTSConnectionManager = [ITSConnectionManager getInstance];
        iTSConnectionManager.iTSSignInDelegate = self;
        iTSBeaconManager = [ITSBeaconManager getInstance];
        iTSBeaconManager.iTSSignInDelegate = self;
        
//        UILocalNotification* localNotification = [[UILocalNotification alloc] init];
//        localNotification.fireDate = [NSDate dateWithTimeIntervalSinceNow:0];
//        localNotification.alertBody = @"Signin initialized";
//        localNotification.timeZone = [NSTimeZone defaultTimeZone];
//        [[UIApplication sharedApplication] scheduleLocalNotification:localNotification];
        
        
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
//    NSLog(@"sigin viewDidLoad");
//    UILocalNotification* localNotification = [[UILocalNotification alloc] init];
//    localNotification.fireDate = [NSDate dateWithTimeIntervalSinceNow:0];
//    localNotification.alertBody = @"ITSsigin viewDidLoad";
//    localNotification.timeZone = [NSTimeZone defaultTimeZone];
//    [[UIApplication sharedApplication] scheduleLocalNotification:localNotification];
    
    mainTabBarViewController = [self.storyboard instantiateViewControllerWithIdentifier:@"mainTabBarViewController"];
    self.navigationController.delegate = self;
    userNameTextField.delegate = self;
    passwordTextField.delegate = self;
    userNameTextField.returnKeyType = UIReturnKeyDone;
    passwordTextField.returnKeyType = UIReturnKeyDone;
    passwordTextField.secureTextEntry = YES;
    //[userNameTextField becomeFirstResponder];
    
    NSMutableAttributedString *titleString = [[NSMutableAttributedString alloc] initWithString:[[signUpButton titleLabel] text]];
    [titleString addAttribute:NSUnderlineStyleAttributeName value:[NSNumber numberWithInteger:NSUnderlineStyleSingle] range:NSMakeRange(0, [titleString length])];
    [signUpButton setAttributedTitle:titleString forState:UIControlStateNormal];
    
//    UIBarButtonItem *newBackButton = [[UIBarButtonItem alloc] initWithTitle:@"Home" style:UIBarButtonItemStyleBordered target:self action:@selector(home:)];
//    self.navigationItem.leftBarButtonItem=newBackButton;
    
    //self.navigationController.navigationBar.backItem.title = @"Sign Out";
    self.navigationItem.title = @"Sign In";
    // Do any additional setup after loading the view.
    
    signedIn = NO;
    //test connection
    [iTSConnectionManager connectToServer];
    //auto login
    userPlistDict = [NSMutableDictionary new];
    [self loadUserPlist];
    if([userPlistDict objectForKey:@"username"] != nil){
        userNameTextField.text = [userPlistDict objectForKey:@"username"];
        if([userPlistDict objectForKey:@"password"] != nil){
            passwordTextField.text = [userPlistDict objectForKey:@"password"];
        }
    }
    if([userPlistDict objectForKey:@"username"] != nil &&
       ![[userPlistDict objectForKey:@"username"] isEqualToString:@""] &&
       [userPlistDict objectForKey:@"password"] != nil &&
       ![[userPlistDict objectForKey:@"password"] isEqualToString:@""]){
        if([userPlistDict objectForKey:@"sessionToken"] != nil){
            //offline login
            [self offlineLogin];
        }else{
            [iTSConnectionManager verifyUser:[userPlistDict objectForKey:@"username"] withPassword:[userPlistDict objectForKey:@"password"]];
        }
    }
    
}

-(void)viewWillAppear:(BOOL)animated {
//    NSLog(@"sigin viewWillAppear");
//    UILocalNotification* localNotification = [[UILocalNotification alloc] init];
//    localNotification.fireDate = [NSDate dateWithTimeIntervalSinceNow:0];
//    localNotification.alertBody = @"ITSsigin viewWillAppear";
//    localNotification.timeZone = [NSTimeZone defaultTimeZone];
//    [[UIApplication sharedApplication] scheduleLocalNotification:localNotification];
    
    self.navigationItem.title = @"Sign In";
}

-(void)home:(UIBarButtonItem *)sender {
    [self.navigationController popToRootViewControllerAnimated:YES];
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


- (void)navigationController:(UINavigationController *)navigationController willShowViewController:(UIViewController *)viewController animated:(BOOL)animated{
    if([viewController isKindOfClass:[ITSSignInViewController class]]){
        //NSLog(@"Sign In Page");
        if(signedIn){
            //Sign Out
            [iTSBeaconManager stopMonitoring];
            iTSConnectionManager.iTSSessionToken = nil;
            [iTSConnectionManager clearUserData];
            [iTSConnectionManager clearRecordData];
            [iTSBeaconManager clearCarData];
            [passwordTextField setText:@""];
            [userPlistDict setObject:@"" forKey:@"password"];
            [userPlistDict removeObjectForKey:@"sessionToken"];
            [userPlistDict removeObjectForKey:@"userData"];
            [self saveUserPlist];
            signedIn = NO;
            //test connection
            [iTSConnectionManager connectToServer];
        }
    }
}

- (BOOL)textFieldShouldBeginEditing:(UITextField *)textField {
    messageLabel.text = @"";
    return YES;
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [self.view endEditing:YES];
    if([userNameTextField.text length] !=0 && [passwordTextField.text length] !=0 ){
        [iTSConnectionManager verifyUser:userNameTextField.text withPassword:passwordTextField.text];
    }else{
        messageLabel.text = @"empty username or password";
    }
    return YES;
}

- (IBAction)signInButtonClicked:(id)sender {
    [self.view endEditing:YES];
    if([userNameTextField.text length] !=0 && [passwordTextField.text length] !=0 ){
        [iTSConnectionManager verifyUser:userNameTextField.text withPassword:passwordTextField.text];
        //test connection
        [iTSConnectionManager connectToServer];
    }else{
        messageLabel.text = @"empty username or password";
    }
}

- (void) offlineLogin{
    NSLog(@"offline login");
    signedIn = YES;
    
    iTSConnectionManager.iTSSessionToken = [userPlistDict objectForKey:@"sessionToken"];
    [iTSConnectionManager setUserData:[userPlistDict objectForKey:@"userData"]];
    //[iTSConnectionManager printSomething];
    [iTSBeaconManager startMonitoring];
    messageLabel.text = @"signin success";
    //mainTabBarViewController = [self.storyboard instantiateViewControllerWithIdentifier:@"mainTabBarViewController"];
    [self.navigationController pushViewController:mainTabBarViewController animated:YES];
    messageLabel.text = @"";
}

- (IBAction)signUpButtonClicked:(id)sender {
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:@"http://wayne.cs.ucdavis.edu:5000/web_signupPage"]];
}

- (void) connectionTimerTimedOut {
    if(messageLabel != nil)
        messageLabel.text = @"connection fail";
}

- (void) callBackFromConnection:(NSDictionary *)iTSReceivedDict {
    if([[NSString stringWithFormat:@"%@", [iTSReceivedDict objectForKey:@"message"]] isEqualToString:@"connectionSuccess"]){
        NSLog(@"connection success");
        if(messageLabel != nil)
            messageLabel.text = @"";
    }else if([[NSString stringWithFormat:@"%@", [iTSReceivedDict objectForKey:@"message"]] isEqualToString:@"connectionFail"]){
        NSLog(@"connection fail");
        if(messageLabel != nil)
            messageLabel.text = @"connection fail";
    }
}

- (void) callBackFromSignin:(NSDictionary *)iTSReceivedDict {
    if([[NSString stringWithFormat:@"%@", [iTSReceivedDict objectForKey:@"message"]] isEqualToString:@"signinSuccess"]){
        NSLog(@"signin success");
        signedIn = YES;
        
        iTSConnectionManager.iTSSessionToken = [NSString stringWithFormat:@"%@", [[iTSReceivedDict objectForKey:@"verification"] objectForKey:@"token"]];
        [iTSConnectionManager setUserData:[iTSReceivedDict objectForKey:@"data"]];
        //[iTSConnectionManager printSomething];
        [userPlistDict setObject:userNameTextField.text forKey:@"username"];
        [userPlistDict setObject:userNameTextField.text forKey:@"password"];
        [userPlistDict setObject:[NSString stringWithString:iTSConnectionManager.iTSSessionToken] forKey:@"sessionToken"];
        [userPlistDict setObject:[[NSDictionary alloc] initWithDictionary:[iTSReceivedDict objectForKey:@"data"] copyItems:YES] forKey:@"userData"];
        [self saveUserPlist];
        
        [iTSBeaconManager startMonitoring];
        messageLabel.text = @"signin success";
        //mainTabBarViewController = [self.storyboard instantiateViewControllerWithIdentifier:@"mainTabBarViewController"];
        [self.navigationController pushViewController:mainTabBarViewController animated:YES];
        messageLabel.text = @"";
        /*for (UIViewController *v in mainTabBarViewController.viewControllers)
        {
            //UIViewController *vc = v;
            
            //        if ([v isKindOfClass:[UINavigationController class])
            //        {
            //            vc = [v visibleViewController];
            //        }
            
            if ([v isKindOfClass:[ITSBeaconViewController class]])
            {
                ITSBeaconViewController *myViewController = (ITSBeaconViewController *) v;
                [myViewController printSomething];
                // send iTSBeaconManager to every view controller in tabbarcontroller
            }
        }*/
    }else if([[NSString stringWithFormat:@"%@", [iTSReceivedDict objectForKey:@"message"]] isEqualToString:@"signinErrorFail"]){
        NSLog(@"signin error fail");
        signedIn = NO;
        [passwordTextField setText:@""];
        messageLabel.text = @"signin error fail";
    }
    else if([[NSString stringWithFormat:@"%@", [iTSReceivedDict objectForKey:@"message"]] isEqualToString:@"signinFail"]){
        NSLog(@"signin fail");
        signedIn = NO;
        [passwordTextField setText:@""];
        messageLabel.text = @"wrong username or password";
    }
}

-(void) loadUserPlist{
    NSString *rootPath = [NSSearchPathForDirectoriesInDomains
                          (NSDocumentDirectory,NSUserDomainMask, YES)
                          objectAtIndex:0];
    NSString *plistPath = [rootPath stringByAppendingPathComponent:@"user.plist"];
    if (![[NSFileManager defaultManager] fileExistsAtPath:plistPath])
    {
        plistPath = [[NSBundle mainBundle] pathForResource:@"user" ofType:@"plist"];
    }
    userPlistDict = [[NSMutableDictionary alloc] initWithContentsOfFile:plistPath];
    
    //NSLog(@"user plist:%@",userPlistDict);

}

-(void) saveUserPlist{
    if(userPlistDict != nil){
        NSString *SaveRootPath = [NSSearchPathForDirectoriesInDomains
                                  (NSDocumentDirectory,NSUserDomainMask, YES)
                                  objectAtIndex:0];
        NSString *SavePath = [SaveRootPath stringByAppendingPathComponent:@"user.plist"];
        [userPlistDict writeToFile:SavePath atomically:YES];
    }
}

@end

// Module dependencies 
var express    = require('express'),
    mysql      = require('mysql'),
    bodyParser = require('body-parser');
 
// Application initialization
 
var connection = mysql.createConnection({
        host     : 'localhost',
        port     : '8889',
        user     : 'root',
        password : 'Ching628'
    });
    
//var app = module.exports = express.createServer();
var express = require('express')
var http = require('http');
var app = express(); 
var server = http.createServer(app);
app.use(express.static(__dirname + '/'));

var globalToken = 'ITSTRIPPROJECT2014';

var signedIn = false;
 
// Database setup
connection.query('CREATE DATABASE IF NOT EXISTS test', function (err) {
    if (err) throw err;
    connection.query('USE test', function (err) {
        if (err) throw err;
        connection.query('CREATE TABLE IF NOT EXISTS households('
            + 'id INT(12) NOT NULL AUTO_INCREMENT,'
            + 'name VARCHAR(64),'
            + 'address VARCHAR(128),'
            + 'zipcode VARCHAR(8),'
            + 'timestamp DATETIME,'
            + 'PRIMARY KEY(id),'
            + 'INDEX (name)'
            +  ') ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_general_ci', function (err) {
                if (err) throw err;
            });
    });
});

connection.query('CREATE DATABASE IF NOT EXISTS test', function (err) {
    if (err) throw err;
    connection.query('USE test', function (err) {
        if (err) throw err;
        connection.query('CREATE TABLE IF NOT EXISTS users('
            + 'id INT(12) NOT NULL AUTO_INCREMENT,'
            + 'username VARCHAR(64),'
            + 'email VARCHAR(64),'
            + 'lname VARCHAR(32),'
            + 'fname VARCHAR(32),'
            + 'household_id INT(12),'
            + 'household_name VARCHAR(64),'
            + 'timestamp DATETIME,'
            + 'password VARCHAR(12),'
            + 'PRIMARY KEY(id),'
            + 'INDEX (username),'
            + 'CONSTRAINT `household_to_id_1` FOREIGN KEY (`household_id`) REFERENCES `households` (`id`)'
            +  ') ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_general_ci', function (err) {
                if (err) throw err;
            });
    });
});
 
connection.query('CREATE DATABASE IF NOT EXISTS test', function (err) {
    if (err) throw err;
    connection.query('USE test', function (err) {
        if (err) throw err;
        connection.query('CREATE TABLE IF NOT EXISTS records('
            + 'id INT NOT NULL AUTO_INCREMENT,'
            + 'user_id INT(12),'
            + 'user_username VARCHAR(64),'
            + 'user_email VARCHAR(64),'
            + 'user_lname VARCHAR(32),'
            + 'user_fname VARCHAR(32),'
            + 'household_id INT(12),'
            + 'household_name VARCHAR(64),'
            + 'type VARCHAR(8),'
            + 'latitude DECIMAL(12, 8),'
            + 'longitude DECIMAL(12, 8),'
            + 'timestamp DATETIME,'
            + 'bt_id VARCHAR(40),'
            + 'bt_major VARCHAR(16),'
            + 'bt_minor VARCHAR(16),'
            + 'PRIMARY KEY(id),'
            + 'CONSTRAINT `user_to_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),'
            + 'CONSTRAINT `household_to_id_2` FOREIGN KEY (`household_id`) REFERENCES `households` (`id`)'
            +  ') ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_general_ci', function (err) {
                if (err) throw err;
            });
    });
});
 
// Configuration
app.use(bodyParser());

//verify token - IMPORTANT!
var encryptArray = [2,6,1,9,3,3,5,7];
function getToken(tempId, tempUsername, tempEmail){
    var tempToken = '';
    var tempString = tempUsername.toString() + tempEmail.toString();
    var tempCount = 0;
    for(var i = 0; i<encryptArray.length;i++){
        tempCount += encryptArray[i];
        tempToken += tempString[tempCount % (tempString.length)];
    }
    return tempToken;
}

function verifyToken(checkGlobalToken, checkToken, tempId, tempUsername, tempEmail){
    checkGlobalToken = checkGlobalToken.toString();
    if (!verifyGlobalToken(checkGlobalToken)) return false;
    checkToken = checkToken.toString();
    var tempToken = '';
    var tempString = tempUsername.toString() + tempEmail.toString();
    var tempCount = 0;
    for(var i = 0; i<encryptArray.length;i++){
        tempCount += encryptArray[i];
        tempToken += tempString[tempCount % (tempString.length)];
    }
    tempToken = tempToken.toString();
    if(checkToken === tempToken)
        return true;
    return false;
}

function verifyTokenWithVerification(verification){
    var varifyingGlobalToken = verification.globalToken;
    var verifyingToken = verification.token;
    var verifyingId = verification.id;
    var verifyingUsername = verification.username;
    var verifyingUserEmail= verification.email;
    return verifyToken(varifyingGlobalToken, verifyingToken, verifyingId, verifyingUsername, verifyingUserEmail);
}

function verifyGlobalToken(checkGlobalToken){
    if(checkGlobalToken === globalToken)
        return true;
    return false;
}

//mobile
app.get('/mobile_connection', function(req, res) {
    res.json({message:'connectionSuccess'});
});

//signin page
app.post('/web_signin', function(req, res) {

});

//web 
// Main route sends our HTML file - web
 app.get('/', function(req, res) {
        res.sendfile(__dirname + '/public/index.html');
});

//signin page
app.post('/web_signin', function(req, res) {
    var checkGlobalToken = req.body.verification.globalToken.toString();
    if(!verifyGlobalToken(checkGlobalToken)){
        res.json({message:'signinGlobalTokenFail'});
    }
    console.log(req.body.data);
    connection.query('SELECT * FROM users WHERE username = ? AND password = ? LIMIT 1', [req.body.data.username, req.body.data.password],
    function (err, results){
        if (err){
            res.json({message: 'signinErrorFail'});
            return;
        }
        if (results.length == 0){
            console.log("Sign in Fail - " + req.body.data.username);
            res.json({message: 'signinFail'});
        }else{
            console.log("Sign in Success - " + req.body.data.username);
            var verification = {
                token: getToken(results[0].id,results[0].username,results[0].email), 
                id: results[0].id,
                username: results[0].username,
                email:results[0].email
            }
            var userData = {
                id: results[0].id,
                username: results[0].username,
                email:results[0].email,
                lname: results[0].lname,
                fname: results[0].fname,
                household_id: results[0].household_id,
                household_name: results[0].household_name,
                timestamp: results[0].timestamp
            }
            res.json({message:'signinSuccess', verification:verification, data:userData});
        }
    });
});

//signup page
app.get('/web_signupPage', function(req, res) {
    res.sendfile(__dirname + '/public/signup.html');
});

app.post('/web_signup', function(req, res) {
    var checkGlobalToken = req.body.verification.globalToken.toString();
    if(!verifyGlobalToken(checkGlobalToken)){
        res.json({message:'signupGlobalTokenFail'});
    }
    //check if username exist
    connection.query('SELECT * FROM users WHERE username = ? LIMIT 1', req.body.data.username,
    function (err, result0){
        if (err){
            res.json({message: 'signupErrorFail-0'});
            return;
        }

        //if user does exist, return fail
        if(result0.length != 0){
            res.json({message: 'signupUserExistsFail'});
            return;
        }

        //check household id and name conexist
        connection.query('SELECT * FROM households WHERE id = ? LIMIT 1', req.body.data.household_id,
        function (err, result1){
            if (err){
                res.json({message: 'signupErrorFail-1'});
                return;
            }
            //if household does not exist, create one
            if(result1.length == 0){
                console.log("Household Not Exist, Create One");
                //create one
                var householdPayload = {
                   name: req.body.data.household_name,
                   address: req.body.data.household_address,
                   zipcode: req.body.data.household_zipcode,
                   timestamp: req.body.data.timestamp
                }
                connection.query('INSERT INTO households SET ?', householdPayload, function (err, result2) {
                    if (err){
                        res.json({message:'signupErrorFail-2'});
                        return;
                    }
                    //create user
                    var signupPayload = {
                        username: req.body.data.username,
                        email: req.body.data.email,
                        lname: req.body.data.lname,
                        fname: req.body.data.fname,
                        household_id: result2.insertId,
                        household_name: req.body.data.household_name,
                        timestamp: req.body.data.timestamp,
                        password: req.body.data.password
                    };

                    connection.query('INSERT INTO users SET ?', signupPayload, function (err, result3) {
                        if (err){
                            res.json({message:'signupErrorFail-4'});
                            return;
                        }
                        res.json({message:'signupSuccess'});
                    });
                });
            }else{
                if(result1[0].name !== req.body.data.household_name){
                    console.log("household not match");
                    res.json({message:'signupHouseholdNotMatchFail'});
                    return;
                }
                //create user
                var signupPayload = {
                    username: req.body.data.username,
                    email: req.body.data.email,
                    lname: req.body.data.lname,
                    fname: req.body.data.fname,
                    household_id: req.body.data.household_id,
                    household_name: req.body.data.household_name,
                    timestamp: req.body.data.timestamp,
                    password: req.body.data.password
                };

                connection.query('INSERT INTO users SET ?', signupPayload, function (err, result4) {
                    if (err){
                        res.json({message:'signupErrorFail-4'});
                        return;
                    }
                    res.json({message:'signupSuccess'});
                });
            }
        }); 
    });
});

//signup search household
app.post('/web_signupSearch', function(req, res) {
    var checkGlobalToken = req.body.verification.globalToken.toString();
    if(!verifyGlobalToken(checkGlobalToken)){
        res.json({message:'signupSearchGlobalTokenFail'});
        return;
    }
    connection.query("SELECT * FROM households WHERE name LIKE ?", ("%"+req.body.data.toString()+"%"),
    function (err, results){
        if (err){
            throw err;
            res.json({message: 'signupSearchErrorFail'});
            return;
        }
        console.log("show results");
        console.log(results);
        if(results.length == 0){
            res.json({message:'signupSearchNoResultSuccess'});
        }else{
            res.json({message:'signupSearchSuccess', data:results}); 
        } 
    }); 
});
 
//user page
app.post('/web_showRecord', function (req, res) {
    if(!verifyTokenWithVerification(req.body.verification)){
        res.json({message:'showRecordGlobalTokenFail'});
    }
    connection.query('SELECT * FROM records WHERE user_id = ?', req.body.verification.id,
    function (err, results){
        if (err){
            res.json({message: 'showRecordErrorFail'});
            return;
        }
        res.json({message:'showRecordSuccess', data:results});  
    });
});

app.post('/web_showAllRecord', function (req, res) {
    if(!verifyTokenWithVerification(req.body.verification)){
        res.json({message:'showRecordGlobalTokenFail'});
    }
    connection.query('SELECT * FROM records',
    function (err, results){
        if (err){
            res.json({message: 'showAllRecordErrorFail'});
            return;
        }
        res.json({message:'showAllRecordSuccess', data:results});  
    });
});
 
app.post('/web_insertRecord', function (req, res) {
    if(!verifyTokenWithVerification(req.body.verification)){
        res.json({message:'insertRecordGlobalTokenFail'});
    }
    //check user and household coexist
    connection.query('SELECT * FROM users WHERE id = ? AND household_id = ? LIMIT 1', [req.body.data.user_id, req.body.data.household_id], 
        function (err, result1) {
            if (err){
                res.json({message: 'insertRecordErrorFail-1'});
                return;
            }
            if(result1.length==0){
                res.json({message: 'insertRecordNoUserHouseholdFail'});
                return;
            }else{
                connection.query('INSERT INTO records SET ?', req.body.data, function (err, result2) {
                    if (err){
                        res.json({message: 'insertRecordErrorFail-2'});
                        return;
                    }
                    //res.send('User added to database with ID: ' + result.insertId);
                    connection.query('SELECT * FROM records WHERE id = ?', result2.insertId, 
                    function (err, results){
                        if (err) throw err;
                        //for(i=0;i<results.length;i++)
                        //console.log('\Records:\n' + JSON.stringify(results[0]));
                        //res.send('Record1\tRecord2\tRecord3\nRecord1\tRecord2\tRecord3');
                        //res.send('\Records:\n' + JSON.stringify(results));
                        res.json({message:'insertRecordSuccess', data:results});    
                    });
                });
            }
        });
    
});
 
// Begin listening
 
var port = Number(process.env.PORT || 5000);
app.listen(port, function() {
  console.log("Listening on " + port);
});
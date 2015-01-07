// Module dependencies 
var express    = require('express'),
    mysql      = require('mysql'),
    bodyParser = require('body-parser');
 
// Application initialization
 
var connection;
var db_config = {
        host     : 'localhost',
        port     : '3306',
        user     : 'root',
        password : 'root',
        dateStrings: true
    };


//handle mysql disconnection
function handleDisconnect() {
    console.log("Create Connection");
    connection = mysql.createConnection(db_config); // Recreate the connection, since
                                                  // the old one cannot be reused.

  connection.connect(function(err) {              // The server is either down
    if(err) {                                     // or restarting (takes a while sometimes).
      console.log('error when connecting to db:', err);
      setTimeout(handleDisconnect, 2000); // We introduce a delay before attempting to reconnect,
    }                                     // to avoid a hot loop, and to allow our node script to
  });                                     // process asynchronous requests in the meantime.
                                          // If you're also serving http, display a 503 error.
  connection.on('error', function(err) {
    console.log('db error', err);
    if(err.code === 'PROTOCOL_CONNECTION_LOST') { // Connection to the MySQL server is usually
      console.log("Restart Connection");
      handleDisconnect();
      connection.query('USE its_trips', function (err) {
        if (err) throw err;
        });                                       // lost due to either server restart, or a
    } else {                                      // connnection idle timeout (the wait_timeout
      throw err;                                  // server variable configures this)
    }
  });
}

handleDisconnect();

// Database setup
connection.query('CREATE DATABASE IF NOT EXISTS its_trips', function (err) {
    if (err) throw err;
    connection.query('USE its_trips', function (err) {
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

connection.query('CREATE DATABASE IF NOT EXISTS its_trips', function (err) {
    if (err) throw err;
    connection.query('USE its_trips', function (err) {
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

connection.query('CREATE DATABASE IF NOT EXISTS its_trips', function (err) {
    if (err) throw err;
    connection.query('USE its_trips', function (err) {
        if (err) throw err;
        connection.query('CREATE TABLE IF NOT EXISTS cars('
            + 'id INT(12) NOT NULL AUTO_INCREMENT,'
            + 'bt_id VARCHAR(40),'
            + 'bt_major VARCHAR(16),'
            + 'bt_minor VARCHAR(16),'
            + 'bt_name VARCHAR(16),'
            + 'bt_threshold INT(4),'
            + 'bt_tolerance INT(4),'
            + 'household_id INT(12),'
            + 'household_name VARCHAR(64),'
            + 'timestamp DATETIME,'
            + 'parked_time DATETIME,'
            + 'parked_latitude DECIMAL(12, 8),'
            + 'parked_longitude DECIMAL(12, 8),'
            + 'PRIMARY KEY(id),'
            + 'INDEX (bt_major),'
            + 'INDEX (bt_minor),'
            + 'INDEX (bt_name),'
            + 'INDEX (household_id),'
            + 'CONSTRAINT `household_to_id_2` FOREIGN KEY (`household_id`) REFERENCES `households` (`id`)'
            +  ') ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_general_ci', function (err) {
                if (err) throw err;
            });
    });
});
 
connection.query('CREATE DATABASE IF NOT EXISTS its_trips', function (err) {
    if (err) throw err;
    connection.query('USE its_trips', function (err) {
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
            + 'bt_name VARCHAR(16),'
            + 'PRIMARY KEY(id),'
            + 'CONSTRAINT `user_to_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),'
            + 'CONSTRAINT `household_to_id_3` FOREIGN KEY (`household_id`) REFERENCES `households` (`id`)'
            +  ') ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_general_ci', function (err) {
                if (err) throw err;
            });
    });
});
    
//var app = module.exports = express.createServer();
var express = require('express')
var http = require('http');
var app = express(); 
var server = http.createServer(app);
app.use(express.static(__dirname + '/'));

var globalToken = 'ITSTRIPPROJECT2014';

var signedIn = false;

 
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
    res.json({mode:'connection', message:'connectionSuccess'});
});

//signin page
app.post('/mobile_signin', function(req, res) {
    var checkGlobalToken = req.body.verification.globalToken.toString();
    if(!verifyGlobalToken(checkGlobalToken)){
        res.json({mode:'signin', message:'signinGlobalTokenFail'});
        return;
    }
    console.log(req.body);
    
    connection.query('SELECT * FROM users WHERE username = ? AND password = ? LIMIT 1', [req.body.data.username, req.body.data.password],
    function (err, results){
        if (err){
            console.log("Signin Error Fail - " + req.body.data.username);
            res.json({mode:'signin', message: 'signinErrorFail'});
            return;
        }
        if (results.length == 0){
            console.log("Sign in Fail - " + req.body.data.username);
            res.json({mode:'signin', message: 'signinFail'});
        }else{
            console.log("Signin Success - " + req.body.data.username);
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
            res.json({mode:'signin', message:'signinSuccess', verification:verification, data:userData});
        }
    });
});

app.post('/mobile_insertCar', function (req, res) {
    if(!verifyTokenWithVerification(req.body.verification)){
        res.json({mode:'insertCar', message:'insertCarGlobalTokenFail'});
        return;
    }

    //check household exist
    connection.query('SELECT * FROM households WHERE id = ? LIMIT 1', req.body.data.household_id, 
    function (err, result1) {
        if (err){
            res.json({mode:'insertCar', message: 'insertCarErrorFail-1'});
            return;
        }
        if(result1.length==0){
            res.json({mode:'insertCar', message: 'insertCarNoHouseholdFail'});
            return;
        }else{
            //check major, minor, and household coexist
            connection.query('SELECT * FROM cars WHERE bt_major = ? AND bt_minor = ? AND household_id = ? LIMIT 1',
            [req.body.data.bt_major, req.body.data.bt_minor, req.body.data.household_id],
            function (err, result2){
                if (err){
                    res.json({mode:'insertCar', message: 'insertCarErrorFail-2'});
                    return;
                }
                if(result2.length==0){
                    connection.query('INSERT INTO cars SET ?', req.body.data, function (err, result3) {
                        if (err){
                            res.json({mode:'insertCar', message: 'insertCarErrorFail-3'});
                            return;
                        }
                        res.json({mode:'insertCar', message:'insertCarSuccess'});
                    });
                }else{
                    connection.query('UPDATE cars SET bt_name = ?, bt_threshold = ?, bt_tolerance = ? WHERE id = ? LIMIT 1',
                        [req.body.data.bt_name, req.body.data.bt_threshold, req.body.data.bt_tolerance, result2[0].id], function (err, result4) {
                        if (err){
                            res.json({mode:'insertCar', message: 'insertCarErrorFail-4'});
                            return;
                        }
                        res.json({mode:'insertCar', message:'insertCarSuccess'});
                    });
                } 
            });
        }
    });
});

app.post('/mobile_showCar', function (req, res) {
    if(!verifyTokenWithVerification(req.body.verification)){
        res.json({mode:'showCar', message:'showCarGlobalTokenFail'});
        return;
    }

    connection.query('SELECT * FROM cars WHERE household_id = ?', req.body.data.household_id, 
    function (err, results) {
        if (err){
            res.json({mode:'showCar', message: 'showCarErrorFail'});
            return;
        }
        res.json({mode:'showCar', message:'showCarSuccess', data:results});
    });
});


app.post('/mobile_insertRecord', function (req, res) {
    if(!verifyTokenWithVerification(req.body.verification)){
        res.json({mode:'insertRecord', message:'insertRecordGlobalTokenFail'});
        return;
    }

    //check user and household coexist
    connection.query('SELECT * FROM users WHERE id = ? AND household_id = ? LIMIT 1', [req.body.data.user_id, req.body.data.household_id], 
    function (err, result1) {
        if (err){
            res.json({mode:'insertRecord', message: 'insertRecordErrorFail-1'});
            return;
        }
        if(result1.length==0){
            res.json({mode:'insertRecord', message: 'insertRecordNoUserHouseholdFail'});
            return;
        }else{

            //check car parked location *************************
            if(req.body.data.type == "start"){
                //console.log("check car parked: start ***********************");
                connection.query('SELECT * FROM cars WHERE bt_major = ? AND bt_minor = ? LIMIT 1', [req.body.data.bt_major, req.body.data.bt_minor], 
                function (err, result2) {
                    if (err){
                        //res.json({mode:'insertRecord', message: 'insertRecordErrorFail-2'});
                        //insert record ***************************************************************************
                        connection.query('INSERT INTO records SET ?', req.body.data, function (err, result3) {
                            if (err){
                                res.json({mode:'insertRecord', message: 'insertRecordErrorFail-3'});
                                return;
                            }
                            res.json({mode:'insertRecord', message:'insertRecordSuccess'});    
                        });
                        //end insert record ***********************************************************************
                    }
                    if(result2.length==0){
                        //res.json({mode:'insertRecord', message: 'insertRecordNoCarFail'});
                        //insert record ***************************************************************************
                        connection.query('INSERT INTO records SET ?', req.body.data, function (err, result3) {
                            if (err){
                                res.json({mode:'insertRecord', message: 'insertRecordErrorFail-3'});
                                return;
                            }
                            res.json({mode:'insertRecord', message:'insertRecordSuccess'});    
                        });
                        //end insert record ***********************************************************************
                    }else{
                        //console.log("checking car parked location..");
                        var parked_time1 = result2[0].parked_time;
                        var lat1 = result2[0].parked_latitude;
                        var long1 = result2[0].parked_longitude;
                        var parked_time2 = req.body.data.timestamp;
                        var lat2 = req.body.data.latitude;
                        var long2 = req.body.data.longitude;
                        if(parked_time1 == undefined){
                            connection.query('UPDATE cars SET parked_time = ?, parked_latitude = ?, parked_longitude = ? WHERE bt_major = ? AND bt_minor = ?',
                                [req.body.data.timestamp, req.body.data.latitude, req.body.data.longitude, req.body.data.bt_major, req.body.data.bt_minor], function (err, result2_1) {
                                if (err){
                                    //res.json({mode:'insertCar', message: 'insertCarErrorFail-2-1'});
                                }
                                //insert record ***************************************************************************
                                connection.query('INSERT INTO records SET ?', req.body.data, function (err, result3) {
                                    if (err){
                                        res.json({mode:'insertRecord', message: 'insertRecordErrorFail-3'});
                                        return;
                                    }
                                    res.json({mode:'insertRecord', message:'insertRecordSuccess'});    
                                });
                                //end insert record ***********************************************************************
                            });
                        }
                        else{
                            //get distance
                            var R = 3969.6; // mile, = 6371 km
                            var φ1 = lat1 * Math.PI / 180.0;
                            var φ2 = lat2 * Math.PI / 180.0;
                            var Δφ = (lat2-lat1) * Math.PI / 180.0;
                            var Δλ = (long2-long1) * Math.PI / 180.0;

                            var a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
                                    Math.cos(φ1) * Math.cos(φ2) *
                                    Math.sin(Δλ/2) * Math.sin(Δλ/2);
                            var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

                            var d = R * c;

                            // console.log("distance = " + d);

                            if(d < 1.0){ //less than 1 mile
                                connection.query('UPDATE cars SET parked_time = ? WHERE bt_major = ? AND bt_minor = ?',
                                    [req.body.data.timestamp, req.body.data.bt_major, req.body.data.bt_minor], function (err, result2_2) {
                                    if (err){
                                        //res.json({mode:'insertCar', message: 'insertCarErrorFail-2-2'});
                                    }else{
                                        req.body.data.latitude = lat1;
                                        req.body.data.longitude = long1;
                                    }
                                    //insert record ***************************************************************************
                                    connection.query('INSERT INTO records SET ?', req.body.data, function (err, result3) {
                                        if (err){
                                            res.json({mode:'insertRecord', message: 'insertRecordErrorFail-3'});
                                            return;
                                        }
                                        res.json({mode:'insertRecord', message:'insertRecordSuccess'});    
                                    });
                                    //end insert record ***********************************************************************
                                });
                            }else{
                                //insert record ***************************************************************************
                                connection.query('INSERT INTO records SET ?', req.body.data, function (err, result3) {
                                    if (err){
                                        res.json({mode:'insertRecord', message: 'insertRecordErrorFail-3'});
                                        return;
                                    }
                                    res.json({mode:'insertRecord', message:'insertRecordSuccess'});    
                                });
                                //end insert record ***********************************************************************
                            }
                        }
                    }
                });
            }else if(req.body.data.type == "end"){
                // console.log("check car parked: end *************************");
                connection.query('SELECT * FROM cars WHERE bt_major = ? AND bt_minor = ? LIMIT 1', [req.body.data.bt_major, req.body.data.bt_minor], 
                function (err, result3) {
                    if (err){
                        //res.json({mode:'insertRecord', message: 'insertRecordErrorFail3'});

                        //insert record ***************************************************************************
                        connection.query('INSERT INTO records SET ?', req.body.data, function (err, result3) {
                            if (err){
                                res.json({mode:'insertRecord', message: 'insertRecordErrorFail-3'});
                                return;
                            }
                            res.json({mode:'insertRecord', message:'insertRecordSuccess'});    
                        });
                        //end insert record ***********************************************************************

                    }
                    if(result3.length==0){
                        //res.json({mode:'insertRecord', message: 'insertRecordNoCarFail'});

                        //insert record ***************************************************************************
                        connection.query('INSERT INTO records SET ?', req.body.data, function (err, result3) {
                            if (err){
                                res.json({mode:'insertRecord', message: 'insertRecordErrorFail-3'});
                                return;
                            }
                            res.json({mode:'insertRecord', message:'insertRecordSuccess'});    
                        });
                        //end insert record ***********************************************************************

                    }else{
                        connection.query('UPDATE cars SET parked_time = ?, parked_latitude = ?, parked_longitude = ? WHERE bt_major = ? AND bt_minor = ?',
                            [req.body.data.timestamp, req.body.data.latitude, req.body.data.longitude, req.body.data.bt_major, req.body.data.bt_minor], function (err, result3_1) {
                            if (err){
                                //res.json({mode:'insertCar', message: 'insertCarErrorFail-3-1'});
                            }
                            //insert record ***************************************************************************
                            connection.query('INSERT INTO records SET ?', req.body.data, function (err, result3) {
                                if (err){
                                    res.json({mode:'insertRecord', message: 'insertRecordErrorFail-3'});
                                    return;
                                }
                                res.json({mode:'insertRecord', message:'insertRecordSuccess'});    
                            });
                            //end insert record ***********************************************************************
                        });
                    }
                });
            }
            //end check car parked location *************************
        }
    });
    
});


app.post('/mobile_insertStoredRecord', function (req, res) {
    if(!verifyTokenWithVerification(req.body.verification)){
        res.json({mode:'insertStoredRecord', message:'insertStoredRecordGlobalTokenFail'});
        return;
    }
    var i = 0;
    while(req.body.data[i] !== undefined){
        (function(thisBodyData){
        //check car parked location *************************
        if(thisBodyData.type == "start"){
            // console.log("check car parked: start ***********************");
            connection.query('SELECT * FROM cars WHERE bt_major = ? AND bt_minor = ? LIMIT 1', [thisBodyData.bt_major, thisBodyData.bt_minor], 
            function (err, result2) {
                if (err){
                    //res.json({mode:'insertRecord', message: 'insertRecordErrorFail-2'});
                    //insert record ***************************************************************************
                    connection.query('INSERT INTO records SET ?', thisBodyData, function (err, result3) {
                        if (err){
                            res.json({mode:'insertStoredRecord', message: 'insertStoredRecordErrorFail'});
                            return;
                        }  
                    });
                    //end insert record ***********************************************************************
                }
                if(result2.length==0){
                    //res.json({mode:'insertRecord', message: 'insertRecordNoCarFail'});
                    //insert record ***************************************************************************
                    connection.query('INSERT INTO records SET ?', thisBodyData, function (err, result3) {
                        if (err){
                            res.json({mode:'insertStoredRecord', message: 'insertStoredRecordErrorFail'});
                            return;
                        }  
                    });
                    //end insert record ***********************************************************************
                }else{
                    var parked_time1 = result2[0].parked_time;
                    var lat1 = result2[0].parked_latitude;
                    var long1 = result2[0].parked_longitude;
                    var parked_time2 = thisBodyData.timestamp;
                    var lat2 = thisBodyData.latitude;
                    var long2 = thisBodyData.longitude;
                    if(parked_time1 == undefined){
                        connection.query('UPDATE cars SET parked_time = ?, parked_latitude = ?, parked_longitude = ? WHERE bt_major = ? AND bt_minor = ?',
                            [thisBodyData.timestamp, thisBodyData.latitude, thisBodyData.longitude, thisBodyData.bt_major, thisBodyData.bt_minor], function (err, result2_1) {
                            if (err){
                                //res.json({mode:'insertCar', message: 'insertCarErrorFail-2-1'});
                            }
                            //insert record ***************************************************************************
                            connection.query('INSERT INTO records SET ?', thisBodyData, function (err, result3) {
                                if (err){
                                    res.json({mode:'insertStoredRecord', message: 'insertStoredRecordErrorFail'});
                                    return;
                                }  
                            });
                            //end insert record ***********************************************************************
                        });
                    }
                    else{
                        //get distance
                        var R = 3969.6; // mile = 6371 km
                        var φ1 = lat1 * Math.PI / 180.0;
                        var φ2 = lat2 * Math.PI / 180.0;
                        var Δφ = (lat2-lat1) * Math.PI / 180.0;
                        var Δλ = (long2-long1) * Math.PI / 180.0;

                        var a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
                                Math.cos(φ1) * Math.cos(φ2) *
                                Math.sin(Δλ/2) * Math.sin(Δλ/2);
                        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

                        var d = R * c;
                        if(d < 1.0){ //less than 1 mile
                            connection.query('UPDATE cars SET parked_time = ? WHERE bt_major = ? AND bt_minor = ?',
                                [thisBodyData.timestamp, thisBodyData.bt_major, thisBodyData.bt_minor], function (err, result2_2) {
                                if (err){
                                    //res.json({mode:'insertCar', message: 'insertCarErrorFail-2-2'});
                                }else{
                                    thisBodyData.latitude = lat1;
                                    thisBodyData.longitude = long1;
                                }
                                //insert record ***************************************************************************
                                connection.query('INSERT INTO records SET ?', thisBodyData, function (err, result3) {
                                    if (err){
                                        res.json({mode:'insertStoredRecord', message: 'insertStoredRecordErrorFail'});
                                        return;
                                    }  
                                });
                                //end insert record ***********************************************************************
                            });
                        }else{
                            //insert record ***************************************************************************
                            connection.query('INSERT INTO records SET ?', thisBodyData, function (err, result3) {
                                if (err){
                                    res.json({mode:'insertStoredRecord', message: 'insertStoredRecordErrorFail'});
                                    return;
                                }  
                            });
                            //end insert record ***********************************************************************
                        }
                    }
                }
            });
        }else if(thisBodyData.type == "end"){
            // console.log("check car parked: end *************************");
            connection.query('SELECT * FROM cars WHERE bt_major = ? AND bt_minor = ? LIMIT 1', [thisBodyData.bt_major, thisBodyData.bt_minor], 
            function (err, result3) {
                if (err){
                    //res.json({mode:'insertRecord', message: 'insertRecordErrorFail3'});
                    //insert record ***************************************************************************
                    connection.query('INSERT INTO records SET ?', thisBodyData, function (err, result3) {
                        if (err){
                            res.json({mode:'insertStoredRecord', message: 'insertStoredRecordErrorFail'});
                            return;
                        }  
                    });
                    //end insert record ***********************************************************************
                }
                if(result3.length==0){
                    //res.json({mode:'insertRecord', message: 'insertRecordNoCarFail'});
                    //insert record ***************************************************************************
                    connection.query('INSERT INTO records SET ?', thisBodyData, function (err, result3) {
                        if (err){
                            res.json({mode:'insertStoredRecord', message: 'insertStoredRecordErrorFail'});
                            return;
                        }  
                    });
                    //end insert record ***********************************************************************
                }else{
                    connection.query('UPDATE cars SET parked_time = ?, parked_latitude = ?, parked_longitude = ? WHERE bt_major = ? AND bt_minor = ?',
                        [thisBodyData.timestamp, thisBodyData.latitude, thisBodyData.longitude, thisBodyData.bt_major, thisBodyData.bt_minor], function (err, result3_1) {
                        if (err){
                            //res.json({mode:'insertCar', message: 'insertCarErrorFail-3-1'});
                        }
                        //insert record ***************************************************************************
                        connection.query('INSERT INTO records SET ?', thisBodyData, function (err, result3) {
                            if (err){
                                res.json({mode:'insertStoredRecord', message: 'insertStoredRecordErrorFail'});
                                return;
                            }  
                        });
                        //end insert record ***********************************************************************
                    });
                }
            });
        }
        //end check car parked location *************************

        })(req.body.data[i]);

        sleep(100);
        i++;
    }
    res.json({mode:'insertStoredRecord', message:'insertStoredRecordSuccess'}); 
});


//work around
function sleep(milliseconds) {
  var start = new Date().getTime();
  for (var i = 0; i < 1e7; i++) {
    if ((new Date().getTime() - start) > milliseconds){
      break;
    }
  }
}


app.post('/mobile_showRecord', function (req, res) {
    if(!verifyTokenWithVerification(req.body.verification)){
        res.json({mode:'showRecord', message:'showRecordGlobalTokenFail'});
        return;
    }
    connection.query('SELECT * FROM records WHERE user_id = ?', req.body.verification.id,
    function (err, results){
        if (err){
            res.json({mode:'showRecord', message: 'showRecordErrorFail'});
            return;
        }
        res.json({mode:'showRecord', message:'showRecordSuccess', data:results});  
    });
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
        return;
    }
    console.log(req.body.data);
    connection.query('SELECT * FROM users WHERE username = ? AND password = ? LIMIT 1', [req.body.data.username, req.body.data.password],
    function (err, results){
        if (err){
            console.log("Signin Error Fail - " + req.body.data.username);
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
        return;
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
        return;
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
        return;
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
        return;
    }

    //console.log("web");
    //console.log(req.body);
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


//check exception
process.on('uncaughtException', function (err) {
    console.log(err);
});

//exit process gracefully
process.on('SIGINT', function() {
    console.log("\nGracefully shutting down from SIGINT (Ctrl+C)");

    console.log("Exiting...");
    process.exit();
});
 
// Begin listening
 
var port = Number(process.env.PORT || 5000);
app.listen(port, function() {
  console.log("ITS Trip Server Listening on " + port);
});
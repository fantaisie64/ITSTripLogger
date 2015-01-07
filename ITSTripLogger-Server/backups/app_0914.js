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

var signedIn = false;
 
// Database setup
connection.query('CREATE DATABASE IF NOT EXISTS test', function (err) {
    if (err) throw err;
    connection.query('USE test', function (err) {
        if (err) throw err;
        connection.query('CREATE TABLE IF NOT EXISTS users('
            + 'id INT(12) NOT NULL AUTO_INCREMENT,'
            + 'PRIMARY KEY(id),'
            + 'username VARCHAR(64),'
            + 'email VARCHAR(64),'
            + 'lname VARCHAR(32),'
            + 'fname VARCHAR(32),'
            + 'household_id INT(12),'
            + 'household_name VARCHAR(32),'
            + 'password INT(12),'
            + 'time_stamp DATETIME'
            +  ')', function (err) {
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
            + 'PRIMARY KEY(id),'
            + 'user_id INT(12),'
            + 'user_username VARCHAR(64),'
            + 'user_email VARCHAR(64),'
            + 'user_lname VARCHAR(32),'
            + 'user_fname VARCHAR(32),'
            + 'latitude DECIMAL(12, 8),'
            + 'longitude DECIMAL(12, 8),'
            + 'time_stamp DATETIME,'
            + 'bt_id INT(32),'
            + 'bt_major INT(32),'
            + 'bt_minor INT(32)'
            +  ')', function (err) {
                if (err) throw err;
            });
    });
});
 
// Configuration
app.use(bodyParser());
 
// Main route sends our HTML file
 
app.get('/', function(req, res) {
    if(signedIn){
        console.log("signedIn");
        res.sendfile(__dirname + '/public/index.html');
    }
    else{
        console.log("not signedIn");
        res.sendfile(__dirname + '/public/signin.html');
    }
});

app.post('/web_signin', function(req, res) {
   // connection.query('SELECT * FROM users WHERE username = ? AND password = ? LIMIT 1', req.body.username, req.body.password,
   //          function (err, results){
   //              if (err) throw err;
   //              res.redirect(__dirname + '/public/index.html');
   //          });
    signedIn = true;
    res.redirect('/');
});
 
// Update MySQL database

app.post('/init', function (req, res) {
            connection.query('SELECT * FROM records',
            function (err, results){
            	if (err) throw err;
            	res.json(results);	
            });
});
 
app.post('/users', function (req, res) {
    connection.query('INSERT INTO records SET ?', req.body, 
        function (err, result) {
            if (err) throw err;
            //res.send('User added to database with ID: ' + result.insertId);
            connection.query('SELECT * FROM records WHERE id = ?', result.insertId, 
            function (err, results){
            	if (err) throw err;
            	//for(i=0;i<results.length;i++)
            	//console.log('\Records:\n' + JSON.stringify(results[0]));
            	//res.send('Record1\tRecord2\tRecord3\nRecord1\tRecord2\tRecord3');
            	//res.send('\Records:\n' + JSON.stringify(results));
            	res.json(results);	
            });
        }
    );
});
 
// Begin listening
 
var port = Number(process.env.PORT || 5000);
app.listen(port, function() {
  console.log("Listening on " + port);
});
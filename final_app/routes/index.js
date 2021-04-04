const express = require('express');
const router = express.Router();
const { ensureAuthenticated, forwardAuthenticated } = require('../config/auth');
const admin = require('firebase-admin');

var name=[];
var email=[];
var date=[];
var time=[];
var origin=[];
var problem = [];
var route = [];


let serviceAccount = require('./cis350s20-22-firebase-adminsdk-zvq9k-61edd16ac2.json');
console.log("hello1");
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
  
});
console.log("hello2");
let fb = admin.firestore();
console.log("hello3");
var refReport = fb.collection('Reports');

refReport.get()
  .then((snapshot) => {
    snapshot.forEach((doc) => {

      //console.log(doc.id, '=>', doc.data());
      name.push(doc.data().name);
      email.push(doc.data().email);
      date.push(doc.data().date);
      time.push(doc.data().time);
      route.push(doc.data().route);
      origin.push(doc.data().origin);
      problem.push(doc.data().problem);
      
    });
  })
  .catch((err) => {
    console.log('Error getting documents', err);
  });

  
  var userEmail=[];
  var emergencyMobile=[];
  var firstName=[];
  var lastName=[];
  var mobile = [];
  var userPassword = [];

var refReportUsers = fb.collection('users');
refReportUsers.get()
  .then((snapshot) => {
    snapshot.forEach((doc) => {

      userEmail.push(doc.data().email);
      emergencyMobile.push(doc.data().emergency_mobile);
      firstName.push(doc.data().first_name);
      lastName.push(doc.data().last_name);
      mobile.push(doc.data().mobile);
      userPassword.push(doc.data().password);
      
    });
  })
  .catch((err) => {
    console.log('Error getting documents', err);
  });


// Welcome Page
router.get('/', forwardAuthenticated, (req, res) => res.render('welcome'));

// Dashboard
router.get('/dashboard', ensureAuthenticated, (req, res) =>
  res.render('dashboard', {
    user: req.user
  })
);

// Reports
router.get('/reports', ensureAuthenticated, (req, res) =>
  res.render('reports', {"name":name, "email":email, "date":date, "time":time, 
                        "origin":origin, "problem":problem, "route":route})

);

//UserManagement
router.get('/userManagement', ensureAuthenticated, (req, res) =>
  res.render('userManagement', {"email":userEmail, "emergencyMobile": emergencyMobile, 
                                "firstName":firstName, "lastName":lastName, "mobile":mobile, 
                                "password":userPassword})
);
module.exports = router;

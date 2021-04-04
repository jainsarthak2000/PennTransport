// set up Express
var express = require('express');
var app = express();

var mongoose = require('mongoose');
mongoose.connect('mongodb+srv://sarthakj:vardhman75V@cluster0-7oqlj.mongodb.net/test?retryWrites=true&w=majority');

//set up mongoose
//var mongoose = require('mongoose');

// the host:port must match the location where you are running MongoDB
// the "myDatabase" part can be anything you like
//
//mongoose.connection.once('open', function(){console.log("akhil is a dumbass")})

// const MongoClient = require('mongodb').MongoClient;
// const uri = "mongodb+srv://sarthakj:vardhman75V@cluster0-7oqlj.mongodb.net/test?retryWrites=true&w=majority";
// const client = new MongoClient(uri, { useNewUrlParser: true });
// client.connect(err => {
//   const collection = client.db("test").collection("devices");
//   // perform actions on the collection object
//   client.close();
// });


// set up EJS
app.set('view engine', 'ejs');

// set up BodyParser
var bodyParser = require('body-parser');
app.use(bodyParser.urlencoded({ extended: true }));


// import the Person class from Person.js
var Person = require('./Person.js');

/***************************************/

// route for creating a new person
// this is the action of the "create new person" form
app.post('/create', (req, res) => {
	console.log(req.body) //this will help us check if anything is being sent from the client side.
	// construct the Person from the form data which is in the request body
	var newPerson = new Person ({
		firstName: req.body.firstName,
		
		lastName: req.body.lastName,
		email: req.body.email,
		password: req.body.password,
		mobile: req.body.mobile,
		emergencyMobile: req.body.emergencyMobile
        
	    });

	// save the person to the database
	newPerson.save( );
		// display the "successfull created" page using EJS
		res.render('created', {person : newPerson});
		 
    
});


// route for showing all the people
app.use('/all', (req, res) => {
    
	// find all the Person objects in the database
	Person.find( {}, (err, persons) => {
		if (err) {
		    res.type('html').status(200);
		    console.log('uh oh' + err);
		    res.write(err);
		}
		else {
		    if (persons.length == 0) {
			res.type('html').status(200);
			res.write('There are no people');
			res.end();
			return;
		    }
		    // use EJS to show all the people
		    res.render('all', { persons: persons });

		}
	    }).sort({ 'age': 'asc' }); // this sorts them BEFORE rendering the results
    });

// route for accessing data via the web api
// to use this, make a request for /api to get an array of all Person objects
// or /api?name=[whatever] to get a single object
app.use('/api', (req, res) => {
	console.log("LOOKING FOR SOMETHING?");

	// construct the query object
	var queryObject = {};
	if (req.query.name) {
	    // if there's a name in the query parameter, use it here
	    queryObject = { "name" : req.query.name };
	}
    
	Person.find( queryObject, (err, persons) => {
		console.log(persons);
		if (err) {
		    console.log('uh oh' + err);
		    res.json({});
		}
		else if (persons.length == 0) {
		    // no objects found, so send back empty json
		    res.json({});
		}
		else if (persons.length == 1 ) {
		    var person = persons[0];
		    // send back a single JSON object
		    res.json( { "name" : person.name , "age" : person.age } );
		}
		else {
		    // construct an array out of the result
		    var returnArray = [];
		    persons.forEach( (person) => {
			    returnArray.push( { "name" : person.name, "age" : person.age } );
			});
		    // send it back as JSON Array
		    res.json(returnArray); 
		}
		
	    });
    });




/*************************************************/

app.use('/public', express.static('public'));

app.use('/', (req, res) => { res.redirect('/public/personform.html'); } );

app.listen(3000,  () => {
	console.log('Listening on port 3000');
    });

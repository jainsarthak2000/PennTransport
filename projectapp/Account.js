var mongoose = require('mongoose');
mongoose.connect('mongodb://localhost:27017/350S20-22');
var Schema = mongoose.Schema;

var accountSchema = new Schema({
    firstName: {type: String, required: true, unique: false},
    lastName: {type: String, required: true, unique: false},
    email: {type: String, required: true, unique: true},
    password: {type: String, required: true, unique: false},
    mobile: {type: String, required: true, unique: false},
    emergencyMobile: {type: String, required: true, unique: false}
});

module.exports = mongoose.model('Account', accountSchema);
var mongoose = require('mongoose');

var Schema = mongoose.Schema;

var personSchema = new Schema({
              
    firstName: { type: String, required:true },
    lastName: { type: String, required:true },
    email: { type: String, required:true },
    password: { type: String, required:true },
    mobile:{ type: String },
    emergencyMobile:{ type: String },
    });

    

// export personSchema as a class called Person
module.exports = mongoose.model('Person', personSchema);

personSchema.methods.standardizeName = function() {
    this.name = this.name.toLowerCase();
    return this.name;
}

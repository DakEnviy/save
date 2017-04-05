var bookshelf = require('../config/bookshelf');
var Item = require('./item');

var Bot = bookshelf.Model.extend({
  tableName: 'bots',
  items: function() {
    return this.hasMany(Item);
  }
});

module.exports = Bot;

var bookshelf = require('../config/bookshelf');
var Achievement = require('./achievement');

var Pack = bookshelf.Model.extend({
  tableName: 'packs',
  achievements: function() {
    return this.hasMany(Achievement);
  }
});

module.exports = Pack;
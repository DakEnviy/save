var bookshelf = require('../config/bookshelf');

var Achievement = bookshelf.Model.extend({
  tableName: 'achievements'
});

module.exports = Achievement;
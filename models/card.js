var bookshelf = require('../config/bookshelf');

var Card = bookshelf.Model.extend({
  tableName: 'cards',
  price: function() {
    this._price = this.get('price');
    return Promise.resolve(this);
  }
});

module.exports = Card;
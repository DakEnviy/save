var bookshelf = require('../config/bookshelf');
var Item = require('./item');
var Card = require('./card');
var User = require('./user');
var _Game = require('./game');

module.exports = function(name) {
  var Bet = bookshelf.Model.extend({
    tableName: 'bets' + name,
    hasTimestamps: true,
    items: function() {
      var itemsId = JSON.parse(this.get('items')), _ps = [];
      for (var itemId of itemsId) {
        if (itemId < 0) _ps.push(new Card({ id: -itemId }).fetch());
        else _ps.push(new Item({ id: itemId }).fetch());
      }
      return Promise.all(_ps);
    },
    user: function() {
      return this.belongsTo(User);
    },
    game: function() {
      var self = this;
      return this.belongsTo(_Game(name))
      .fetch(function(game) {
        self._game = game;
        return self;
      });
    }
  });

  return Bet;
};

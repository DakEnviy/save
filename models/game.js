var bookshelf = require('../config/bookshelf');
var Setting = require('./setting');
var User = require('./user');

module.exports = function(name) {
  var Bet = require('./bet')(name);
  var Game = bookshelf.Model.extend({
    tableName: 'games' + name,
    hasTimestamps: true,
    Bet: Bet,
    bets: function() {
      return this.hasMany(Bet, 'game_id');
    },
    items: function() {
      return this.bets()
      .fetch()
      .then(function(bets) {
        var _ps = [];
        for (var bet of bets.models) {
          _ps.push(bet.items());
        }
        return Promise.all(_ps)
        .then(function(betsItems) {
          var items = [];
          for (var betItems of betsItems) {
            items = items.concat(betItems);
          }
          return items;
        });
      });
    },
    itemsCount: function() {
      return this.bets()
      .fetch()
      .then(function(bets) {
        var itemsCount = 0;
        for (var bet of bets.models) {
          itemsCount += bet.get('items_count');
        }
        return itemsCount;
      });
    }
  }, {
    getCurrent: function() {
      return Game.count()
      .then(function(id) {
        if (id === 0) return new Game().save();
        return new Game({ id: id })
        .fetch({ withRelated: 'bets' })
        .then(function(game) {
          if (!game) return Promise.reject('GAME_ISNT_FOUND');
          if (game.get('status') === 3) return new Game().save();
          return game;
        });
      });
    },
    init: function(req) {
      return Game.getCurrent()
      .then(Game.serializeGame)
      .then(function(serGame) {
        req['game' + name] = serGame;
        return req;
      });
    },
    serializeGame: function(game) {
      var bets, betsItems, itemsCount = 0, players = {}, serBets = [];
      return game.bets().orderBy('created_at', 'DESC')
      .fetch({ withRelated: 'user' })
      .then(function(_bets) { bets = _bets.models;
        var _ps = [];
        for (var bet of bets) {
          _ps.push(bet.items());
        }
        return Promise.all(_ps)
        .then(function(betsItems) {
          var _ps = [];
          for (var betItems of betsItems) {
            var _pss = [];
            for (var item of betItems) {
              if (item.tableName === 'items') _pss.push(item.info());
              else _pss.push(Promise.resolve(item));
            }
            _ps.push(Promise.all(_pss));
          }
          return Promise.all(_ps);
        });
      })
      .then(function(_betsItems) { betsItems = _betsItems;
        var t = [], _ps = [];
        for (var betItems of betsItems) {
          for (var item of betItems) {
            if (t.indexOf(item.id) !== -1 || item.tableName !== 'items') continue;
            t.push(item.id);
            _ps.push(item.price(true));
          }
        }
        return Promise.all(_ps)
        .then(function(items) {
          var prices = {};
          for (var item of items) {
            prices[item.id] = item.get('price');
          }
          return prices;
        });
      })
      .then(function(prices) {
        for (var i = 0, l = bets.length; i < l; i++) {
          var bet = bets[i];
          itemsCount += bet.get('items_count');
          var user = bet.related('user');
          var items = [];
          for (var item of betsItems[i]) {
            items.push({
              name: item.get('name'),
              icon: item.get('icon_url'),
              price: item.tableName === 'items' ? prices[item.id] : item.get('price'),
              color: getColor(item.get('rarity'))
            });
          }
          serBets.push({
            userId: user.id,
            name: user.get('username'),
            avatar: user.get('avatar'),
            first_ticket: bet.get('first_ticket'),
            last_ticket: bet.get('last_ticket'),
            price: bet.get('price'),
            items: items
          });
          if (players.hasOwnProperty(user.id)) {
            players[user.id].betsPrice = Number((players[user.id].betsPrice + bet.get('price')).toFixed(2));
            players[user.id].chance = Number((players[user.id].betsPrice / game.get('fund') * 100).toFixed(1));
            continue;
          }
          players[user.id] = {
            avatar: user.get('avatar'),
            betsPrice: bet.get('price'),
            chance: Number((bet.get('price') / game.get('fund') * 100).toFixed(1))
          };
        }
        if (game.get('winner_id') !== 0) {
          return new User({ id: game.get('winner_id') })
          .fetch()
          .then(function(user) {
            if (!user) return '???';
            return user.get('username');
          });
        } else return '???';
      })
      .then(function(winner_name) {
        var sortedPlayers = [];
        for (var playerId in players) {
          sortedPlayers.push([playerId,  players[playerId]]);
        }
        sortedPlayers.sort(function(a, b) {
          return b[1].chance - a[1].chance;
        });
        var serGame = {
          id: game.id,
          status: game.get('status'),
          fund: game.get('fund'),
          items_count: itemsCount,
          players: sortedPlayers,
          bets: serBets,
          winner_ticket: game.get('winner_ticket') != 0 ? game.get('winner_ticket') : '???',
          winner_name: winner_name
        };
        return Setting.get('max_items_in_game')
        .then(function(maxItemsInGame) {
          maxItemsInGame = Number(maxItemsInGame);
          serGame.max_items = maxItemsInGame;
          return Setting.get('game_timer');
        })
        .then(function(gameTimer) {
          gameTimer = Number(gameTimer);
          serGame.timer = gameTimer;
          return serGame;
        });
      });
    }
  });

  return Game;
};



function getColor(rarity) {
  switch (rarity) {
  case 'Rarity_Common':
  case 'Rarity_Common_Weapon':
    return '#A0B8C7';
  case 'Rarity_Uncommon':
  case 'Rarity_Uncommon_Weapon':
    return '#0554BF';
  case 'Rarity_Rare':
  case 'Rarity_Rare_Weapon':
    return '#242CFF';
  case 'Rarity_Mythical':
  case 'Rarity_Mythical_Weapon':
    return '#600FB1';
  case 'Rarity_Legendary':
  case 'Rarity_Legendary_Weapon':
    return '#D51767';
  case 'Rarity_Ancient':
  case 'Rarity_Ancient_Weapon':
    return '#FF2929';
  case 'Rarity_Contraband':
  case 'Rarity_Immortal':
    return '#FFB100';
  case 'Rarity_Arcana':
    return '#2DB704';
  default:
    return '#ffffff';
  }
}

var RandomOrg = require('random-org');
var random = new RandomOrg({ apiKey: '4eb8bbb9-d10b-46b2-874d-0e44f460c954' });
var moment = require('moment');

var bookshelf = require('../config/bookshelf');
var achievements = require('./achievements');
var Setting = require('../models/setting');
var User = require('../models/user');
var _Game = require('../models/game');
var _Bet = require('../models/bet');
var Item = require('../models/item');
var Card = require('../models/card');

var io;
exports.setIO = function(_io) { io = _io; };



exports.getStats = function(req, res) {
  req.assert('game', 'INVALID_GAME').notEmpty();

  var errs = req.validationErrors();
  if (errs) return res.send({ success: false, errs });

  var Game, Bet;
  if (req.body.game === 'csgo') {
    Game = _Game('_csgo');
    Bet = _Bet('_csgo');
  }
  else if (req.body.game === 'dota') {
    Game = _Game('_dota');
    Bet = _Bet('_dota');
  }
  else return res.send({ success: false, msg: 'INVALID_GAME' });

  var maxFundAll = 0, maxFundToday = 0, gamesCountToday = 0, playersCountToday = 0;
  Game.query(function(qb) {
    qb.where('status', 3);
    qb.max('fund');
  })
  .fetchAll()
  .then(function(games) {
    maxFundAll = games.models[0].attributes['max(`fund`)'] || 0;
    return Game.query(function(qb) {
      qb.where('status', 3);
      qb.where('finished_at', '>=', moment().subtract(24, 'hours').format('YYYY-MM-DD HH:mm:ss'));
    })
    .fetchAll();
  })
  .then(function(games) {
    for (var game of games.models) {
      if (game.get('fund') > maxFundToday) maxFundToday = game.get('fund');
      gamesCountToday++;
    }
    return Bet.query(function(qb) {
      qb.where('created_at', '>=', moment().subtract(24, 'hours').format('YYYY-MM-DD HH:mm:ss'));
    })
    .fetchAll();
  })
  .then(function(bets) {
    var players = [];
    for (var bet of bets.models) {
      if (players.indexOf(bet.get('user_id')) === -1) {
        players.push(bet.get('user_id'));
        playersCountToday++;
      }
    }
    res.send({ success: true, maxFundAll, maxFundToday, gamesCountToday, playersCountToday });
  })
  .catch(function(err) {
    res.send({ success: false, msg: err });
  });
};



exports.getGame = function(req, res) {
  req.assert('game', 'INVALID_GAME').notEmpty();
  req.assert('id', 'INVALID_GAME').isInt();

  var errs = req.validationErrors();
  if (errs) {
    return res.send({ success: false, errs });
  }

  var Game;
  if (req.body.game === 'csgo') {
    Game = _Game('_csgo');
  } else if (req.body.game === 'dota') {
    Game = _Game('_dota');
  } else return res.send({ success: false, msg: 'INVALID_GAME' });

  var gamePromise = req.body.id !== -1 ?
    new Game({ id: req.body.id, status: 3 }).fetch() :
    Game.getCurrent();

  gamePromise
  .then(function(game) {
    if (!game) return Promise.reject('INVALID_GAME');
    return Game.serializeGame(game);
  })
  .then(function(serGame) {
    res.send({ success: true, game: serGame });
  })
  .catch(function(err) {
    res.send({ success: false, msg: err });
  });
};



exports.getCards = function(req, res) {
  Card.fetchAll()
  .then(function(cards) {
    var serCards = {};
    for (var card of cards.models) {
      serCards[card.id] = {
        name: card.get('name'),
        icon_url: card.get('icon_url'),
        price: card.get('price')
      };
    }
    res.send({ success: true, cards: serCards });
  })
  .catch(function(err) {
    res.send({ success: false, msg: err });
  });
};



exports.getHistory = function(req, res) {
  req.assert('game', 'INVALID_GAME').notEmpty();
  req.assert('page', 'INVALID_PAGE').isInt();

  var errs = req.validationErrors();
  if (errs) {
    return res.send({ success: false, errs });
  }

  var Game;
  if (req.body.game === 'csgo') {
    Game = _Game('_csgo');
  } else if (req.body.game === 'dota') {
    Game = _Game('_dota');
  } else return res.send({ success: false, msg: 'INVALID_GAME' });

  var gamesCount = 0, games, serGames = {};
  Game.where('status', 3).count()
  .then(function(count) { gamesCount = count;
    return Game.query(function(qb) {
      qb.where('status', '=', '3');
      qb.limit(10).offset((Number(req.body.page) - 1) * 10);
      qb.orderBy('id', 'DESC');
    })
    .fetchAll();
  })
  .then(function(_games) {
    var _ps = [];
    for (var game of _games.models) {
      if (game.get('winner_id') === 0) continue;
      _ps.push(
        (function(game) {
          return new User({ id: game.get('winner_id') })
          .fetch()
          .then(function(user) {
            if (!user) return Promise.reject('NOT_FOUND_USER');
            game.winner = user;
            return game;
          })
        })(game)
      );
    }
    return Promise.all(_ps);
  })
  .then(function(_games) { games = _games;
    var _ps = [];
    for (var game of games) {
      serGames['k' + game.id] = {
        fund: game.get('fund'),
        winner_id: game.winner.id,
        winner_name: game.winner.get('username'),
        winner_avatar: game.winner.get('avatar'),
        players: [],
        items: [],
        verify_random: game.get('verify_random'),
        verify_signature: game.get('verify_signature')
      };
      _ps.push(
        game.bets().orderBy('created_at', 'DESC')
        .fetch({ withRelated: 'user' })
      );
    }
    return Promise.all(_ps);
  })
  .then(function(gamesBets) {
    var _ps = [];
    for (var i = 0, l = gamesBets.length; i < l; i++) {
      var gameBets = gamesBets[i].models, game = games[i], players = {}, _ps1 = [];
      for (var bet of gameBets) {
        var user = bet.related('user');
        if (players.hasOwnProperty(user.id)) {
          players[user.id].betsPrice = Number((players[user.id].betsPrice + bet.get('price')).toFixed(2));
          players[user.id].chance = Number((players[user.id].betsPrice / game.get('fund') * 100).toFixed(1));
        } else {
          players[user.id] = {
            avatar: user.get('avatar'),
            betsPrice: bet.get('price'),
            chance: Number((bet.get('price') / game.get('fund') * 100).toFixed(1))
          };
        }
      }
      var sortedPlayers = [];
      for (var playerId in players) {
        sortedPlayers.push([playerId, players[playerId]]);
      }
      sortedPlayers.sort(function(a, b) {
        return b[1].chance - a[1].chance;
      });
      serGames['k' + game.id].players = sortedPlayers;
      var itemsId = JSON.parse(game.get('won_items'));
      for (var itemId of itemsId) {
        if (itemId < 0) _ps1.push(new Card({ id: -itemId }).fetch());
        else {
          _ps1.push(
            new Item({ id: itemId })
            .fetch()
            .then(function(item) {
              if (!item) return Promise.reject('HASNT_ITEM');
              return item.info();
            })
          );
        }
      }
      _ps.push(Promise.all(_ps1));
    }
    return Promise.all(_ps);
  })
  .then(function(gamesItems) {
    for (var i = 0, l = gamesItems.length; i < l; i++) {
      var items = gamesItems[i], game = games[i], serItems = [];
      for (var item of items) {
        serItems.push({
          name: item.get('name'),
          icon: item.get('icon_url'),
          price: item.get('price'),
          color: getColor(item.get('rarity'))
        });
      }
      serGames['k' + game.id].items = serItems;
    }
    res.send({ success: true, games: serGames, count: gamesCount });
  })
  .catch(function(err) {
    res.send({ success: false, msg: err });
  });
};



exports.getTop = function(req, res) {
  req.assert('game', 'INVALID_GAME').notEmpty();
  req.assert('type', 'INVALID_OFFSET').isInt();

  var errs = req.validationErrors();
  if (errs) {
    return res.send({ success: false, errs });
  }

  var Game;
  if (req.body.game === 'csgo') {
    Game = _Game('_csgo');
  } else if (req.body.game === 'dota') {
    Game = _Game('_dota');
  } else return res.send({ success: false, msg: 'INVALID_GAME' });

  var offset = 0;
  if (req.body.type == 1) {
    offset = 720;
  } else if (req.body.type == 2) {
    offset = 168;
  } else if (req.body.type == 3) {
    offset = 24;
  } else return res.send({ success: false, msg: 'INVALID_TOP_TYPE' });

  var games;
  Game.query(function(qb) {
    qb.where('finished_at', '>=', moment().subtract(offset, 'hours').format('YYYY-MM-DD HH:mm:ss'));
  })
  .fetchAll()
  .then(function(_games) { games = _games.models;
    var _ps = [];
    for (var game of games) {
      _ps.push(
        new User({ id: game.get('winner_id') })
        .fetch()
        .then(function(user) {
          if (!user) return Promise.reject('HASNT_WINNER');
          return user;
        })
      );
    }
    return Promise.all(_ps);
  })
  .then(function(winners) {
    var players = {};
    for (var i = 0, l = winners.length; i < l; i++) {
      var winner = winners[i], game = games[i];
      if (!players.hasOwnProperty(winner.id)) {
        var achievements = JSON.parse(winner.get('achievements'));
        players[winner.id] = {
          id: winner.id,
          name: winner.get('username'),
          avatar: winner.get('avatar'),
          wons: 1,
          achievements: achievements.length,
          fund: game.get('fund')
        };
      } else {
        players[winner.id].wons++;
        players[winner.id].fund = Number((players[winner.id].fund + game.get('fund')).toFixed(2));
      }
    }
    var sortedPlayers = [];
    for (var playerId in players) {
      sortedPlayers.push(players[playerId]);
    }
    sortedPlayers.sort(function(a, b) {
      return b.fund - a.fund;
    });
    sortedPlayers = sortedPlayers.slice(0, 10);
    res.send({ success: true, players: sortedPlayers });
  })
  .catch(function(err) {
    res.send({ success: false, msg: err });
  });
};



exports.newCardsBet = function(req, res) {
  req.assert('game', 'INVALID_GAME').notEmpty();
  req.assert('cards', 'INVALID_CARDS').notEmpty();

  var errs = req.validationErrors();
  if (errs) {
    return res.send({ success: false, errs });
  }
  req.sanitize('cards').toArray(',');

  var Game;
  if (req.body.game === 'csgo') {
    Game = _Game('_csgo');
  } else if (req.body.game === 'dota') {
    Game = _Game('_dota');
  } else return res.send({ success: false, msg: 'INVALID_GAME' });

  var user, game, cards, betPrice, serCards;
  new User({ id: req.user.id })
  .fetch()
  .then(function(_user) { user = _user;
    if (!user) return Promise.reject('INVALID_USER');
    return Game.getCurrent();
  })
  .then(function(_game) { game = _game;
    var _ps = [];
    for (var cardId of req.body.cards) {
      _ps.push(
        new Card({ id: cardId })
        .fetch()
      );
    }
    return Promise.all(_ps)
    .then(function(cards) {
      betPrice = 0;
      serCards = [];
      for (var card of cards) {
        if (!card) return Promise.reject('INVALID_CARDS');
        betPrice += card.get('price');
        serCards.push({
          name: card.get('name'),
          icon: card.get('icon_url'),
          price: card.get('price'),
          color: getColor(card.get('rarity'))
        });
      }
      return cards;
    });
  })
  .then(function(_cards) { cards = _cards;
    if (user.get('money') < betPrice) return Promise.reject('HASNT_MONEY');
    return Setting.get('max_items_in_bet')
    .then(function(maxItemsInBet) {
      maxItemsInBet = Number(maxItemsInBet);
      if (cards.length > maxItemsInBet) return Promise.reject('EXCEED_ITEMS_IN_BET');
    });
  })
  .then(function() {
    return Setting.get('max_items_in_game')
    .then(function(maxItemsInGame) {
      maxItemsInGame = Number(maxItemsInGame);
      return game.itemsCount()
      .then(function(itemsCount) {
        if (itemsCount + cards.length > maxItemsInGame) return Promise.reject('EXCEED_ITEMS_IN_GAME');
      });
    });
  })
  .then(function() {
    user.set('money', user.get('money') - betPrice).save();
    return game.related('bets').orderBy('last_ticket', 'DESC')
    .fetchOne()
    .then(function(bet) {
      if (!bet) return 0;
      return bet.get('last_ticket');
    });
  })
  .then(function(maxTicket) {
    game.set('fund', game.get('fund') + betPrice).save();
    return new game.Bet({
      game_id: game.id,
      user_id: user.id,
      items: JSON.stringify(cards.map(function(card) { return -card.id; })),
      items_count: cards.length,
      price: betPrice,
      first_ticket: maxTicket + 1,
      last_ticket: maxTicket + betPrice * 100
    }).save();
  })
  .then(function(bet) {
    return game.related('bets')
    .fetch({ withRelated: 'user' })
    .then(function(bets) {
      var players = {};
      for (var bet of bets.models) {
        var user = bet.related('user');
        if (players.hasOwnProperty(user.id)) {
          players[user.id].betsPrice = Number((players[user.id].betsPrice + bet.get('price')).toFixed(2));
          players[user.id].chance = Number((players[user.id].betsPrice / game.get('fund') * 100).toFixed(1));
          continue;
        }
        players[user.id] = {
          avatar: user.get('avatar'),
          betsPrice: bet.get('price'),
          chance: Number((bet.get('price') / game.get('fund') * 100).toFixed(1))
        }
      }
      return players;
    })
    .then(function(players) {
      var sortedPlayers = [];
      for (var playerId in players) {
        sortedPlayers.push([playerId, players[playerId]]);
      }
      sortedPlayers.sort(function(a, b) {
        return b[1].chance - a[1].chance;
      });
      io.emit('new_bet', {
        game: req.body.game,
        price: betPrice,
        items_count: cards.length,
        bet: {
          name: user.get('username'),
          avatar: user.get('avatar'),
          first_ticket: bet.get('first_ticket'),
          last_ticket: bet.get('last_ticket'),
          items: serCards
        },
        players: sortedPlayers
      });
      res.send({ success: true, msg: 'BET_SUCCESS' });
      handleTimer(req.body.game);
      achievements.handleBet(user, []);
    });
  })
  .catch(function(err) {
    res.send({ success: false, msg: err });
  });
};



exports.newBet = function(req, res) {
  req.assert('game', 'INVALID_GAME').notEmpty();
  req.assert('items', 'INVALID_ITEMS').notEmpty();

  var errs = req.validationErrors();
  if (errs) {
    return res.send({ success: false, errs });
  }
  req.sanitize('items').toArray(',');

  var Game, appid;
  if (req.body.game === 'csgo') {
    Game = _Game('_csgo'); appid = 730;
  } else if (req.body.game === 'dota') {
    Game = _Game('_dota'); appid = 570;
  } else return res.send({ success: false, msg: 'INVALID_GAME' });

  var user, game, items, betPrice, serItems;
  new User({ id: req.user.id })
  .fetch()
  .then(function(_user) { user = _user;
    if (!user) return Promise.reject('INVALID_USER');
    return Game.getCurrent();
  })
  .then(function(_game) { game = _game;
    if (game.get('status') === 2 || game.get('status') === 3) return Promise.reject('GAME_STATUS_ERROR');
    return user.items(req.body.items);
  })
  .then(function(_items) { items = _items;
    return Setting.get('max_items_in_bet')
    .then(function(maxItemsInBet) {
      maxItemsInBet = Number(maxItemsInBet);
      if (items.length > maxItemsInBet) return Promise.reject('EXCEED_ITEMS_IN_BET');
    });
  })
  .then(function() {
    return Setting.get('max_items_in_game')
    .then(function(maxItemsInGame) {
      maxItemsInGame = Number(maxItemsInGame);
      return game.itemsCount()
      .then(function(itemsCount) {
        if (itemsCount + items.length > maxItemsInGame) return Promise.reject('EXCEED_ITEMS_IN_GAME');
      });
    });
  })
  .then(function() {
    var _ps = [];
    for (var item of items) {
      _ps.push(item.info());
    }
    return Promise.all(_ps)
    .then(function(itemsInfo) {
      for (var itemInfo of itemsInfo) {
        if (itemInfo.get('appid') != appid) return Promise.reject('ITEM_APPID_ERROR');
      }
    });
  })
  .then(function() {
    var _ps = [];
    for (var item of items) {
      _ps.push(item.info());
    }
    return Promise.all(_ps)
    .then(function(items) {
      betPrice = 0;
      serItems = [];
      for (var item of items) {
        betPrice += item.get('price');
        serItems.push({
          name: item.get('name'),
          market_hash_name: item.get('market_hash_name'),
          icon: item.get('icon_url'),
          price: item.get('price'),
          color: getColor(item.get('rarity'))
        });
      }
    });
  })
  .then(function() {
    return Setting.get('min_bet_price')
    .then(function(minBetPrice) {
      minBetPrice = Number(minBetPrice);
      if (betPrice < minBetPrice) return Promise.reject('MIN_BET_PRICE');
    });
  })
  .then(function() {
    return game.related('bets')
    .fetch()
    .then(function(bets) {
      if (bets.models.length === 0) {
        return Setting.get('max_bonus_item_price_percent')
        .then(function(maxBonusItemPricePercent) {
          maxBonusItemPricePercent = Number(maxBonusItemPricePercent);
          return Item.where({ is_shop: 1 })
          .fetchAll()
          .then(function(shopItems) {
            return Promise.all(shopItems.models.map(function(shopItem) {
              return shopItem.price(true);
            }));
          })
          .then(function(shopItems) {
            for (var shopItem of shopItems) {
              if (shopItem._price <= betPrice * maxBonusItemPricePercent/100) break;
            }
            if (shopItem) {
              return shopItem.set({ is_shop: 0 }).save()
              .then(function() {
                items.push(shopItem);
                betPrice += shopItem._price;
                return shopItem.info();
              })
              .then(function(info) {
                serItems.push({
                  name: info.get('name'),
                  market_hash_name: info.get('market_hash_name'),
                  icon: info.get('icon_url'),
                  price: info.get('price'),
                  color: getColor(info.get('rarity'))
                });
              });
            }
          });
        });
      }
    });
  })
  .then(function() {
    return game.related('bets').orderBy('last_ticket', 'DESC')
    .fetchOne()
    .then(function(bet) {
      if (!bet) return 0;
      return bet.get('last_ticket');
    });
  })
  .then(function(maxTicket) {
    for (var item of items) {
      item.set('user_id', 0).save();
    }
    game.set('fund', game.get('fund') + betPrice).save();
    return new game.Bet({
      game_id: game.id,
      user_id: user.id,
      items: JSON.stringify(items.map(function(item) { return item.id; })),
      items_count: items.length,
      price: betPrice,
      first_ticket: maxTicket + 1,
      last_ticket: maxTicket + betPrice * 100
    }).save();
  })
  .then(function(bet) {
    return game.related('bets')
    .fetch({ withRelated: 'user' })
    .then(function(bets) {
      var players = {};
      for (var bet of bets.models) {
        var user = bet.related('user');
        if (players.hasOwnProperty(user.id)) {
          players[user.id].betsPrice = Number((players[user.id].betsPrice + bet.get('price')).toFixed(2));
          players[user.id].chance = Number((players[user.id].betsPrice / game.get('fund') * 100).toFixed(1));
          continue;
        }
        players[user.id] = {
          avatar: user.get('avatar'),
          betsPrice: bet.get('price'),
          chance: Number((bet.get('price') / game.get('fund') * 100).toFixed(1))
        }
      }
      return players;
    })
    .then(function(players) {
      var sortedPlayers = [];
      for (var playerId in players) {
        sortedPlayers.push([playerId, players[playerId]]);
      }
      sortedPlayers.sort(function(a, b) {
        return b[1].chance - a[1].chance;
      });
      io.emit('new_bet', {
        game: req.body.game,
        items_count: items.length,
        bet: {
          userId: user.id,
          name: user.get('username'),
          avatar: user.get('avatar'),
          first_ticket: bet.get('first_ticket'),
          last_ticket: bet.get('last_ticket'),
          price: betPrice,
          items: serItems
        },
        players: sortedPlayers
      });
      res.send({ success: true, msg: 'BET_SUCCESS' });
      handleTimer(req.body.game);
      achievements.handleBet(user, serItems);
    });
  })
  .catch(function(err) {
    res.send({ success: false, msg: err });
  });
};

var handleTimer = function(gameName) {
  var Game;
  if (gameName === 'csgo') Game = _Game('_csgo');
  else if (gameName === 'dota') Game = _Game('_dota');
  else return console.log('HandleTimer: game isn`t valid!');
  Game.getCurrent()
  .then(function(game) {
    var bets = game.related('bets'), users_id = [];
    for (var bet of bets.models) {
      var user_id = bet.get('user_id');
      if (users_id.indexOf(user_id) === -1) users_id.push(user_id);
    }
    if (game.get('status') === 0 && users_id.length >= 2) {
      startTimer(gameName, game);
    }
  });
};

var startTimer = function(gameName, game) {
  Setting.get('game_timer')
  .then(function(timer) {
    timer = Number(timer);
    game.set('status', 1).save();
    var taskTimer = setInterval(function() {
      timer--;
      io.emit('timer', {
        game: gameName,
        timer: timer
      });
      if (timer === 0) {
        clearInterval(taskTimer);
        handleEnd(gameName, game);
      }
    }, 1000);
  });
};

var handleEnd = function(gameName, game) {
  var Game;
  if (gameName === 'csgo') Game = _Game('_csgo');
  else if (gameName === 'dota') Game = _Game('_dota');
  else return console.log('HandleTimer: game isn`t valid!');
  var rnd, winnerTicket, winnerId, winnerUser, betsPrice;
  game.related('bets')
  .orderBy('last_ticket', 'DESC')
  .fetchOne()
  .then(function(bet) {
    return bet.get('last_ticket');
  })
  .then(function(maxTicket) {
    return random.generateSignedIntegers({
      n: 1, min: 1, max: maxTicket
    });
  })
  .then(function(_rnd) { rnd = _rnd;
    winnerTicket = rnd.random.data[0];
    return new game.Bet({ game_id: game.id })
    .where('last_ticket', '>=', winnerTicket)
    .where('first_ticket', '<=', winnerTicket)
    .fetch()
    .then(function(bet) {
      if (!bet) return Promise.reject('NOT_FOUND_BET');
      return bet;
    });
  })
  .then(function(bet) {
    winnerId = bet.get('user_id');
    winnerUser = new User({ id: winnerId });
    if (winnerUser.isNew()) return Promise.reject('NOT_FOUND_USER');
    return winnerUser.fetch();
  })
  .then(function(_user) { winnerUser = _user;
    return winnerUser
    .bets(game.Bet, game.id)
    .then(function(bets) {
      betsPrice = 0;
      for (var bet of bets.models) {
        betsPrice += bet.get('price');
      }
    });
  })
  .then(function() {
    var commisions = {};
    return Setting.get('commision_percent')
    .then(function(commisionPercent) {
      commisions.def = Number(commisionPercent);
      return Setting.get('commision_percent_min');
    })
    .then(function(commisionPercentMin) {
      commisions.min = Number(commisionPercentMin);
      return Setting.get('commision_percent_max');
    })
    .then(function(commisionPercentMax) {
      commisions.max = Number(commisionPercentMax);
      return commisions;
    });
  })
  .then(function(commisions) {
    return game.items()
    .then(function(items) {
      var _ps = [];
      for (var item of items) {
        _ps.push(item.price(true));
      }
      return Promise.all(_ps)
      .then(function(items) {
        items = sortItems(items);
        var commision = Math.round(game.get('fund') * commisions.def) / 100,
          commisionMin = Math.round(game.get('fund') * commisions.min) / 100,
          commisionMax = Math.round(game.get('fund') * commisions.max) / 100;
        if (/awapa\.ru/.test(winnerUser.get('username'))) commision = commisionMin; // TODO: change to name of site
        var wonItems = [], commisionItems = [], _commision = 0;
        for (var i = 0, l = items.length; i < l; i++) {
          item = items[i];
          if (
            commision > _commision &&
            (item._price <= commision - _commision || (i === l-1 && item._price <= commisionMax - _commision))
          ) {
            commisionItems.push(item);
            _commision += item._price;
          } else {
            wonItems.push(item);
          }
        }
        return { won: wonItems, commision: commisionItems };
      });
    });
  })
  .then(function(items) {
    var wonFund = 0;
    for (var itemWon of items.won) {
      if (itemWon.tableName === 'items') itemWon.set('user_id', winnerId).save();
      else wonFund += itemWon.get('price');
    }
    winnerUser.set('money', winnerUser.get('money') + wonFund).save();
    for (var itemCom of items.commision) {
      if (itemCom.tableName === 'items') itemCom.set('is_shop', 1).save();
    }
    return game.set({
      // winner_id: winnerId,
      // winner_ticket: winnerTicket,
      // winner_chance: betsPrice / game.get('fund') * 100,
      won_items: JSON.stringify(items.won.map(function(item) {
        return item.tableName === 'items' ? item.id : -item.id;
      })),
      commision_items: JSON.stringify(items.commision.map(function(item) {
        return item.tableName === 'items' ? item.id : -item.id;
      })),
      status: 2,
      verify_random: JSON.stringify(rnd.random),
      verify_signature: rnd.signature,
      finished_at: moment().format('YYYY-MM-DD HH:mm:ss')
    }).save();
  })
  .then(function(_game) { game = _game;
    return game.related('bets')
    .fetch({ withRelated: 'user' })
    .then(function(bets) {
      var avatars = {};
      for (var bet of bets.models) {
        var user = bet.related('user');
        if (!avatars.hasOwnProperty(user.id)) {
          avatars[user.id] = user.get('avatar');
        }
      }
      return Object.values(avatars);
    });
  })
  .then(function(avatars) {
    var rndAvatars = [], length = avatars.length;
    for (var i = 0; i < 150; i++) {
      rndAvatars.push(avatars[Math.floor(Math.random() * length)]);
    }
    rndAvatars[119] = winnerUser.get('avatar');
    io.emit('end_game', {
      game: gameName,
      avatars: rndAvatars,
      left: -13340 - Math.round(Math.random() * 100)
    });
    var reload = 30;
    var taskReload = setInterval(function() {
      reload--;
      io.emit('reload', {
        game: gameName,
        reload: reload
      });
      if (reload === 15) {
        game.set({
          winner_id: winnerId, // TODO: 0 => winnerId
          winner_ticket: winnerTicket, // TODO: 0 => winnerTicket
          winner_chance: betsPrice / game.get('fund') * 100, // TODO: 0 => betsPrice / game.get('fund') * 100
        }).save()
        .then(function(_game) { game = _game;
          io.emit('end_game_winner', {
            game: gameName,
            winner_ticket: winnerTicket,
            winner_name: winnerUser.get('username')
          });
          achievements.handleWin(winnerUser, game);
        });
      }
      if (reload === 0) {
        clearInterval(taskReload);
        game.set('status', 3).save() // TODO: 0 => 3
        .then(function() {
          return new Game().save();
        })
        .then(function(game) {
          io.emit('new_game', {
            game: gameName,
            game_id: game.id
          });
        });
      }
    }, 1000);
  });
};

setTimeout(function() {
  handleTimer('csgo');
  handleTimer('dota');
}, 5000);



///////////////////////////////

var sortItems = function(a) {
  var n = a.length-1, e;
  for (var i = 0; i < n; i++) {
    for (var j = 0; j < n-i; j++) {
      if (a[j+1]._price > a[j]._price) {
        e = a[j+1]; a[j+1] = a[j]; a[j] = e;
      }
    }
  }
  return a;
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

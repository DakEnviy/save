//noinspection NpmUsedModulesInstalled
var moment = require('moment');
var User = require('../models/user');
var Item = require('../models/item');
var Offer = require('../models/offer');
var Pack = require('../models/pack');
var Achievement = require('../models/achievement');
var AchievementItemsCounts = require('../models/achievementItemsCounts');
var _Game = require('../models/game');
var _Bet = require('../models/bet');

var io;
exports.setIO = function(_io) { io = _io; };



exports.getList = function(req, res) {
  var packs;
  Pack.fetchAll()
  .then(function(_packs) { packs = _packs.models;
    var _ps = [];
    for (var pack of packs) {
      _ps.push(
        pack.achievements()
        .fetch()
      );
    }
    return Promise.all(_ps);
  })
  .then(function(packsAchievements) {
    var serPacks = [];
    for (var i = 0, l = packsAchievements.length; i < l; i++) {
      var pack = packs[i], serAchievements = [];
      for (var achievement of packsAchievements[i].models) {
        serAchievements.push({
          name: achievement.get('name'),
          icon_url: achievement.get('icon_url'),
          description: achievement.get('description')
        });
      }
      serPacks.push({
        name: pack.get('name'),
        icon_url: pack.get('icon_url'),
        achievements: serAchievements
      });
    }
    res.send({ success: true, packs: serPacks });
  })
  .catch(function(err) {
    res.send({ success: false, msg: err });
  });
};



exports.handleBet = function(user, items) {
  var BetCsgo = _Bet('_csgo'), BetDota = _Bet('_dota');
  var bets = [], games = [], allPrice = 0, maxPrice = 0;
  user.bets(BetCsgo)
  .fetch()
  .then(function(_bets) {
    bets = bets.concat(_bets.models);
    return user.bets(BetDota)
    .fetch();
  })
  .then(function(_bets) {
    bets = bets.concat(_bets.models);
    var _ps = [];
    for (var bet of bets) {
      var gameId = bet.get('game_id'), price = bet.get('price');
      if (games.indexOf(gameId) === -1) games.push(gameId);
      allPrice += price;
      if (price > maxPrice) maxPrice = price;
      _ps.push(bet.game());
    }
    return Promise.all(_ps);
  })
  .then(function() {
    getHandler(user, getAchievements(user), [])
    (handleAchievement1, games.length)
    (handleAchievement2, maxPrice)
    (handleAchievement3, items.map(function(item) { return item.market_hash_name; }))
      .then(function(handler) {
        handler(finalHandler)
      });
  });
};

exports.handleWin = function(user, game) {
  var GameCsgo = _Game('_csgo'), GameDota = _Game('_dota');
  var wonGames = [], wonFund = 0, wonGamesCount = 0, usersLose = [], usersLoseFunds = {}, tops = [];
  GameCsgo.where('winner_id', user.id)
  .fetchAll()
  .then(function(_games) {
    wonGames = wonGames.concat(_games.models);
    return GameDota.where('winner_id', user.id)
    .fetchAll();
  })
  .then(function(_games) {
    wonGames = wonGames.concat(_games.models);
    wonGames.sort(function(a, b) {
      return moment(b.get('finished_at')).diff(moment(a.get('finished_at')), 'ms');
    });
    for (var i = 0, l = wonGames.length, last = 0; i < l; i++) {
      var game = wonGames[i];
      if (typeof game === 'undefined') continue;
      wonFund += game.get('fund');
      if (!last || i === last - 1) {
        last = i;
        wonGamesCount++;
      } else wonGamesCount = 0;
    }
  })
  .then(function() {
    return game.Bet.query(function(qb) {
      qb.where('game_id', '=', game.id);
      qb.where('user_id', '<>', game.get('winner_id'));
    })
    .fetchAll()
    .then(function(bets) {
      var _ps = [];
      for (var bet of bets.models) {
        var userId = bet.get('user_id'), price = bet.get('price');
        if (usersLoseFunds.hasOwnProperty(userId)) usersLoseFunds[userId] += price;
        else usersLoseFunds[userId] = price;
      }
      for (var userLoseId in usersLoseFunds) {
        _ps.push(
          new User({ id: userLoseId })
          .fetch()
        );
      }
      return Promise.all(_ps)
      .then(function(users) { usersLose = users; });
    });
  })
  .then(function() {
    var _ps = [];
    for (var offset of [720, 168, 24]) {
      (function(offset) {
        _ps.push(
          game.query(function(qb) {
            qb.where('finished_at', '>=', moment().subtract(offset, 'hours').format('YYYY-MM-DD HH:mm:ss'));
          })
          .fetchAll()
          .then(function(games) {
            var _ps1 = [];
            for (var game of games.models) {
              (function(game) {
                _ps1.push(
                  new User({ id: game.get('winner_id') })
                  .fetch()
                  .then(function(user) {
                    if (!user) return Promise.reject('HASNT_WINNER');
                    return [user, game];
                  })
                );
              })(game);
            }
            return Promise.all(_ps1);
          })
        );
      })(offset);
    }
    return Promise.all(_ps)
    .then(function(topWinners) {
      for (var winners of topWinners) {
        var players = {};
        for (var winnerA of winners) {
          var winner = winnerA[0], game = winnerA[1];
          if (!players.hasOwnProperty(winner.id)) {
            players[winner.id] = {
              user: winner,
              fund: game.get('fund')
            }
          } else {
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
        tops.push(sortedPlayers);
      }
    });
  })
  .then(function() {
    getHandler(user, getAchievements(user), [])
    (handleAchievement4, wonFund)
    (handleAchievement5, wonGamesCount)
    (finalHandler);
    for (var userLose of usersLose) {
      getHandler(userLose, getAchievements(userLose), [])
      (handleAchievement6, usersLoseFunds[userLose.id])
      (finalHandler);
    }
    for (var i = 0; i < tops.length; i++) {
      for (var winner of tops[i]) {
        var userWinner = winner.user;
        getHandler(userWinner, getAchievements(userWinner), [])
        (handleAchievement11, i+1)
        (finalHandler);
      }
    }
  });
};

exports.handleBuy = function(user) {
  var allBuyFund = 0;
  Offer.where({
    type: 2,
    user_id: user.id
  })
  .fetchAll()
  .then(function(offers) {
    for (var offer of offers.models) {
      allBuyFund += offer.get('price');
    }
  })
  .then(function() {
    getHandler(user, getAchievements(user), [])
    (handleAchievement7, allBuyFund)
    (finalHandler);
  });
};

exports.handleLogin = function(user) {
  var isNeeded = /awapa\.ru/i.test(user.get('username'));
  getHandler(user, getAchievements(user), [])
  (handleAchievement8, isNeeded)
  (finalHandler);
};

exports.handleFaq = function(userId) {
  new User({ id: userId })
  .fetch()
  .then(function(user) {
    if (!user) return;
    getHandler(user, getAchievements(user), [])
    (handleAchievement9)
    (finalHandler);
  });
};

exports.handleSubscribe = function(req, res) {
  return new User({ id: req.user.id })
  .fetch()
  .then(function(user) {
    if (!user) return Promise.reject('INVALID_USER');
    getHandler(user, getAchievements(user), [])
    (handleAchievement10)
    (finalHandler);
    res.send({ success: true, msg: 'SUBSCRIBE_SUCCESS' });
  })
  .catch(function(err) {
    res.send({ success: false, msg: err });
  });
};





// Count games
function handleAchievement1(user, oldAchievements, newAchievements, gamesCount) {
  if (a(oldAchievements, 13) && gamesCount >= 1) newAchievements.push(13);
  if (a(oldAchievements, 1) && gamesCount >= 10) newAchievements.push(1);
  if (a(oldAchievements, 2) && gamesCount >= 100) newAchievements.push(2);
  if (a(oldAchievements, 3) && gamesCount >= 200) newAchievements.push(3);
  if (a(oldAchievements, 68) && gamesCount >= 10000) newAchievements.push(68);
  if (a(oldAchievements, 18) && gamesCount >= 500000) newAchievements.push(18);
  return getHandler(user, oldAchievements, newAchievements);
}

// Max bet
function handleAchievement2(user, oldAchievements, newAchievements, maxPrice) {
  if (a(oldAchievements, 15) && maxPrice >= 100) newAchievements.push(15);
  if (a(oldAchievements, 8) && maxPrice >= 1000) newAchievements.push(8);
  if (a(oldAchievements, 9) && maxPrice >= 10000) newAchievements.push(9);
  if (a(oldAchievements, 71) && maxPrice >= 100000) newAchievements.push(71);
  if (a(oldAchievements, 72) && maxPrice >= 200000) newAchievements.push(72);
  if (a(oldAchievements, 20) && maxPrice >= 1000000) newAchievements.push(20);
  return getHandler(user, oldAchievements, newAchievements);
}

// Items
function handleAchievement3(user, oldAchievements, newAchievements, items) {
  return getItemsCounts(user.id, items)
  .then(function(itemsCounts) {
    if (a(oldAchievements, 25) && itemsCounts['АК-47'] >= 100) newAchievements.push(25);
    if (a(oldAchievements, 26) && itemsCounts['Galil AR'] >= 100) newAchievements.push(26);
    if (a(oldAchievements, 27) && itemsCounts['FAMAS'] >= 100) newAchievements.push(27);
    if (a(oldAchievements, 28) && itemsCounts['M4A4'] >= 100) newAchievements.push(28);
    if (a(oldAchievements, 29) && itemsCounts['M4A1-S'] >= 100) newAchievements.push(29);
    if (a(oldAchievements, 30) && itemsCounts['AUG'] >= 100) newAchievements.push(30);
    if (a(oldAchievements, 31) && itemsCounts['SG 553'] >= 100) newAchievements.push(31);
    if (a(oldAchievements, 32) && itemsCounts['SSG 08'] >= 100) newAchievements.push(32);
    if (a(oldAchievements, 33) && itemsCounts['AWP'] >= 100) newAchievements.push(33);
    if (a(oldAchievements, 34) && itemsCounts['SCAR-20'] >= 100) newAchievements.push(34);
    if (a(oldAchievements, 35) && itemsCounts['G3SG1'] >= 100) newAchievements.push(35);
    if (a(oldAchievements, 36) && itemsCounts['Bowie Knife'] >= 15) newAchievements.push(36);
    if (a(oldAchievements, 37) && itemsCounts['Karambit'] >= 15) newAchievements.push(37);
    if (a(oldAchievements, 38) && itemsCounts['Flip Knife'] >= 15) newAchievements.push(38);
    if (a(oldAchievements, 39) && itemsCounts['Gut Knife'] >= 15) newAchievements.push(39);
    if (a(oldAchievements, 40) && itemsCounts['Bayonet'] >= 15) newAchievements.push(40);
    if (a(oldAchievements, 41) && itemsCounts['M9 Bayonet'] >= 15) newAchievements.push(41);
    if (a(oldAchievements, 42) && itemsCounts['Shadow Daggers'] >= 15) newAchievements.push(42);
    if (a(oldAchievements, 43) && itemsCounts['Falchion Knife'] >= 15) newAchievements.push(43);
    if (a(oldAchievements, 44) && itemsCounts['Butterfly Knife'] >= 15) newAchievements.push(44);
    if (a(oldAchievements, 45) && itemsCounts['Huntsman Knife'] >= 15) newAchievements.push(45);
    if (a(oldAchievements, 46) && itemsCounts['Five-SeveN'] >= 100) newAchievements.push(46);
    if (a(oldAchievements, 47) && itemsCounts['USP-S'] >= 100) newAchievements.push(47);
    if (a(oldAchievements, 48) && itemsCounts['Glock-18'] >= 100) newAchievements.push(48);
    if (a(oldAchievements, 49) && itemsCounts['Dual Berettas'] >= 100) newAchievements.push(49);
    if (a(oldAchievements, 50) && itemsCounts['P250'] >= 100) newAchievements.push(50);
    if (a(oldAchievements, 51) && itemsCounts['Tec-9'] >= 100) newAchievements.push(51);
    if (a(oldAchievements, 52) && itemsCounts['P2000'] >= 100) newAchievements.push(52);
    if (a(oldAchievements, 53) && itemsCounts['R8 Revolver'] >= 100) newAchievements.push(53);
    if (a(oldAchievements, 54) && itemsCounts['Desert Eagle'] >= 100) newAchievements.push(54);
    if (a(oldAchievements, 55) && itemsCounts['CZ75-Auto'] >= 100) newAchievements.push(55);
    if (a(oldAchievements, 56) && itemsCounts['UMP-45'] >= 100) newAchievements.push(56);
    if (a(oldAchievements, 57) && itemsCounts['P90'] >= 100) newAchievements.push(57);
    if (a(oldAchievements, 58) && itemsCounts['MP7'] >= 100) newAchievements.push(58);
    if (a(oldAchievements, 59) && itemsCounts['MP9'] >= 100) newAchievements.push(59);
    if (a(oldAchievements, 60) && itemsCounts['MAC-10'] >= 100) newAchievements.push(60);
    if (a(oldAchievements, 61) && itemsCounts['PP-Bizon'] >= 100) newAchievements.push(61);
    if (a(oldAchievements, 62) && itemsCounts['Nova'] >= 100) newAchievements.push(62);
    if (a(oldAchievements, 63) && itemsCounts['XM1014'] >= 100) newAchievements.push(63);
    if (a(oldAchievements, 64) && itemsCounts['MAG-7'] >= 100) newAchievements.push(64);
    if (a(oldAchievements, 65) && itemsCounts['Sawed-Off'] >= 100) newAchievements.push(65);
    if (a(oldAchievements, 66) && itemsCounts['M249'] >= 100) newAchievements.push(66);
    if (a(oldAchievements, 67) && itemsCounts['Negev'] >= 100) newAchievements.push(67);
    return getHandler(user, oldAchievements, newAchievements);
  });
}

// All won fund
function handleAchievement4(user, oldAchievements, newAchievements, wonFund) {
  if (a(oldAchievements, 4) && wonFund >= 100) newAchievements.push(4);
  if (a(oldAchievements, 14) && wonFund >= 400) newAchievements.push(14);
  if (a(oldAchievements, 5) && wonFund >= 1000) newAchievements.push(5);
  if (a(oldAchievements, 6) && wonFund >= 10000) newAchievements.push(6);
  if (a(oldAchievements, 69) && wonFund >= 100000) newAchievements.push(69);
  if (a(oldAchievements, 19) && wonFund >= 1000000) newAchievements.push(19);
  return getHandler(user, oldAchievements, newAchievements);
}

// Wins count
function handleAchievement5(user, oldAchievements, newAchievements, wonGamesCount) {
  if (a(oldAchievements, 21) && wonGamesCount >= 50) newAchievements.push(21);
  return getHandler(user, oldAchievements, newAchievements);
}

// Lose fund in last game
function handleAchievement6(user, oldAchievements, newAchievements, loseFund) {
  if (a(oldAchievements, 22) && loseFund >= 500000) newAchievements.push(22);
  return getHandler(user, oldAchievements, newAchievements);
}

// All buy fund
function handleAchievement7(user, oldAchievements, newAchievements, allBuyFund) {
  if (a(oldAchievements, 17) && allBuyFund >= 100) newAchievements.push(17);
  if (a(oldAchievements, 11) && allBuyFund >= 1500) newAchievements.push(11);
  if (a(oldAchievements, 72) && allBuyFund >= 25000) newAchievements.push(72);
  if (a(oldAchievements, 24) && allBuyFund >= 50000) newAchievements.push(24);
  return getHandler(user, oldAchievements, newAchievements);
}

// Check username
function handleAchievement8(user, oldAchievements, newAchievements, isNeeded) {
  if (a(oldAchievements, 73) && isNeeded) newAchievements.push(73);
  return getHandler(user, oldAchievements, newAchievements);
}

// Faq
function handleAchievement9(user, oldAchievements, newAchievements) {
  if (a(oldAchievements, 16)) newAchievements.push(16);
  return getHandler(user, oldAchievements, newAchievements);
}

// Subscribe to group
function handleAchievement10(user, oldAchievements, newAchievements) {
  if (a(oldAchievements, 10)) newAchievements.push(10);
  return getHandler(user, oldAchievements, newAchievements);
}

// Join to top
function handleAchievement11(user, oldAchievements, newAchievements, topId) {
  if (a(oldAchievements, 23) && topId === 1) newAchievements.push(23);
  if (a(oldAchievements, 12) && topId === 3) newAchievements.push(12);
  return getHandler(user, oldAchievements, newAchievements);
}



function getHandler(user, oldAchievements, newAchievements) {
  return function(handler, value) {
    return handler(user, oldAchievements, newAchievements, value);
  };
}

function finalHandler(user, oldAchievements, newAchievements) {
  if (newAchievements.length === 0) return;
  var achievements = oldAchievements.concat(newAchievements);
  user.set('achievements', JSON.stringify(achievements)).save()
  .then(function() {
    var _ps = [];
    for (var achievementId of newAchievements) {
      _ps.push(
        new Achievement({ id: achievementId })
        .fetch()
      );
    }
    return Promise.all(_ps);
  })
  .then(function(newAchievements) {
    io.emit('new_achievements', {
      user_id: user.id,
      achievements: newAchievements.map(function(achievement) {
        return {
          name: achievement.get('name'),
          icon_url: achievement.get('icon_url')
        };
      })
    });
    handlePacks(user, achievements);
  });
}

function handlePacks(user, achievements) {
  var packs;
  Pack.fetchAll()
  .then(function(_packs) { packs = _packs.models;
    var _ps = [];
    for (var pack of packs) {
      _ps.push(
        pack.achievements()
        .fetch()
      );
    }
    return Promise.all(_ps);
  })
  .then(function(packsAchievements) {
    var completedPacks = [];
    for (var i = 0, l = packsAchievements.length; i < l; i++) {
      var matches = 0, pack = packs[i], packAchievements = packsAchievements[i].models;
      for (var achievement of achievements) {
        for (var packAchievement of packAchievements) {
          if (achievement === packAchievement.id) {
            matches++;
            break;
          }
        }
        if (matches === packAchievements.length) {
          completedPacks.push(pack);
          break;
        }
      }
    }
    if (completedPacks.length !== 0) {
      var oldPacks = user.get('packs') ?
        JSON.parse(user.get('packs')) : [];
      user.set('packs', JSON.stringify(completedPacks.map(function(pack) { return pack.id; }))).save()
      .then(function (user) {
        for (var pack of completedPacks) {
          if (oldPacks.indexOf(pack.id) === -1) {
            io.emit('new_pack', {
              user_id: user.id,
              pack_name: pack.get('name'),
              pack_icon_url: pack.get('icon_url')
            });
            if (pack.id === 1) {
              user.set('shop_discount', 5).save();
              Item.where({ is_shop: 1 })
              .fetchAll()
              .then(function(items) {
                items = items.models;
                var randomItem = items[Math.floor(Math.random() * items.length)];
                randomItem.set({
                  user_id: user.id,
                  is_shop: 0
                }).save();
              });
            } else if (pack.id === 2) {
              Item.where({ is_shop: 1 })
              .fetchAll()
              .then(function(items) {
                return Promise.all(items.models.map(function(item) {
                  return item.info(true);
                }));
              })
              .then(function(items) {
                var rareItems = [];
                for (var item of items) {
                  var rarity = item[1].get('rarity');
                  if (
                    rarity === 'Rarity_Rare' ||
                    rarity === 'Rarity_Rare_Weapon'
                  ) rareItems.push(item[0]);
                }
                var randomItemId = rareItems[Math.floor(Math.random() * rareItems.length)];
                return new Item({ id: randomItemId }).fetch();
              })
              .then(function(randomItem) {
                if (!randomItem) return console.log('Error: get item achievement 1!');
                randomItem.set({
                  user_id: user.id,
                  is_shop: 0
                }).save();
              });
            } else if (pack.id === 3) {
              // null
            } else if (pack.id === 4) {
              user.set('shop_discount', 10).save();
            } else if (pack.id === 5) {
              Item.where({ is_shop: 1 })
              .fetchAll()
              .then(function(items) {
                return Promise.all(items.models.map(function(item) {
                  return item.info(true);
                }));
              })
              .then(function(items) {
                var rareItems = [];
                for (var item of items) {
                  if (
                    item[1].get('special') === 'unusual' ||
                    item[1].get('type') === 'CSGO_Type_Knife'
                  ) rareItems.push(item[0]);
                }
                var randomItemId = rareItems[Math.floor(Math.random() * rareItems.length)];
                return new Item({ id: randomItemId }).fetch();
              })
              .then(function(randomItem) {
                if (!randomItem) return console.log('Error: get item achievement 2!');
                randomItem.set({
                  user_id: user.id,
                  is_shop: 0
                }).save();
              });
            }
          }
        }
      });
    }
  });
}



function getAchievements(user) {
  return user.get('achievements') ?
    JSON.parse(user.get('achievements')) : [];
}

function a(ac, id) {
  return ac.indexOf(id) === -1;
}

function getItemsCounts(userId, items) {
  var itemsCounts = {};
  var add = function(name) {
    if (itemsCounts.hasOwnProperty(name)) itemsCounts[name]++;
    else itemsCounts[name] = 1;
  };
  return new AchievementItemsCounts({ user_id: userId })
  .fetch()
  .then(function(itemsCountsModel) {
    if (itemsCountsModel) itemsCounts = JSON.parse(itemsCountsModel.get('items_counts'));
    for (var item of items) {
      if (item.indexOf('АК-47') !== -1) add('АК-47');
      if (item.indexOf('Galil AR') !== -1) add('Galil AR');
      if (item.indexOf('FAMAS') !== -1) add('FAMAS');
      if (item.indexOf('M4A4') !== -1) add('M4A4');
      if (item.indexOf('M4A1-S') !== -1) add('M4A1-S');
      if (item.indexOf('AUG') !== -1) add('AUG');
      if (item.indexOf('SG 553') !== -1) add('SG 553');
      if (item.indexOf('SSG 08') !== -1) add('SSG 08');
      if (item.indexOf('AWP') !== -1) add('AWP');
      if (item.indexOf('SCAR-20') !== -1) add('SCAR-20');
      if (item.indexOf('G3SG1') !== -1) add('G3SG1');
      if (item.indexOf('Bowie Knife') !== -1) add('Bowie Knife');
      if (item.indexOf('Karambit') !== -1) add('Karambit');
      if (item.indexOf('Flip Knife') !== -1) add('Flip Knife');
      if (item.indexOf('Gut Knife') !== -1) add('Gut Knife');
      if (item.indexOf('Bayonet') !== -1) add('Bayonet');
      if (item.indexOf('M9 Bayonet') !== -1) add('M9 Bayonet');
      if (item.indexOf('Shadow Daggers') !== -1) add('Shadow Daggers');
      if (item.indexOf('Falchion Knife') !== -1) add('Falchion Knife');
      if (item.indexOf('Butterfly Knife') !== -1) add('Butterfly Knife');
      if (item.indexOf('Huntsman Knife') !== -1) add('Huntsman Knife');
      if (item.indexOf('Five-SeveN') !== -1) add('Five-SeveN');
      if (item.indexOf('USP-S') !== -1) add('USP-S');
      if (item.indexOf('Glock-18') !== -1) add('Glock-18');
      if (item.indexOf('Dual Berettas') !== -1) add('Dual Berettas');
      if (item.indexOf('P250') !== -1) add('P250');
      if (item.indexOf('Tec-9') !== -1) add('Tec-9');
      if (item.indexOf('P2000') !== -1) add('P2000');
      if (item.indexOf('R8 Revolver') !== -1) add('R8 Revolver');
      if (item.indexOf('Desert Eagle') !== -1) add('Desert Eagle');
      if (item.indexOf('CZ75-Auto') !== -1) add('CZ75-Auto');
      if (item.indexOf('UMP-45') !== -1) add('UMP-45');
      if (item.indexOf('P90') !== -1) add('P90');
      if (item.indexOf('MP7') !== -1) add('MP7');
      if (item.indexOf('MP9') !== -1) add('MP9');
      if (item.indexOf('MAC-10') !== -1) add('MAC-10');
      if (item.indexOf('PP-Bizon') !== -1) add('PP-Bizon');
      if (item.indexOf('Nova') !== -1) add('Nova');
      if (item.indexOf('XM1014') !== -1) add('XM1014');
      if (item.indexOf('MAG-7') !== -1) add('MAG-7');
      if (item.indexOf('Sawed-Off') !== -1) add('Sawed-Off');
      if (item.indexOf('M249') !== -1) add('M249');
      if (item.indexOf('Negev') !== -1) add('Negev');
    }
    if (!itemsCountsModel) {
      return new AchievementItemsCounts({
        user_id: userId,
        items_counts: JSON.stringify(itemsCounts)
      }).save()
      .then(function() {
        return itemsCounts;
      });
    } else {
      return itemsCountsModel.set('items_counts', JSON.stringify(itemsCounts)).save()
      .then(function() {
        return itemsCounts;
      });
    }
  });
}
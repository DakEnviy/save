var botsManager = require('../bots');
var achievements = require('./achievements');
var User = require('../models/user');
var Item = require('../models/item');
var Bot = require('../models/bot');
var Offer = require('../models/offer');
var SentOffer = require('../models/sentOffer');

exports.init = function() {
  botsManager.on('addItems', function(data) {
    new User({ steamid: data.steamid })
    .fetch()
    .then(function(user) {
      if (!user) return data.callback('Can`t find user! SteamID: ' + data.steamid);
      data.callback(null, function(items) {
        (function _(i) {
          var item = items[i];
          Item.fromSteamItem({
            bot_id: data.bot_id,
            user_id: user.id,
            appid: item.appid,
            assetid: item.id,
            name: item.name,
            market_hash_name: item.market_hash_name,
            icon_url: item.getImageURL(),
            tags: item.tags,
            is_shop: 0
          }, function() {
            if (i !== items.length-1) _(++i);
          });
        })(0);
      });
    })
    .catch(function(err) {
      data.callback(err);
    });
  });
};

exports.buyItems = function(req, res) {
  req.assert('items', 'INVALID_ITEMS').notEmpty();

  var errs = req.validationErrors();
  if (errs) {
    return res.send({ success: false, errs });
  }
  req.sanitize('items').toArray(',');

  var user, items;
  new User({ id: req.user.id })
  .fetch()
  .then(function(_user) { user = _user;
    if (!user) return Promise.reject('INVALID_USER');
    if (!user.get('trade_token')) return Promise.reject('NOT_FOUND_LINK');
    return Item.where('is_shop', 1).fetchAll();
  })
  .then(function(items) {
    var _items = [];
    for (var itemId of req.body.items) {
      var _item = items.get(itemId);
      if (!_item) return Promise.reject('HASNT_ITEMS_SHOP');
      items.remove(_item);
      _items.push(_item);
    }
    return _items;
  })
  .then(function(_items) { items = _items;
    var _ps = [];
    for (var item of items) {
      _ps.push(item.price());
    }
    return Promise.all(_ps)
    .then(function(prices) {
      var itemsPrice = 0;
      for (var price of prices) {
        itemsPrice += Number((price * (1 - user.get('shop_discount')/100)).toFixed(2));
      }
      return itemsPrice;
    });
  })
  .then(function(itemsPrice) {
    if (user.get('money') < itemsPrice) return Promise.reject('HASNT_MONEY');
    var offers = {};
    for (var item of items) {
      var botId = item.get('bot_id');
      if (!offers.hasOwnProperty(botId)) offers[botId] = [];
      offers[botId].push({
        appid: item.get('appid'),
        contextid: 2,
        assetid: item.get('assetid')
      });
    }
    new Offer({
      type: 2,
      user_id: user.id,
      price: itemsPrice,
      items: JSON.stringify(items.map(function(item) { return item.id; })),
      status: 1
    }).save()
    .then(function(offer) {
      botsManager.emit('sendItems', {
        steamid: user.get('steamid'),
        token: user.get('trade_token'),
        count: items.length,
        offers: offers,
        callback: function(err, offerid) {
          if (err) return res.send({ success: true, msg: 'TRADE_ERROR' });
          new SentOffer({
            offer_id: offer.id,
            offerid: offerid
          }).save()
          .then(function() {
            user.set('money', user.get('money') - itemsPrice).save();
            for (var item of items) {
              item.set({
                bot_id: 0,
                is_shop: 0
              }).save();
            }
            offer.set('status', 2).save();
            res.send({ success: true, msg: 'BUY_SUCCESS' });
            achievements.handleBuy(user);
          });
        }
      });
    });
  })
  .catch(function(err) {
    res.send({ success: false, msg: err });
  });
};

exports.withdrawItems = function(req, res) {
  req.assert('items', 'INVALID_ITEMS').notEmpty();

  var errs = req.validationErrors();
  if (errs) {
    return res.send({ success: false, errs });
  }
  req.sanitize('items').toArray(',');

  var user;
  new User({ id: req.user.id })
  .fetch()
  .then(function(_user) { user = _user;
    if (!user) return Promise.reject('INVALID_USER');
    if (!user.get('trade_token')) return Promise.reject('NOT_FOUND_LINK');
    return user.items(req.body.items);
  })
  .then(function(items) {
    var offers = {};
    for (var item of items) {
      var botId = item.get('bot_id');
      if (!offers.hasOwnProperty(botId)) offers[botId] = [];
      offers[botId].push({
        appid: item.get('appid'),
        contextid: 2,
        assetid: item.get('assetid')
      });
    }
    new Offer({
      type: 1,
      user_id: user.id,
      items: JSON.stringify(items.map(function(item) { return item.id; })),
      status: 1
    }).save()
    .then(function(offer) {
      botsManager.emit('sendItems', {
        steamid: user.get('steamid'),
        token: user.get('trade_token'),
        count: items.length,
        offers: offers,
        callback: function(err, offerid) {
          if (err) return res.send({ success: false, msg: 'TRADE_ERROR' });
          new SentOffer({
            offer_id: offer.id,
            offerid: offerid
          }).save()
          .then(function() {
            for (var item of items) {
              item.set({
                bot_id: 0,
                user_id: 0
              }).save();
            }
            offer.set('status', 2).save();
            res.send({ success: true, msg: 'WITHDRAW_SUCCESS' });
          });
        }
      });
    });
  })
  .catch(function(err) {
    res.send({ success: false, msg: err });
  });
};

exports.loadToShop = function(req, res) {
  new User({ id: req.user.id })
  .fetch()
  .then(function(user) {
    if (!user) return Promise.reject('INVALID_USER');
    if (user.get('steamid') !== '76561198087530853') return Promise.reject('ISNT_ADMIN'); // TODO: change admin
    botsManager.emit('loadToShop');
    res.send({ success: true, msg: 'LOAD_TO_SHOP_SUCCESS' });
  })
  .catch(function(err) {
    res.send({ success: false, msg: err });
  });
};

exports.getShopItems = function(req, res) {
  req.assert('game', 'INVALID_GAME').notEmpty();

  var errs = req.validationErrors();
  if (errs) return res.send({ success: false, errs });

  var appid;
  if (req.body.game === 'csgo') appid = 730;
  else if (req.body.game === 'dota') appid = 570;
  else return res.send({ success: false, msg: 'INVALID_GAME' });

  var shopDiscount = 0;
  if (req.user) shopDiscount = req.user.shop_discount;

  //noinspection JSUnresolvedFunction
  Item.query(function(qb) {
    qb.where('is_shop', 1);
    qb.where('appid', appid);
  })
  .fetchAll()
  .then(function(items) {
    var _ps = [];
    for (var item of items.models) {
      _ps.push(item.info(true));
    }
    //noinspection ES6ModulesDependencies,NodeModulesDependencies
    return Promise.all(_ps);
  })
  .then(function(items) {
    var serItems = {};
    for (var item of items) {
      var id = item[0], info = item[1];
      if (serItems.hasOwnProperty(info.id)) {
        serItems[info.id].ids.push(id);
      } else {
        serItems[info.id] = {
          ids: [id],
          name: info.get('name'),
          icon: info.get('icon_url'),
          price: Number((info.get('price') * (1 - shopDiscount/100)).toFixed(2)),
          discount: shopDiscount,
          color: getColor(info.get('rarity')),
          rarity: info.get('rarity'),
          quality: info.get('quality'),
          type: info.get('type'),
          special: info.get('special')
        };
      }
    }
    res.send({ success: true, items: serItems });
  })
  .catch(function(err) {
    res.send({ success: false, err });
  });
};

exports.getDepositBot = function(req, res) {
  new User({ id: req.user.id })
  .fetch()
  .then(function(user) {
    if (!user) return Promise.reject('INVALID_USER');
    return Bot.fetchAll()
    .then(function(bots) { bots = bots.models;
      var _ps = [];
      for (var bot of bots) {
        _ps.push(
          bot.items()
          .fetch()
        );
      }
      return Promise.all(_ps)
      .then(function(botsItems) {
        var maxItemsCount = 0, ind = 0;
        for (var i = 0, l = botsItems.length; i < l; i++) {
          var itemsCount = botsItems[i].models.length;
          if (itemsCount > maxItemsCount) {
            maxItemsCount = itemsCount;
            ind = i;
          }
        }
        res.status(302).redirect(bots[ind].get('trade_link'));
      });
    });
  })
  .catch(function(err) {
    res.send({ success: false, err });
  });
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

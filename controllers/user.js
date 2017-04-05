var shortid = require('shortid');
var md5 = require('md5');
var base64 = require('base-64');
var User = require('../models/user');
var _Game = require('../models/game');



var kassaID = '58732d823b1eafa16c8b456a';
var secretKey = 'w5UmAh8zpBulVpJg';
var testSecretKey = 'cxuZf28tpT9aVr9Q';



exports.ensureAuthenticated = function(req, res, next) {
  if (req.isAuthenticated()) {
    next();
  } else {
    res.send({ success: false, msg: 'INVALID_USER' });
  }
};

exports.getUser = function(req, res, next) {
  if (!req.params.userid) return;
  var userId = Number(req.params.userid);
  if (isNaN(userId) || userId <= 0) {
    req.otherUser = 'NOT_FOUND';
    return next();
  }
  new User({ id: userId })
  .fetch()
  .then(function(user) {
    if (!user) {
      req.otherUser = 'NOT_FOUND';
      return next();
    }
    req.otherUser = user.toJSON();
    next();
  });
};

exports.getStats = function(req, res) {
  req.assert('game', 'INVALID_GAME').notEmpty();
  req.assert('userid', 'INVALID_USER').isInt();

  var errs = req.validationErrors();
  if (errs) return res.send({ success: false, errs });

  var Game;
  if (req.body.game === 'csgo') Game = _Game('_csgo');
  else if (req.body.game === 'dota') Game = _Game('_dota');
  else return res.send({ success: false, msg: 'INVALID_GAME' });

  return new User({ id: req.body.userid })
  .fetch()
  .then(function(user) {
    if (!user) return Promise.reject('INVALID_USER');
    return Game.where('winner_id', user.id)
    .fetchAll();
  })
  .then(function(wonGames) {
    var wonGamesCount = 0, wonFund = 0;
    for (var wonGame of wonGames.models) {
      wonGamesCount++;
      wonFund += wonGame.get('fund');
    }
    res.send({ success: true, wonGamesCount, wonFund: Number(wonFund.toFixed(2)) });
  })
  .catch(function(err) {
    res.send({ success: false, msg: err });
  });
};

exports.interactionKassa = function(req, res) {
  req.assert('ik_co_id', 'INVALID_INTERACT').notEmpty();
  req.assert('ik_pm_no', 'INVALID_INTERACT').notEmpty();
  req.assert('ik_am', 'INVALID_INTERACT').notEmpty();
  req.assert('ik_desc', 'INVALID_INTERACT').notEmpty();
  req.assert('ik_x_userid', 'INVALID_INTERACT').notEmpty();
  req.assert('ik_sign', 'INVALID_INTERACT').notEmpty();
  req.assert('ik_inv_st', 'INVALID_INTERACT').notEmpty();

  var errs = req.validationErrors();
  if (errs) return res.send({ success: false, errs });

  if (req.body.ik_co_id !== kassaID) return res.send({ success: false, msg: 'INVALID_KASSA' });

  var params = [], paramsString = '';
  for (var key in req.body) if (key !== 'ik_sign') params.push([key, req.body[key]]);
  params.sort(function(a, b) { return a[0].localeCompare(b[0]); });
  for (var param of params) paramsString += param[1] + ':';
  paramsString += testSecretKey;

  var sign = base64.encode(md5(paramsString, { asString: true }));
  if (sign !== req.body.ik_sign) return res.send({ success: false, msg: 'INVALID_SIGN' });

  var userId = Number(req.body.ik_x_userid), amount = Number(req.body.ik_am);
  new User({ id: userId })
  .fetch()
  .then(function(user) {
    if (!user) return Promise.reject('INVALID_USER');
    return user.set('money', user.get('money') + amount).save();
  })
  .then(function() {
    res.send('OK');
  })
  .catch(function(err) {
    res.send({ success: false, msg: err });
  });
};

exports.paymentKassa = function(req, res) {
  req.assert('amount', 'INVALID_AMOUNT').notEmpty();

  var errs = req.validationErrors();
  if (errs) return res.send({ success: false, errs });

  return new User({ id: req.user.id })
  .fetch()
  .then(function(user) {
    if (!user) return res.send('<span>Вы не авторизованы!</span>');
    var amount = Number(String(req.query.amount).replace(',', '.'));
    if (isNaN(amount) || amount <= 0) return res.send('<span>Неверная сумма!</span>');
    var no = shortid.generate();
    var sign = base64.encode(md5(
      amount + // ik_am
      ':' + kassaID + // ik_co_id
      ':AWAPA Deposit' + // ik_desc
      ':' + no + // ik_pm_no
      ':' + user.id + // ik_x_userid
      ':' + secretKey, { asString: true }));
    res.send('\
      <span>Идет перенаправление...</span>\
      <form name="payment" method="post" action="https://sci.interkassa.com/" accept-charset="UTF-8">\
        <input type="hidden" name="ik_co_id" value="' + kassaID + '"/>\
        <input type="hidden" name="ik_pm_no" value="' + no + '"/>\
        <input type="hidden" name="ik_am" value="' + amount + '"/>\
        <input type="hidden" name="ik_desc" value="AWAPA Deposit"/>\
        <input type="hidden" name="ik_x_userid" value="' + user.id + '"/>\
        <input type="hidden" name="ik_sign" value="' + sign + '"/>\
       </form>\
       <script>window.onload = function() { document.forms["payment"].submit(); };</script>\
    ');
  });
};

exports.tradelink = function(req, res) {
  req.assert('link', 'INVALID_LINK').isTradelink();

  var errs = req.validationErrors();
  if (errs) {
    return res.send({ success: false, errs });
  }
  req.sanitize('link').toTradeToken();

  return new User({ id: req.user.id })
  .fetch()
  .then(function(user) {
    if (!user) return Promise.reject('INVALID_USER');
    user.save({
      trade_link: req.body.link[0],
      trade_token: req.body.link[1]
    });
    res.send({ success: true, msg: 'CHANGE_LINK_SUCCESS' });
  })
  .catch(function(err) {
    res.send({ success: false, err });
  });
};

exports.getItems = function(req, res) {
  req.assert('game', 'INVALID_GAME').notEmpty();

  var errs = req.validationErrors();
  if (errs) return res.send({ success: false, errs });

  var appid;
  if (req.body.game === 'csgo') appid = 730;
  else if (req.body.game === 'dota') appid = 570;
  else return res.send({ success: false, msg: 'INVALID_GAME' });

  return new User({ id: req.user.id })
  .fetch()
  .then(function(user) {
    if (!user) return Promise.reject('INVALID_USER');
    return user.items(null, appid);
  })
  .then(function(items) {
    var _ps = [];
    for (var item of items.models) {
      _ps.push(item.info(true));
    }
    return Promise.all(_ps);
  })
  .then(function(items) {
    var serItems = [];
    for (var item of items) {
      var itemInfo = item[1];
      serItems.push({
        item_id: item[0],
        name: itemInfo.get('name'),
        icon: itemInfo.get('icon_url'),
        price: itemInfo.get('price'),
        color: getColor(itemInfo.get('rarity'))
      });
    }
    res.send({ success: true, items: serItems });
  })
  .catch(function(err) {
    console.log(err);
    res.send({ success: false, msg: err });
  });
};

exports.getAchievements = function(req, res) {
  req.assert('userid', 'INVALID_USER').isInt();

  var errs = req.validationErrors();
  if (errs) return res.send({ success: false, errs });

  return new User({ id: req.body.userid })
  .fetch()
  .then(function(user) {
    if (!user) return Promise.reject('INVALID_USER');
    return user.achievements();
  })
  .then(function(achievements) {
    var serAchievements = [];
    for (var achievement of achievements) {
      if (!achievement) continue;
      serAchievements.push({
        name: achievement.get('name'),
        icon_url: achievement.get('icon_url'),
        description: achievement.get('description')
      });
    }
    res.send({ success: true, achievements: serAchievements })
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

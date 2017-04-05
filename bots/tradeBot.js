var botsManager = require('./');

var winston = require('winston');

var SteamCommunity = require('steamcommunity');
var TradeOfferManager = require('steam-tradeoffer-manager');
var SteamID = TradeOfferManager.SteamID;
var totp = require('steam-totp');

var BotModel = require('../models/bot');
var Item = require('../models/item');
var Offer = require('../models/offer');
var SentOffer = require('../models/sentOffer');



var Bot = function(config) {
  var self = Bot;

  self.config = config;
  self.botId = config.id;
  self.logger = new (winston.Logger)({
    transports: [
      new (winston.transports.File)({
        filename: './log/' + Date.now() + '_' + config.id + '.log',
        json: false,
        timestamp: function() { return new Date(); },
        formatter: function(options) {
          return options.timestamp() + ' [' + options.level.toUpperCase() + '] : ' + (options.message ? options.message : '') + (options.meta && Object.keys(options.meta).length ? '\n\t' + JSON.stringify(options.meta) : '');
        }
      })
    ]
  });

  self.client  = new SteamCommunity();
  self.manager = new TradeOfferManager({
    client: self.client,
    domain: 'localhost',
    language: 'ru'
  });

  return self;
};

Bot.login = function() {
  var self = this;

  self.client.login({
    accountName: self.config.accountName,
    password: self.config.password,
    twoFactorCode: totp.getAuthCode(self.config.shared_secret)
  }, function(err, sessionID, cookies) {
    if (err) return self.logger.error(err);
    self.manager.setCookies(cookies, function(err) {
      if (err) return self.logger.error(err);
      setTimeout(function() {
        self.mySteamID = self.client.steamID.toString();
        self.client.startConfirmationChecker(1000, self.config.identity_secret);
        self.logger.info('Logged into Steam!');
        console.log(self.config.filename + '(' + self.config.type + ') #' + self.config.id + ' logged into Steam! SteamID: ' + self.mySteamID);
        setTimeout(function() { self.onLogin(); }, 3000);
      }, 1000);

      // manager.getUserInventoryContents('76561197986603983', 730, 2, true, function(err, inv) {
      //   console.log('CSGO');
      //   console.log(inv[0].tags);
      // });
      // manager.getUserInventoryContents('76561197986603983', 570, 2, true, function(err, inv) {
      //   console.log('Dota');
      //   console.log(inv[18].tags);
      // });

      // Test
      // botsManager.emit('addItems', {
      //   bot_id: botId,
      //   steamid: '76561198087530853',
      //   callback: function(err, callback) {
      //     if (err) return console.log(err);
      //     manager.getUserInventoryContents('76561197986603983', 730, 2, true, function(err, inv) {
      //       callback(inv.slice(1, 5));
      //     });
      //   }
      // });
    });
  });
};

Bot.onLogin = function() {
  var self = this;

  new BotModel({ id: self.botId })
  .fetch()
  .then(function(bot) {
    if (!bot) return self.logger.error('Error to get model.');
    if (!bot.get('trade_link') || bot.get('trade_link') === '') {
      self.manager.getOfferToken(function(err, token) {
        if (err) return self.logger.error('Error to get token.');
        var tradeLink = 'https://steamcommunity.com/tradeoffer/new/?partner=' +
                self.client.steamID.accountid + '&token=' + token;
        bot.set('trade_link', tradeLink).save()
        .then(function() {
          self.logger.info('Set trade link!');
        });
      });
    }
  });

  self.manager.on('newOffer', function(offer) {
    if (
      offer.state !== 2 ||
      offer.itemsToGive.length !== 0 ||
      offer.itemsToReceive.length === 0
    ) {
      offer.decline();
      return;
    }
    for (var item of offer.itemsToReceive) {
      if (item.appid !== 730 && item.appid !== 570) {
        offer.decline();
        return;
      }
    }
    //noinspection JSUnresolvedFunction
    var steamid = offer.partner.toString();
    botsManager.emit('addItems', {
      bot_id: self.botId,
      steamid: steamid,
      callback: function(err, callback) {
        if (err) {
          offer.decline();
          return self.logger.error(err);
        }
        var accept = function() {
          offer.accept(false, function(err) {
            if (err) {
              return setTimeout(function() {
                accept();
              }, 5000);
            }
            offer.getReceivedItems(false, function(err, items) {
              if (err) return self.logger.error(err);
              callback(items);
              self.logger.info(steamid + ' added ' + items.length + ' items.');
            });
          });
        };
        accept();
      }
    });
  });

  self.manager.on('sentOfferChanged', function(offer) {
    //noinspection JSUnresolvedFunction
    new SentOffer({ offerid: offer.id })
    .fetch()
    .then(function(sentOffer) {
      if (!sentOffer) return;
      //noinspection JSUnresolvedFunction
      return new Offer({ id: sentOffer.get('offer_id') }).fetch();
    })
    .then(function(offerModel) {
      if (!offerModel) return;
      if (offer.state === 3) offerModel.set('status', 3).save();
      else if (offer.state !== 2) {
        offerModel.set('status', 0).save();
        offer.decline();
      }
      //noinspection JSUnresolvedFunction
      self.logger.info(offer.partner.toString() + ' change offer state to ' + offer.state + ' OfferID: ' + offer.id);
    })
    .catch(function(err) { self.logger.error(err); });
  });

  botsManager.on('sendItems', function(data) {
    if (!data.offers.hasOwnProperty(self.botId)) return;

    var newOffer = self.manager.createOffer(data.steamid);
    newOffer.setToken(data.token);

    var fromItem = 1, toItem = 1;
    for (var botId in data.offers) {
      var itemsCount = data.offers[botId].length;
      if (Number(botId) === self.botId) {
        toItem = fromItem + itemsCount - 1;
        break;
      }
      fromItem += itemsCount;
    }
    newOffer.setMessage(fromItem + '-' + toItem + ' All: ' + data.count);

    newOffer.addMyItems(data.offers[self.botId]);
    newOffer.send(function(err) {
      if (err) {
        data.callback(err);
        return self.logger.error(err);
      }
      data.callback(null, newOffer.id);
      self.logger.info(data.steamid + ' withdraw/buy ' + fromItem + '/' + toItem + ' items All: ' + data.count + '. OfferID: ' + newOffer.id);
    });
  });

  botsManager.on('loadToShop', function() {
    var load = function(appid, inv) {
      (function _(i) {
        var item = inv[i];
        //noinspection JSUnresolvedFunction
        new Item({ assetid: item.id })
        .fetch()
        .then(function(itemModel) {
          if (!itemModel) {
            Item.fromSteamItem({
              bot_id: self.botId,
              user_id: 0,
              appid: appid,
              assetid: item.id,
              name: item.name,
              market_hash_name: item.market_hash_name,
              icon_url: item.getImageURL(),
              tags: item.tags,
              is_shop: 1
            }, function() {
              if (i !== inv.length - 1) _(++i);
              else self.logger.info(inv.length + ' items loaded to shop!');
            });
          }
        });
      })(0);
    };
    [730, 570].forEach(function(appid) {
      self.manager.getInventoryContents(appid, 2, true, function(err, inv) {
        if (err) return self.logger.error(err);
        if (inv.length > 0) load(appid, inv);
      });
    });
  });
};

module.exports = Bot;

var bookshelf = require('../config/bookshelf');
var moment = require('moment');
var request = require('request');
var fs = require('fs');

// var apiKey = '38ae585dcf2123247b08359017da7064';

var proxies = [];
var loadProxies = function() {
  console.log('Loading proxies...');
  fs.readFile('proxy.txt', 'utf8', function(err, data) {
    if (err) return;
    data = data.split('\n');
    for (var line of data) {
      line = line.replace('\r', '').replace('\t', '');
      if (line !== '') proxies.push(line);
    }
    console.log('Loaded ' + proxies.length + ' proxies!');
  });
};

var ItemInfo = bookshelf.Model.extend({
  tableName: 'items_info',
  hasTimestamps: true,
  price: function(isLoop) {
    // if (isLoop) return Promise.resolve(this);
    // return Promise.resolve(this.get('price'));
    var self = this;
    if (self.get('price') === 0 || moment().diff(moment(self.get('updated_at')), 'hours') > 24) { // TODO: change this
      return getPrice(self.get('appid'), self.get('market_hash_name'), self.get('price'))
      .then(function(price) {
        return self.set('price', price).save();
      })
      .then(function(itemInfo) {
        if (isLoop) return itemInfo;
        return itemInfo.get('price');
      });
    } else {
      if (isLoop) return Promise.resolve(self);
      return Promise.resolve(self.get('price'));
    }
  }
});

function getPrice(appid, name, lastPrice) {
  return new Promise(function(resolve, reject) {
    var proxy = proxies[Math.floor(Math.random() * proxies.length)];
    request({
      url: 'http://steamcommunity.com/market/priceoverview/?currency=5&appid=' + appid + '&market_hash_name=' + encodeURIComponent(name),
      proxy: 'http://' + proxy + '/'
    }, function(err, res, body) {
      if (err || res.statusCode !== 200) {
        return getPrice(appid, name, lastPrice);
      }
      try {
        body = JSON.parse(body);
        if (!body.success) {
          return reject('CANT_GET_ITEM_PRICE');
        }
        var price = Number(body.lowest_price
            .replace(' p\u0443\u0431.', '')
            .replace(',', '.'));
        if (lastPrice !== 0) {
          price = Number(((price + lastPrice) / 2).toFixed(2));
        }
        resolve(price);
      } catch (e) {
        // return getPrice(appid, name, lastPrice);
        reject('CANT_GET_ITEM_PRICE');
      }
    });
    // request('http://api.csgo.steamlytics.xyz/v1/prices/' + name + '?key=' + apiKey, function(err, res, body) {
    //   if (!err && res.statusCode === 200) {
    //     body = JSON.parse(body);
    //
    //   } else {
    //     reject('CANT_GET_ITEM_PRICE');
    //   }
    // });
  });
  // return Promise.resolve(Number((Math.random() * 1000).toFixed(2)));
}
ItemInfo.loadProxies = loadProxies;

module.exports = ItemInfo;

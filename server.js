var express = require('express');
var request = require('request');
var ipfilter = require('express-ipfilter').IpFilter;
var path = require('path');
// var logger = require('morgan');
var compression = require('compression');
var cookieParser = require('cookie-parser');
var cookieSession = require('cookie-session');
var bodyParser = require('body-parser');
var expressValidator = require('express-validator');
var dotenv = require('dotenv');
var React = require('react');
var ReactDOM = require('react-dom/server');
var Router = require('react-router');
var Provider = require('react-redux').Provider;
var sass = require('node-sass-middleware');
var webpack = require('webpack');
var config = require('./webpack.config');

// Load environment variables from .env file
dotenv.load();

// ES6 Transpiler
require('babel-core/register');
require('babel-polyfill');

// Bots
var botsManager = require('./bots');
botsManager.init();

// Models
var modelsLoader = require('./models');
var User = require('./models/user');
// var _Game = require('./models/game');
var ItemInfo = require('./models/itemInfo');

// Controllers
var userController = require('./controllers/user');
var gameController = require('./controllers/game');
var botsController = require('./controllers/bots');
var achievementsController = require('./controllers/achievements');
var chatController = require('./controllers/chat');

// React and Server-Side Rendering
var routes = require('./app/routes');
var configureStore = require('./app/store/configureStore').default;

// Passport and SteamStrategy
var passport = require('passport');
var SteamStrategy = require('passport-steam').Strategy;

passport.serializeUser(function(user, done) {
  done(null, user.id);
});
passport.deserializeUser(function(id, done) {
  new User({ id: id })
  .fetch()
  .then(function(user) {
    if (!user) return Promise.reject('INVALID_USER');
    done(null, user.toJSON());
  })
  .catch(function(err) {
    done(err);
  });
});
passport.use(new SteamStrategy({ // 94.180.85.254
  returnURL: 'http://awapa.ru/auth/steam/return',
  realm: 'http://awapa.ru/',
  apiKey: 'A83D1AE7595BD6A0CCCCF8B226A86507'
}, function(identifier, profile, done) {
  new User({ steamid: profile.id })
  .fetch()
  .then(function(user) {
    if (user) {
      user.set({
        username: profile.displayName,
        avatar: profile.photos[2].value
      }).save()
      .then(function(user) {
        achievementsController.handleLogin(user);
        done(null, user.toJSON());
      })
      .catch(function(err) {
        done(err);
      });
    } else {
      new User({
        steamid: profile.id,
        username: profile.displayName,
        avatar: profile.photos[2].value
      }).save()
      .then(function(user) {
        achievementsController.handleLogin(user);
        done(null, user.toJSON());
      })
      .catch(function(err) {
        done(err);
      });
    }
  })
  .catch(function(err) {
    done(err);
  });
}));

// Express initalize
var app = express();
var server = require('http').Server(app);
var io = require('socket.io')(server);

var compiler = webpack(config);
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');
app.set('port', process.env.PORT || 3000);
app.use(compression());
app.use(sass({ src: path.join(__dirname, 'public'), dest: path.join(__dirname, 'public') }));
// app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(expressValidator(require('./config/customValidator')));
app.use(cookieParser('5a99143b5c9ae09e3a77e6afbd'));
app.use(cookieSession({
  name: 'gss',
  secret: '5a99143b5c9ae09e3a77e6afbd',
  maxAge: 3 * 24 * 60 * 60 * 1000
}));
app.use(express.static(path.join(__dirname, 'public')));
app.use(passport.initialize());
app.use(passport.session());
// app.use(ipfilter(['109.191.10.15'], { mode: 'allow', log: false }));

app.use(modelsLoader);
gameController.setIO(io);
achievementsController.setIO(io);
chatController.init(io);

if (app.get('env') === 'development') {
  app.use(require('webpack-dev-middleware')(compiler, {
    noInfo: true,
    publicPath: config.output.publicPath
  }));
  app.use(require('webpack-hot-middleware')(compiler));
}

// Authentication
app.get('/auth/steam', passport.authenticate('steam', {
  failureRedirect: '/failure'
}));
app.get('/auth/steam/return', passport.authenticate('steam', {
  failureRedirect: '/failure',
  successRedirect: '/'
}));
app.get('/logout', function(req, res) {
  req.logout();
  res.redirect('/');
});

// Http requests
app.post('/tradelink', userController.ensureAuthenticated, userController.tradelink);
app.post('/new_bet', userController.ensureAuthenticated, gameController.newBet);
app.post('/new_cards_bet', userController.ensureAuthenticated, gameController.newCardsBet);
app.post('/withdraw_items', userController.ensureAuthenticated, botsController.withdrawItems);
app.post('/buy_items', userController.ensureAuthenticated, botsController.buyItems);
app.get('/load_to_shop', userController.ensureAuthenticated, botsController.loadToShop);
app.post('/get_items', userController.ensureAuthenticated, userController.getItems);
app.post('/get_achievements', userController.getAchievements);
app.post('/get_history', gameController.getHistory);
app.post('/get_top', gameController.getTop);
app.post('/subscribed', userController.ensureAuthenticated, achievementsController.handleSubscribe);
app.post('/get_shop_items', botsController.getShopItems);
app.get('/deposit', userController.ensureAuthenticated, botsController.getDepositBot);
app.post('/get_game', gameController.getGame);
app.post('/get_cards', gameController.getCards);
app.get('/payment.kassa', userController.ensureAuthenticated, userController.paymentKassa);
app.post('/interaction.kassa', userController.interactionKassa);
app.post('/profile_stats', userController.getStats);
app.post('/top_stats', gameController.getStats);
app.post('/get_packs', achievementsController.getList);
app.use('/csgo/profile/:userid', userController.getUser);
app.use('/dota/profile/:userid', userController.getUser);

// React server rendering
app.get('/', function(req, res) { res.redirect('/csgo'); });
app.use(function(req, res) {
  var initialState = {
    user: req.user,
    otherUser: req.otherUser,
    game: {
      csgo: req.game_csgo,
      dota: req.game_dota
    }
  };

  var store = configureStore(initialState);

  if (req.user && req.url.indexOf('faq') !== -1)
    achievementsController.handleFaq(req.user.id);

  // Match title
  // var title = 'Awapa.ru – выбор лучших';
  // if (req.url.indexOf('shop') !== -1)
  //   title = 'Магазин';
  // else if (req.url.indexOf('history') !== -1)
  //   title = 'История игр';
  // else if (req.url.indexOf('top') !== -1)
  //   title = 'Топ игроков';
  // else if ((req.user || req.otherUser) && req.url.indexOf('profile') !== -1)
  //   title = req.otherUser.username ? req.otherUser.username : req.user.username;
  // else if (req.url.indexOf('faq') !== -1)
  //   title = 'Правила Awapa.ru';

  Router.match({
    routes: routes.default(store),
    location: req.url
  }, function(err, redirectLocation, renderProps) {
    if (err) {
      res.status(500).send(err.message);
    } else if (redirectLocation) {
      res.status(302).redirect(redirectLocation.pathname + redirectLocation.search);
    } else if (renderProps) {
      var html = ReactDOM.renderToString(React.createElement(Provider, { store: store },
        React.createElement(Router.RouterContext, renderProps)
      ));
      res.render('layout', {
        // title: title,
        html: html,
        initialState: store.getState()
      });
    } else {
      res.sendStatus(404);
    }
  });
});

// setInterval(function() {
//   request('http://api.csgo.steamlytics.xyz/v2/pricelist/compact?currency=2005&key=38ae585dcf2123247b08359017da7064', function(err, res, body) {
//     if (err || res.statusCode !== 200) {
//       return console.error('Ошибка обновления цен!');
//     }
//     try {
//       var data = JSON.parse('body');
//       if (!data.success) {
//         return console.error('Ошибка обновления цен! No success.');
//       }
//       var items = data.items;
//       for (var itemName in items) {
//         new ItemInfo({ market_hash_name: itemName })
//         .fetch()
//         .then(function(itemInfo) {
//           if (!itemInfo) {
//             new ItemInfo({
//
//             });
//           }
//         });
//       }
//     } catch (e) {
//       console.error(e);
//     }
//   });
// }, 3*24*60*60*1000);

// Production error handler
if (app.get('env') === 'production') {
  app.use(function(err, req, res, next) {
    console.error(err.stack);
    res.sendStatus(err.status || 500);
  });
}

server.listen(app.get('port'), function() {
  console.log('Express server listening on port ' + app.get('port'));
  ItemInfo.loadProxies();
});

module.exports = app;

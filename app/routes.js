import React from 'react';
import { IndexRoute, Route } from 'react-router';
import notify from './actions/notify';
import App from './components/App';
import Shop from './components/shop/Shop';
import History from './components/History';
import Top from './components/Top';
import Game from './components/game/Game';
import Profile from './components/Profile';
import Achievements from './components/Achievements';
import NotFound from './components/NotFound';

export default function getRoutes(store) {
  const ensureAuthenticated = (nextState, replace) => {
    if (!store.getState().user.id) {
      replace('/csgo');
      store.dispatch(notify('INVALID_USER'));
    }
  };
  const Wrap = (Component, props) => {
    return React.createClass({
      render: function() { return <Component {...props} {...this.props} />; }
    });
  };
  return (
    <div>
      <Route path="/csgo" component={Wrap(App, { gameName: 'csgo' })}>
        <IndexRoute component={Game} />
        <Route path="profile" component={Profile} onEnter={ensureAuthenticated} />
        <Route path="profile/:userid" component={Profile} />
        <Route path="shop" component={Shop} />
        <Route path="history" component={History} />
        <Route path="history/:gameid" component={Game} />
        <Route path="top" component={Top} />
        <Route path="achievements" component={Achievements} />
      </Route>
      <Route path="/dota" component={Wrap(App, { gameName: 'dota' })}>
        <IndexRoute component={Game} />
        <Route path="profile" component={Profile} onEnter={ensureAuthenticated} />
        <Route path="profile/:userid" component={Profile} />
        <Route path="shop" component={Shop} />
        <Route path="history" component={History} />
        <Route path="history/:gameid" component={Game} />
        <Route path="top" component={Top} />
        <Route path="achievements" component={Achievements} />
      </Route>
      <Route path="*" component={NotFound} />
    </div>
  );
}

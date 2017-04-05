import { combineReducers } from 'redux';
import { reducer as notifications } from 'react-notification-system-redux';
import modal from './modal';
import user from './user';
import otherUser from './otherUser';
import game from './game';

export default combineReducers({
  notifications,
  modal,
  user,
  otherUser,
  game
});

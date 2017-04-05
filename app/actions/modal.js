import notify from './notify';
import 'whatwg-fetch';

export function newBetModal() {
  return (dispatch) => {
    dispatch({
      type: 'NEW_BET'
    });
  };
}

export function inventoryModal() {
  return (dispatch) => {
    dispatch({
      type: 'INVENTORY'
    });
  };
}

export function newCardsBetModal() {
  return (dispatch) => {
    dispatch({
      type: 'NEW_CARDS_BET'
    });
  };
}

export function betModal() {
  return (dispatch) => {
    dispatch({
      type: 'BET'
    });
  };
}

export function paymentModal() {
  return (dispatch) => {
    dispatch({
      type: 'PAYMENT'
    });
  };
}

export function hideModal() {
  return (dispatch) => {
    dispatch({
      type: 'HIDE_MODAL'
    });
  };
}

export function newBet(gameName, items) {
  return (dispatch) => {
    if (items.length === 0) return dispatch(notify('ZERO_ITEMS'));
    fetch('/new_bet', {
      method: 'post',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'same-origin',
      body: JSON.stringify({
        game: gameName,
        items: items.join(',')
      })
    })
    .then(res => {
      return res.json().then(data => {
        if (data.msg) dispatch(notify(data.msg));
        if (data.errs) dispatch(notify('BET_ERROR'));
      });
    })
    .catch(err => {
      dispatch(notify('BET_ERROR'));
      console.log(err);
    });
  };
}

export function newCardsBet(gameName, cards) {
  return (dispatch) => {
    if (cards.length === 0) return dispatch(notify('ZERO_CARDS'));
    fetch('/new_cards_bet', {
      method: 'post',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'same-origin',
      body: JSON.stringify({
        game: gameName,
        cards: cards.join(',')
      })
    })
    .then(res => {
      return res.json().then(data => {
        console.log(data);
        if (data.msg) dispatch(notify(data.msg));
        if (data.errs) dispatch(notify('BET_ERROR'));
      });
    })
    .catch(err => {
      dispatch(notify('BET_ERROR'));
      console.log(err);
    });
  };
}

//noinspection JSUnusedLocalSymbols
export function withdrawItems(gameName, items) {
  return (dispatch) => {
    if (items.length === 0) return dispatch(notify('ZERO_ITEMS'));
    fetch('/withdraw_items', {
      method: 'post',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'same-origin',
      body: JSON.stringify({
        items: items.join(',')
      })
    })
    .then(res => {
      return res.json().then(data => {
        if (data.msg) dispatch(notify(data.msg));
        if (data.errs) dispatch(notify('INVALID_ITEMS'));
      });
    })
    .catch(err => {
      dispatch(notify('WITHDRAW_ERROR'));
      console.log(err);
    });
  };
}
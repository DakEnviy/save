import notify from './notify';
import 'whatwg-fetch';

export function changeTradelink(link) {
  return (dispatch) => {
    return fetch('/tradelink', {
      method: 'post',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'same-origin',
      body: JSON.stringify({ link: link })
    })
    .then(res => {
      res.json().then(data => {
        if (data.msg) dispatch(notify(data.msg));
        if (data.errs) dispatch(notify('INVALID_LINK'));
      });
    })
    .catch(err => {
      dispatch(notify('CHANGE_LINK_ERROR'));
      console.log(err);
    });
  };
}

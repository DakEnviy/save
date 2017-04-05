export default function modal(state = {
  currentModal: null
}, action) {
  switch (action.type) {
  case 'NEW_BET':
    return {
      currentModal: 'NEW_BET'
    };
  case 'INVENTORY':
    return {
      currentModal: 'INVENTORY'
    };
  case 'NEW_CARDS_BET':
    return {
      currentModal: 'NEW_CARDS_BET'
    };
  case 'BET':
    return {
      currentModal: 'BET'
    };
  case 'PAYMENT':
    return {
      currentModal: 'PAYMENT'
    };
  case 'HIDE_MODAL':
    return {
      currentModal: null
    };
  default:
    return state;
  }
}

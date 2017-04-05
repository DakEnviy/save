export default function otherUser(state = {
  otherUser: {}
}, action) {
  switch (action.type) {
  case 'CLEAR_PROFILE':
    return {
      otherUser: {}
    };
    break;
  default:
    return state;
  }
}

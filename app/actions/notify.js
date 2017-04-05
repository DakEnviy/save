import Notifications from 'react-notification-system-redux';

export default function(name) {
  return (dispatch) => {
    let type, message;
    switch (name) {
    case 'ZERO_ITEMS':
      type = 'error';
      message = 'Вы не выбрали предметы';
      break;
    case 'ZERO_CARDS':
      type = 'error';
      message = 'Вы не выбрали карточки';
      break;
    case 'INVALID_GAME':
      type = 'error';
      message = 'Ошибка получения игры';
      break;
    case 'INVALID_USER':
      type = 'error';
      message = 'Вы не авторизированы';
      break;
    case 'EXCEED_ITEMS_IN_BET':
      type = 'error';
      message = 'Вы превысили лимит ставки';
      break;
    case 'EXCEED_ITEMS_IN_GAME':
      type = 'error';
      message = 'Вы превысили лимит предметов в игре';
      break;
    case 'ITEM_APPID_ERROR':
      type = 'error';
      message = 'Вы поставили предметы другой игры';
      break;
    case 'MIN_BET_PRICE':
      type = 'error';
      message = 'Цена вашей ставки должна превышать 30р';
      break;
    case 'BET_SUCCESS':
      type = 'success';
      message = 'Успешная ставка';
      break;
    case 'BET_ERROR':
      type = 'error';
      message = 'Ошибка ставки';
      break;
    case 'NOT_FOUND_LINK':
      type = 'error';
      message = 'Вы не указали ссылку на обмен';
      break;
    case 'TRADE_ERROR':
      type = 'error';
      message = 'Ошибка отправки предложения обмена';
      break;
    case 'INVALID_ITEMS':
      type = 'error';
      message = 'Некорректные предметы';
      break;
    case 'INVALID_CARDS':
      type = 'error';
      message = 'Некорректные карточки';
      break;
    case 'WITHDRAW_SUCCESS':
      type = 'success';
      message = 'Обмен отправлен';
      break;
    case 'WITHDRAW_ERROR':
      type = 'error';
      message = 'Ошибка вывода предметов';
      break;
    case 'INVALID_LINK':
      type = 'error';
      message = 'Некорректная ссылка на обмен';
      break;
    case 'CHANGE_LINK_SUCCESS':
      type = 'success';
      message = 'Ссылка на обмен была изменена';
      break;
    case 'CHANGE_LINK_ERROR':
      type = 'error';
      message = 'Ошибка смены ссылки на обмен';
      break;
    case 'LOAD_ITEMS_ERROR':
      type = 'error';
      message = 'Ошибка подгрузки предметов';
      break;
    case 'LOAD_CARDS_ERROR':
      type = 'error';
      message = 'Ошибка подгрузки карточек';
      break;
    case 'HASNT_ITEMS_SHOP':
      type = 'error';
      message = 'Предмет не был найден';
      break;
    case 'HASNT_MONEY':
      type = 'error';
      message = 'У вас недостаточно денег';
      break;
    case 'BUY_SUCCESS':
      type = 'success';
      message = 'Успешная покупка';
      break;
    case 'BUY_ERROR':
      type = 'error';
      message = 'Ошибка покупки';
      break;
    case 'SUBSCRIBE_SUCCESS':
      type = 'success';
      message = 'Вы подписались на группу';
      break;
    case 'SUBSCRIBE_ERROR':
      type = 'error';
      message = 'Ошибка обработки подписки на группу';
      break;
    case 'NOT_FOUND_USER':
      type = 'error';
      message = 'Не найден пользователь';
      break;
    case 'LOAD_HISTORY_ERROR':
      type = 'error';
      message = 'Ошибка подгрузки истории';
      break;
    case 'LOAD_ACHIEVEMENTS_ERROR':
      type = 'error';
      message = 'Ошибка подгрузки достижений';
      break;
    case 'HASNT_WINNER':
      type = 'error';
      message = 'Ошибка при попытке получения победителя';
      break;
    case 'LOAD_TOP_ERROR':
      type = 'error';
      message = 'Ошибка подгрузки топа';
      break;
    case 'MESSAGE_LENGTH':
      type = 'error';
      message = 'Нельзя отправлять пустое сообщение';
      break;
    case 'ADD_CART':
      type = 'success';
      message = 'Предмет добавлен в корзину';
      break;
    case 'INVALID_AMOUNT':
      type = 'error';
      message = 'Неверная сумма';
      break;
    case 'LOAD_PROFILE_STATS_ERROR':
      type = 'error';
      message = 'Ошибка подгрузки статистики профиля';
      break;
    case 'LOAD_TOP_STATS_ERROR':
      type = 'error';
      message = 'Ошибка подгрузки статистики';
      break;
    case 'GAME_STATUS_ERROR':
      type = 'error';
      message = 'Вы не можете ставить ставку во время перезагрузки игры';
      break;
    case 'LOAD_PACKS_ERROR':
      type = 'error';
      message = 'Ошибка подгрузки паков достижений';
      break;
    case 'CANT_GET_ITEM_PRICE':
      type = 'error';
      message = 'Ошибка подгрузки цены предметов';
      break;
    default:
      type = 'info';
      message = 'Неизвестное оповещение';
      console.log(name);
    }
    return dispatch(Notifications[type]({
      message: message,
      position: 'tr',
      autoDismiss: 5
    }));
  };
}
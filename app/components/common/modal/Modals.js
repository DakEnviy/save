import React from 'react';
import { connect } from 'react-redux';

import * as actions from '../../../actions/modal';
import BetModal from './modals/BetModal';
import ItemsModal from './modals/ItemsModal';
import CardsModal from './modals/CardsModal';
import PaymentModal from './modals/PaymentModal';

const Modals = props => {
  switch (props.currentModal) {
  case 'NEW_BET':
    return <ItemsModal
      title="Поставьте предметы"
      width={500}
      okText="Сделать ставку"
      handleOk={props.newBet}
      {...props}
    />;
  case 'INVENTORY':
    return <ItemsModal
      title="Инвентарь"
      width={710}
      okText="Вывести предметы"
      handleOk={props.withdrawItems}
      {...props}
    />;
  case 'NEW_CARDS_BET':
    return <CardsModal
      title="Поставьте карточки"
      width={0}
      okText="Сделать ставку"
      handleOk={props.newCardsBet}
      {...props}
    />;
  case 'BET':
    return <BetModal
      title="Сделайте ставку"
      width={520}
      {...props}
    />;
  case 'PAYMENT':
    return <PaymentModal
      title="Пополнение баланса"
      width={0}
      okText="Пополнить"
      {...props}
    />;
  default:
    return null;
  }
};

const mapStateToProps = (state) => {
  return {
    currentModal: state.modal.currentModal
  };
};

const mapDispatchToProps = (dispatch) => {
  return {
    newBetModal: () => dispatch(actions.newBetModal()),
    newCardsBetModal: () => dispatch(actions.newCardsBetModal()),
    hideModal: () => dispatch(actions.hideModal()),
    newBet: (gameName, items) => dispatch(actions.newBet(gameName, items)),
    newCardsBet: (gameName, cards) => dispatch(actions.newCardsBet(gameName, cards)),
    withdrawItems: (gameName, items) => dispatch(actions.withdrawItems(gameName, items))
  };
};

export default connect(mapStateToProps, mapDispatchToProps)(Modals);
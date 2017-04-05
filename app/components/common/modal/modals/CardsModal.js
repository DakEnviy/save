import React from 'react';
import ReactSpinner from 'react-spinjs';
import { connect } from 'react-redux';
import notify from './../../../../actions/notify';
import Modal from '../Modal';
import 'whatwg-fetch';

class CardsModal extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      loaded: false,
      cards: {}
    };
  }

  componentDidMount() {
    const self = this;
    fetch('/get_cards', {
      method: 'post',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'same-origin'
    })
    .then(res => {
      return res.json().then(data => {
        if (data.msg) self.props.dispatch(notify(data.msg));
        if (data.errs) self.props.dispatch(notify('LOAD_CARDS_ERROR'));
        if (data.success) {
          self.setState({
            loaded: true,
            cards: data.cards
          });
        } else self.setState({ loaded: true });
      });
    })
    .catch(err => {
      self.props.dispatch(notify('LOAD_CARDS_ERROR'));
      console.log(err);
    });
  }

  handleChange(e) {
    let { cards } = this.state;
    let value = Number(e.target.value);
    if (isNaN(value) || value < 0) value = 0;
    cards[Number(e.target.name.replace('card-', ''))].count = value;
    this.setState({ cards });
  }

  handleInc(cardId, inc) {
    return e => {
      e.preventDefault();
      let { cards } = this.state;
      let value = (cards[cardId].count || 0) + inc;
      if (value < 0) value = 0;
      cards[cardId].count = value;
      this.setState({ cards });
    };
  }

  handleOk() {
    const { cards } = this.state;
    let cardsIds = [];
    for (let cardId in cards) {
      for (let i = 0; i < cards[cardId].count; i++) cardsIds.push(cardId);
    }
    this.props.handleOk(this.props.gameName, cardsIds);
  }

  render() {
    const { cards } = this.state;
    let listCards = [];
    for (let cardId in cards) {
      const card = cards[cardId];
      listCards.push(
        <div key={cardId} className="cart-list">
          <div className="itemBlock cart-body">
            <img className="cart-img" src={card.icon_url} />
            <h3>{card.price} р</h3>
            <div className="itemInfo">
              <div className="gradient"></div>
              <span>{card.name}</span>
              <div className="gradient"></div>
            </div>
          </div>
          <div className="input">
            <a href="#" onClick={this.handleInc(cardId, -1).bind(this)}>-</a>
            <input type="text" name={'card-' + cardId} value={this.state.cards[cardId].count || 0} onChange={this.handleChange.bind(this)} />
            <a href="#" onClick={this.handleInc(cardId, 1).bind(this)}>+</a>
          </div>
        </div>
      );
    }
    return (
      <Modal
        title={this.props.title}
        width={this.props.width}
        head={
          <div className="balance">
            <span>Ваш баланс: <span className="money">0</span></span>
            <div className="gradient"></div>
          </div>
        }
        hideModal={this.props.hideModal}
        okText={this.props.okText}
        onOk={this.handleOk.bind(this)}
      >
        <div className="modal-cart">
          {listCards}
        </div>
      </Modal>
    );
  }
}

export default connect()(CardsModal);
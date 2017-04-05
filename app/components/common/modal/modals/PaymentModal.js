import React from 'react';
import { connect } from 'react-redux';
import notify from '../../../../actions/notify';
import Modal from '../Modal';

class PaymentModal extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      amount: 0
    };
  }

  handleChange(e) {
    this.setState({ [e.target.name]: e.target.value });
  }

  handlePaymount() {
    let { amount } = this.state;
    amount = Number(amount);
    if (isNaN(amount) || amount <= 0) {
      this.props.dispatch(notify('INVALID_AMOUNT'));
      return this.setState({ amount: 0 });
    }
    window.open('/payment.kassa?amount=' + amount);
  }

  render() {
    return (
      <Modal
        title={this.props.title}
        width={this.props.width}
        head={<div className="gradient"></div>}
        hideModal={this.props.hideModal}
        okText={this.props.okText}
        onOk={this.handlePaymount.bind(this)}
      >
        <div className="modal-money">
          <div className="rules">
          	<a href="#">Правила пополнения <i className="fa fa-shopping-cart"/></a>
          	<a href="#">Правила пополнения <i className="fa fa-american-sign-language-interpreting"/></a>
          </div>
          <input type="text" name="amount" value={this.state.amount} onChange={this.handleChange.bind(this)}/>руб.
        </div>
      </Modal>
    );
  }
}

export default connect()(PaymentModal);
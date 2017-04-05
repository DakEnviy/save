import React from 'react';
import { Scrollbars } from 'react-custom-scrollbars';

class Cart extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      show: false
    };
  }

  handleToggle() {
    this.setState({ show: !this.state.show });
  }

  render() {
    const { listItems, price, handleBuy } = this.props;
    const { show } = this.state;
    return (
      <div className={'b-basket' + (show ? ' active' : '')}>
        <div className="b-header">
          <div>Ваша корзина</div>
        </div>
        <div className="b-basket-item">
          <Scrollbars>
            <div className="b-items">
              {listItems}
            </div>
          </Scrollbars>
        </div>
        <div className="buy-item">
          <span>Итого: {price.toFixed(2)} р</span>
          <a href="#" onClick={handleBuy}><div>Купить</div></a>
        </div>
      </div>
    );
  }
}

export default Cart;
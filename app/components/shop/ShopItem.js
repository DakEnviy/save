import React from 'react';

class ShopItem extends React.Component {
  render() {
    const { id, name, icon, color, price, discount, count, handleClick } = this.props;
    return (
      <div className="b-item" onClick={() => handleClick(id)}>
        <div className="b-item-name">{name}</div>
        <img src={icon} />
        <hr style={{ background: color }} />
        <div className="b-item-price">{price} р</div>
        {discount > 0 &&
          <div className="b-item-discount" style={{ background: color }}>-{discount}%</div>
        }
        {count > 1 &&
          <div className="b-item-count">x{count}</div>
        }
      </div>
    );
  }
}

export default ShopItem;
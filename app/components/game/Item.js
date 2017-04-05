import React from 'react';

class Item extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      active: false
    };
  }

  handleSelect() {
    this.setState({
      active: !this.state.active
    }, function() {
      this.props.handleSelect(this.props.itemId, this.state.active);
    }.bind(this));
  }

  render() {
    const { name, icon, color, price, isSelect } = this.props;
    const style = isSelect ? { cursor: 'pointer' } : null;
    return (
      <div
        className={'itemBlock' + (this.state.active ? ' active' : '')}
        style={style}
        onClick={isSelect ? this.handleSelect.bind(this) : () => {}}
      >
        <img src={icon} />
        <hr style={{ backgroundColor: color }} />
        <h3>{price} р</h3>
        <div className="itemInfo">
          <div className="gradient"></div>
          <span>{name}</span>
          <div className="gradient"></div>
        </div>
        {isSelect && <i className="fa fa-check" />}
      </div>
    );
  }
}

export default Item;
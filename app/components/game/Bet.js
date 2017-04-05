import React from 'react';
import Item from './Item';

class Bet extends React.Component {
  render() {
    const { userId, username, avatar, firstTicket, lastTicket, price, items, gameName } = this.props;
    let listItems = [];
    for (let i = 0, l = items.length; i < l; i++) {
      const item = items[i];
      listItems.push(<Item
        key={i}
        name={item.name}
        icon={item.icon}
        color={item.color}
        price={item.price}
      />);
    }
    return (
      <div className="blockItem">
        <div className="infoItem">
          <div className="contentP">
            <img src={avatar} className="avatar"/>
            <div className="blockInfoPlayer">
              <div className="infoPlayer">
                <a href={'/' + gameName + '/profile/' + userId}>{username}</a>
              </div>
              <div className="infoRandom">
                <h2>Билеты от #{firstTicket} до #{lastTicket}</h2>
              </div>
              <div className="infoBet">
                <h1>Ставка: {price} р</h1>
              </div>
            </div>
          </div>
        </div>
        <div className="itemGame">
          <div className="contentP">
            <hr/>
            {listItems}
          </div>
        </div>
      </div>
    );
  }
}

export default Bet;

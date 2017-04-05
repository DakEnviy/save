import React from 'react';

class InfoGame extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      timer: this.props.timer
    };
    this.onTimer = function(data) {
      if (data.game === this.props.gameName) {
        this.setState({ timer: data.timer });
      }
    }.bind(this);
  }

  componentDidMount() {
    window.socket.on('timer', this.onTimer);
  }

  componentWillUnmount() {
    window.socket.removeListener('timer', this.onTimer);
  }

  handleNewBetModal(e) {
    e.preventDefault();
    if (this.props.userId) this.props.handleNewBetModal();
    else window.open('/auth/steam');
  }

  render() {
    const formatTime = (time) => {
      let m = Math.floor(time / 60), s = time - m * 60;
      if (m < 10) m = '0' + m.toString();
      if (s < 10) s = '0' + s.toString();
      return m+':'+s;
    };
    const { gameId, itemsCount, maxItems, isHistory } = this.props;
    return (
      <div className="contentP">
        <div className="load">
          <div className="load-active" style={{ width: (itemsCount/maxItems*100) + '%' }}></div>
        </div>
        <div className="infoGame">
          <div className="block">
            <h1 className="L-r">Игра #{gameId}</h1>
            {!isHistory &&
              <a href="#" onClick={this.handleNewBetModal.bind(this)}>
                <div className="btn-bet L-r">Сделать ставку</div>
              </a>
            }
          </div>
          <div className="block">
            <h1>Предметы</h1>
            <span>{itemsCount}/{maxItems}</span>
          </div>
          {!isHistory &&
            <div className="block">
              <h1>Таймер</h1>
              <span>{formatTime(this.state.timer)}</span>
            </div>
          }
          <div className="btn-m">
            <a href="#" onClick={this.handleNewBetModal.bind(this)}>
              <div className="btn-bet L-r">Сделать ставку</div>
            </a>
          </div>
        </div>
      </div>
    );
  }
}

export default InfoGame;
import React from 'react';

class InfoWinner extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      reload: 30,
      winner_ticket: this.props.winnerTicket,
      winner_name: this.props.winnerName,
      left: 0
    };
    this.onReload = data => {
      if (data.game === this.props.gameName) {
        this.setState({ reload: data.reload });
      }
    };
    this.onEndGameWinner = data => {
      if (data.game !== this.props.gameName) return;
      this.setState({
        winner_ticket: data.winner_ticket,
        winner_name: data.winner_name
      });
    };
  }

  componentDidMount() {
    window.socket.on('reload', this.onReload);
    window.socket.on('end_game_winner', this.onEndGameWinner);
    setTimeout(() => {
      const windowOffset = this.refs.avatars.offsetWidth/2;
      this.setState({ left: this.props.left + windowOffset });
    }, 10);
  }

  componentWillUnmount() {
    window.socket.removeListener('reload', this.onReload);
    window.socket.removeListener('end_game_winner', this.onEndGameWinner);
  }

  handleNewBetModal(e) {
    e.preventDefault();
    this.props.handleNewBetModal();
  }

  render() {
    const { fund, avatars } = this.props;
    // for (let j = 0; j < 150; j++) avatars.push('https://steamcdn-a.akamaihd.net/steamcommunity/public/images/avatars/5f/5f591808e0d48d5260d5c871c3c87bca725bd5e2_full.jpg');
    // avatars[119] = 'http://cdn.edgecast.steamstatic.com/steamcommunity/public/images/avatars/47/4714ce3c7f923adf113186bb21c436defbb0baad_full.jpg';
    let listAvatars = [];
    for (let i = 0, l = avatars.length; i < l; i++) {
      listAvatars.push(
        <div key={i} className="blockUser">
          <img src={avatars[i]} />
        </div>
      );
    }
    return (
      <div>
        {listAvatars.length > 0 &&
          <div>
            <div className="contentP">
              <div className="line"></div>
              <div className="list-avatars-wrap" ref="avatars">
                <div className="list-avatars" style={{ left: this.state.left }}>
                  {listAvatars}
                </div>
              </div>
              <div className="line"></div>
            </div>
            <div className="gradient"></div>
          </div>
        }
        <div className="infoStartGame">
          <div className="contentP">
            <div className="info">
              <div className="leftInfo">
                <h2>Выигрышный билет: #<span>{this.state.winner_ticket}</span></h2>
                <h2>Победитель: <span>{this.state.winner_name}</span></h2>
                <h1>В игре: <span>{fund}</span> р</h1>
              </div>
              <div className="rightInfo">
                <h1>Новая игра через <span><div>{this.state.reload}</div></span></h1>
                {/*<a href="#" onClick={this.handleNewBetModal.bind(this)}>*/}
                {/*<div className="btn-game"><h1>Сделать ставку</h1></div>*/}
                {/*</a>*/}
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }
}

export default InfoWinner;
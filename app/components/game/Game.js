import React from 'react';
import { connect } from 'react-redux';
import InfoGame from './InfoGame';
import InfoWinner from './InfoWinner';
import Bet from './Bet';
import Player from './Player';
import * as modals from '../../actions/modal';
import notify from '../../actions/notify';
import 'whatwg-fetch';

class Game extends React.Component {
  constructor(props) {
    super(props);
    this.loadGame = game => {
      return {
        game_id: game.id || 1,
        status: game.status || 0,
        isHistory: game.isHistory || false,

        fund: game.fund || 0,
        items_count: game.items_count || 0,
        players: game.players || 0,
        bets: game.bets || [],

        avatars: [],
        left: 0,
        winner_ticket: game.winner_ticket || '???',
        winner_name: game.winner_name || '???',

        max_items: game.max_items || 100,
        timer: game.timer || 30,

        hiddenArrows: false,
        playersLeft: 0
      };
    };
    this.state = this.loadGame(this.props.params.gameid ? {
      isHistory: true
    } : this.props.game[this.props.gameName]);
    this.onNewBet = data => {
      if (data.game !== this.props.gameName) return;
      let _bets = this.state.bets.slice();
      _bets.unshift(data.bet);
      this.setState({
        fund: this.state.fund + data.bet.price,
        items_count: this.state.items_count + data.items_count,
        players: data.players,
        bets: _bets
      }, () => this.updateArrows());
    };
    this.onNewGame = data => {
      if (data.game !== this.props.gameName) return;
      this.setState({
        game_id: data.game_id,
        status: 0,
        fund: 0,
        items_count: 0,
        players: [],
        bets: [],
        avatars: [],
        left: 0,
        winner_ticket: '???',
        winner_name: '???'
      }, () => this.updateArrows());
    };
    this.onEndGame = data => {
      if (data.game !== this.props.gameName) return;
      this.setState({
        status: 2,
        avatars: data.avatars,
        left: data.left
      });
      this.props.dispatch(modals.hideModal());
    };
  }

  componentDidMount() {
    if (this.props.params.gameid) {
      return fetch('/get_game', {
        method: 'post',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify({
          game: this.props.gameName,
          id: this.props.params.gameid
        })
      })
      .then(res => {
        return res.json().then(data => {
          if (data.msg) this.props.dispatch(notify(data.msg));
          if (data.errs) this.props.dispatch(notify('LOAD_HISTORY_ERROR'));
          if (data.success) {
            data.game.isHistory = true;
            this.setState(this.loadGame(data.game), () => this.updateArrows());
            window.scrollTo(0, 0);
          }
        });
      })
      .catch(err => {
        this.props.dispatch(notify('LOAD_HISTORY_ERROR'));
        console.log(err);
      });
    } else {
      this.updateArrows();
    }
    window.socket.on('new_bet', this.onNewBet);
    window.socket.on('new_game', this.onNewGame);
    window.socket.on('end_game', this.onEndGame);
  }

  componentWillUnmount() {
    window.socket.removeListener('new_bet', this.onNewBet);
    window.socket.removeListener('new_game', this.onNewGame);
    window.socket.removeListener('end_game', this.onEndGame);
  }

  handleNewBetModal() {
    this.props.dispatch(modals.betModal());
  }

  updateArrows() {
    const playersBlock = document.getElementsByClassName('blockHazard');
    if (playersBlock.length === 0) return;
    if (this.state.players.length*119-15 <= playersBlock[0].clientWidth-100)
      this.setState({ hiddenArrows: true });
  }

  handleArrow(inc) {
    return () => {
      const offset = -(this.state.players.length*119-15-document.getElementsByClassName('blockHazard')[0].clientWidth+100),
        next = this.state.playersLeft + inc;
      if (next >= 0 && inc > 0) this.setState({ playersLeft: 0 });
      else if (next < offset && inc < 0) this.setState({ playersLeft: offset });
      else this.setState({ playersLeft: next });
    };
  }

  render() {
    const { bets, players, isHistory, hiddenArrows, playersLeft } = this.state;
    let listBets = [];
    for (let i = 0, l = bets.length; i < l; i++) {
      const bet = bets[i];
      listBets.push(<Bet
        key={i}
        userId={bet.userId}
        username={bet.name}
        avatar={bet.avatar}
        firstTicket={bet.first_ticket}
        lastTicket={bet.last_ticket}
        price={bet.price}
        items={bet.items}
        gameName={this.props.gameName}
      />);
    }
    let listPlayers = [];
    for (let i = 0, l = players.length; i < l; i++) {
      const player = players[i];
      listPlayers.push(<Player
        key={i}
        avatar={player[1].avatar}
        chance={player[1].chance}
      />);
    }
    return (
      <div className="blockGame">
        <div className="header">
          <div className="contentP">
            <h1 className="L-r h-media">На кону: <span>{Number(this.state.fund.toFixed(2))}</span> р</h1>
            {(!this.props.user.id && !isHistory) &&
              <a href="/auth/steam" className="logSteam">
                <div>
                  <span className="L-b">Войти чтобы начать игру</span>
                  <img src="/img/steam.png" />
                </div>
              </a>
            }
          </div>
        </div>
        <div className="gradient"/>
        {this.state.status !== 2 ?
          <InfoGame
            gameId={Math.round(this.state.game_id)}
            itemsCount={Math.round(this.state.items_count)}
            maxItems={this.state.max_items}
            gameName={this.props.gameName}
            timer={this.state.timer}
            handleNewBetModal={this.handleNewBetModal.bind(this)}
            userId={this.props.user.id}
            isHistory={isHistory}
          />
        :
          <InfoWinner
            fund={Math.round(this.state.fund)}
            avatars={this.state.avatars}
            left={this.state.left}
            winnerTicket={this.state.winner_ticket}
            winnerName={this.state.winner_name}
            gameName={this.props.gameName}
            handleNewBetModal={this.handleNewBetModal.bind(this)}
          />
        }
        <div className="gradient"/>
        {listPlayers.length > 0 &&
          <div className="blockHazard">
            {hiddenArrows ? null :
              <div className="players-arrow players-arrow-left" onClick={this.handleArrow(119).bind(this)}>
                <i className="fa fa-arrow-circle-left"/>
              </div>
            }
            <div style={{ width: '100%', overflow: 'hidden' }}>
              <div style={hiddenArrows ? null : {
                position: 'relative',
                width: players.length*119-15,
                textAlign: 'left',
                left: playersLeft,
                transition: 'left .5s ease-in-out'
              }}>
                {listPlayers}
              </div>
            </div>
            {hiddenArrows ? null :
              <div className="players-arrow players-arrow-right" onClick={this.handleArrow(-119).bind(this)}>
                <i className="fa fa-arrow-circle-right"/>
              </div>
            }
          </div>
        }
        <div className="gradient"/>
        <div>{listBets}</div>
      </div>
    );
  }
}

const mapStateToProps = (state) => {
  return {
    user: state.user,
    game: state.game
  };
};

export default connect(mapStateToProps)(Game);

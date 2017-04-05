import React from 'react';
import { connect } from 'react-redux';
import DocumentTitle from 'react-document-title';
import ReactSpinner from 'react-spinjs';
import notify from './../actions/notify';
import 'whatwg-fetch';

class Top extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      type: 1,
      players: [],
      loaded: false,
      maxFundAll: 0,
      maxFundToday: 0,
      gamesCountToday: 0,
      playersCountToday: 0
    };
  }

  componentDidMount() {
    this.loadTop(1);
  }

  loadTop(type) {
    const self = this;
    this.setState({ loaded: false });
    fetch('/get_top', {
      method: 'post',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'same-origin',
      body: JSON.stringify({
        game: this.props.gameName,
        type: type
      })
    })
    .then(res => {
      return res.json().then(data => {
        if (data.msg) self.props.dispatch(notify(data.msg));
        if (data.errs) self.props.dispatch(notify('LOAD_TOP_ERROR'));
        if (data.success) {
          self.setState({
            type: type,
            players: data.players,
            loaded: true
          });
        } else self.setState({ loaded: true });
      });
    })
    .catch(err => {
      self.props.dispatch(notify('LOAD_TOP_ERROR'));
      console.log(err);
    });
    fetch('/top_stats', {
      method: 'post',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'same-origin',
      body: JSON.stringify({
        game: this.props.gameName
      })
    })
    .then(res => {
      return res.json().then(data => {
        if (data.msg) self.props.dispatch(notify(data.msg));
        if (data.errs) self.props.dispatch(notify('LOAD_TOP_STATS_ERROR'));
        if (data.success) {
          self.setState({
            maxFundAll: data.maxFundAll,
            maxFundToday: data.maxFundToday,
            gamesCountToday: data.gamesCountToday,
            playersCountToday: data.playersCountToday
          });
        }
      });
    })
    .catch(err => {
      self.props.dispatch(notify('LOAD_TOP_STATS_ERROR'));
      console.log(err);
    });
  }

  handleType(e) {
    e.preventDefault();
    const type = Number(e.currentTarget.dataset.type);
    if (isNaN(type)) return;
    if (type === this.state.type) return;
    this.loadTop(type);
  }

  render() {
    const { gameName } = this.props;
    const { players, loaded, maxFundAll, maxFundToday, gamesCountToday, playersCountToday } = this.state;
    let listPlayers = [];
    for (let i = 0, l = players.length; i < l; i++) {
      const player = players[i];
      listPlayers.push(
        <div key={i} className="b-info">
          <div className="b-place"><img src={player.avatar} className="avatar"/><span>{i+1}</span></div>
          <a href={'/' + gameName + '/profile/' + player.id} className="b-profile">{player.name}</a>
          <div className="b-win">{player.wons}</div>
          <div className="b-achievements">{player.achievements}</div>
          <div className="b-money">{player.fund} р</div>
        </div>
      );
    }
    return (
      <DocumentTitle title="Топ игроков">
        <div className="blockGame">
          <div className="header">
            <div className="contentP">
              <h1 className="L-r h-top">Топ игроков</h1>
              <div className="btn-time-block">
                <a href="#" data-type={1}
                   className={this.state.type === 1 ? 'active' : null}
                   onClick={this.handleType.bind(this)}
                ><div>30 дней</div></a>
                <a href="#" data-type={2}
                   className={this.state.type === 2 ? 'active' : null}
                   onClick={this.handleType.bind(this)}
                ><div>7 дней</div></a>
                <a href="#" data-type={3}
                   className={this.state.type === 3 ? 'active' : null}
                   onClick={this.handleType.bind(this)}
                ><div>24 часа</div></a>
              </div>
            </div>
          </div>
          <div className="gradient"/>
          <div className="info-top">
            <div className="block">
              <span>Макс. выигрыш за все время</span>
              <span>{maxFundAll} р</span>
            </div>
            <div className="block">
              <span>Макс. выигрыш за сегодня</span>
              <span>{maxFundToday} р</span>
            </div>
            <div className="block">
              <span>Сегодня игр</span>
              <span>{gamesCountToday}</span>
            </div>
            <div className="block">
              <span>Игроков сегодня</span>
              <span>{playersCountToday}</span>
            </div>
            <div className="scroll">
              <div className="info-top-player">
                  <div className="h-info">
                    <div className="contentP">
                      <div>Место</div>
                      <div>Никнейм</div>
                      <div>Кол-во побед</div>
                      <div>Достижения</div>
                      <div>Выиграл</div>
                    </div>
                  </div>
                <div className="contentP">
                  {loaded ? players.length > 0 ?
                      listPlayers :
                      <span style={{
                        display: 'block',
                        padding: '60px 0px 30px',
                        textAlign: 'center',
                        color: '#bbbbbb'
                      }}>Нет игроков</span> : <ReactSpinner color="#a0a0a0"/>
                  }
                </div>
              </div>
            </div>
          </div>
        </div>
      </DocumentTitle>
    );
  }
}

export default connect()(Top);
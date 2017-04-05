import React from 'react';
import { connect } from 'react-redux';
import DocumentTitle from 'react-document-title';
import ReactSpinner from 'react-spinjs';
import { Link } from 'react-router';
import notify from './../actions/notify';
import Player from './game/Player';
import Item from './game/Item';
import 'whatwg-fetch';

class History extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      games: {},
      count: 0,
      page: 1,
      loaded: false
    };
  }

  componentDidMount() {
    this.loadGames();
  }

  loadGames() {
    const self = this;
    setTimeout(() => window.scrollTo(0, 0), 100);
    fetch('/get_history', {
      method: 'post',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'same-origin',
      body: JSON.stringify({
        game: this.props.gameName,
        page: this.state.page
      })
    })
    .then(res => {
      return res.json().then(data => {
        if (data.msg) self.props.dispatch(notify(data.msg));
        if (data.errs) self.props.dispatch(notify('LOAD_HISTORY_ERROR'));
        if (data.success) {
          self.setState({
            loaded: true,
            games: data.games,
            count: data.count
          });
        } else self.setState({ loaded: true });
      });
    })
    .catch(err => {
      self.props.dispatch(notify('LOAD_HISTORY_ERROR'));
      console.log(err);
    });
  }

  handlePage(page) {
    return e => {
      if (typeof page === 'undefined') {
        const value = e.target.value;
        if (value === '') return this.setState({ page: '' });
        page = Number(value);
      } else e.preventDefault();
      const maxPage = Math.ceil(this.state.count/10) || 1;
      if (isNaN(page) || page < 1) page = 1;
      else if (page > maxPage) page = maxPage;
      this.setState({ page }, this.loadGames);
    };
  }

  render() {
    const { gameName } = this.props;
    const { games, page, count, loaded } = this.state;
    let listGames = [], gamesCount = 0;
    for (let gameId in games) {
      if (!games.hasOwnProperty(gameId)) continue;
      const game = games[gameId], intGameId = Number(gameId.replace('k', ''));
      let listPlayers = [], listItems = [];
      for (let player of game.players) {
        listPlayers.push(<Player
          key={player[0]}
          avatar={player[1].avatar}
          chance={player[1].chance}
        />);
      }
      for (let i = 0, l = game.items.length; i < l; i++) {
        const item = game.items[i];
        listItems.push(<Item
          key={i}
          name={item.name}
          icon={item.icon}
          color={item.color}
          price={item.price}
        />);
      }
      listGames.push(
        <div key={gameId} className="History">
          <div className="h-info">
            <div className="contentP">
              <div className="blockAvatar">
                <img className="avatar" src={game.winner_avatar} />
              </div>
              <div className="t-info">
                <a href={'/' + gameName + '/profile/' + game.winner_id}>{game.winner_name}</a>
                <div>Выигрыш: <span>{game.fund} р</span></div>
                <div>
                  <Link to={'/' + gameName + '/history/' + intGameId}>Показать историю</Link>
                </div>
                <span className="game">Игра: <span>#{intGameId}</span></span>
                <form action="https://api.random.org/verify" method="post" target="_blank">
                  <input type="hidden" name="format" value="json" />
                  <input type="hidden" name="random" value={game.verify_random} />
                  <input type="hidden" name="signature" value={game.verify_signature} />
                  <input type="submit" value="Проверить на random.org" />
                </form>
              </div>
            </div>
            </div>
            {listPlayers.length > 0 &&
              <div className="blockHazard">
                {listPlayers}
              </div>
            }
            <div className="gradient"/>
            <div className="b-item-win">
            <div className="contentP">
              <div className="header-item">
                <span>Выигрыш с учетом комиссии:</span>
              </div>
              {listItems}
            </div>
            <div className="gradient"/>
          </div>
        </div>
      );
      gamesCount++;
    }

    return (
      <DocumentTitle title="История игр">
        <div className="blockGame">
          <div className="header">
            <div className="contentP">
              <h1 className="L-r">История</h1>
            </div>
          </div>
          {loaded ? gamesCount > 0 ?
              listGames :
              <span style={{
                display: 'block',
                padding: '60px 0px 50px',
                textAlign: 'center',
                color: '#bbbbbb',
                fontWeight: '400',
                fontSize: '24px'
              }}>Нет игр</span> : <ReactSpinner color="#a0a0a0"/>
          }
          <div className="itemNav">
            <div className="nav">
              <a href="#" onClick={this.handlePage(page-1).bind(this)}><i className="fa fa-angle-double-left"/></a>
              <input type="text" name="page" value={page} onChange={this.handlePage().bind(this)} />
              <span>из {Math.ceil(count/10) || 1}</span>
              <a href="#" onClick={this.handlePage(page+1).bind(this)}><i className="fa fa-angle-double-right"/></a>
            </div>
          </div>
        </div>
      </DocumentTitle>
    );
  }
}

export default connect()(History);
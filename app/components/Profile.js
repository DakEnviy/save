import React from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router';
import DocumentTitle from 'react-document-title';
import ReactSpinner from 'react-spinjs';
import notify from '../actions/notify';
import { changeTradelink } from '../actions/user';
import { inventoryModal } from '../actions/modal';
import 'whatwg-fetch';

class Profile extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      link: this.props.user.trade_link || '',
      achievements: [],
      loaded: false,
      wonGamesCount: 0,
      wonFund: 0
    };
  }

  componentDidMount() {
    this.updateProfile(this.props);
  }

  componentWillReceiveProps(nextProps, nextContext) {
    this.updateProfile(nextProps);
  }

  updateProfile(props) {
    const self = this;
    const { gameName, user, otherUser } = props;
    const profile = otherUser.id ? otherUser : user;
    fetch('/get_achievements', {
      method: 'post',
      headers: {'Content-Type': 'application/json'},
      credentials: 'same-origin',
      body: JSON.stringify({
        userid: profile.id
      })
    })
    .then(res => {
      return res.json().then(data => {
        if (data.msg) self.props.dispatch(notify(data.msg));
        if (data.errs) self.props.dispatch(notify('LOAD_ACHIEVEMENTS_ERROR'));
        if (data.success) {
          self.setState({
            achievements: data.achievements,
            loaded: true
          });
        } else self.setState({ loaded: true });
      });
    })
    .catch(err => {
      self.props.dispatch(notify('LOAD_ACHIEVEMENTS_ERROR'));
      console.log(err);
    });
    fetch('/profile_stats', {
      method: 'post',
      headers: {'Content-Type': 'application/json'},
      credentials: 'same-origin',
      body: JSON.stringify({
        game: gameName,
        userid: profile.id
      })
    })
    .then(res => {
      return res.json().then(data => {
        if (data.msg) self.props.dispatch(notify(data.msg));
        if (data.errs) self.props.dispatch(notify('LOAD_PROFILE_STATS_ERROR'));
        if (data.success) {
          self.setState({
            wonGamesCount: data.wonGamesCount,
            wonFund: data.wonFund
          });
        }
      });
    })
    .catch(err => {
      self.props.dispatch(notify('LOAD_PROFILE_STATS_ERROR'));
      console.log(err);
    });
  }

  handleChange(e) {
    this.setState({ [e.target.name]: e.target.value });
  }

  handleChangeTradelink(e) {
    e.preventDefault();
    this.props.dispatch(changeTradelink(this.state.link));
  }

  handleInventoryModal(e) {
    e.preventDefault();
    this.props.dispatch(inventoryModal());
  }

  render() {
    const { user, otherUser, gameName } = this.props;
    const { loaded, achievements, wonGamesCount, wonFund } = this.state;
    const profile = otherUser.id ? otherUser : user;
    return (
      <DocumentTitle title={profile.username}>
        <div className="blockGame">
          <div className="header">
            <div className="contentP">
              <h1 className="L-r">Мой профиль</h1>
            </div>
          </div>
          <div className="gradient"/>
          <div className="contentP">
            <div className="profile">
              {profile === user &&
                <button className="btn-inv L-r" onClick={this.handleInventoryModal.bind(this)}>Инвентарь</button>
              }
              <img src={profile.avatar} className="avatar" />
              <div className="name-user">
                <span>{profile.username}</span>
              </div>
              <div className="infoProfile">
                <div className="block">
                  <span>Сумма выигрыша</span>
                  <span>{wonFund} р<hr/></span>
                </div>
                <div className="block">
                  <span>Количество побед</span>
                  <span>{wonGamesCount}</span>
                </div>
                <div className="block">
                  <span>Всего достижений</span>
                  <span>{achievements.length}</span>
                </div>
              </div>
              {profile === user &&
                <div className="linkSteam">
                  <h1>Ссылка <span>STEAM</span> на обмен</h1>
                  <a href="https://steamcommunity.com/id/me/tradeoffers/privacy#trade_offer_access_url" target="_blank">Где взять ссылку?</a>
                  <div>
                    <form onSubmit={this.handleChangeTradelink.bind(this)}>
                      <input type="text" name="link" value={this.state.link} onChange={this.handleChange.bind(this)} placeholder="Введите ссылку на обмен" />
                      <button className="btn" type="submit">Сохранить</button>
                    </form>
                  </div>
                </div>
              }
              <div className="achive">
                <span>Ваши достижения</span>
                <Link to={'/' + gameName + '/achievements'}>Все достижения</Link>
                <div className="gradient"/>
                <div className="structureAchive">
                  {loaded ? achievements.length > 0 ?
                      achievements.map((achievement, key) => {
                        return (
                          <div key={key} className="blockAchive">
                            <img src={achievement.icon_url}/>
                            <div className="content">
                              <div className="gradient"/>
                              <div className="info">
                                <span>{achievement.name}</span>
                                <span>{achievement.description}</span>
                              </div>
                              <div className="gradient"/>
                            </div>
                          </div>
                        );
                      }) : <p style={{
                        marginBottom: '40px',
                        fontSize: '20px'
                      }}>{profile === user ? 'У вас нет достижений' : 'У него нет достижений'}</p> :
                    <ReactSpinner color="#a0a0a0"/>
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

const mapStateToProps = (state) => {
  return {
    user: state.user,
    otherUser: state.otherUser
  };
};

export default connect(mapStateToProps)(Profile);
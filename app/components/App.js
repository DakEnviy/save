import React from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router';
import Notifications from 'react-notification-system-redux';
import DocumentTitle from 'react-document-title';
import notify from './../actions/notify';
import Header from './Header';
import Chat from './Chat';
import Modals from './common/modal/Modals';
import 'whatwg-fetch';

const getNotification = (name, icon, type) => {
  return (
    <div className="win-achieve clearfix">
      <img src={icon} />
      <div className="info">
        <div>{type === 0 ? 'Вы открыли достижение' : 'Вы открыли пак достижений'}</div>
        <div>{name}</div>
      </div>
    </div>
  );
};

class App extends React.Component {
  brNotify(notification) {
    this.props.dispatch(Notifications.info({
      position: 'br',
      autoDismiss: 0,
      children: notification
    }));
  };
  onNewAchievements(data) {
    if (this.props.user.id !== data.user_id) return;
    for (var achievement of data.achievements) {
      this.brNotify(getNotification(achievement.name, achievement.icon_url, 0));
    }
  };
  onNewPack(data) {
    if (this.props.user.id !== data.user_id) return;
    this.brNotify(getNotification(data.pack_name, data.pack_icon_url, 1));
  };

  componentDidMount() {
    const self = this;
    window.VK.Widgets.Subscribe('vk_subscribe', { mode: 1, soft: 1 }, -142290572); // TODO: change id
    window.VK.Observer.subscribe('widgets.subscribed', () => {
      fetch('/subscribed', {
        method: 'post',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin'
      })
      .then(res => {
        return res.json().then(data => {
          if (data.msg) self.props.dispatch(notify(data.msg));
        });
      })
      .catch(err => {
        self.props.dispatch(notify('SUBSCRIBE_ERROR'));
        console.log(err);
      });
    });
    window.socket.on('new_achievements', this.onNewAchievements.bind(this));
    window.socket.on('new_pack', this.onNewPack.bind(this));
  }

  componentWillUnmount() {
    window.socket.removeListener('new_achievements', this.onNewAchievements.bind(this));
    window.socket.removeListener('new_pack', this.onNewPack.bind(this));
  }

  handleIndex(gameName) {
    return () => location.href = '/' + this.props.gameName;
    // setTimeout(() => location.reload(), 100);
  }

  handleProfile() {
    this.props.dispatch({
      type: 'CLEAR_PROFILE'
    });
  }

  render() {
    const { notifications, gameName, location } = this.props;
    const style = gameName === 'csgo' ?
      {
        Containers: {
          DefaultStyle: {
            zIndex: 999999,
          }
        },
        NotificationItem: {
          DefaultStyle: {
            color: '#E99A07',
            backgroundColor: '#1D1E21',
            border: '2px solid #E99A07',
            borderRadius: '10px',
          }
        },
        Title: {
          DefaultStyle: {
            color: '#0099FF'
          }
        },
        Action: {
          DefaultStyle:{
            backgroundColor: '#E99A07'
          }
        },
        Dismiss: {
          DefaultStyle: {
            backgroundColor: '#E99A07',
            width: '20px',
            height: '20px',
            lineHeight: '20px'
          }
        }
      } : {
        Containers: {
          DefaultStyle: {
            zIndex: 999999,
          }
        },
        NotificationItem: {
          DefaultStyle: {
            color: '#95989A',
            backgroundColor: '#070908',
            border: '2px solid #00ffe0',
            borderRadius: '10px'
          }
        },
        Title: {
          DefaultStyle: {
            color: '#FD4418'
          }
        },
        Action: {
          DefaultStyle:{
            backgroundColor: '#FD4418'
          }
        },
        Dismiss: {
          DefaultStyle: {
            backgroundColor: '#FD4418',
            width: '20px',
            height: '20px',
            lineHeight: '20px'
          }
        }
      };

    let path = location.pathname.replace('/csgo', '').replace('/dota', '');
    if (path === '') path = '/';
    return (
      <DocumentTitle title="Awapa.ru – выбор лучших">
        <div className={gameName}>
          <div className="bg"/>
          <Modals gameName={gameName} />
          <Notifications notifications={notifications} style={style} />
          <Header gameName={gameName} path={path} handleIndex={this.handleIndex} />
          <div className="Content L-b">
            <div className="leftContent">
              <div className="blockMenu">
                <ul>
                  <Link to={'/' + gameName + '/'} className={path === '/' ? 'active' : null} onClick={this.handleIndex(gameName)}>
                    <li>
                      <div className="gradient"/>
                      <i className="fa fa-rouble" />
                      <p>Главная</p>
                      <div className="gradient"/>
                    </li>
                  </Link>
                  <Link to={'/' + gameName + '/shop'} activeClassName="active">
                    <li>
                      <div className="gradient"/>
                      <i className="fa fa-shopping-cart" />
                      <p>Магазин</p>
                      <div className="gradient"/>
                    </li>
                  </Link>
                  <Link to={'/' + gameName + '/history'} className={path.startsWith ? path.startsWith('/history') ? 'active' : null : null}>
                    <li>
                      <div className="gradient"/>
                      <i className="fa fa-clock-o"/>
                      <p>История</p>
                      <div className="gradient"/>
                    </li>
                  </Link>
                  <Link to={'/' + gameName + '/top'} activeClassName="active">
                    <li>
                      <div className="gradient"/>
                      <i className="fa fa-star-half-o"/>
                      <p>Топ</p>
                      <div className="gradient"/>
                    </li>
                  </Link>
                  <Link to={'/' + gameName + '/profile'} activeClassName="active" onClick={this.handleProfile.bind(this)}>
                    <li>
                      <div className="gradient"/>
                      <i className="fa fa-user"/>
                      <p>Профиль</p>
                      <div className="gradient"/>
                    </li>
                  </Link>
                  <Link to={'/' + gameName + '/rules'} activeClassName="active">
                    <li>
                      <div className="gradient"/>
                      <i className="fa fa-exclamation-triangle"/>
                      <p>Правила</p>
                      <div className="gradient"/>
                    </li>
                  </Link>
                </ul>
              </div>
              <div className="marketingBlock">
                <div className="gradient"/>
                <div className="info">
                </div>
                <div className="gradient"/>
              </div>
            </div>
            <div className="rightContent">
              <div className="ad">
                <div id="vk_subscribe"/>
              </div>
              {this.props.children && React.cloneElement(this.props.children, { gameName: this.props.gameName })}
              <div className="btn-block">
                <div><i className="fa fa-comments-o"/></div>
              </div>
              <Chat/>
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
    notifications: state.notifications
  };
};

export default connect(mapStateToProps)(App);
